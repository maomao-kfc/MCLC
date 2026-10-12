import request from "@/utils/request";
import type {
  RegistrationTypeQueryParams,
  RegistrationTypeItem,
  RegistrationTypeForm,
} from "@/types/api";

const BASE_URL = "/api/v1/registrationType";

const RegistrationTypeAPI = {
  /** 获取注册类型分页数据 */
  getPage(queryParams?: RegistrationTypeQueryParams) {
    return request<any, PageResult<RegistrationTypeItem>>({
      url: `${BASE_URL}`,
      method: "get",
      params: queryParams,
    });
  },
  /** 获取注册类型表单数据 */
  getFormData(id: number) {
    return request<any, RegistrationTypeForm>({
      url: `${BASE_URL}/${id}/form`,
      method: "get",
    });
  },
  /** 添加注册类型 */
  create(data: RegistrationTypeForm) {
    return request({ url: `${BASE_URL}`, method: "post", data });
  },
  /** 更新注册类型 */
  update(id: number, data: RegistrationTypeForm) {
    return request({ url: `${BASE_URL}/${id}`, method: "put", data });
  },
  /** 批量删除注册类型，多个以英文逗号(,)分割 */
  deleteByIds(ids: string) {
    return request({ url: `${BASE_URL}/${ids}`, method: "delete" });
  },
};

export default RegistrationTypeAPI;
