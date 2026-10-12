import request from "@/utils/request";

const BASE_URL = "/api/v1/architecture";

const ArchitectureAPI = {
  /** 获取组织架构分页数据 */
  getPage(queryParams?: any) {
    return request<any, PageResult<any>>({
      url: `${BASE_URL}`,
      method: "get",
      params: queryParams,
    });
  },
  /** 新增组织架构 */
  create(data: any) {
    return request({ url: `${BASE_URL}`, method: "post", data });
  },
  /** 更新组织架构 */
  update(id: string, data: any) {
    return request({ url: `${BASE_URL}/${id}`, method: "put", data });
  },
  /** 批量删除组织架构，多个以英文逗号(,)分割 */
  deleteByIds(ids: string) {
    return request({ url: `${BASE_URL}/${ids}`, method: "delete" });
  },
};

export default ArchitectureAPI;
