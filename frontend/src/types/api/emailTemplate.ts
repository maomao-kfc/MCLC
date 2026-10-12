/**
 * 邮箱模板类型定义
 */

import type { BaseQueryParams } from "./common";

/** 邮箱模板分页查询参数 */
export interface EmailTemplateQueryParams extends BaseQueryParams {
  /** 关键字（邮箱标题） */
  keywords?: string;
  /** 状态（1-启用，0-禁用） */
  status?: number;
}

/** 邮箱模板分页对象 */
export interface EmailTemplateItem {
  /** 模板ID */
  id?: number;
  /** 邮箱标题 */
  emailTitle?: string;
  /** 邮箱类型 */
  emailType?: string;
  /** 邮箱内容（富文本JSON，分页列表返回摘要） */
  emailContent?: string;
  /** 排序 */
  sort?: number;
  /** 状态（1-启用，0-禁用） */
  status?: number;
  /** 备注 */
  remark?: string;
  /** 创建时间 */
  createTime?: string;
  /** 更新时间 */
  updateTime?: string;
}

/** 邮箱模板表单对象 */
export interface EmailTemplateForm {
  /** 模板ID */
  id?: number;
  /** 邮箱标题 */
  emailTitle?: string;
  /** 邮箱类型 */
  emailType?: string;
  /** 邮箱内容（富文本JSON） */
  emailContent?: string;
  /** 排序 */
  sort?: number;
  /** 状态（1-启用，0-禁用） */
  status?: number;
  /** 备注 */
  remark?: string;
}
