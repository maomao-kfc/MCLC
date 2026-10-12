package com.youlai.boot.portal.service;

import java.io.OutputStream;

/**
 * DOCX 转 PDF 服务接口
 *
 * @author system
 * @since 2026/7/21
 */
public interface DocxToPdfService {

    /**
     * 将 DOCX 文件转换为 PDF
     *
     * @param docxUrl    DOCX 文件的 URL 地址（MinIO/OSS 外链）
     * @param outputStream PDF 输出流
     * @throws Exception 转换过程中的异常
     */
    void convertDocxToPdf(String docxUrl, OutputStream outputStream) throws Exception;
}