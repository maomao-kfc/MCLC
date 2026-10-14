package com.youlai.boot.portal.controller;

import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.module.file.model.FileInfo;
import com.youlai.boot.module.file.service.FileService;
import com.youlai.boot.portal.model.form.PaperSubmissionForm;
import com.youlai.boot.portal.service.PaperSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 会议投稿公开提交控制器（webpage前端专用）
 *
 * 与 PaperSubmissionController 的区别：
 * - 本控制器专门接收 webpage（会议官网投稿页面）的 multipart/form-data 提交
 * - 字段名与 webpage Submit.vue 表单对齐（authorPhone/authorEmail/paperFile）
 * - 自动将 webpage 字段名映射为后端 PaperSubmissionForm 字段名
 * - 无需登录认证（通过 SecurityConfig ignore-urls 白名单放行）
 *
 * 字段映射关系：
 * - paperTitle    → paperTitle（同名）
 * - firstAuthor   → firstAuthor（同名）
 * - authorPhone   → contactNumber
 * - authorEmail   → contactEmail
 * - confirmAuthorEmail → （仅前端校验用，后端不存储）
 * - company       → company（同名）
 * - paperAbstract → paperAbstract（同名）
 * - keywords      → keywords（同名）
 * - paperFile     → 上传文件后URL存入 appendix
 *
 * @author 243269112
 * @since 2026/10/14
 */
@Tag(name = "会议投稿公开提交接口")
@RestController
@RequestMapping("/api/paper")
@RequiredArgsConstructor
@Slf4j
public class PaperSubmitController {

    private final PaperSubmissionService paperSubmissionService;
    private final FileService fileService;

    /**
     * 投稿提交（webpage前端调用）
     *
     * 请求：POST /api/paper/submit
     * Content-Type: multipart/form-data
     * 表单字段：paperTitle, firstAuthor, authorPhone, authorEmail, confirmAuthorEmail,
     *           company, paperAbstract, keywords, paperFile(文件)
     *
     * 流程：
     * 1. 上传论文文件（paperFile）→ 获取文件URL
     * 2. 将webpage字段名映射为后端PaperSubmissionForm字段名
     * 3. 调用Service保存投稿记录
     */
    @Operation(summary = "投稿提交（webpage前端）")
    @PostMapping("/submit")
    @RepeatSubmit
    public Result<?> submitPaper(
            @RequestParam("paperTitle") String paperTitle,
            @RequestParam("firstAuthor") String firstAuthor,
            @RequestParam("authorPhone") String authorPhone,
            @RequestParam("authorEmail") String authorEmail,
            @RequestParam(value = "company", required = false) String company,
            @RequestParam("paperAbstract") String paperAbstract,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "paperFile", required = false) MultipartFile paperFile
    ) {
        log.info("收到投稿提交：paperTitle={}, firstAuthor={}, company={}, authorEmail={}",
                paperTitle, firstAuthor, company, authorEmail);

        // 步骤1：上传论文文件
        String appendixUrl = null;
        if (paperFile != null && !paperFile.isEmpty()) {
            FileInfo fileInfo = fileService.uploadFile(paperFile);
            appendixUrl = fileInfo.getUrl();
            log.info("论文文件上传成功：{}", appendixUrl);
        }

        // 步骤2：构建后端表单对象（webpage字段名 → 后端字段名映射）
        PaperSubmissionForm form = new PaperSubmissionForm();
        form.setPaperTitle(paperTitle);
        form.setFirstAuthor(firstAuthor);
        form.setName(firstAuthor);           // 投稿人姓名默认取第一作者
        form.setContactNumber(authorPhone);   // authorPhone → contactNumber
        form.setContactEmail(authorEmail);    // authorEmail → contactEmail
        form.setCompany(company);             // company → company（同名）
        form.setPaperAbstract(paperAbstract);
        form.setKeywords(keywords);
        form.setAppendix(appendixUrl);        // 文件URL → appendix

        // 步骤3：保存投稿记录
        boolean result = paperSubmissionService.savePaperSubmission(form);
        log.info("投稿保存结果：{}", result);
        return Result.judge(result);
    }
}