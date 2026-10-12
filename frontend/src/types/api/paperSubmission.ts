import type { BaseQueryParams } from "./common";

export interface PaperSubmissionPageVO {
  id: number;
  name: string;
  surname: string;
  company: string;
  paperTitle: string;
  firstAuthor: string;
  contactNumber: string;
  contactEmail: string;
  paperAbstract: string;
  keywords: string;
  appendix: string;
  contributionId: number;
  paperStatus: number;
  emailStatus: number;
  rejectReason: string;
}

export interface PaperSubmissionForm {
  id?: number;
  name?: string;
  surname?: string;
  company?: string;
  paperTitle: string;
  firstAuthor: string;
  contactNumber: string;
  contactEmail: string;
  paperAbstract: string;
  keywords: string;
  appendix: string;
  contributionId?: number;
  rejectReason?: string;
}

export interface PaperSubmissionQuery extends BaseQueryParams {
  paperTitle?: string;
  firstAuthor?: string;
  keywords?: string;
  startDate?: string;
  endDate?: string;
}

export interface ExpertEmailItem {
  name: string;
  email: string;
}

export interface PaperSubmissionReviewForm {
  emailsList: ExpertEmailItem[];
  url: string;
  senderName: string;
}

export interface PaperSubmissionEmailVO {
  id: number;
  submissionId: number;
  expertName: string;
  expertEmail: string;
  url: string;
  senderName: string;
  sendStatus: number;
  sendTime: string;
  sendErrorMsg: string;
  reviewResult: number;
  rejectReason: string;
  reviewTime: string;
}
