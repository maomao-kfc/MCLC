package com.youlai.boot.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.core.web.PageResult;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.portal.model.entity.PaperSubmission;
import com.youlai.boot.portal.model.entity.PaperSubmissionEmail;
import com.youlai.boot.portal.model.form.PaperSubmissionForm;
import com.youlai.boot.portal.model.form.PaperSubmissionReviewForm;
import com.youlai.boot.portal.model.form.PaperSubmissionUpdateForm;
import com.youlai.boot.portal.model.query.PaperSubmissionQuery;
import com.youlai.boot.portal.model.vo.EmailSendResultVO;
import com.youlai.boot.portal.model.vo.PaperSubmissionPageVO;
import com.youlai.boot.portal.model.vo.PaperSubmissionQueryVO;
import com.youlai.boot.portal.service.DocxToPdfService;
import com.youlai.boot.portal.service.PaperPdfGeneratorService;
import com.youlai.boot.portal.service.PaperSubmissionService;
import com.youlai.boot.portal.service.impl.PaperPdfGeneratorServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 论文投稿控制层
 *
 * API接口一览：
 * - GET    /api/v1/paperSubmission              → 分页查询论文投稿列表
 * - POST   /api/v1/paperSubmission              → 新增论文投稿
 * - PUT    /api/v1/paperSubmission/{id}         → 修改论文投稿
 * - DELETE /api/v1/paperSubmission/{ids}        → 删除论文投稿
 * - PUT    /api/v1/paperSubmission/{id}/review  → 发送审稿邮件
 * - POST/PUT /api/v1/paperSubmission/expertReview → 专家审核
 * - GET    /api/v1/paperSubmission/expertReview/list → 查询专家审核记录
 * - POST   /api/v1/paperSubmission/resendEmail/{emailId} → 重发审稿邮件
 * - GET    /api/v1/paperSubmission/emailRecord/{emailId} → 查询邮件记录详情
 * - GET    /api/v1/paperSubmission/{id}/exportPdf → 导出论文PDF
 *
 * 投稿者公开接口（无需登录，邮箱验证码鉴权）：
 * - POST   /api/v1/paperSubmission/query/sendCode  → 发送查询验证码
 * - GET    /api/v1/paperSubmission/query/byEmail   → 按邮箱查询投稿列表
 * - GET    /api/v1/paperSubmission/query/detail     → 查询投稿详情
 * - PUT    /api/v1/paperSubmission/query/update     → 投稿者修改投稿信息
 *
 * @author 243269112
 * @since 2026/5/6
 */
@Tag(name = "论文投稿接口")
@RestController
@RequestMapping("/api/v1/paperSubmission")
@RequiredArgsConstructor
@Slf4j
public class PaperSubmissionController {

    /** 论文投稿业务服务 */
    private final PaperSubmissionService paperSubmissionService;

    /** DOCX转PDF服务（开发环境LibreOffice未安装时为null） */
    @Autowired(required = false)
    private DocxToPdfService docxToPdfService;

    /** 论文PDF生成服务（封面页、信息页、PDF合并） */
    private final PaperPdfGeneratorService paperPdfGeneratorService;

    /**
     * 【接口1】论文投稿分页列表
     *
     * 请求：GET /api/v1/paperSubmission?pageNum=1&pageSize=10
     * 响应：分页数据（PaperSubmissionPageVO列表）
     */
    @Operation(summary = "论文投稿分页列表")
    @GetMapping
    @Log(value = "论文投稿分页列表", module = LogModuleEnum.PAPER_SUBMISSION)
    public PageResult<PaperSubmissionPageVO> getPaperSubmissionPage(PaperSubmissionQuery queryParams) {
        Page<PaperSubmissionPageVO> result = paperSubmissionService.getPaperSubmissionPage(queryParams);
        return PageResult.success(result);
    }

    /**
     * 【接口2】新增论文投稿
     *
     * 请求：POST /api/v1/paperSubmission
     * Body：PaperSubmissionForm（姓名、论文标题、摘要、附件等）
     */
    @Operation(summary = "新增论文投稿")
    @PostMapping
    @RepeatSubmit
    public Result<?> addPaperSubmission(@Valid @RequestBody PaperSubmissionForm paperSubmissionForm) {
        boolean result = paperSubmissionService.savePaperSubmission(paperSubmissionForm);
        return Result.judge(result);
    }

