// 会议日程 - TypeScript 类型定义（双语字段模型）

/** 会议信息实体 */
export interface MeetingInfo {
  id?: number;
  meetName: string;
  meetTime: string;
  meetAddress: string;
  status: number;
  meetSort: number;
  sessions?: SessionForm[];
}

/** 板块表单（管理端创建/更新） */
export interface MeetingSessionForm {
  id?: number;
  meetingId?: number;
  title: string;
  titleEn?: string;
  chairperson?: string;
  chairpersonEn?: string;
  panelists?: string;
  panelistsEn?: string;
  timePeriod?: string;
  timePeriodEn?: string;
  topicContent?: string;
  topicContentEn?: string;
  remark?: string;
  remarkEn?: string;
  itemType?: string;
  sortOrder?: number;
  items?: AgendaItemForm[];
  participants?: ParticipantForm[];
}

/** 日程条目表单 */
export interface AgendaItemForm {
  id?: number;
  sessionId?: number;
  title: string;
  titleEn?: string;
  type?: string;
  typeEn?: string;
  itemType?: string;
  timePeriod?: string;
  timePeriodEn?: string;
  timePeriodEn?: string;
  location?: string;
  locationEn?: string;
  speakerName?: string;
  speakerNameEn?: string;
  speakerTitle?: string;
  speakerTitleEn?: string;
  speakerOrg?: string;
  speakerOrgEn?: string;
  description?: string;
  descriptionEn?: string;
  sortOrder?: number;
  speakerId?: number;
  /** UI-only: 是否独立编辑时间（不参与合并） */
  _timeIndependent?: boolean;
}

/** 参与人表单 */
export interface ParticipantForm {
  id?: number;
  sessionId?: number;
  name: string;
  nameEn?: string;
  title?: string;
  titleEn?: string;
  organization?: string;
  organizationEn?: string;
  sortOrder?: number;
}

/** 会议查询参数 */
export interface MeetingQueryParams {
  meetName?: string;
  status?: number;
  pageNum: number;
  pageSize: number;
}

/** 整体日程VO（前台展示） */
export interface MeetingScheduleVO {
  meetingId: number;
  meetingName: string;
  meetingNameEn: string;
  status: number;
  date: string;
  week: string;
  weekEn: string;
  hall: string;
  hallEn: string;
  /** 原始会议日期（ISO格式，用于编辑） */
  meetingDate: string;
  /** 排序值 */
  sortOrder: number;
  sessions: SessionVO[];
}

/** 板块VO */
export interface SessionVO {
  id: number;
  title: string;
  titleEn: string;
  chair: string;
  chairEn: string;
  panelists: string;
  panelistsEn: string;
  remark: string;
  remarkEn: string;
  itemType: string;
  sortOrder: number;
  items: AgendaItemVO[];
  participants: ParticipantVO[];
}

/** 条目VO */
export interface AgendaItemVO {
  id: number;
  time: string;
  type: string;
  typeEn: string;
  topic: string;
  topicEn: string;
  speaker: string;
  speakerEn: string;
  affiliation: string;
  affiliationEn: string;
  itemType: string;
  isSpecial: boolean;
  sortOrder: number;
  speakerId: number;
}

/** 参与人VO */
export interface ParticipantVO {
  id: number;
  sessionId: number;
  speakerId: number;
  name: string;
  nameEn: string;
  role: string;
  sortOrder: number;
  speakerTitle: string;
  speakerTitleEn: string;
  affiliation: string;
  affiliationEn: string;
}

/** 条目类型选项 */
export const ITEM_TYPE_OPTIONS = [
  { value: "OPENING", label: "开幕式", labelEn: "Opening Ceremony" },
  { value: "KEYNOTE", label: "主旨报告", labelEn: "Keynote Speech" },
  { value: "INVITED_SPEECH", label: "专题发言", labelEn: "Invited Speaker" },
  { value: "GENERAL_SPEECH", label: "大会发言", labelEn: "Free Paper" },
  { value: "DISCUSSION", label: "真实临床案例专家研讨", labelEn: "Panel Discussion" },
  { value: "BREAK", label: "茶歇/壁报", labelEn: "Tea/Poster Break" },
  { value: "LUNCH", label: "中餐-专题会", labelEn: "Lunchtime Symposium" },
  { value: "OTHER", label: "其他", labelEn: "Other" },
] as const;

/** 板块类型选项（不含茶歇/中餐/大会发言） */
export const SESSION_TYPE_OPTIONS = [
  { value: "OPENING", label: "开幕式", labelEn: "Opening Ceremony" },
  { value: "CLOSING", label: "闭幕式", labelEn: "Closing Ceremony" },
  ...ITEM_TYPE_OPTIONS.filter(
    (opt) => opt.value !== "OPENING" && opt.value !== "CLOSING" && opt.value !== "BREAK" && opt.value !== "LUNCH" && opt.value !== "GENERAL_SPEECH"
  ),
];

/** 会议编辑表单 */
export interface MeetingEditForm {
  meetingId: number;
  name: string;
  nameEn?: string;
  meetingDate?: string;
  venue?: string;
  venueEn?: string;
  sortOrder?: number;
  status?: number;
}