import request from "@/utils/request";
import type { GuestQueryParams, GuestItem, GuestForm } from "@/types/api";

const GUEST_BASE_URL = "/api/v1/speech";

const SpeechAPI = {
  /** 获取分页数据 */
  getPage(queryParams?: GuestQueryParams) {
    return request<any, PageResult<GuestItem>>({
      url: `${GUEST_BASE_URL}`,
      method: "get",
      params: queryParams,
    });
  },
  /** 获取下拉数据源 */
  getOptions() {
    return request<any, OptionItem[]>({ url: `${GUEST_BASE_URL}/options`, method: "get" });
  },
  /** 获取表单数据 */
  getFormData(id: string) {
    return request<any, GuestForm>({ url: `${GUEST_BASE_URL}/${id}/form`, method: "get" });
  },
  /** 新增 */
  create(data: GuestForm) {
    return request({ url: `${GUEST_BASE_URL}`, method: "post", data });
  },
  /** 更新 */
  update(id: string, data: GuestForm) {
    return request({ url: `${GUEST_BASE_URL}/${id}`, method: "put", data });
  },
  /** 批量删除 */
  deleteByIds(ids: string) {
    return request({ url: `${GUEST_BASE_URL}/${ids}`, method: "delete" });
  },
  /** 获取详情 */
  getDetail(id: string) {
    return request({ url: `/api/v1/portal/${id}/form`, method: "get" });
  },
};

export default SpeechAPI;
export class getDeatil {}
