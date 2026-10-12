/**
 * 会议日程
 */

import request from "@/utils/request";

// 查询所有日程（前台展示）
export function getAllSchedules() {
  return request({
    url: "/api/v1/meetingSchedule/allMeetingSchedule",
    method: "get",
  });
}

// 会议信息分页查询（管理端）
export function getPage(params: any) {
  return request({
    url: "/api/v1/meetingSchedule/page",
    method: "get",
    params,
  });
}

// 新增日程板块（含条目、参与人级联创建）
export function createSession(data: any) {
  return request({
    url: "/api/v1/meetingSchedule/session",
    method: "post",
    data,
  });
}

// 修改日程板块（含条目、参与人级联更新）
export function updateSession(id: number, data: any) {
  return request({
    url: `/api/v1/meetingSchedule/session/${id}`,
    method: "put",
    data,
  });
}

// 删除日程板块（级联删除条目和参与人）
export function deleteSession(id: number) {
  return request({
    url: `/api/v1/meetingSchedule/session/${id}`,
    method: "delete",
  });
}

// 更新会议状态（启用/禁用）
export function updateMeetingStatus(meetingId: number, status: number) {
  return request({
    url: `/api/v1/meetingSchedule/${meetingId}/status`,
    method: "put",
    params: { status },
  });
}

// 更新会议基础信息（名称、日期、会场等）
export function updateMeeting(meetingId: number, data: Record<string, any>) {
  return request({
    url: `/api/v1/meetingSchedule/meeting/${meetingId}`,
    method: "put",
    params: data,
  });
}

// 删除会议（级联删除板块、条目、参与人）
export function deleteMeeting(meetingId: number) {
  return request({
    url: `/api/v1/meetingSchedule/${meetingId}`,
    method: "delete",
  });
}

// 批量更新板块排序值
export function batchUpdateSessionSortOrder(sortOrders: Array<{ id: number; sortOrder: number }>) {
  return request({
    url: "/api/v1/meetingSchedule/session/batchSortOrder",
    method: "put",
    data: sortOrders,
  });
}

// 默认导出
export default {
  getAllSchedules,
  getPage,
  createSession,
  updateSession,
  deleteSession,
  updateMeetingStatus,
  updateMeeting,
  deleteMeeting,
  batchUpdateSessionSortOrder,
};