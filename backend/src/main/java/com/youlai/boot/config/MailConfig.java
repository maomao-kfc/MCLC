package com.youlai.boot.config;

import com.youlai.boot.config.property.ImapProperties;
import com.youlai.boot.config.property.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * MailConfig 配置类，用于手动配置和注入 JavaMailSender。
 * 通过读取 MailProperties 类中配置的邮件相关属性来初始化 JavaMailSender。
 *
 * 手动注入的原因是为了避免在使用 application-dev.yml 或其他配置文件时，
 * IDEA 提示无法找到 JavaMailSender 的 bean。
 *
 * @author Ray
 * @since 2024/8/17
 */
@Configuration
@EnableConfigurationProperties({MailProperties.class, ImapProperties.class})
public class MailConfig {

    private final MailProperties mailProperties;

    public MailConfig(MailProperties mailProperties) {
        this.mailProperties = mailProperties;
    }

    /**
     * 创建并配置 JavaMailSender bean。
     *
     * @return 配置好的 JavaMailSender 实例
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", mailProperties.getProperties().getSmtp().isAuth());
        properties.put("mail.smtp.starttls.enable", mailProperties.getProperties().getSmtp().getStarttls().isEnable());
        if (mailProperties.getProperties().getSmtp().getSsl().isEnable()) {
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.socketFactory.port", String.valueOf(mailProperties.getPort()));
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.socketFactory.fallback", "false");
        }
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        properties.put("mail.smtp.ssl.trust", mailProperties.getHost());
        properties.put("mail.debug", "true");

        return mailSender;
    }
}
