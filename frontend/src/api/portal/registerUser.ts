import request from "@/utils/request";
import type { RegisterUserQueryParams, RegisterUserItem, RegisterUserForm, CheckInForm } from "@/types/api";

const REGISTER_USER_BASE_URL = "/api/v1/registerUser";

const RegisterUserAPI = {
  /** 获取注册用户分页列表 */
  getPage(queryParams?: RegisterUserQueryParams) {
    return request<any, PageResult<RegisterUserItem>>({
      url: `${REGISTER_USER_BASE_URL}`,
      method: "get",
      params: queryParams,
    });
  },

  /** 获取注册用户详情 */
  getDetail(id: number) {
    return request<any, RegisterUserItem>({
      url: `${REGISTER_USER_BASE_URL}/${id}`,
      method: "get",
    });
  },

  /** 新增注册用户 */
  create(data: RegisterUserForm) {
    return request({
      url: `${REGISTER_USER_BASE_URL}`,
      method: "post",
      data,
    });
  },

  /** 修改注册用户 */
  update(id: number, data: RegisterUserForm) {
    return request({
      url: `${REGISTER_USER_BASE_URL}/${id}`,
      method: "put",
      data,
    });
  },

  /** 审核注册用户 */
  audit(id: number, data: { reviewStatus: number; rejectReason?: string; deductionCode?: string }) {
    return request({
      url: `${REGISTER_USER_BASE_URL}/${id}/audit`,
      method: "post",
      data,
    });
  },

  /** 签到 */
  checkIn(data: CheckInForm) {
    return request({
      url: `${REGISTER_USER_BASE_URL}/checkIn`,
      method: "post",
      data,
    });
  },

  /** 批量删除注册用户，多个以英文逗号(,)分割 */
  deleteByIds(ids: string) {
    return request({
      url: `${REGISTER_USER_BASE_URL}/${ids}`,
      method: "delete",
    });
  },
};

export default RegisterUserAPI;
