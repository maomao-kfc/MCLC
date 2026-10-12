package com.youlai.boot.support.mail.service.impl;

import com.youlai.boot.config.property.MailProperties;
import com.youlai.boot.support.mail.model.dto.MailSendResult;
import com.youlai.boot.support.mail.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 邮件服务实现类
 *
 * 核心功能：
 * 1. send() — 发送MimeMessage邮件，支持HTML+附件，异步执行(@Async)
 * 2. sendSimpleMail() — 发送简单文本邮件（无HTML/附件）
 * 3. sendWithAttachment() — 发送带附件邮件
 *
 * 邮件发送流程：
 * - 构建MimeMessage → JavaMailSender.send() → 成功/失败回调
 * - 由MailSendRecordService记录发送状态(9态状态机)
 *
 * 配置：spring.mail(smtp.163.com:465) + MailProperties
 *
 * 技术文档详见：docs/05-email-subsystem.md
 *
 * @author Ray
 * @since 2024/8/17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    private final MailProperties mailProperties;

    /** 最大重试次数 */
    private static final int MAX_RETRIES = 2;

    /** 重试间隔（毫秒） */
    private static final long RETRY_INTERVAL_MS = 2000;

    /**
     * 发送简单文本邮件（异步，失败自动重试一次）
     */
    @Async("mailExecutor")
    @Override
    public void sendMail(String to, String subject, String text) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(mailProperties.getFrom());
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                mailSender.send(message);
                log.info("邮件发送成功：to={}, subject={}", to, subject);
                return;
            } catch (Exception e) {
                if (i < MAX_RETRIES - 1) {
                    log.warn("邮件发送失败，准备重试({}/{})：to={}, error={}", i + 1, MAX_RETRIES, to, e.getMessage());
                    sleep(RETRY_INTERVAL_MS);
                } else {
                    log.error("邮件发送失败，已重试{}次：to={}, error={}", MAX_RETRIES, to, e.getMessage());
                }
            }
        }
    }

    /**
     * 发送带附件的邮件
     */
    @Override
    public void sendMailWithAttachment(String to, String subject, String text, String filePath) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            FileSystemResource file = new FileSystemResource(new File(filePath));
            helper.addAttachment(file.getFilename(), file);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("发送带附件的邮件失败{}", e.getMessage());
        }
    }

    /**
     * 发送HTML邮件（异步，失败仅记录日志）
     */
    @Async("mailExecutor")
    @Override
    public void sendHtmlMail(String to, String subject, String html) {
        try {
            sendHtmlMailInternal(to, subject, html);
        } catch (MessagingException e) {
            log.error("HTML邮件异步发送失败：to={}, error={}", to, e.getMessage());
        }
    }

    /**
     * 同步发送HTML邮件（带重试，返回成功/失败）
     */
    @Override
    public boolean sendHtmlMailSync(String to, String subject, String html) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                sendHtmlMailInternal(to, subject, html);
                log.info("HTML邮件同步发送成功：to={}, subject={}", to, subject);
                return true;
            } catch (Exception e) {
                if (i < MAX_RETRIES - 1) {
                    log.warn("HTML邮件同步发送失败，准备重试({}/{})：to={}, error={}", i + 1, MAX_RETRIES, to, e.getMessage());
                    sleep(RETRY_INTERVAL_MS);
                } else {
                    log.error("HTML邮件同步发送失败，已重试{}次：to={}, error={}", MAX_RETRIES, to, e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * 同步发送HTML邮件并返回详细结果（包含Message-ID、耗时、错误信息）
     *
     * 用于邮件发送记录系统，记录完整的发送信息。
     * 内部带2次重试，最终返回最后一次的结果。
     */
    @Override
    public MailSendResult sendHtmlMailWithResult(String to, String subject, String html) {
        String lastError = null;
        for (int i = 0; i < MAX_RETRIES; i++) {
            long startTime = System.currentTimeMillis();
            try {
                MimeMessage message = sendHtmlMailInternalAndGetMessage(to, subject, html);
                long consumeTime = System.currentTimeMillis() - startTime;
                String messageId = message.getMessageID();
                log.info("HTML邮件发送成功：to={}, subject={}, messageId={}, 耗时={}ms", to, subject, messageId, consumeTime);
                return MailSendResult.success(messageId, consumeTime);
            } catch (Exception e) {
                long consumeTime = System.currentTimeMillis() - startTime;
                lastError = e.getMessage();
                if (i < MAX_RETRIES - 1) {
                    log.warn("HTML邮件发送失败，准备重试({}/{})：to={}, error={}, 耗时={}ms", i + 1, MAX_RETRIES, to, e.getMessage(), consumeTime);
                    sleep(RETRY_INTERVAL_MS);
                } else {
                    log.error("HTML邮件发送失败，已重试{}次：to={}, error={}, 耗时={}ms", MAX_RETRIES, to, e.getMessage(), consumeTime);
                }
            }
        }
        return MailSendResult.fail(lastError, 0);
    }

    /**
     * 内部发送HTML邮件（抛出异常）
     */
    private void sendHtmlMailInternal(String to, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailProperties.getFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    /**
     * 内部发送HTML邮件并返回MimeMessage（用于获取Message-ID）
     */
    private MimeMessage sendHtmlMailInternalAndGetMessage(String to, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailProperties.getFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
        return message;
    }

    /**
     * 线程休眠辅助方法
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}