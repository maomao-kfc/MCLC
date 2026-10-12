import request from "@/utils/request";
import type { EmailTemplateQueryParams, EmailTemplateItem, EmailTemplateForm } from "@/types/api";

const BASE_URL = "/api/v1/email-templates";

const EmailTemplateAPI = {
  /** 获取邮箱模板分页数据 */
  getPage(queryParams?: EmailTemplateQueryParams) {
    return request<any, PageResult<EmailTemplateItem>>({
      url: `${BASE_URL}`,
      method: "get",
      params: queryParams,
    });
  },

  /** 获取邮箱模板表单数据 */
  getFormData(emailType: string) {
    return request<any, EmailTemplateForm>({
      url: `${BASE_URL}/form`,
      method: "get",
      params: { emailType },
    });
  },

  /** 新增邮箱模板 */
  create(data: EmailTemplateForm) {
    return request({ url: `${BASE_URL}`, method: "post", data });
  },

  /** 修改邮箱模板 */
  update(id: number, data: EmailTemplateForm) {
    return request({ url: `${BASE_URL}/${id}`, method: "put", data });
  },

  /** 批量删除邮箱模板，多个以英文逗号(,)分割 */
  deleteByIds(ids: string) {
    return request({ url: `${BASE_URL}/${ids}`, method: "delete" });
  },
};

export default EmailTemplateAPI;
