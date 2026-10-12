/**
 * 签到记录类型定义
 */

import type { BaseQueryParams } from "./common";

/** 签到记录查询参数 */
export interface UserCheckInQueryParams extends BaseQueryParams {
  /** 签到人姓名 */
  checkName?: string;
  /** 签到人邮箱 */
  checkEmail?: string;
  /** 签到人编号 */
  checkNumber?: string;
  /** 签到状态 */
  checkStatus?: string;
}

/** 签到记录分页对象 */
export interface UserCheckInItem {
  /** 签到记录ID */
  id?: number;
  /** 关联签到ID */
  checkId?: number;
  /** 签到时间 */
  checkTime?: string;
  /** 签到人姓名 */
  checkName?: string;
  /** 签到人编号 */
  checkNumber?: string;
  /** 签到人邮箱 */
  checkEmail?: string;
  /** 签到状态 */
  checkStatus?: string;
}
