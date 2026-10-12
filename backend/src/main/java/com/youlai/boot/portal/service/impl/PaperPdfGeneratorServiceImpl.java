package com.youlai.boot.portal.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfReader;
import com.youlai.boot.portal.model.entity.PaperSubmission;
import com.youlai.boot.portal.service.PaperPdfGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 论文信息 PDF 生成服务实现（257行）
 *
 * 使用 OpenPDF 根据数据库中的论文信息生成格式化 PDF。
 *
 * 两种生成模式：
 * 1. generateCoverPage() — 封面页(isCoverPage=true)：4行2列表格
 *    与附件PDF合并：封面页在前，附件内容在后(mergePdfs)
 * 2. generatePdf() — 完整信息页(isCoverPage=false)：同样的4行2列表格
 *
 * 中文字体回退机制(9+7路径)：
 * CHINESE_FONT_PATHS(9个文件路径) → CHINESE_FONT_NAMES(7个字体名) → HELVETICA(最终回退)
 * - Windows: C:/Windows/Fonts/simsun.ttc 等
 * - Linux: /usr/share/fonts/truetype/wqy/wqy-zenhei.ttc 等
 * ⚠ PDF-001: HELVETICA回退时中文将显示为方框，部署前需确认字体安装
 *
 * mergePdfs():193 — PdfCopy逐页合并，封面页在前
 *
 * 已知问题：PDF-001(中文字体HELVETICA回退) / PDF-003(临时文件清理)
 *
 * 技术文档详见：docs/10-paper-submission.md 第9节
 *
 * @author system
 * @since 2026/7/21
 */
@Slf4j
@Service
public class PaperPdfGeneratorServiceImpl implements PaperPdfGeneratorService {

    /**
     * 中文字体文件路径（按优先级尝试）
     * 格式：路径,索引（TTC字体集合需要指定索引，OTF/TTF不需要）
     *
     * Docker 容器中安装 fonts-wqy-zenhei 和 fonts-noto-cjk 后，
     * 字体文件可能分布在多个目录，这里列出所有常见路径
     */
    private static final String[] CHINESE_FONT_PATHS = {
            // Windows 字体
            "C:/Windows/Fonts/simsun.ttc,0",           // Windows 宋体
            "C:/Windows/Fonts/msyh.ttc,0",             // Windows 微软雅黑
            // Linux 文泉驿（fonts-wqy-zenhei 包）
            "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc,0",
            // Linux Noto CJK（fonts-noto-cjk 包）- 多种可能的路径和格式
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0",
            "/usr/share/fonts/opentype/noto/NotoSansCJKsc-Regular.otf",
            "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc,0",
            "/usr/share/fonts/truetype/noto/NotoSansCJKsc-Regular.otf",
            "/usr/share/fonts/noto-cjk/NotoSansCJK-Regular.ttc,0",
            // Linux Noto Sans CJK SC 单独包
            "/usr/share/fonts/opentype/noto/NotoSansSC-Regular.otf",
            "/usr/share/fonts/truetype/noto/NotoSansSC-Regular.otf",
    };

    /** 中文字体名称（字体文件路径都找不到时的回退方案，通过 fontconfig 查找） */
    private static final String[] CHINESE_FONT_NAMES = {
            "SimSun",
            "WenQuanYi Zen Hei",
            "Noto Sans CJK SC",
            "NotoSansCJKsc-Regular",
            "Noto Sans SC",
            "STSong",
            "AR PL UMing CN",
    };

    /**
     * 生成封面信息页 PDF（有附件时使用）
     * 内容：4行2列表格（论文标题、姓名、联系电话、论文摘要）
     */
    @Override
    public void generateCoverPage(PaperSubmission paper, OutputStream outputStream) throws Exception {
        log.info("生成封面页 PDF，paperId: {}, title: {}", paper.getId(), paper.getPaperTitle());
        generateInfoPdf(paper, outputStream, true);
    }

    /**
     * 生成完整信息 PDF（无附件时使用）
     * 内容：同样的4行2列表格
     */
    @Override
    public void generatePdf(PaperSubmission paper, OutputStream outputStream) throws Exception {
        log.info("生成完整信息 PDF，paperId: {}, title: {}", paper.getId(), paper.getPaperTitle());
        generateInfoPdf(paper, outputStream, false);
    }

