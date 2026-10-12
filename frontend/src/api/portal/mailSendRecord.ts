import request from "@/utils/request";

const MAIL_RECORD_API = "/api/v1/mailRecord";

/** 邮件发送记录分页查询参数 */
export interface MailSendRecordQuery {
  pageNum: number;
  pageSize: number;
  businessType?: string;
  businessId?: number;
  receiver?: string;
  sendStatus?: number;
  deliveryStatus?: number;
  subject?: string;
  startTime?: string;
  endTime?: string;
}

/** 邮件发送记录分页对象 */
export interface MailSendRecordPageVO {
  id: number;
  businessType: string;
  businessTypeDesc: string;
  businessId: number;
  receiver: string;
  receiverName: string;
  subject: string;
  sendStatus: number;
  sendStatusDesc: string;
  retryCount: number;
  errorMessage: string;
  bounceReason: string;
  messageId: string;
  consumeTime: number;
  sender: string;
  sendTime: string;
  createTime: string;
  deliveryStatus: number | null;
  deliveryStatusDesc: string | null;
  bounceCode: string | null;
  bounceType: string | null;
  bouncePermanent: boolean | null;
  parentRecordId: number | null;
  recordType: string | null;
  retrySequence: number | null;
  rootRecordId: number | null;
  children?: MailSendRecordPageVO[];
}

/** 邮件发送记录详情 */
export interface MailSendRecordDetail {
  id: number;
  businessType: string;
  businessTypeDesc: string;
  businessId: number;
  receiver: string;
  receiverName: string;
  subject: string;
  content: string;
  sendStatus: number;
  sendStatusDesc: string;
  retryCount: number;
  errorMessage: string;
  bounceReason: string;
  messageId: string;
  consumeTime: number;
  sender: string;
  sendTime: string;
  createTime: string;
  updateTime: string;
  deliveryStatus: number | null;
  deliveryStatusDesc: string | null;
  bounceCode: string | null;
  bounceMessageId: string | null;
  imapCheckCount: number | null;
  lastImapCheckTime: string | null;
  bounceType: string | null;
  bouncePermanent: boolean | null;
  parentRecordId: number | null;
  recordType: string | null;
  retrySequence: number | null;
  rootRecordId: number | null;
}

/** 分页查询邮件发送记录 */
export function getMailSendRecordPage(query: MailSendRecordQuery) {
  return request({
    url: `${MAIL_RECORD_API}/page`,
    method: "get",
    params: query,
  });
}

/** 查看邮件发送记录详情 */
export function getMailSendRecordDetail(id: number) {
  return request({
    url: `${MAIL_RECORD_API}/${id}`,
    method: "get",
  });
}

/** 重试发送失败的邮件 */
export function retryMailSendRecord(id: number) {
  return request({
    url: `${MAIL_RECORD_API}/retry/${id}`,
    method: "post",
  });
}

/** 取消待发送的邮件 */
export function cancelMailSendRecord(id: number) {
  return request({
    url: `${MAIL_RECORD_API}/cancel/${id}`,
    method: "post",
  });
}

/** 按业务查询邮件记录 */
export function listMailRecordByBusiness(businessType: string, businessId: number) {
  return request({
    url: `${MAIL_RECORD_API}/business`,
    method: "get",
    params: { businessType, businessId },
  });
}

/** 手动标记邮件为退信-投递失败 */
export function markBounce(id: number, bounceReason: string) {
  return request({
    url: `${MAIL_RECORD_API}/markBounce/${id}`,
    method: "post",
    params: { bounceReason },
  });
}