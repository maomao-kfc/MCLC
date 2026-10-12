import request from "@/utils/request";

const ARRANGE_BASE_URL = "/api/v1/arrange";

const ArrangeAPI = {
  /** 获取会前安排分页数据 */
  getPage(queryParams?: any) {
    return request<any, any>({
      url: `${ARRANGE_BASE_URL}`,
      method: "get",
      params: queryParams,
    });
  },
  /** 新增会前安排 */
  create(data: any) {
    return request({ url: `${ARRANGE_BASE_URL}`, method: "post", data });
  },
  /** 更新会前安排 */
  update(id: number | string, data: any) {
    return request({ url: `${ARRANGE_BASE_URL}/${id}`, method: "put", data });
  },
  /** 批量删除会前安排，多个以英文逗号(,)分割 */
  delete(ids: string) {
    return request({ url: `${ARRANGE_BASE_URL}/${ids}`, method: "delete" });
  },
};

export default ArrangeAPI;
