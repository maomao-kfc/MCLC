package com.youlai.boot.portal.service.impl;

import com.youlai.boot.portal.service.OcrService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Tesseract OCR 实现
 * <p>依赖服务器/Docker容器内安装 tesseract-ocr 及语言包(chi_sim/eng)</p>
 * <p>Dockerfile 已添加安装命令；若未安装，识别时记录错误日志并返回 null，不影响主流程</p>
 */
@Slf4j
@Service
public class TesseractOcrServiceImpl implements OcrService {

    @Value("${ocr.tessdata-path:}")
    private String tessdataPath;

    @Value("${ocr.language:chi_sim+eng}")
    private String language;

    private Tesseract tesseract;

    private boolean available = false;

    @PostConstruct
    public void init() {
        try {
            tesseract = new Tesseract();
            // 未配置时自动探测常见 tessdata 路径
            if (tessdataPath == null || tessdataPath.isBlank()) {
                tessdataPath = detectTessdataPath();
            }
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage(language);
            // 支付截图场景：单行文本少，用默认引擎模式即可
            tesseract.setPageSegMode(6);
            available = true;
            log.info("Tesseract OCR 初始化成功, tessdataPath={}, language={}", tessdataPath, language);
        } catch (Exception | UnsatisfiedLinkError e) {
            available = false;
            log.warn("Tesseract OCR 初始化失败（服务器可能未安装tesseract-ocr），OCR初筛功能不可用: {}", e.getMessage());
        }
    }

    @Override
    public String recognizeText(InputStream imageStream, String fileName) {
        if (!available) {
            log.debug("Tesseract OCR 不可用，跳过识别");
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(imageStream);
            if (image == null) {
                log.warn("OCR图片读取失败, fileName={}", fileName);
                return null;
            }
            String result = tesseract.doOCR(image);
            return result != null ? result.trim() : null;
        } catch (TesseractException e) {
            log.error("Tesseract OCR 识别异常, fileName={}: {}", fileName, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("OCR处理异常, fileName={}", fileName, e);
            return null;
        }
    }

    /**
     * 探测常见 tessdata 路径（Debian/Ubuntu Docker 镜像）
     */
    private String detectTessdataPath() {
        String[] candidates = {
                "/usr/share/tesseract-ocr/5/tessdata",
                "/usr/share/tesseract-ocr/4.00/tessdata",
                "/usr/share/tessdata",
                "C:\\Program Files\\Tesseract-OCR\\tessdata"
        };
        for (String path : candidates) {
            if (java.nio.file.Files.exists(java.nio.file.Paths.get(path))) {
                return path;
            }
        }
        return "/usr/share/tesseract-ocr/5/tessdata";
    }
}