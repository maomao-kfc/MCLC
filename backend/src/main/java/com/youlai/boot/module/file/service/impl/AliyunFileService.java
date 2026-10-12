package com.youlai.boot.module.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.UploadFileRequest;
import com.youlai.boot.module.file.service.FileService;
import com.youlai.boot.module.file.model.FileInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 阿里云 OSS 上传（支持大文件分片上传）
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "oss.type", havingValue = "aliyun")
@ConfigurationProperties(prefix = "oss.aliyun")
@RequiredArgsConstructor
@Data
public class AliyunFileService implements FileService {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    private OSS ossClient;

    /** 大文件阈值：超过此大小使用分片上传（默认10MB） */
    private static final long MULTIPART_THRESHOLD = 10 * 1024 * 1024;

    /** 分片大小：每个分片5MB */
    private static final long PART_SIZE = 5 * 1024 * 1024;

    /** 分片上传并发数 */
    private static final int CONCURRENT_UPLOAD_THREADS = 3;

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        log.info("Aliyun OSS client initialized, endpoint={}, bucket={}", endpoint, bucketName);
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    /**
     * 根据文件Content-Type确定OSS存储路径前缀
     */
    private String resolvePathPrefix(String contentType) {
        if (contentType != null && contentType.startsWith("video/")) {
            return "speakers/videos/";
        }
        return "speakers/avatars/";
    }

    @Override
    @SneakyThrows
    public FileInfo uploadFile(MultipartFile file) {
        String pathPrefix = resolvePathPrefix(file.getContentType());
        String fileName = pathPrefix + DateUtil.today() + "/" + IdUtil.simpleUUID() + "." + FileUtil.getSuffix(file.getOriginalFilename());
        long fileSize = file.getSize();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(fileSize);

        if (fileSize > MULTIPART_THRESHOLD) {
            // 大文件：使用分片上传，支持断点续传，速度更快
            log.info("Large file detected ({}MB), using multipart upload: {}", fileSize / 1024 / 1024, fileName);
            uploadLargeFile(file, fileName, metadata);
        } else {
            // 小文件：使用简单上传
            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(bucketName, fileName, inputStream, metadata);
            }
        }

        String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(file.getOriginalFilename());
        fileInfo.setUrl(url);
        log.info("File uploaded successfully: {}, size={}MB", fileName, fileSize / 1024 / 1024);
        return fileInfo;
    }

    /**
     * 大文件分片上传：先将MultipartFile保存为临时文件，再使用OSS分片上传
     * 分片上传优势：并发上传速度快、支持断点续传、单个文件最大48.8TB
     */
    @SneakyThrows
    private void uploadLargeFile(MultipartFile file, String fileName, ObjectMetadata metadata) {
        Path tempFile = Files.createTempFile("oss-upload-", ".tmp");
        try {
            file.transferTo(tempFile.toFile());

            UploadFileRequest uploadFileRequest = new UploadFileRequest(bucketName, fileName);
            uploadFileRequest.setUploadFile(tempFile.toString());
            uploadFileRequest.setPartSize(PART_SIZE);
            uploadFileRequest.setTaskNum(CONCURRENT_UPLOAD_THREADS);
            uploadFileRequest.setObjectMetadata(metadata);
            uploadFileRequest.setCheckpointFile(tempFile.toString() + ".checkpoint");

            ossClient.uploadFile(uploadFileRequest);
        } finally {
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(Path.of(tempFile.toString() + ".checkpoint"));
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        String host = "https://" + bucketName + "." + endpoint;
        String key = filePath.substring(host.length() + 1);
        ossClient.deleteObject(bucketName, key);
        return true;
    }
}