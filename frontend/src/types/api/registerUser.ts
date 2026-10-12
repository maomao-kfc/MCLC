/**
 * 注册用户类型定义
 */

import type { BaseQueryParams } from "./common";

/** 注册用户查询参数 */
export interface RegisterUserQueryParams extends BaseQueryParams {
  name?: string;
  surname?: string;
  email?: string;
  organization?: string;
  mobilePhone?: string;
  status?: number;
  country?: string;
  registrationType?: string;
  registrationCategory?: string;
  teamId?: number;
}

/** 注册用户分页对象 */
export interface RegisterUserItem {
  id?: number;
  connectId?: number;
  teamId?: number;
  name?: string;
  surname?: string;
  email?: string;
  organization?: string;
  mobilePhone?: string;
  street?: string;
  postalCode?: string;
  status?: number;
  country?: string;
  billOrganization?: string;
  billVat?: string;
  billProcurement?: string;
  billStreet?: string;
  billPostalCode?: string;
  billCity?: string;
  billStatus?: number;
  billCountry?: string;
  registrationType?: string;
  registrationFee?: string;
  registrationCategory?: string;
  registrationDocument?: string;
  registrationWhichMember?: string;
  registrationCode?: string;
  deductionCode?: string;
  reviewStatus?: number;
  registrationDate?: string;
  paymentAmount?: number;
  actualPayment?: number;
  registerLabel?: string;
  picBank?: string;
  paymentMethod?: number;
  isMatchDrug?: number;
  dietaryRequirements?: string;
  specialRequirements?: string;
  isFirst?: number;
  gender?: string;
  occupation?: string;
  experience?: string;
  fieldOfInterest?: string;
  other?: string;
  createTime?: string;
  updateTime?: string;
}

/** 签到表单对象 */
export interface CheckInForm {
  checkId: number;
  checkTime: string;
  checkName: string;
  checkNumber?: string;
  checkEmail?: string;
}

/** 注册用户表单对象 */
export interface RegisterUserForm {
  id?: number;
  connectId?: number;
  teamId?: number;
  name: string;
  surname: string;
  email?: string;
  organization?: string;
  mobilePhone?: string;
  street?: string;
  postalCode?: string;
  status?: number;
  country?: string;
  billOrganization?: string;
  billVat?: string;
  billProcurement?: string;
  billStreet?: string;
  billPostalCode?: string;
  billCity?: string;
  billStatus?: number;
  billCountry?: string;
  registrationType?: string;
  registrationFee?: string;
  registrationCategory?: string;
  registrationDocument?: string;
  registrationWhichMember?: string;
  registrationCode?: string;
  isMatchDrug?: number;
  dietaryRequirements?: string;
  specialRequirements?: string;
  isFirst?: number;
  gender?: string;
  occupation?: string;
  experience?: string;
  fieldOfInterest?: string;
  other?: string;
}
