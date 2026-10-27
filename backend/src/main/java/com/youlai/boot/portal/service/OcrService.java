package com.youlai.boot.portal.service;

import java.io.InputStream;

/**
 * OCR 文字识别服务接口（可插拔设计）
 * <p>默认实现: TesseractOcrServiceImpl（基于 Tesseract 本地引擎）</p>
 * <p>如需切换为百度/阿里云等云 OCR，实现此接口替换 Bean 即可，主流程无需改动</p>
 */
public interface OcrService {

    /**
     * 从图片中识别文字
     *
     * @param imageStream 图片输入流
     * @param fileName    原始文件名（用于判断图片格式）
     * @return 识别出的文本，识别失败返回 null
     */
    String recognizeText(InputStream imageStream, String fileName);
}