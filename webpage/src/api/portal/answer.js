/* 注册接口封装 */

import request from '@/utils/request'

// 特邀嘉宾接口
export const getPageQuest = () => {
    return request({
        url: `/api/v1/portal/getQuestionList`,
        method: 'GET',
    })
}
