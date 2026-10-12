/* 注册接口封装 */

import request from '@/utils/request'

// 特邀嘉宾接口
export const getSpeechList = () => {
    return request({
        url: `/api/v1/portal/getSpeechList`,
        method: 'GET',
    })
}
// 获取讲演集锦ID查询信息
export const getInformation = (id) => {
    return request({
        url: `/api/v1/portal/${id}/form`,
        method: 'GET',
    })
}
