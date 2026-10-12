package com.youlai.boot.portal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.PaperSubmission;
import com.youlai.boot.portal.model.entity.PaperSubmissionEmail;
import com.youlai.boot.portal.model.form.PaperSubmissionForm;
import com.youlai.boot.portal.model.form.PaperSubmissionReviewForm;
import com.youlai.boot.portal.model.form.PaperSubmissionUpdateForm;
import com.youlai.boot.portal.model.query.PaperSubmissionQuery;
import com.youlai.boot.portal.model.vo.EmailSendResultVO;
import com.youlai.boot.portal.model.vo.PaperSubmissionPageVO;
import com.youlai.boot.portal.model.vo.PaperSubmissionQueryVO;

import java.util.List;

/**
 * 论文投稿业务接口层
 *
 * @author 243269112
 * @since 2026/5/6
 */
public interface PaperSubmissionService extends IService<PaperSubmission> {

    Page<PaperSubmissionPageVO> getPaperSubmissionPage(PaperSubmissionQuery queryParams);

    boolean savePaperSubmission(PaperSubmissionForm paperSubmissionForm);

    void deletePaperSubmissions(String ids);

    List<PaperSubmissionPageVO> getPaperSubmissionList(PaperSubmissionQuery queryParams);

    List<EmailSendResultVO> updatePaperSubmissionStatus(Long id, PaperSubmissionReviewForm form);

    boolean expertReview(Long id, Integer reviewResult, String rejectReason);

    List<PaperSubmissionEmail> getExpertReviewList(Long id);

    /**
     * 重发审稿邮件（根据邮件记录ID重发，不创建新记录）
     *
     * @param emailId 邮件记录ID（paper_submission_email表的主键）
     * @return 发送结果
     */
    EmailSendResultVO resendReviewEmail(Long emailId);

    /**
     * 查询邮件发送记录详情（供重发前校验使用）
     *
     * @param emailId 邮件记录ID
     * @return 邮件发送记录
     */
    PaperSubmissionEmail getEmailRecord(Long emailId);

    // ==================== 投稿者公开接口（无需登录，邮箱验证码鉴权） ====================

    /**
     * 发送投稿查询验证码
     *
     * @param email 投稿者邮箱
     */
    void sendQueryCode(String email);

    /**
     * 根据邮箱查询投稿列表（需验证码校验通过后调用）
     *
     * @param email 投稿者邮箱
     * @param code  验证码
     * @return 该邮箱下的所有投稿记录
     */
    List<PaperSubmissionQueryVO> queryByEmail(String email, String code);

    /**
     * 根据ID查询投稿详情（需验证邮箱归属）
     *
     * @param id    投稿记录ID
     * @param email 投稿者邮箱
     * @return 投稿详情
     */
    PaperSubmissionQueryVO getDetailByEmail(Long id, String email);

    /**
     * 投稿者修改投稿信息（仅待审核状态可修改）
     *
     * @param updateForm 修改表单
     */
    void updateBySubmitter(PaperSubmissionUpdateForm updateForm);
}
