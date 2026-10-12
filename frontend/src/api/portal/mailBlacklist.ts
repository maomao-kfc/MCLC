import request from "@/utils/request";

const MAIL_BLACKLIST_API = "/api/v1/mailBlacklist";

/** 查询黑名单列表 */
export function getBlacklistList(params?: object) {
  return request({
    url: `${MAIL_BLACKLIST_API}/list`,
    method: "get",
    params,
  });
}

/** 检查邮箱是否在黑名单 */
export function checkBlacklist(email: string) {
  return request({
    url: `${MAIL_BLACKLIST_API}/check`,
    method: "get",
    params: { email },
  });
}

/** 手动添加黑名单 */
export function addBlacklist(email: string, reason?: string) {
  return request({
    url: `${MAIL_BLACKLIST_API}/add`,
    method: "post",
    params: { email, reason },
  });
}

/** 移除黑名单 */
export function removeBlacklist(id: number) {
  return request({
    url: `${MAIL_BLACKLIST_API}/remove/${id}`,
    method: "post",
  });
}