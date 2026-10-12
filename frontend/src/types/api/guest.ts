/**
 * guest特邀嘉宾类型定义
 */

import type { BaseQueryParams } from "./common";

/** 角色分页查询参数 */
export interface GuestQueryParams extends BaseQueryParams {
  /** 搜索姓名 */
  hotelName?: string;
  title?: string;
  /** 国家类型 */
  countryType?: string;
  /** 主办方 */
  sponsor?: string;
  /** 承办方 */
  organizer?: string;
  /** 是否开启注册 */
  registrationEnabled?: number;
  /** 是否开启日程 */
  scheduleEnabled?: number;
}

/** 角色分页对象 */
export interface GuestItem {
  /** 特邀嘉宾ID */
  id?: string;
  /** 嘉宾姓名 */
  name?: string;
  /** 排序 */
  sort?: number;
  /** 角色状态 */
  status?: number;
  /** 特邀嘉宾图像 */
  image?: string;
  /** 嘉宾类型 */
  type?: string;
  /** 修改时间 */
  updateTime?: Date;
  /** 主办方 */
  sponsor?: string;
  /** 承办方 */
  organizer?: string;
  /** 论文投稿截至日期 */
  paperSubmissionDeadline?: string;
  /** 会议开始时间 */
  startTmie?: string;
  /** 会议结束时间 */
  endTmie?: string;
  /** 常规注册开始时间 */
  generalRegStart?: string;
  /** 常规注册结束时间 */
  generalRegEnd?: string;
  /** 早鸟优惠注册开始时间 */
  earlyBirdRegStart?: string;
  /** 早鸟优惠注册结束时间 */
  earlyBirdRegEnd?: string;
  /** 会议举办开始时间 */
  conferenceDateStart?: string;
  /** 会议举办结束时间 */
  conferenceDateEnd?: string;
  /** 是否开启日程 */
  scheduleEnabled?: number;
}

/** 特邀嘉宾表单对象 */
export interface GuestForm {
  /** 特邀嘉宾ID */
  id?: string;
  /** 嘉宾姓名 */
  name?: string;
  /** 特邀嘉宾职务 */
  position?: string;
  /** 排序 */
  sort?: number;
  /** 特邀嘉宾图像 */
  image?: string;
  /** 角色状态 */
  status?: number;
  /** 嘉宾类型 */
  type?: string;
  /** 备注 */
  remark?: string;
  /** 国家类型 */
  countryType?: string;
  /** 主办方 */
  sponsor?: string;
  /** 承办方 */
  organizer?: string;
  /** 论文投稿截至日期 */
  paperSubmissionDeadline?: string;
  /** 是否开启日程 */
  scheduleEnabled?: number;
}
