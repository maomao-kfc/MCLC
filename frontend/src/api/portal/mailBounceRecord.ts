import request from "@/utils/request";

const MAIL_BOUNCE_RECORD_API = "/api/v1/mailBounceRecord";

/** 退信记录分页查询参数 */
export interface MailBounceRecordQuery {
  pageNum: number;
  pageSize: number;
  bouncedRecipient?: string;
  bounceType?: string;
  bouncePermanent?: boolean;
  matchStrategy?: string;
  processed?: boolean;
  matchedRecordId?: number;
  startTime?: string;
  endTime?: string;
}

/** 退信记录对象 */
export interface MailBounceRecord {
  id: number;
  bounceMessageId: string;
  bounceSender: string;
  bounceRecipient: string;
  bounceSubject: string;
  originalMessageId: string;
  originalSender: string;
  originalRecipient: string;
  originalSubject: string;
  matchedRecordId: number | null;
  matchStrategy: string;
  matchConfidence: number | null;
  recipientSource: string;
  bounceType: string;
  bouncePermanent: boolean;
  bounceCode: string;
  bounceReason: string;
  dsnStatus: string;
  dsnDiagnosticCode: string;
  dsnAction: string;
  finalRecipient: string;
  originalRecipientDsn: string;
  inReplyTo: string;
  referencesHeader: string;
  bouncedRecipient: string;
  bounceDate: string;
  processed: boolean;
  processTime: string | null;
  processResult: string | null;
  createTime: string;
  updateTime: string;
}

/** 分页查询退信记录 */
export function getMailBounceRecordPage(query: MailBounceRecordQuery) {
  return request({
    url: `${MAIL_BOUNCE_RECORD_API}/page`,
    method: "get",
    params: query,
  });
}

/** 查看退信记录详情 */
export function getMailBounceRecordDetail(id: number) {
  return request({
    url: `${MAIL_BOUNCE_RECORD_API}/${id}`,
    method: "get",
  });
}

/** 根据发送记录ID查询关联退信记录 */
export function listBounceBySendRecordId(matchedRecordId: number) {
  return request({
    url: `${MAIL_BOUNCE_RECORD_API}/bySendRecord/${matchedRecordId}`,
    method: "get",
  });
}

/** 查询未处理的退信记录 */
export function listUnprocessedBounce(limit: number = 20) {
  return request({
    url: `${MAIL_BOUNCE_RECORD_API}/unprocessed`,
    method: "get",
    params: { limit },
  });
}