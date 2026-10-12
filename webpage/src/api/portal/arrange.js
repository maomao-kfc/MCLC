/* 注册接口封装 */

import request from '@/utils/request'

// 特邀嘉宾接口
export const getArrangeList = () => {
    return request({
        url: `/api/v1/portal/getArrangeList`,
        method: 'GET',
    })
}
