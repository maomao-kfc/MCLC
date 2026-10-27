package com.youlai.boot.portal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.ReconciliationBatch;
import com.youlai.boot.portal.model.entity.ReconciliationRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 支付对账服务接口
 */
public interface PaymentReconciliationService {

    /**
     * 导入协会对账 Excel 并执行自动匹配
     *
     * @param file 对账 Excel 文件
     * @return 批次摘要（总数、自动匹配数、未匹配数）
     */
    Map<String, Object> importReconciliation(MultipartFile file);

    /**
     * 对账批次分页列表
     */
    Page<ReconciliationBatch> getBatchPage(Integer pageNum, Integer pageSize);

    /**
     * 对账明细分页列表
     *
     * @param batchId     批次ID（可空=查全部）
     * @param matchStatus 匹配状态筛选（可空）
     */
    Page<ReconciliationRecord> getRecordPage(Integer pageNum, Integer pageSize, Long batchId, String matchStatus);

    /**
     * 人工确认匹配（将未匹配的对账记录关联到指定注册用户，并标记已支付）
     */
    void confirmMatch(Long recordId, Long registrationId);

    /**
     * 驳回对账记录（确认该笔协会收款不对应任何报名）
     */
    void rejectMatch(Long recordId);
}