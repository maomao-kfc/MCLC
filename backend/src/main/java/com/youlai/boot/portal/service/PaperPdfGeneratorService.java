package com.youlai.boot.portal.service;

import com.youlai.boot.portal.model.entity.PaperSubmission;

import java.io.OutputStream;

/**
 * 论文信息 PDF 生成服务
 *
 * 两种生成模式：
 * 1. 封面页（有附件时）：论文标题、姓名、联系电话居中显示
 * 2. 完整信息页（无附件时）：论文标题、第一作者、联系邮箱、论文摘要、关键词
 *
 * @author system
 * @since 2026/7/21
 */
public interface PaperPdfGeneratorService {

    /**
     * 生成封面信息页 PDF（有附件时使用）
     * 内容：论文标题、姓名、联系电话，居中显示
     *
     * @param paper       论文投稿实体
     * @param outputStream PDF 输出流
     * @throws Exception 生成过程中的异常
     */
    void generateCoverPage(PaperSubmission paper, OutputStream outputStream) throws Exception;

    /**
     * 生成完整信息 PDF（无附件时使用）
     * 内容：论文标题、第一作者、联系邮箱、论文摘要、关键词
     *
     * @param paper       论文投稿实体
     * @param outputStream PDF 输出流
     * @throws Exception 生成过程中的异常
     */
    void generatePdf(PaperSubmission paper, OutputStream outputStream) throws Exception;
}