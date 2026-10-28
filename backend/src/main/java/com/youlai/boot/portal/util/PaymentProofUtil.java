package com.youlai.boot.portal.util;

import cn.hutool.crypto.digest.DigestUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 支付凭证工具类：水印生成、图片哈希、OCR文本提取
 */
public class PaymentProofUtil {

    private PaymentProofUtil() {}

    /**
     * 给图片添加平铺斜向半透明水印
     *
     * @param original      原始图片
     * @param watermarkText 水印文字（如：MCLC-REG12345-u2-20261027153000）
     * @return 带水印的图片
     */
    public static BufferedImage addWatermark(BufferedImage original, String watermarkText) {
        int width = original.getWidth();
        int height = original.getHeight();
        // 创建带透明通道的副本
        BufferedImage watermarked = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = watermarked.createGraphics();
        // 先绘制原图
        g2d.drawImage(original, 0, 0, null);

        // 水印设置：半透明灰色、斜向45度平铺
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2d.setColor(new Color(120, 120, 120));
        int fontSize = Math.max(14, Math.min(width, height) / 25);
        g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));

        // 旋转-30度，平铺水印
        g2d.rotate(Math.toRadians(-30), width / 2.0, height / 2.0);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(watermarkText);
        int stepX = textWidth + 80;
        int stepY = fontSize * 4;
        // 扩大绘制范围确保旋转后覆盖全图
        for (int x = -width; x < width * 2; x += stepX) {
            for (int y = -height; y < height * 2; y += stepY) {
                g2d.drawString(watermarkText, x, y);
            }
        }
        g2d.dispose();
        return watermarked;
    }

    /**
     * BufferedImage 转 byte[]
     */
    public static byte[] imageToBytes(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format != null ? format : "png", baos);
            return baos.toByteArray();
        }
    }

    /**
     * 计算文件 MD5 哈希
     */
    public static String calculateMd5(byte[] data) {
        return DigestUtil.md5Hex(data);
    }

    /**
     * 计算图片 dHash（差异哈希，感知哈希的一种）
     * <p>原理：缩放到 9x8，比较每行相邻像素亮度，生成 64bit 哈希</p>
     * <p>能识别裁剪、压缩、轻微改色后的同一张图</p>
     *
     * @param image 原始图片
     * @return 16位十六进制哈希字符串
     */
    public static String calculateDHash(BufferedImage image) {
        // 缩放到 9x8
        BufferedImage scaled = new BufferedImage(9, 8, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, 9, 8, null);
        g.dispose();

        // 转灰度并计算差异
        long hash = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int grayLeft = grayValue(scaled.getRGB(x, y));
                int grayRight = grayValue(scaled.getRGB(x + 1, y));
                if (grayLeft > grayRight) {
                    hash |= (1L << (y * 8 + x));
                }
            }
        }
        return String.format("%016x", hash);
    }

    private static int grayValue(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000;
    }

    /**
     * 计算两个 dHash 的汉明距离（0=完全相同，<5=极可能是同一张图）
     */
    public static int hammingDistance(String hash1, String hash2) {
        if (hash1 == null || hash2 == null || hash1.length() != hash2.length()) {
            return 64;
        }
        long h1 = Long.parseUnsignedLong(hash1, 16);
        long h2 = Long.parseUnsignedLong(hash2, 16);
        return Long.bitCount(h1 ^ h2);
    }

    /**
     * 从 OCR 文本中提取金额
     * <p>匹配模式：-¥1000.00 / ¥1,000.00 / ¥1000 / -1000 / 100.00元 / 金额：100.00 / Amount: 100.00</p>
     * <p>支持：负号（微信支出截图）、千分位逗号、整数金额（无小数点）</p>
     */
    public static String extractAmountFromOcrText(String ocrText) {
        if (ocrText == null || ocrText.isBlank()) {
            return null;
        }
        // 预处理：去掉千分位逗号（1,000.00 → 1000.00），避免逗号被误认为小数点
        String text = ocrText.replaceAll("(\\d),(\\d{3})", "$1$2");
        // 优先匹配带货币符号的金额（支持前置负号和小数可选）
        Pattern[] patterns = {
                Pattern.compile("[¥￥€$]\\s*-?\\s*(\\d+(?:\\.\\d{1,2})?)"),
                Pattern.compile("-?\\s*(\\d+(?:\\.\\d{1,2})?)\\s*元"),
                Pattern.compile("金额[:：\\s]*-?\\s*(\\d+(?:\\.\\d{1,2})?)"),
                Pattern.compile("Amount[:：\\s]*-?\\s*(\\d+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE),
                // 兜底：负号开头的数字（微信/支付宝支出截图特征：-1000）
                Pattern.compile("(?<!\\d)-\\s*(\\d+(?:\\.\\d{1,2})?)(?!\\d)")
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    /**
     * 从 OCR 文本中提取交易单号
     * <p>微信支付截图格式：交易单号 4200001234202610271234567890（通常20位以上纯数字）</p>
     *
     * @param ocrText OCR识别文本
     * @return 交易单号，未识别返回null
     */
    public static String extractTradeNoFromOcrText(String ocrText) {
        if (ocrText == null || ocrText.isBlank()) {
            return null;
        }
        // 匹配"交易单号"后跟的数字串（微信交易单号15~32位纯数字，兼容OCR漏字）
        Pattern tradeNoPattern = Pattern.compile("交易单号[:：\\s]*(\\d{15,32})");
        Matcher m = tradeNoPattern.matcher(ocrText);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * 从 OCR 文本中提取商户单号
     * <p>微信支付截图格式：商户单号 字母/数字/横线组合（商户侧订单号）</p>
     *
     * @param ocrText OCR识别文本
     * @return 商户单号，未识别返回null
     */
    public static String extractMerchantNoFromOcrText(String ocrText) {
        if (ocrText == null || ocrText.isBlank()) {
            return null;
        }
        // 匹配"商户单号"后跟的字母数字横线组合（至少6位，避免误匹配短词）
        Pattern merchantNoPattern = Pattern.compile("商户单号[:：\\s]*([A-Za-z0-9\\-]{6,40})");
        Matcher m = merchantNoPattern.matcher(ocrText);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * 比较两个金额是否一致（忽略格式差异，容差0.01）
     */
    public static boolean amountsMatch(String amount1, String amount2) {
        try {
            if (amount1 == null || amount2 == null) {
                return false;
            }
            BigDecimal a = new BigDecimal(amount1.replace(",", ".").replace("¥", "").replace("€", "").replace("$", "").trim());
            BigDecimal b = new BigDecimal(amount2.replace(",", ".").replace("¥", "").replace("€", "").replace("$", "").trim());
            return a.subtract(b).abs().compareTo(new BigDecimal("0.01")) <= 0;
        } catch (Exception e) {
            return false;
        }
    }
}