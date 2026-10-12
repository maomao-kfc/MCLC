package com.youlai.boot.support.mail.service;

import com.youlai.boot.support.mail.model.dto.MailSendResult;

/**
 * 邮件服务接口层
 *
 * @author Ray
 * @since 2024/8/17
 */
public interface MailService {


    /**
     * 发送简单文本邮件
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param text    邮件内容
     */
    void sendMail(String to, String subject, String text) ;

    /**
     * 发送带附件的邮件
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param text    邮件内容
     * @param filePath 附件路径
     */
    void sendMailWithAttachment(String to, String subject, String text, String filePath);

    /**
     * 发送HTML邮件（异步，失败仅记录日志）
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param html    HTML邮件内容
     */
    void sendHtmlMail(String to, String subject, String html);

    /**
     * 同步发送HTML邮件（带重试，返回成功/失败）
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param html    HTML邮件内容
     * @return 发送成功返回true，失败返回false
     */
    boolean sendHtmlMailSync(String to, String subject, String html);

    /**
     * 同步发送HTML邮件并返回详细结果（包含Message-ID、耗时、错误信息）
     *
     * 用于邮件发送记录系统，需要记录完整的发送信息。
     * 内部带2次重试，最终返回最后一次的结果。
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param html    HTML邮件内容
     * @return MailSendResult 包含成功/失败、Message-ID、耗时、错误信息
     */
    MailSendResult sendHtmlMailWithResult(String to, String subject, String html);

}