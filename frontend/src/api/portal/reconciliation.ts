import request from "@/utils/request";

const RECON_BASE_URL = "/api/v1/reconciliation";

/** 对账批次查询参数 */
export interface BatchQueryParams {
  pageNum: number;
  pageSize: number;
}

/** 对账明细查询参数 */
export interface RecordQueryParams {
  pageNum: number;
  pageSize: number;
  batchId?: number;
  matchStatus?: string;
}

const ReconciliationAPI = {
  /** 导入对账Excel并执行自动匹配 */
  importExcel(file: File) {
    const formData = new FormData();
    formData.append("file", file);
    return request<any, any>({
      url: `${RECON_BASE_URL}/import`,
      method: "post",
      data: formData,
      headers: { "Content-Type": "multipart/form-data" },
      timeout: 120000,
    });
  },

  /** 对账批次分页列表 */
  getBatchPage(params: BatchQueryParams) {
    return request<any, PageResult<any>>({
      url: `${RECON_BASE_URL}/batch/page`,
      method: "get",
      params,
    });
  },

  /** 对账明细分页列表 */
  getRecordPage(params: RecordQueryParams) {
    return request<any, PageResult<any>>({
      url: `${RECON_BASE_URL}/record/page`,
      method: "get",
      params,
    });
  },

  /** 人工确认匹配 */
  confirmMatch(recordId: number, registrationId: number) {
    return request({
      url: `${RECON_BASE_URL}/record/${recordId}/confirm`,
      method: "post",
      params: { registrationId },
    });
  },

  /** 驳回对账记录 */
  rejectMatch(recordId: number) {
    return request({
      url: `${RECON_BASE_URL}/record/${recordId}/reject`,
      method: "post",
    });
  },
};

export default ReconciliationAPI;