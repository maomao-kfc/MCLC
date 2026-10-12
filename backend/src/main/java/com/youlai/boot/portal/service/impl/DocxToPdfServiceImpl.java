package com.youlai.boot.portal.service.impl;

import com.youlai.boot.portal.service.DocxToPdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * DOCX 转 PDF 服务实现（159行）
 *
 * 直接调用 LibreOffice 命令行进行转换（soffice --headless --convert-to pdf）。
 * 比 JODConverter 更可靠，避免进程池连接超时问题。
 *
 * 条件装配：@ConditionalOnProperty(name="jodconverter.local.enabled", havingValue="true")
 * 未启用时Bean不创建，Controller层通过@Autowired(required=false)注入，null时回退到OpenPDF
 *
 * 转换流程(5步)：
 * 1. 下载DOCX到临时目录
 * 2. 构建soffice命令（--headless --convert-to pdf --outdir）
 * 3. 执行ProcessBuilder命令
 * 4. 在输出目录查找生成的PDF文件
 * 5. 写入OutputStream + 清理临时文件
 *
 * soffice路径4级查找：
 * office-home/program/soffice.exe → office-home/program/soffice → /usr/bin/soffice → 系统PATH
 *
 * 前置条件：服务器需安装 LibreOffice
 * - CentOS/RHEL: sudo yum install libreoffice
 * - Ubuntu/Debian: sudo apt install libreoffice
 * - Windows: 安装 LibreOffice 并配置 jodconverter.local.office-home
 *
 * 已知问题：PDF-002(ProcessBuilder命令注入风险，已通过URI.create()限制) / PDF-003(临时文件残留)
 *
 * 技术文档详见：docs/10-paper-submission.md 第9.3节
 *
 * @author 毛江俊
 * @since 2026/7/21
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "jodconverter.local.enabled", havingValue = "true")
public class DocxToPdfServiceImpl implements DocxToPdfService {

    /** LibreOffice 安装路径，从配置文件读取 */
    @Value("${jodconverter.local.office-home:}")
    private String officeHome;

    /**
     * 将 DOCX 文件转换为 PDF
     *
     * 流程：下载DOCX到临时文件 → 调用LibreOffice命令行转换 → 读取PDF临时文件 → 写入输出流
     *
     * @param docxUrl      DOCX 文件的 URL 地址（MinIO/OSS 外链）
     * @param outputStream PDF 输出流
     * @throws Exception 转换过程中的异常
     */
    @Override
    public void convertDocxToPdf(String docxUrl, OutputStream outputStream) throws Exception {
        log.info("开始转换 DOCX 到 PDF（LibreOffice 命令行），URL: {}", docxUrl);

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "jodconverter");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File tempDocx = null;
        File tempPdf = null;

        try {
            // 1. 下载 DOCX 到临时文件
            tempDocx = new File(tempDir, "source_" + System.currentTimeMillis() + ".docx");
            try (InputStream docxInputStream = URI.create(docxUrl).toURL().openStream()) {
                Files.copy(docxInputStream, tempDocx.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("DOCX 下载完成，临时文件: {}, 大小: {} bytes", tempDocx.getAbsolutePath(), tempDocx.length());

            // 2. 构建 LibreOffice 命令行
            String sofficePath = getSofficePath();
            ProcessBuilder pb = new ProcessBuilder(
                    sofficePath,
                    "--headless",
                    "--convert-to", "pdf",
                    "--outdir", tempDir.getAbsolutePath(),
                    tempDocx.getAbsolutePath()
            );
            pb.redirectErrorStream(true);

            log.info("执行 LibreOffice 命令: {}", String.join(" ", pb.command()));

            // 3. 执行转换
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("LibreOffice 转换失败，exitCode: {}, output: {}", exitCode, output);
                throw new RuntimeException("LibreOffice 转换失败 (exitCode=" + exitCode + "): " + output);
            }

            // 4. 查找生成的 PDF 文件（LibreOffice 输出文件名与源文件同名但扩展名改为 .pdf）
            String pdfFileName = tempDocx.getName().replace(".docx", ".pdf").replace(".doc", ".pdf");
            tempPdf = new File(tempDir, pdfFileName);

            if (!tempPdf.exists()) {
                log.error("LibreOffice 转换后未找到 PDF 文件: {}", tempPdf.getAbsolutePath());
                throw new RuntimeException("LibreOffice 转换后未找到 PDF 文件");
            }

            log.info("DOCX 转 PDF 完成，PDF 文件大小: {} bytes", tempPdf.length());

            // 5. 将 PDF 临时文件写入输出流
            Files.copy(tempPdf.toPath(), outputStream);
            outputStream.flush();

        } catch (Exception e) {
            log.error("DOCX 转 PDF 失败", e);
            throw new RuntimeException("DOCX 转 PDF 失败: " + e.getMessage(), e);
        } finally {
            // 清理临时文件
            try {
                if (tempDocx != null && tempDocx.exists()) tempDocx.delete();
                if (tempPdf != null && tempPdf.exists()) tempPdf.delete();
            } catch (Exception e) {
                log.warn("清理临时文件失败", e);
            }
        }
    }

    /**
     * 获取 soffice 可执行文件路径
     *
     * 查找顺序：
     * 1. office-home 配置路径下的 soffice.exe（Windows）
     * 2. office-home 配置路径下的 soffice（Linux）
     * 3. /usr/bin/soffice（Debian/Ubuntu 包管理器创建的符号链接）
     * 4. 系统 PATH 中的 soffice（最后回退）
     */
    private String getSofficePath() {
        log.info("查找 soffice 路径，office-home 配置: {}", officeHome);

        if (officeHome != null && !officeHome.isEmpty()) {
            // 配置了 office-home，使用指定路径
            String winPath = officeHome + File.separator + "program" + File.separator + "soffice.exe";
            if (new File(winPath).exists()) {
                log.info("找到 Windows soffice: {}", winPath);
                return winPath;
            }
            // Linux 路径
            String linuxPath = officeHome + File.separator + "program" + File.separator + "soffice";
            if (new File(linuxPath).exists()) {
                log.info("找到 Linux soffice: {}", linuxPath);
                return linuxPath;
            }
            log.warn("office-home 配置为 {}，但 soffice 不存在于 program/ 目录下", officeHome);
        }

        // Debian/Ubuntu 包管理器安装的 LibreOffice 会在 /usr/bin/ 创建符号链接
        String usrBinPath = "/usr/bin/soffice";
        if (new File(usrBinPath).exists()) {
            log.info("找到 /usr/bin/soffice 符号链接");
            return usrBinPath;
        }

        // 最后回退：尝试系统 PATH 中的 soffice
        log.warn("未找到 soffice 可执行文件，将尝试系统 PATH 中的 soffice（可能失败）");
        return "soffice";
    }
}