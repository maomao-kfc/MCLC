package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.common.constant.RedisConstants;
import com.youlai.boot.common.util.QRCodeUtils;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.RegisterUserConverter;
import com.youlai.boot.portal.mapper.PaperSubmissionMapper;
import com.youlai.boot.portal.mapper.QuestionnaireSurveyMapper;
import com.youlai.boot.portal.mapper.RegisterUserInformationMapper;
import com.youlai.boot.portal.mapper.UserCheckInMapper;
import com.youlai.boot.portal.model.entity.PaperSubmission;
import com.youlai.boot.portal.model.entity.QuestionnaireSurvey;
import com.youlai.boot.portal.model.entity.RegisterUserInformation;
import com.youlai.boot.portal.model.entity.UserCheckIn;
import com.youlai.boot.portal.model.form.CheckInForm;
import com.youlai.boot.portal.model.form.RegisterUserForm;
import com.youlai.boot.portal.model.query.RegisterUserQuery;
import com.youlai.boot.portal.model.vo.RegisterUserPageVO;
import com.youlai.boot.portal.service.RegisterUserService;
import com.youlai.boot.support.mail.service.MailService;
import com.youlai.boot.system.model.form.EmailTemplateForm;
import com.youlai.boot.system.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 注册用户服务实现类
 *
 * 核心业务流程（6步注册向导）：
 * 1. enroll() — Step1: 登记，创建register_user记录(状态=ENROLLED)
 * 2. consent() — Step2: 同意条款，更新consent_status
 * 3. contact() — Step3: 联系方式，更新contact_email/mobile
 * 4. signup() — Step4: 报名，计算费用+折扣+生成QR码→上传→回写URL(状态=SIGNED_UP)
 * 5. confirm() — Step5: 确认，更新确认状态(状态=CONFIRMED)
 * 6. final() — Step6: 最终，查询最终状态+支付信息
 *
 * 其他功能：
 * - 签到 — checkIn(): 验证QR码+更新check_in_status
 * - 发送确认邮件 — sendConfirmationEmail(): 使用EmailTemplateService模板
 * - 抵扣码验证 — validateDiscountCode(): 校验discount_code有效性
 *
 * 依赖注入(8个)：
 * - registerUserConverter, registerUserMapper, paperSubmissionMapper
 * - questionnaireSurveyMapper, userCheckInMapper, mailService
 * - emailTemplateService, redisTemplate
 * - confirmReviewUrl(@Value)
 *
 * 已知问题：CODE-008(team_id exist=false) / CODE-009(问卷功能注释) / ARCH-007(团队折扣仅前端计算)
 *
 * 技术文档详见：docs/07-registration-workflow.md
 */
@Service
@RequiredArgsConstructor
public class RegisterUserServiceImpl extends ServiceImpl<RegisterUserInformationMapper, RegisterUserInformation> implements RegisterUserService {

    private final RegisterUserInformationMapper registerUserInformationMapper;
    private final QuestionnaireSurveyMapper questionnaireSurveyMapper;
    private final PaperSubmissionMapper paperSubmissionMapper;
    private final UserCheckInMapper userCheckInMapper;
    private final RegisterUserConverter registerUserConverter;
    private final MailService mailService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailTemplateService emailTemplateService;

    @Value("${check-in}")
    private String checkInUrl;

