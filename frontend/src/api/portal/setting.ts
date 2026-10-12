import request from "@/utils/request";
import type { GuestQueryParams, GuestForm } from "@/types/api";

const BASE_URL = "/api/v1/meetingSetting";

const SettingAPI = {
  /** 获取分页数据 */
  getPage(queryParams?: GuestQueryParams) {
    return request<any, any>({
      url: `${BASE_URL}`,
      method: "get",
      params: queryParams,
    });
  },
  /** 获取表单数据 */
  getFormData(id: string) {
    return request<any, GuestForm>({ url: `${BASE_URL}/${id}/form`, method: "get" });
  },
  /** 新增 */
  create(data: GuestForm) {
    return request({ url: `${BASE_URL}`, method: "post", data });
  },
  /** 更新 */
  update(id: string, data: GuestForm) {
    return request({ url: `${BASE_URL}/${id}`, method: "put", data });
  },
  /** 批量删除 */
  deleteByIds(ids: string) {
    return request({ url: `${BASE_URL}/${ids}`, method: "delete" });
  },
};

export default SettingAPI;
