package com.youlai.boot.portal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.form.PaymentProofSubmitForm;
import com.youlai.boot.portal.model.form.ProofReviewForm;
import com.youlai.boot.portal.model.vo.PaymentProofPageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 支付凭证服务接口
 */
public interface PaymentProofService {

    /**
     * 用户提交支付凭证（含水印、哈希去重、OCR初筛）
     *
     * @param file 凭证截图文件
     * @param form 提交表单（支付时间、金额等）
     * @return 凭证信息（含状态、去重/OCR结果提示）
     */
    Map<String, Object> submitProof(MultipartFile file, PaymentProofSubmitForm form);

    /**
     * 查询注册用户的凭证状态（用户端）
     */
    Map<String, Object> getProofStatus(Long registrationId);

    /**
     * 管理端凭证审核分页列表
     *
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @param status    凭证状态筛选（可空）
     * @param name      报名人姓名模糊搜索（可空）
     */
    Page<PaymentProofPageVO> getProofPage(Integer pageNum, Integer pageSize, String status, String name);

    /**
     * 管理端审核凭证（通过/驳回）
     */
    void reviewProof(ProofReviewForm form);

    /**
     * 管理端线下支付登记（财务直接标记已支付）
     */
    void offlineRegister(Long registrationId, String remark);
}