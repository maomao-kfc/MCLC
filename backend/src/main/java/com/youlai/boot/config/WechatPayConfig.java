package com.youlai.boot.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.cipher.AeadAesCipher;
import com.wechat.pay.java.core.cipher.AeadCipher;
import com.wechat.pay.java.core.cipher.Verifier;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.certificate.CertificateService;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.refund.RefundService;
import com.youlai.boot.config.property.WechatPayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties(WechatPayProperties.class)
@RequiredArgsConstructor
public class WechatPayConfig {

    private final WechatPayProperties wechatPayProperties;

    @Bean
    public Config payConfig() {
        try {
            Config config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(wechatPayProperties.getMchId())
                    .privateKey(loadPrivateKey())
                    .merchantSerialNumber(wechatPayProperties.getMchSerialNo())
                    .apiV3Key(wechatPayProperties.getApiV3Key())
                    .build();
            log.info("RSAAutoCertificateConfig 初始化成功，平台证书已自动下载");
            return config;
        } catch (Exception e) {
            log.warn("自动下载平台证书失败: {}，回退到 RSAPublicKeyConfig", e.getMessage());
            RSAPublicKeyConfig.Builder builder = new RSAPublicKeyConfig.Builder()
                    .merchantId(wechatPayProperties.getMchId())
                    .privateKey(loadPrivateKey())
                    .merchantSerialNumber(wechatPayProperties.getMchSerialNo())
                    .apiV3Key(wechatPayProperties.getApiV3Key());

            try {
                String certPath = wechatPayProperties.getPlatformCertPath();
                if (certPath != null && !certPath.isBlank()) {
                    String certContent = loadPemContent(certPath);
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(
                            new ByteArrayInputStream(certContent.getBytes(StandardCharsets.UTF_8)));
                    builder.publicKey(cert.getPublicKey());
                    builder.publicKeyId(cert.getSerialNumber().toString(16).toUpperCase());
                }
            } catch (Exception ex) {
                log.warn("本地平台证书不可用，使用商户公钥作为回退");
                builder.publicKey(loadPublicKey());
                builder.publicKeyId(wechatPayProperties.getMchSerialNo());
            }

            return builder.build();
        }
    }

    private String loadPublicKey() {
        String pubPath = wechatPayProperties.getPrivateKeyPath().replace("apiclient_key.pem", "apiclient_pub.pem");
        return loadPemContent(pubPath);
    }

    private String loadPrivateKey() {
        return loadPemContent(wechatPayProperties.getPrivateKeyPath());
    }

    private String loadPemContent(String path) {
        try {
            if (path.startsWith("classpath:")) {
                ClassPathResource resource = new ClassPathResource(path.substring("classpath:".length()));
                try (InputStream is = resource.getInputStream()) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException("无法读取PEM文件: " + path, e);
        }
    }

    @Bean
    public NotificationParser notificationParser(NotificationConfig wechatNotificationConfig) {
        return new NotificationParser(wechatNotificationConfig);
    }

    @Bean
    public NotificationConfig wechatNotificationConfig(Config payConfig) {
        String apiV3Key = wechatPayProperties.getApiV3Key();
        String certPath = wechatPayProperties.getPlatformCertPath();
        PublicKey platformPublicKey = null;
        String platformSerialNo = "";

        if (certPath != null && !certPath.isBlank()) {
            try {
                String certContent = loadPemContent(certPath);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) cf.generateCertificate(
                        new ByteArrayInputStream(certContent.getBytes(StandardCharsets.UTF_8)));
                platformPublicKey = cert.getPublicKey();
                platformSerialNo = cert.getSerialNumber().toString(16).toUpperCase();
            } catch (Exception e) {
                log.warn("本地平台证书加载失败: {}，尝试从微信API自动下载...", e.getMessage());
                try {
                    X509Certificate cert = downloadPlatformCert(payConfig, apiV3Key, certPath);
                    if (cert != null) {
                        platformPublicKey = cert.getPublicKey();
                        platformSerialNo = cert.getSerialNumber().toString(16).toUpperCase();
                        log.info("平台证书已从微信API下载成功，序列号: {}", platformSerialNo);
                    }
                } catch (Exception ex) {
                    log.warn("自动下载平台证书失败: {}，回调签名验证将不可用", ex.getMessage());
                }
            }
        }

        PublicKey finalPublicKey = platformPublicKey;
        String finalSerialNo = platformSerialNo;

        return new NotificationConfig() {
            @Override
            public String getSignType() {
                return "WECHATPAY2-SHA256-RSA2048";
            }
            @Override
            public String getCipherType() {
                return "AEAD_AES_256_GCM";
            }
            @Override
            public Verifier createVerifier() {
                if (finalPublicKey == null) {
                    return new Verifier() {
                        @Override
                        public String getSerialNumber() {
                            return "";
                        }
                        @Override
                        public boolean verify(String serialNumber, String message, String signature) {
                            log.warn("微信支付平台证书未配置，回调验签不可用");
                            return false;
                        }
                    };
                }
                return new Verifier() {
                    @Override
                    public String getSerialNumber() {
                        return finalSerialNo;
                    }
                    @Override
                    public boolean verify(String serialNumber, String message, String signature) {
                        try {
                            Signature sign = Signature.getInstance("SHA256withRSA");
                            sign.initVerify(finalPublicKey);
                            sign.update(message.getBytes(StandardCharsets.UTF_8));
                            return sign.verify(Base64.getDecoder().decode(signature));
                        } catch (Exception e) {
                            return false;
                        }
                    }
                };
            }
            @Override
            public AeadCipher createAeadCipher() {
                return new AeadAesCipher(apiV3Key.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    private X509Certificate downloadPlatformCert(Config payConfig, String apiV3Key, String certPath) {
        AeadAesCipher cipher = new AeadAesCipher(apiV3Key.getBytes(StandardCharsets.UTF_8));
        CertificateService certService = new CertificateService.Builder()
                .config(payConfig)
                .build();
        List<X509Certificate> certs = certService.downloadCertificate(cipher);
        if (certs.isEmpty()) {
            log.warn("微信API未返回平台证书");
            return null;
        }
        X509Certificate cert = certs.get(0);
        saveCertToFile(cert, certPath);
        return cert;
    }

    private void saveCertToFile(X509Certificate cert, String certPath) {
        try {
            String path = certPath.startsWith("classpath:") ? certPath.substring("classpath:".length()) : certPath;
            String baseDir = System.getProperty("user.dir");
            Path savePath = Paths.get(baseDir, "src", "main", "resources", path);
            Files.createDirectories(savePath.getParent());
            String pem = "-----BEGIN CERTIFICATE-----\n"
                    + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
                    .encodeToString(cert.getEncoded())
                    + "\n-----END CERTIFICATE-----\n";
            Files.writeString(savePath, pem, StandardCharsets.UTF_8);
            log.info("平台证书已保存到: {}", savePath.toAbsolutePath());
        } catch (Exception e) {
            log.warn("保存平台证书到文件失败 (证书已加载到内存): {}", e.getMessage());
        }
    }

    @Bean
    public NativePayService nativePayService(Config payConfig) {
        return new NativePayService.Builder().config(payConfig).build();
    }

    @Bean
    public H5Service h5Service(Config payConfig) {
        return new H5Service.Builder().config(payConfig).build();
    }

    @Bean
    public RefundService refundService(Config payConfig) {
        return new RefundService.Builder().config(payConfig).build();
    }
}
