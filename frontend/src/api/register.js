import request from "@/utils/request";
const AUTH_BASE_URL = "/api/v1/users/registerUser";
const UsersAPI = {
  /** 新增用户 */
  registerUser(data) {
    return request({ url: `${AUTH_BASE_URL}`, method: "post", data });
  },
};
export default UsersAPI;
