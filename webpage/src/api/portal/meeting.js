/**
 * 会议日程
 */

import request from '@/utils/request'

export function getAllSchedules() {
  return request({
    url: '/api/v1/meetingSchedule/allMeetingSchedule',
    method: 'get'
  })
}