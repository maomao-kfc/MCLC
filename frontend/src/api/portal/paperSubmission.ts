import request from "@/utils/request";
import type {
  PaperSubmissionPageVO,
  PaperSubmissionForm,
  PaperSubmissionQuery,
  PaperSubmissionReviewForm,
  PaperSubmissionEmailVO,
} from "@/types/api";
import type { PageResult } from "@/types/api";

const BASE_URL = "/api/v1/paperSubmission";

const PaperSubmissionAPI = {
  getPage(queryParams: PaperSubmissionQuery) {
    return request<any, PageResult<PaperSubmissionPageVO>>({
      url: BASE_URL,
      method: "get",
      params: queryParams,
    });
  },

  create(data: PaperSubmissionForm) {
    return request({
      url: BASE_URL,
      method: "post",
      data,
    });
  },

  update(id: number, data: PaperSubmissionForm) {
    return request({
      url: `${BASE_URL}/${id}`,
      method: "put",
      data,
    });
  },

  deleteByIds(ids: string) {
    return request({
      url: `${BASE_URL}/${ids}`,
      method: "delete",
    });
  },

  review(id: number, data: PaperSubmissionReviewForm) {
    return request({
      url: `${BASE_URL}/${id}/review`,
      method: "put",
      data,
    });
  },

  getExpertReviewList(id: number) {
    return request<any, PaperSubmissionEmailVO[]>({
      url: `${BASE_URL}/expertReview/list`,
      method: "get",
      params: { id },
    });
  },

  /** 重发审稿邮件 */
  resendEmail(emailId: number) {
    return request<any, { expertName: string; expertEmail: string; success: boolean }>({
      url: `${BASE_URL}/resendEmail/${emailId}`,
      method: "post",
    });
  },

  /**
   * 导出论文投稿为 PDF
   *
   * 后端接口：GET /api/v1/paperSubmission/{id}/exportPdf
   * 三种情况统一由后端处理，返回 PDF 文件流：
   * - PDF 附件：后端从 URL 读取并流式返回
   * - DOCX/DOC 附件：后端 JODConverter + LibreOffice 转换后流式返回
   * - 无附件：后端 OpenPDF 根据数据库信息生成 PDF 流式返回
   *
   * @param id 论文投稿ID
   * @returns Promise<Blob> PDF 文件 Blob
   */
  exportPdf(id: number) {
    return request<any, Blob>({
      url: `${BASE_URL}/${id}/exportPdf`,
      method: "get",
      responseType: "blob",
    });
  },
};

export default PaperSubmissionAPI;
