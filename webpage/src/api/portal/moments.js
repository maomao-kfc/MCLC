/* 注册接口封装 */

import request from '@/utils/request'

// 特邀嘉宾接口
export const getMomentsList = () => {
    return request({
        url: `/api/v1/portal/getMomentsList`,
        method: 'GET',
    })
}
