import request from "@/utils/request";
import type { UserCheckInQueryParams, UserCheckInItem } from "@/types/api";

const USER_CHECK_IN_BASE_URL = "/api/v1/userCheckIn";

const UserCheckInAPI = {
  /** 获取签到记录分页列表 */
  getPage(queryParams?: UserCheckInQueryParams) {
    return request<any, PageResult<UserCheckInItem>>({
      url: `${USER_CHECK_IN_BASE_URL}`,
      method: "get",
      params: queryParams,
    });
  },

  /** 批量删除签到记录，多个以英文逗号(,)拼接 */
  deleteByIds(ids: string) {
    return request({
      url: `${USER_CHECK_IN_BASE_URL}/${ids}`,
      method: "delete",
    });
  },
};

export default UserCheckInAPI;
