package com.youlai.boot.portal.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.idev.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.mapper.ReconciliationBatchMapper;
import com.youlai.boot.portal.mapper.ReconciliationRecordMapper;
import com.youlai.boot.portal.mapper.RegisterUserInformationMapper;
import com.youlai.boot.portal.model.dto.ReconciliationImportDTO;
import com.youlai.boot.portal.model.entity.ReconciliationBatch;
import com.youlai.boot.portal.model.entity.ReconciliationRecord;
import com.youlai.boot.portal.model.entity.RegisterUserInformation;
import com.youlai.boot.portal.service.PaymentReconciliationService;
import com.youlai.boot.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支付对账服务实现
 * <p>核心：多维度匹配引擎（姓名+手机+金额+时间），不依赖协会改造系统</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciliationServiceImpl implements PaymentReconciliationService {

    private final ReconciliationBatchMapper batchMapper;
    private final ReconciliationRecordMapper recordMapper;
    private final RegisterUserInformationMapper registerUserInformationMapper;

    /** 自动匹配分数阈值（满分100，≥60自动匹配） */
    private static final int AUTO_MATCH_THRESHOLD = 60;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importReconciliation(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("对账文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            throw new RuntimeException("仅支持Excel文件（xlsx/xls）");
        }

        // 1. 解析 Excel
        List<ReconciliationImportDTO> rows;
        try {
            rows = EasyExcel.read(file.getInputStream())
                    .head(ReconciliationImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("对账Excel解析失败", e);
            throw new RuntimeException("Excel解析失败: " + e.getMessage());
        }
        if (rows == null || rows.isEmpty()) {
            throw new RuntimeException("Excel内容为空");
        }

        // 2. 创建批次
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        ReconciliationBatch batch = new ReconciliationBatch();
        batch.setBatchNo("RC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        batch.setFileName(fileName);
        batch.setTotalCount(rows.size());
        batch.setStatus("completed");
        batch.setOperatorId(operatorId);
        batch.setOperatorName(operatorName);
        batch.setCreateTime(LocalDateTime.now());
        batchMapper.insert(batch);

        // 3. 加载待匹配的注册用户（已提交凭证或初审通过的）
        List<RegisterUserInformation> candidates = registerUserInformationMapper.selectList(
                new LambdaQueryWrapper<RegisterUserInformation>()
                        .in(RegisterUserInformation::getProofStatus, "proof_submitted", "review_passed"));
        log.info("对账批次{}: 导入{}条, 待匹配注册用户{}人", batch.getBatchNo(), rows.size(), candidates.size());

        // 4. 逐条匹配
        int matchedCount = 0;
        int unmatchedCount = 0;
        for (ReconciliationImportDTO row : rows) {
            ReconciliationRecord record = toRecord(batch.getId(), row);
            MatchResult matchResult = findBestMatch(row, candidates);

            if (matchResult != null && matchResult.score >= AUTO_MATCH_THRESHOLD) {
                // 自动匹配成功
                record.setMatchedRegistrationId(matchResult.registrationId);
                record.setMatchScore(matchResult.score);
                record.setMatchDetail(matchResult.detail);
                record.setMatchStatus("auto_matched");
                recordMapper.insert(record);

                // 更新注册用户为已支付
                RegisterUserInformation ru = registerUserInformationMapper.selectById(matchResult.registrationId);
                if (ru != null && !"paid".equals(ru.getProofStatus()) && !"offline_paid".equals(ru.getProofStatus())) {
                    ru.setProofStatus("paid");
                    ru.setPaymentStatus("paid");
                    ru.setProofReviewTime(LocalDateTime.now());
                    ru.setProofReviewerId(operatorId);
                    registerUserInformationMapper.updateById(ru);
                    log.info("对账自动匹配: batchNo={}, record姓名={}, → registrationId={}, score={}",
                            batch.getBatchNo(), row.getName(), matchResult.registrationId, matchResult.score);
                }
                matchedCount++;
            } else {
                // 未匹配，进入差异列表
                if (matchResult != null) {
                    record.setMatchScore(matchResult.score);
                    record.setMatchDetail(matchResult.detail);
                }
                record.setMatchStatus("unmatched");
                recordMapper.insert(record);
                unmatchedCount++;
            }
        }

        // 5. 更新批次统计
        batch.setMatchedCount(matchedCount);
        batch.setUnmatchedCount(unmatchedCount);
        batchMapper.updateById(batch);

        log.info("对账批次{}完成: 总计{}条, 自动匹配{}条, 未匹配{}条",
                batch.getBatchNo(), rows.size(), matchedCount, unmatchedCount);

        Map<String, Object> result = new HashMap<>();
        result.put("batchId", batch.getId());
        result.put("batchNo", batch.getBatchNo());
        result.put("totalCount", rows.size());
        result.put("matchedCount", matchedCount);
        result.put("unmatchedCount", unmatchedCount);
        return result;
    }

    @Override
    public Page<ReconciliationBatch> getBatchPage(Integer pageNum, Integer pageSize) {
        return batchMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ReconciliationBatch>().orderByDesc(ReconciliationBatch::getCreateTime));
    }

    @Override
    public Page<ReconciliationRecord> getRecordPage(Integer pageNum, Integer pageSize, Long batchId, String matchStatus) {
        LambdaQueryWrapper<ReconciliationRecord> wrapper = new LambdaQueryWrapper<>();
        if (batchId != null) {
            wrapper.eq(ReconciliationRecord::getBatchId, batchId);
        }
        if (StrUtil.isNotBlank(matchStatus)) {
            wrapper.eq(ReconciliationRecord::getMatchStatus, matchStatus);
        }
        wrapper.orderByDesc(ReconciliationRecord::getCreateTime);
        return recordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmMatch(Long recordId, Long registrationId) {
        ReconciliationRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("对账记录不存在");
        }
        if (!"unmatched".equals(record.getMatchStatus())) {
            throw new RuntimeException("该记录已处理，不可重复操作");
        }
        RegisterUserInformation ru = registerUserInformationMapper.selectById(registrationId);
        if (ru == null) {
            throw new RuntimeException("注册用户不存在");
        }

        Long operatorId = SecurityUtils.getUserId();
        record.setMatchedRegistrationId(registrationId);
        record.setMatchScore(100);
        record.setMatchDetail("人工确认匹配");
        record.setMatchStatus("manual_matched");
        record.setOperatorId(operatorId);
        recordMapper.updateById(record);

        // 标记注册用户已支付
        if (!"paid".equals(ru.getProofStatus()) && !"offline_paid".equals(ru.getProofStatus())) {
            ru.setProofStatus("paid");
            ru.setPaymentStatus("paid");
            ru.setProofReviewTime(LocalDateTime.now());
            ru.setProofReviewerId(operatorId);
            registerUserInformationMapper.updateById(ru);
        }

        log.info("人工确认对账匹配: recordId={}, → registrationId={}, operator={}", recordId, registrationId, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectMatch(Long recordId) {
        ReconciliationRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("对账记录不存在");
        }
        if (!"unmatched".equals(record.getMatchStatus())) {
            throw new RuntimeException("该记录已处理，不可重复操作");
        }
        record.setMatchStatus("rejected");
        record.setOperatorId(SecurityUtils.getUserId());
        recordMapper.updateById(record);
        log.info("驳回对账记录: recordId={}", recordId);
    }

    // ==================== 多维度匹配引擎 ====================

    /**
     * 匹配结果内部类
     */
    private static class MatchResult {
        Long registrationId;
        int score;
        String detail;
    }

    /**
     * 多维度匹配：对一条协会对账记录，在注册用户候选列表中找最佳匹配
     * <p>维度权重：姓名30 + 手机25 + 金额25 + 时间20 = 100</p>
     * <p>不依赖协会加字段，纯靠双方已有信息交叉匹配</p>
     */
    private MatchResult findBestMatch(ReconciliationImportDTO row, List<RegisterUserInformation> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        MatchResult best = null;
        for (RegisterUserInformation ru : candidates) {
            int score = 0;
            List<String> dimensions = new ArrayList<>();

            // 维度1：姓名匹配（30分）
            String ruFullName = buildFullName(ru);
            int nameScore = matchName(row.getName(), ruFullName);
            if (nameScore > 0) {
                score += nameScore;
                dimensions.add(nameScore >= 30 ? "姓名精确匹配" : "姓名部分匹配");
            }

            // 维度2：手机号匹配（25分）
            if (StrUtil.isNotBlank(row.getPhone()) && StrUtil.isNotBlank(ru.getMobilePhone())) {
                String rawPhone = normalizePhone(row.getPhone());
                String ruPhone = normalizePhone(ru.getMobilePhone());
                if (rawPhone.equals(ruPhone)) {
                    score += 25;
                    dimensions.add("手机号匹配");
                }
            }

            // 维度3：金额匹配（25分）
            if (row.getAmount() != null && StrUtil.isNotBlank(ru.getPaymentAmount())) {
                try {
                    BigDecimal ruAmount = new BigDecimal(ru.getPaymentAmount().replace(",", ".").replace("¥", "").replace("€", "").trim());
                    if (row.getAmount().compareTo(ruAmount) == 0) {
                        score += 25;
                        dimensions.add("金额匹配");
                    }
                } catch (Exception ignored) {
                }
            }

            // 维度4：时间匹配（20分）— 协会支付时间 vs 用户声明支付时间/报名时间，±1天20分，±3天10分
            LocalDateTime rawPayTime = parseDateTime(row.getPayTime());
            if (rawPayTime != null) {
                int timeScore = matchTime(rawPayTime, ru);
                if (timeScore > 0) {
                    score += timeScore;
                    dimensions.add(timeScore >= 20 ? "时间±1天匹配" : "时间±3天匹配");
                }
            }

            // 更新最佳匹配
            if (score > 0 && (best == null || score > best.score)) {
                best = new MatchResult();
                best.registrationId = ru.getId();
                best.score = score;
                best.detail = String.join("+", dimensions) + " (得分:" + score + ")";
            }
        }
        return best;
    }

    /**
     * 姓名匹配：完全匹配30分，包含匹配20分
     */
    private int matchName(String rawName, String ruFullName) {
        if (StrUtil.isBlank(rawName) || StrUtil.isBlank(ruFullName)) {
            return 0;
        }
        String rn = rawName.trim().toLowerCase();
        String ru = ruFullName.trim().toLowerCase();
        if (rn.equals(ru)) {
            return 30;
        }
        // 去空格后再比
        String rnNoSpace = rn.replace(" ", "");
        String ruNoSpace = ru.replace(" ", "");
        if (rnNoSpace.equals(ruNoSpace)) {
            return 30;
        }
        // 包含匹配（协会备注里可能带其他字）
        if (ruNoSpace.contains(rnNoSpace) || rnNoSpace.contains(ruNoSpace)) {
            return 20;
        }
        return 0;
    }

    /**
     * 时间匹配：±1天20分，±3天10分
     */
    private int matchTime(LocalDateTime rawPayTime, RegisterUserInformation ru) {
        // 优先对比用户声明的支付时间
        if (ru.getDeclaredPayTime() != null) {
            long daysDiff = Math.abs(java.time.Duration.between(rawPayTime, ru.getDeclaredPayTime()).toDays());
            if (daysDiff <= 1) {
                return 20;
            }
            if (daysDiff <= 3) {
                return 10;
            }
        }
        // 其次对比报名时间（支付应在报名后7天内）
        if (StrUtil.isNotBlank(ru.getRegistrationDate())) {
            LocalDateTime regTime = parseDateTime(ru.getRegistrationDate());
            if (regTime != null) {
                long daysDiff = Math.abs(java.time.Duration.between(rawPayTime, regTime).toDays());
                if (daysDiff <= 7) {
                    return 10;
                }
            }
        }
        return 0;
    }

    private String buildFullName(RegisterUserInformation ru) {
        return (ru.getSurname() != null ? ru.getSurname() : "") + (ru.getName() != null ? ru.getName() : "");
    }

    private String normalizePhone(String phone) {
        return phone.replaceAll("[\\s\\-\\+\\(\\)]", "");
    }

    /**
     * 解析多种格式的时间字符串
     */
    private LocalDateTime parseDateTime(String str) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        String s = str.trim();
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss",
                "yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm",
                "yyyy-MM-dd", "yyyy/MM/dd",
                "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd"
        };
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                if (pattern.length() <= 10) {
                    return LocalDate.parse(s, formatter).atStartOfDay();
                }
                return LocalDateTime.parse(s, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private ReconciliationRecord toRecord(Long batchId, ReconciliationImportDTO row) {
        ReconciliationRecord record = new ReconciliationRecord();
        record.setBatchId(batchId);
        record.setRawName(row.getName());
        record.setRawPhone(row.getPhone());
        record.setRawAmount(row.getAmount());
        record.setRawPayTime(parseDateTime(row.getPayTime()));
        record.setRawTradeNo(row.getTradeNo());
        record.setRawRemark(row.getRemark());
        record.setCreateTime(LocalDateTime.now());
        return record;
    }
}