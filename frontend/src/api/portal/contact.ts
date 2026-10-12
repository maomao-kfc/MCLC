import request from "@/utils/request";
import type { GuestQueryParams, GuestItem, GuestForm } from "@/types/api";

const GUEST_BASE_URL = "/api/v1/contact";

const ContactAPI = {
  /** 获取角色分页数据 */
  getPage(queryParams?: GuestQueryParams) {
    return request<any, PageResult<GuestItem>>({
      url: `${GUEST_BASE_URL}`,
      method: "get",
      params: queryParams,
    });
  },
  /** 获取角色下拉数据源 */
  getOptions() {
    return request<any, OptionItem[]>({ url: `${GUEST_BASE_URL}/options`, method: "get" });
  },
  /** 获取角色的菜单ID集合 */
  getRoleMenuIds(roleId: string) {
    return request<any, string[]>({ url: `${GUEST_BASE_URL}/${roleId}/menu-ids`, method: "get" });
  },
  /** 分配菜单权限 */
  updateRoleMenus(roleId: string, data: number[]) {
    return request({ url: `${GUEST_BASE_URL}/${roleId}/menus`, method: "put", data });
  },
  /** 获取角色表单数据 */
  getFormData(id: string) {
    return request<any, GuestForm>({ url: `${GUEST_BASE_URL}/${id}/form`, method: "get" });
  },
  /** 获取角色的部门ID集合(自定义数据权限) */
  getRoleDeptIds(roleId: string) {
    return request<any, string[]>({ url: `${GUEST_BASE_URL}/${roleId}/dept-ids`, method: "get" });
  },
  /** 新增角色 */
  create(data: GuestForm) {
    return request({ url: `${GUEST_BASE_URL}`, method: "post", data });
  },
  /** 更新角色 */
  update(id: string, data: GuestForm) {
    return request({ url: `${GUEST_BASE_URL}/${id}`, method: "put", data });
  },
  /** 批量删除角色，多个以英文逗号(,)分割 */
  deleteByIds(ids: string) {
    return request({ url: `${GUEST_BASE_URL}/${ids}`, method: "delete" });
  },
};

export default ContactAPI;
