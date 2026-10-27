import request from "@/utils/request";

const PROOF_BASE_URL = "/api/v1/paymentProof";

/** 凭证分页查询参数 */
export interface ProofQueryParams {
  pageNum: number;
  pageSize: number;
  status?: string;
  name?: string;
}

/** 凭证审核表单 */
export interface ProofReviewForm {
  proofId: number;
  action: "pass" | "reject";
  rejectReason?: string;
}

const PaymentProofAPI = {
  /** 凭证审核分页列表 */
  getPage(params: ProofQueryParams) {
    return request<any, PageResult<any>>({
      url: `${PROOF_BASE_URL}/page`,
      method: "get",
      params,
    });
  },

  /** 审核凭证：通过/驳回 */
  review(data: ProofReviewForm) {
    return request({
      url: `${PROOF_BASE_URL}/review`,
      method: "post",
      data,
    });
  },

  /** 线下支付登记 */
  offlineRegister(registrationId: number, remark?: string) {
    return request({
      url: `${PROOF_BASE_URL}/offline/${registrationId}`,
      method: "post",
      params: { remark },
    });
  },
};

export default PaymentProofAPI;