    /**
     * 【接口3】修改论文投稿
     *
     * 请求：PUT /api/v1/paperSubmission/{id}
     * Body：PaperSubmissionForm
     */
    @Operation(summary = "修改论文投稿")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:paperSubmission:update')")
    public Result<?> updatePaperSubmission(@Valid @RequestBody PaperSubmissionForm paperSubmissionForm) {
        boolean result = paperSubmissionService.savePaperSubmission(paperSubmissionForm);
        return Result.judge(result);
    }

    /**
     * 【接口4】删除论文投稿（支持批量）
     *
     * 请求：DELETE /api/v1/paperSubmission/1,2,3
     */
    @Operation(summary = "删除论文投稿")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:paperSubmission:delete')")
    public Result<Void> deletePaperSubmissions(
            @Parameter(description = "删除的论文投稿，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        paperSubmissionService.deletePaperSubmissions(ids);
        return Result.success();
    }

    /**
     * 【接口5】发送审稿邮件
     *
     * 请求：PUT /api/v1/paperSubmission/{id}/review
     * Body：PaperSubmissionReviewForm（专家列表、附件链接、发送人姓名）
     *
     * 流程：前端点击"发送审稿邮件" → 调用此接口 → Service层为每个专家发送邮件
     */
    @Operation(summary = "发送论文投稿")
    @PutMapping(value = "/{id}/review")
    public Result<List<EmailSendResultVO>> reviewPaperSubmission(
            @Parameter(description = "论文投稿ID") @PathVariable Long id,
            @Valid @RequestBody PaperSubmissionReviewForm form
    ) {
        List<EmailSendResultVO> result = paperSubmissionService.updatePaperSubmissionStatus(id, form);
        return Result.success(result);
    }

    /**
     * 【接口6】专家审核
     *
     * 请求：POST/PUT /api/v1/paperSubmission/expertReview?id=1&reviewResult=1
     *
     * 流程：专家点击邮件中的审批链接 → 前端页面调用此接口 → Service层更新审核结果
     *       → 全部审核完毕后计算通过率 → 更新论文投稿状态 → 通知投稿人
     */
    @Operation(summary = "专家审核")
    @RequestMapping(value = "/expertReview", method = {RequestMethod.POST, RequestMethod.PUT})
    public Result<?> expertReview(
            @Parameter(description = "发送记录ID") @RequestParam Long id,
            @Parameter(description = "审核结果(0:未审核 1:已通过 2:未通过)") @RequestParam Integer reviewResult,
            @Parameter(description = "拒绝原因") @RequestParam(required = false) String rejectReason
    ) {
        boolean result = paperSubmissionService.expertReview(id, reviewResult, rejectReason);
        return Result.judge(result);
    }

    /**
     * 【接口7】查询同投稿的所有专家审核记录
     *
     * 请求：GET /api/v1/paperSubmission/expertReview/list?id=1
     * 响应：PaperSubmissionEmail列表
     */
    @Operation(summary = "查询同投稿的所有专家记录")
    @GetMapping(value = "/expertReview/list")
    public Result<?> getExpertReviewList(
            @Parameter(description = "论文投稿ID") @RequestParam Long id
    ) {
        List<PaperSubmissionEmail> list = paperSubmissionService.getExpertReviewList(id);
        return Result.success(list);
    }

    /**
     * 【接口8】重发审稿邮件
     *
     * 请求：POST /api/v1/paperSubmission/resendEmail/{emailId}
     *
     * 流程：前端点击"重发"按钮 → 调用此接口 → Service层基于已有邮件记录重新发送
     */
    @Operation(summary = "重发审稿邮件")
    @PostMapping(value = "/resendEmail/{emailId}")
    public Result<EmailSendResultVO> resendReviewEmail(
            @Parameter(description = "邮件记录ID") @PathVariable Long emailId
    ) {
        EmailSendResultVO result = paperSubmissionService.resendReviewEmail(emailId);
        return Result.success(result);
    }

    /**
     * 【接口9】查询邮件发送记录详情
     *
     * 请求：GET /api/v1/paperSubmission/emailRecord/{emailId}
     */
    @Operation(summary = "查询邮件发送记录详情")
    @GetMapping(value = "/emailRecord/{emailId}")
    public Result<PaperSubmissionEmail> getEmailRecord(
            @Parameter(description = "邮件记录ID") @PathVariable Long emailId
    ) {
        PaperSubmissionEmail record = paperSubmissionService.getEmailRecord(emailId);
        return Result.success(record);
    }

    /**
     * 【接口10】导出论文附件PDF
     *
     * 请求：GET /api/v1/paperSubmission/{id}/exportPdf
     *
     * 三种情况统一返回PDF文件流，前端用axios blob下载：
     * 1. PDF附件  → 生成封面页 + 从URL读取附件PDF → 合并后返回
     * 2. DOCX/DOC附件 → 生成封面页 + JODConverter转PDF → 合并后返回
     * 3. 无附件   → OpenPDF生成完整信息PDF（标题、作者、邮箱、摘要、关键词）
     *
     * @param id       论文投稿ID
     * @param response HTTP响应
     */
    @Operation(summary = "导出论文附件PDF")
    @GetMapping(value = "/{id}/exportPdf")
    public void exportPdf(
            @Parameter(description = "论文投稿ID") @PathVariable Long id,
            HttpServletResponse response
    ) {
        try {
            // 查询论文投稿记录
            PaperSubmission paperSubmission = paperSubmissionService.getById(id);
            if (paperSubmission == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "论文投稿记录不存在");
                return;
            }

            // 构建下载文件名：使用姓名命名
            String authorName = paperSubmission.getName() != null ? paperSubmission.getName() : "paper_" + id;
            String encodedFileName = URLEncoder.encode(authorName, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            // 设置响应头
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + encodedFileName + ".pdf");

            String appendix = paperSubmission.getAppendix();
            String lowerUrl = (appendix != null) ? appendix.toLowerCase() : "";

            if (lowerUrl.endsWith(".pdf")) {
                // 情况1：PDF附件 → 封面页 + 附件PDF 合并
                log.info("导出PDF附件（含封面页），paperId: {}, URL: {}", id, appendix);
                ByteArrayOutputStream coverStream = new ByteArrayOutputStream();
                paperPdfGeneratorService.generateCoverPage(paperSubmission, coverStream);
                byte[] coverBytes = coverStream.toByteArray();

                ByteArrayOutputStream attachmentStream = new ByteArrayOutputStream();
                try (InputStream pdfInputStream = java.net.URI.create(appendix).toURL().openStream()) {
                    pdfInputStream.transferTo(attachmentStream);
                }
                byte[] attachmentBytes = attachmentStream.toByteArray();

                PaperPdfGeneratorServiceImpl.mergePdfs(coverBytes, attachmentBytes, response.getOutputStream());
                response.getOutputStream().flush();

            } else if (lowerUrl.endsWith(".docx") || lowerUrl.endsWith(".doc")) {
                // 情况2：DOCX/DOC附件 → 封面页 + JODConverter转PDF 合并
                if (docxToPdfService == null) {
                    // LibreOffice未安装，回退到OpenPDF生成完整信息PDF
                    log.warn("LibreOffice 未安装，回退到OpenPDF生成完整信息PDF");
                    paperPdfGeneratorService.generatePdf(paperSubmission, response.getOutputStream());
                } else {
                    try {
                        log.info("DOCX转PDF导出（含封面页），paperId: {}, URL: {}", id, appendix);
                        // 生成封面页
                        ByteArrayOutputStream coverStream = new ByteArrayOutputStream();
                        paperPdfGeneratorService.generateCoverPage(paperSubmission, coverStream);
                        byte[] coverBytes = coverStream.toByteArray();

                        // 转换DOCX为PDF
                        ByteArrayOutputStream attachmentStream = new ByteArrayOutputStream();
                        docxToPdfService.convertDocxToPdf(appendix, attachmentStream);
                        byte[] attachmentBytes = attachmentStream.toByteArray();

                        // 合并封面页 + 附件内容
                        PaperPdfGeneratorServiceImpl.mergePdfs(coverBytes, attachmentBytes, response.getOutputStream());
                    } catch (Exception e) {
                        // DOCX转PDF失败，回退到OpenPDF生成完整信息PDF
                        log.warn("DOCX转PDF失败，回退到OpenPDF生成完整信息PDF，原因: {}", e.getMessage());
                        response.reset();
                        response.setContentType("application/pdf");
                        response.setHeader("Content-Disposition",
                                "attachment; filename*=UTF-8''" + encodedFileName + ".pdf");
                        paperPdfGeneratorService.generatePdf(paperSubmission, response.getOutputStream());
                    }
                }
                response.getOutputStream().flush();

            } else {
                // 情况3：无附件 → OpenPDF生成完整信息PDF
                log.info("无附件，OpenPDF生成完整信息PDF，paperId: {}", id);
                paperPdfGeneratorService.generatePdf(paperSubmission, response.getOutputStream());
                response.getOutputStream().flush();
            }

        } catch (Exception e) {
            log.error("导出论文PDF失败, id={}", id, e);
            try {
                response.reset();
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"code\":1,\"msg\":\"导出PDF失败: " + e.getMessage() + "\"}");
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    // ==================== 投稿者公开接口（无需登录，邮箱验证码鉴权） ====================

    /**
     * 【公开接口1】发送投稿查询验证码
     *
     * 请求：POST /api/v1/paperSubmission/query/sendCode?email=xxx@xxx.com
     * 响应：Result<Void>
     *
     * 流程：投稿者输入邮箱 → 点击"发送验证码" → 后端校验邮箱有投稿记录 → 发送验证码邮件
     */
    @Operation(summary = "发送投稿查询验证码")
    @PostMapping("/query/sendCode")
    public Result<Void> sendQueryCode(@RequestParam String email) {
        paperSubmissionService.sendQueryCode(email);
        return Result.success();
    }

    /**
     * 【公开接口2】按邮箱查询投稿列表
     *
     * 请求：GET /api/v1/paperSubmission/query/byEmail?email=xxx@xxx.com&code=123456
     * 响应：Result<List<PaperSubmissionQueryVO>>
     *
     * 流程：投稿者输入邮箱+验证码 → 后端校验验证码 → 返回该邮箱下所有投稿记录
     */
    @Operation(summary = "按邮箱查询投稿列表")
    @GetMapping("/query/byEmail")
    public Result<List<PaperSubmissionQueryVO>> queryByEmail(
            @RequestParam String email,
            @RequestParam String code
    ) {
        List<PaperSubmissionQueryVO> list = paperSubmissionService.queryByEmail(email, code);
        return Result.success(list);
    }

    /**
     * 【公开接口3】查询投稿详情
     *
     * 请求：GET /api/v1/paperSubmission/query/detail?id=1&email=xxx@xxx.com
     * 响应：Result<PaperSubmissionQueryVO>
     *
     * 流程：投稿者点击"查看详情" → 后端验证邮箱归属 → 返回投稿详情
     */
    @Operation(summary = "查询投稿详情（投稿者）")
    @GetMapping("/query/detail")
    public Result<PaperSubmissionQueryVO> getDetailByEmail(
            @RequestParam Long id,
            @RequestParam String email
    ) {
        PaperSubmissionQueryVO vo = paperSubmissionService.getDetailByEmail(id, email);
        return Result.success(vo);
    }

    /**
     * 【公开接口4】投稿者修改投稿信息（仅待审核状态可修改）
     *
     * 请求：PUT /api/v1/paperSubmission/query/update
     * Body：PaperSubmissionUpdateForm
     * 响应：Result<Void>
     *
     * 流程：投稿者在详情弹窗中点击"修改" → 修改字段 → 提交 → 后端校验邮箱归属+状态 → 更新
     */
    @Operation(summary = "投稿者修改投稿信息")
    @PutMapping("/query/update")
    public Result<Void> updateBySubmitter(@Valid @RequestBody PaperSubmissionUpdateForm updateForm) {
        paperSubmissionService.updateBySubmitter(updateForm);
        return Result.success();
    }

}