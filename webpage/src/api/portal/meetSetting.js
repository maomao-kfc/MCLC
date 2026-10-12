/**
 * 会议日程
 */

import request from '@/utils/request'

export function getMeetingSettingList() {
    return request({
        url: '/api/v1/portal/getMeetingSettingList',
        method: 'get'
    })
}