    @Override
    public Page<RegisterUserPageVO> getRegisterUserPage(RegisterUserQuery queryParams) {
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();

        Page<RegisterUserPageVO> page = new Page<>(pageNum, pageSize);
        return registerUserInformationMapper.selectRegisterUserPage(page, queryParams);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveRegisterUser(RegisterUserForm form) {
        Long userId = form.getId();

        RegisterUserInformation registerUser = null;
        QuestionnaireSurvey questionnaire = null;

        if (userId != null) {
            registerUser = this.getById(userId);
            Assert.isTrue(registerUser != null, "注册用户不存在");

            Long connectId = registerUser.getConnectId();
            if (connectId != null) {
                questionnaire = questionnaireSurveyMapper.selectById(connectId);
            }
        }

        boolean isNew = (registerUser == null);

        if (StrUtil.isNotBlank(form.getEmail())) {
            String email = form.getEmail().trim().toLowerCase();
            LambdaQueryWrapper<RegisterUserInformation> emailCheck = new LambdaQueryWrapper<RegisterUserInformation>()
                    .eq(RegisterUserInformation::getEmail, email);
            if (!isNew) {
                emailCheck.ne(RegisterUserInformation::getId, userId);
            }
            long count = this.count(emailCheck);
            if (count > 0) {
                throw new BusinessException("该邮箱已被注册，请使用其他邮箱");
            }
        }

        if (isNew) {
            registerUser = registerUserConverter.toRegisterUserInformation(form);
            registerUser.setTeamId(form.getTeamId());
            registerUser.setPaymentStatus("pending");
            registerUser.setRegistrationDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            registerUser.setName(form.getName());
            registerUser.setSurname(form.getSurname());
            registerUser.setEmail(form.getEmail());
            registerUser.setOrganization(form.getOrganization());
            registerUser.setMobilePhone(form.getMobilePhone());
            registerUser.setStreet(form.getStreet());
            registerUser.setPostalCode(form.getPostalCode());
            registerUser.setStatus(form.getStatus());
            registerUser.setCountry(form.getCountry());
            registerUser.setBillOrganization(form.getBillOrganization());
            registerUser.setBillVat(form.getBillVat());
            registerUser.setBillProcurement(form.getBillProcurement());
            registerUser.setBillStreet(form.getBillStreet());
            registerUser.setBillPostalCode(form.getBillPostalCode());
            registerUser.setBillCity(form.getBillCity());
            registerUser.setBillStatus(form.getBillStatus());
            registerUser.setBillCountry(form.getBillCountry());
            registerUser.setRegistrationType(form.getRegistrationType());
            registerUser.setRegistrationFee(form.getRegistrationFee());
            registerUser.setPaymentMethod(form.getPaymentMethod());
            registerUser.setPaymentAmount(form.getPaymentAmount());
            registerUser.setVisaName(form.getVisaName());
            registerUser.setVisaSurname(form.getVisaSurname());
            registerUser.setVisaPassport(form.getVisaPassport());
            registerUser.setVisaIssueTime(form.getVisaIssueTime());
            registerUser.setVisaEndTime(form.getVisaEndTime());
            registerUser.setVisaTime(form.getVisaTime());
            registerUser.setVisaArea(form.getVisaArea());
            registerUser.setVisaIsNeed(form.getVisaIsNeed());
            registerUser.setDeductionCode(form.getDeductionCode());
            registerUser.setPicBank(form.getPicBank());
            registerUser.setActualPayment(form.getActualPayment());
            registerUser.setRegisterLabel(form.getRegisterLabel());
            if (registerUser.getRegistrationDate() == null) {
                registerUser.setRegistrationDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        }

        sanitizeDateFields(registerUser);

        if (isNew) {
            // questionnaire = registerUserConverter.toQuestionnaireSurvey(form);
            // questionnaireSurveyMapper.insert(questionnaire);

            // registerUser.setConnectId(questionnaire.getId());
            this.save(registerUser);

            if (StrUtil.isNotBlank(registerUser.getEmail())) {
                EmailTemplateForm templateForm = emailTemplateService.getEmailTemplateForm("注册通过模板");
                String content = templateForm.getEmailContent()
                        .replace("{{name1}}", registerUser.getName() + " " + registerUser.getSurname());
                mailService.sendHtmlMail(registerUser.getEmail(), "注册成功", content);
            }

            List<RegisterUserForm.TeamMemberForm> teamMembers = form.getTeamMembers();
            if (teamMembers != null && !teamMembers.isEmpty()) {
                for (RegisterUserForm.TeamMemberForm member : teamMembers) {
                    // QuestionnaireSurvey memberQuestionnaire = buildQuestionnaireFromForm(form);
                    // questionnaireSurveyMapper.insert(memberQuestionnaire);

                    RegisterUserInformation memberUser = new RegisterUserInformation();
                    // memberUser.setConnectId(memberQuestionnaire.getId());
                    memberUser.setTeamId(registerUser.getId());
                    memberUser.setName(member.getName());
                    memberUser.setSurname(member.getSurname());
                    memberUser.setEmail(member.getEmail());
                    memberUser.setMobilePhone(member.getMobilePhone());
                    memberUser.setOrganization(form.getOrganization());
                    memberUser.setStreet(form.getStreet());
                    memberUser.setPostalCode(form.getPostalCode());
                    memberUser.setStatus(form.getStatus());
                    memberUser.setCountry(form.getCountry());
                    memberUser.setBillOrganization(form.getBillOrganization());
                    memberUser.setBillVat(form.getBillVat());
                    memberUser.setBillProcurement(form.getBillProcurement());
                    memberUser.setBillStreet(form.getBillStreet());
                    memberUser.setBillPostalCode(form.getBillPostalCode());
                    memberUser.setBillCity(form.getBillCity());
                    memberUser.setBillStatus(form.getBillStatus());
                    memberUser.setBillCountry(form.getBillCountry());
                    memberUser.setRegistrationType(form.getRegistrationType());
                    memberUser.setRegistrationFee(form.getRegistrationFee());
                    memberUser.setPaymentMethod(form.getPaymentMethod());
                    memberUser.setVisaName(form.getVisaName());
                    memberUser.setVisaSurname(form.getVisaSurname());
                    memberUser.setVisaPassport(form.getVisaPassport());
                    memberUser.setVisaIssueTime(form.getVisaIssueTime());
                    memberUser.setVisaEndTime(form.getVisaEndTime());
                    memberUser.setVisaTime(form.getVisaTime());
                    memberUser.setVisaArea(form.getVisaArea());
                    memberUser.setVisaIsNeed(form.getVisaIsNeed());
                    memberUser.setPicBank(form.getPicBank());
                    memberUser.setRegisterLabel(form.getRegisterLabel());
                    memberUser.setRegistrationDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    sanitizeDateFields(memberUser);
                    this.save(memberUser);
                }
            }
            return buildResult(registerUser, false);
        } else {
            this.updateById(registerUser);

            deleteTeamMembers(registerUser.getId());
            List<RegisterUserForm.TeamMemberForm> teamMembers = form.getTeamMembers();
            if (teamMembers != null && !teamMembers.isEmpty()) {
                for (RegisterUserForm.TeamMemberForm member : teamMembers) {
                    // QuestionnaireSurvey memberQuestionnaire = buildQuestionnaireFromForm(form);
                    // questionnaireSurveyMapper.insert(memberQuestionnaire);

                    RegisterUserInformation memberUser = new RegisterUserInformation();
                    // memberUser.setConnectId(memberQuestionnaire.getId());
                    memberUser.setTeamId(registerUser.getId());
                    memberUser.setName(member.getName());
                    memberUser.setSurname(member.getSurname());
                    memberUser.setEmail(member.getEmail());
                    memberUser.setMobilePhone(member.getMobilePhone());
                    memberUser.setOrganization(form.getOrganization());
                    memberUser.setStreet(form.getStreet());
                    memberUser.setPostalCode(form.getPostalCode());
                    memberUser.setStatus(form.getStatus());
                    memberUser.setCountry(form.getCountry());
                    memberUser.setBillOrganization(form.getBillOrganization());
                    memberUser.setBillVat(form.getBillVat());
                    memberUser.setBillProcurement(form.getBillProcurement());
                    memberUser.setBillStreet(form.getBillStreet());
                    memberUser.setBillPostalCode(form.getBillPostalCode());
                    memberUser.setBillCity(form.getBillCity());
                    memberUser.setBillStatus(form.getBillStatus());
                    memberUser.setBillCountry(form.getBillCountry());
                    memberUser.setRegistrationType(form.getRegistrationType());
                    memberUser.setRegistrationFee(form.getRegistrationFee());
                    memberUser.setPaymentMethod(form.getPaymentMethod());
                    memberUser.setVisaName(form.getVisaName());
                    memberUser.setVisaSurname(form.getVisaSurname());
                    memberUser.setVisaPassport(form.getVisaPassport());
                    memberUser.setVisaIssueTime(form.getVisaIssueTime());
                    memberUser.setVisaEndTime(form.getVisaEndTime());
                    memberUser.setVisaTime(form.getVisaTime());
                    memberUser.setVisaArea(form.getVisaArea());
                    memberUser.setVisaIsNeed(form.getVisaIsNeed());
                    memberUser.setPicBank(form.getPicBank());
                    memberUser.setRegisterLabel(form.getRegisterLabel());
                    memberUser.setRegistrationDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    sanitizeDateFields(memberUser);
                    this.save(memberUser);
                }
            }
            return buildResult(registerUser, false);
        }
    }

    private Map<String, Object> buildResult(RegisterUserInformation registerUser, boolean isNew) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", registerUser.getId());
        result.put("paymentMethod", registerUser.getPaymentMethod());
        result.put("paymentStatus", registerUser.getPaymentStatus());
        result.put("paymentAmount", registerUser.getPaymentAmount());
        return result;
    }

    private void sanitizeDateFields(RegisterUserInformation entity) {
        if (StrUtil.isBlank(entity.getVisaIssueTime())) {
            entity.setVisaIssueTime(null);
        }
        if (StrUtil.isBlank(entity.getVisaEndTime())) {
            entity.setVisaEndTime(null);
        }
        if (StrUtil.isBlank(entity.getVisaTime())) {
            entity.setVisaTime(null);
        }
    }

    // 问卷调查表暂不使用
    // private QuestionnaireSurvey buildQuestionnaireFromForm(RegisterUserForm form) {
    //     QuestionnaireSurvey qs = new QuestionnaireSurvey();
    //     qs.setRegistrationCategory(form.getRegistrationCategory());
    //     qs.setRegistrationDocument(form.getRegistrationDocument());
    //     qs.setRegistrationWhichMember(form.getRegistrationWhichMember());
    //     qs.setRegistrationCode(form.getRegistrationCode());
    //     qs.setIsMatchDrug(form.getIsMatchDrug());
    //     qs.setDietaryRequirements(form.getDietaryRequirements());
    //     qs.setSpecialRequirements(form.getSpecialRequirements());
    //     qs.setIsFirst(form.getIsFirst());
    //     qs.setGender(form.getGender());
    //     qs.setOccupation(form.getOccupation());
    //     qs.setExperience(form.getExperience());
    //     qs.setFieldOfInterest(form.getFieldOfInterest());
    //     qs.setOther(form.getOther());
    //     return qs;
    // }

    private void deleteTeamMembers(Long teamId) {
        // team_id 字段 (exist = false) 尚未在数据库表中创建，暂无团队成员关联功能
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRegisterUsers(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的注册用户ID不能为空");
        List<Long> userIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();

        for (Long userId : userIds) {
            RegisterUserInformation registerUser = this.getById(userId);
            Assert.isTrue(registerUser != null, "注册用户不存在");

            deleteTeamMembers(userId);

            // Long connectId = registerUser.getConnectId();
            // if (connectId != null) {
            //     questionnaireSurveyMapper.deleteById(connectId);
            // }

            this.removeById(userId);
        }
    }

    @Override
    public RegisterUserPageVO getRegisterUserDetail(Long id) {
        RegisterUserPageVO vo = registerUserInformationMapper.selectRegisterUserDetail(id);
        Assert.isTrue(vo != null, "注册用户不存在");
        return vo;
    }

    @Override
    public List<RegisterUserPageVO> getRegisterUserList(RegisterUserQuery queryParams) {
        List<RegisterUserInformation> list = this.list(
                new LambdaQueryWrapper<RegisterUserInformation>()
                        .eq(queryParams.getStatus() != null, RegisterUserInformation::getStatus, queryParams.getStatus())
                        .like(StrUtil.isNotBlank(queryParams.getName()), RegisterUserInformation::getName, queryParams.getName())
                        .like(StrUtil.isNotBlank(queryParams.getEmail()), RegisterUserInformation::getEmail, queryParams.getEmail())
                        .orderByDesc(RegisterUserInformation::getId)
        );

        return list.stream().map(entity -> {
            RegisterUserPageVO vo = new RegisterUserPageVO();
            vo.setId(entity.getId());
            vo.setConnectId(entity.getConnectId());
            vo.setTeamId(entity.getTeamId());
            vo.setName(entity.getName());
            vo.setSurname(entity.getSurname());
            vo.setEmail(entity.getEmail());
            vo.setOrganization(entity.getOrganization());
            vo.setMobilePhone(entity.getMobilePhone());
            vo.setStreet(entity.getStreet());
            vo.setPostalCode(entity.getPostalCode());
            vo.setStatus(entity.getStatus());
            vo.setCountry(entity.getCountry());
            vo.setBillOrganization(entity.getBillOrganization());
            vo.setBillVat(entity.getBillVat());
            vo.setBillProcurement(entity.getBillProcurement());
            vo.setBillStreet(entity.getBillStreet());
            vo.setBillPostalCode(entity.getBillPostalCode());
            vo.setBillCity(entity.getBillCity());
            vo.setBillStatus(entity.getBillStatus());
            vo.setBillCountry(entity.getBillCountry());
            vo.setRegistrationType(entity.getRegistrationType());
            vo.setRegistrationFee(entity.getRegistrationFee());
            vo.setRegistrationDate(entity.getRegistrationDate());
            vo.setPaymentMethod(entity.getPaymentMethod());
            vo.setPaymentStatus(entity.getPaymentStatus());
            vo.setStripePaymentIntentId(entity.getStripePaymentIntentId());
            vo.setPaymentAmount(entity.getPaymentAmount());
            vo.setVisaName(entity.getVisaName());
            vo.setVisaSurname(entity.getVisaSurname());
            vo.setVisaPassport(entity.getVisaPassport());
            vo.setVisaIssueTime(entity.getVisaIssueTime());
            vo.setVisaEndTime(entity.getVisaEndTime());
            vo.setVisaTime(entity.getVisaTime());
            vo.setVisaArea(entity.getVisaArea());
            vo.setVisaIsNeed(entity.getVisaIsNeed());
            vo.setPicBank(entity.getPicBank());
            vo.setActualPayment(entity.getActualPayment());
            vo.setRegisterLabel(entity.getRegisterLabel());

            // 问卷调查表暂不使用
            // if (entity.getConnectId() != null) {
            //     QuestionnaireSurvey qs = questionnaireSurveyMapper.selectById(entity.getConnectId());
            //     if (qs != null) {
            //         vo.setRegistrationCategory(qs.getRegistrationCategory());
            //         vo.setRegistrationDocument(qs.getRegistrationDocument());
            //         vo.setRegistrationWhichMember(qs.getRegistrationWhichMember());
            //         vo.setRegistrationCode(qs.getRegistrationCode());
            //         vo.setIsMatchDrug(qs.getIsMatchDrug());
            //         vo.setDietaryRequirements(qs.getDietaryRequirements());
            //         vo.setSpecialRequirements(qs.getSpecialRequirements());
            //         vo.setIsFirst(qs.getIsFirst());
            //         vo.setGender(qs.getGender());
            //         vo.setOccupation(qs.getOccupation());
            //         vo.setExperience(qs.getExperience());
            //         vo.setFieldOfInterest(qs.getFieldOfInterest());
            //         vo.setOther(qs.getOther());
            //     }
            // }
            return vo;
        }).toList();
    }

    @Override
    public void sendLoginEmailCode(String email) {
        String emailTrimmed = email.trim().toLowerCase();
        long count = this.count(new LambdaQueryWrapper<RegisterUserInformation>()
                .eq(RegisterUserInformation::getEmail, emailTrimmed));
        if (count == 0) {
            throw new BusinessException("该邮箱未注册");
        }

        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        mailService.sendMail(emailTrimmed, "登录验证码", "您的登录验证码为：" + code + "，请在5分钟内使用");

        String redisCacheKey = StrUtil.format(RedisConstants.Captcha.EMAIL_LOGIN_CODE, emailTrimmed);
        redisTemplate.opsForValue().set(redisCacheKey, code, 5, TimeUnit.MINUTES);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRegisterUser(Long id, Integer reviewStatus, String rejectReason) {
        RegisterUserInformation registerUser = this.getById(id);
        Assert.isTrue(registerUser != null, "注册用户不存在");

        registerUser.setReviewStatus(reviewStatus);
        registerUser.setRejectReason(rejectReason);
        this.updateById(registerUser);

        if (reviewStatus == 1) {
            String email = registerUser.getEmail();
            Assert.isTrue(StrUtil.isNotBlank(email), "该用户未填写邮箱，无法发送审核邮件");

            String deductionCode = registerUser.getDeductionCode();
            if (StrUtil.isNotBlank(deductionCode)) {
                PaperSubmission submission = paperSubmissionMapper.selectOne(
                        new LambdaQueryWrapper<PaperSubmission>()
                                .eq(PaperSubmission::getDiscountCode, deductionCode)
                );
                if (submission == null) {
                    throw new BusinessException("注册码不存在");
                }
                if (submission.getDeductionStatus() != 0) {
                    throw new BusinessException("注册码无效");
                }
                submission.setDeductionStatus(1);
                paperSubmissionMapper.updateById(submission);
            }

            String qrContent = checkInUrl + registerUser.getId();
            String qrBase64 = QRCodeUtils.generateBase64QRCode(qrContent);

            String fullName = (registerUser.getName() != null ? registerUser.getName() : "")
                    + (registerUser.getSurname() != null ? " " + registerUser.getSurname() : "");

            String html = "<div style='text-align:center;font-family:Arial,sans-serif;'>"
                    + "<h2>审核通过通知</h2>"
                    + "<p>尊敬的 " + fullName.trim() + "：</p>"
                    + "<p>您的注册已审核通过，请保存以下二维码，用于现场签到扫码登记。</p>"
                    + "<img src='data:image/png;base64," + qrBase64 + "' alt='签到二维码' style='width:250px;height:250px;margin:20px 0;' />"
                    + "<p style='color:#999;font-size:12px;'>二维码内容: " + qrContent + "</p>"
                    + "</div>";

            mailService.sendHtmlMail(email, "注册审核通过 - 现场签到二维码", html);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkIn(CheckInForm form) {
        RegisterUserInformation registerUser = registerUserInformationMapper.selectById(form.getCheckId());
        Assert.isTrue(registerUser != null, "注册用户不存在");

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        UserCheckIn checkIn = userCheckInMapper.selectOne(
                new LambdaQueryWrapper<UserCheckIn>()
                        .eq(UserCheckIn::getCheckId, form.getCheckId())
        );

        if (checkIn != null) {
            checkIn.setCheckTime(now);
            checkIn.setCheckName(form.getCheckName());
            checkIn.setCheckNumber(form.getCheckNumber());
            checkIn.setCheckEmail(form.getCheckEmail());
            checkIn.setCheckStatus("签到成功");
            userCheckInMapper.updateById(checkIn);
        } else {
            checkIn = new UserCheckIn();
            checkIn.setCheckId(form.getCheckId());
            checkIn.setCheckTime(now);
            checkIn.setCheckName(form.getCheckName());
            checkIn.setCheckNumber(form.getCheckNumber());
            checkIn.setCheckEmail(form.getCheckEmail());
            checkIn.setCheckStatus("签到成功");
            userCheckInMapper.insert(checkIn);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("checkStatus", "签到成功");
        result.put("checkTime", now);
        return result;
    }
}