    /**
     * 生成投稿者信息 PDF（封面页和完整信息页共用）
     *
     * @param paper       论文投稿实体
     * @param outputStream PDF 输出流
     * @param isCoverPage  是否为封面页模式（封面页顶部空行多一些）
     */
    private void generateInfoPdf(PaperSubmission paper, OutputStream outputStream, boolean isCoverPage) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 60, 40);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // 获取中文字体
            BaseFont chineseBaseFont = getChineseBaseFont();
            Font titleFont = new Font(chineseBaseFont, 18, Font.BOLD);
            Font labelFont = new Font(chineseBaseFont, 12, Font.BOLD);
            Font contentFont = new Font(chineseBaseFont, 12, Font.NORMAL);
            Font smallFont = new Font(chineseBaseFont, 8, Font.NORMAL);
            smallFont.setColor(Color.GRAY);

            // 封面页顶部多空一些
            if (isCoverPage) {
                for (int i = 0; i < 3; i++) {
                    document.add(Chunk.NEWLINE);
                }
            }

            // 1. 论文标题居中
            Paragraph title = new Paragraph(paper.getPaperTitle(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 2. 4行2列信息表格
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.setSpacingBefore(10);
            table.setSpacingAfter(20);

            // 设置列宽：标签列窄，内容列宽
            table.setWidths(new float[]{1f, 3f});

            // 添加表格行
            addTableRow(table, "论文标题", paper.getPaperTitle(), labelFont, contentFont);
            addTableRow(table, "姓名", paper.getName(), labelFont, contentFont);
            addTableRow(table, "联系电话", paper.getContactNumber(), labelFont, contentFont);
            addTableRow(table, "论文摘要", paper.getPaperAbstract(), labelFont, contentFont);

            document.add(table);

            // 3. Generated 时间戳
            Paragraph footer = new Paragraph(
                    "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    smallFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

            log.info("投稿者信息 PDF 生成完成");

        } catch (Exception e) {
            log.error("生成投稿者信息 PDF 失败", e);
            throw new RuntimeException("生成投稿者信息 PDF 失败: " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }

    /**
     * 添加表格行
     */
    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font contentFont) {
        // 标签单元格
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(new Color(245, 245, 245));
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setPadding(10);
        table.addCell(labelCell);

        // 内容单元格
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", contentFont));
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setPadding(10);
        table.addCell(valueCell);
    }

    /**
     * 合并封面页PDF和附件PDF
     * 封面页在前，附件内容在后
     */
    public static void mergePdfs(byte[] coverPdfBytes, byte[] attachmentPdfBytes, OutputStream outputStream) throws Exception {
        log.info("开始合并PDF，封面页大小: {} bytes，附件大小: {} bytes", coverPdfBytes.length, attachmentPdfBytes.length);
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, outputStream);
        document.open();

        PdfReader coverReader = new PdfReader(coverPdfBytes);
        log.info("封面页页数: {}", coverReader.getNumberOfPages());
        for (int i = 1; i <= coverReader.getNumberOfPages(); i++) {
            copy.addPage(copy.getImportedPage(coverReader, i));
        }

        PdfReader attachmentReader = new PdfReader(attachmentPdfBytes);
        log.info("附件页数: {}", attachmentReader.getNumberOfPages());
        for (int i = 1; i <= attachmentReader.getNumberOfPages(); i++) {
            copy.addPage(copy.getImportedPage(attachmentReader, i));
        }

        document.close();
        coverReader.close();
        attachmentReader.close();
        log.info("PDF合并完成");
    }

    /**
     * 获取中文字体 BaseFont
     * 优先使用字体文件路径加载（更可靠），回退到字体名称
     */
    private BaseFont getChineseBaseFont() {
        // 方式1：通过字体文件路径加载（最可靠）
        log.info("开始查找中文字体，共 {} 个字体路径和 {} 个字体名称待尝试",
                CHINESE_FONT_PATHS.length, CHINESE_FONT_NAMES.length);
        for (String fontPath : CHINESE_FONT_PATHS) {
            try {
                BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                log.info("成功加载中文字体（文件路径）: {}", fontPath);
                return bf;
            } catch (Exception e) {
                log.debug("字体文件 {} 不可用: {}", fontPath, e.getMessage());
            }
        }

        // 方式2：通过字体名称加载（依赖 fontconfig）
        for (String fontName : CHINESE_FONT_NAMES) {
            try {
                BaseFont bf = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                log.info("成功加载中文字体（名称）: {}", fontName);
                return bf;
            } catch (Exception e) {
                log.debug("字体名称 {} 不可用: {}", fontName, e.getMessage());
            }
        }

        // 所有中文字体都不可用 - 输出诊断信息
        log.error("未找到任何中文字体！PDF中中文将无法显示。请安装中文字体。");
        log.error("诊断信息：请检查容器内是否安装了字体包（fonts-wqy-zenhei, fonts-noto-cjk）");
        log.error("诊断信息：请在容器内执行 fc-list :lang=zh 查看已安装的中文字体");
        log.error("诊断信息：请在容器内执行 ls -la /usr/share/fonts/ 查看字体文件目录");
        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException("无法创建 PDF 字体", e);
        }
    }
}