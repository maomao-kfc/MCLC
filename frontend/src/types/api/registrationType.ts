/**
 * RegistrationType 注册类型定义
 */

import type { BaseQueryParams } from "./common";

/** 注册类型分页查询参数 */
export interface RegistrationTypeQueryParams extends BaseQueryParams {
  /** 搜索关键字 */
  fieldName?: string;
  /** 语言类型 */
  countryType?: string;
}

/** 注册类型分页对象 */
export interface RegistrationTypeItem {
  /** ID */
  id: number;
  /** 字段名称 */
  fieldName: string;
  /** 常规有效期 */
  normalValidDate?: string;
  /** 截止日期 */
  deadlineDate?: string;
  /** 常规有效期金额 */
  normalAmount: number;
  /** 截止日期金额 */
  deadlineAmount: number;
  /** 排序 */
  sort?: number;
  /** 备注 */
  remark?: string;
  /** 语言类型 */
  countryType?: string;
}

/** 注册类型表单对象 */
export interface RegistrationTypeForm {
  /** ID */
  id?: number;
  /** 字段名称 */
  fieldName?: string;
  /** 常规有效期 */
  normalValidDate?: string;
  /** 截止日期 */
  deadlineDate?: string;
  /** 常规有效期金额 */
  normalAmount?: number;
  /** 截止日期金额 */
  deadlineAmount?: number;
  /** 排序 */
  sort?: number;
  /** 备注 */
  remark?: string;
  /** 语言类型 */
  countryType?: string;
}