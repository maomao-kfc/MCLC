/* 投稿接口封装 */

import request from '@/utils/request';

// 上传
export const uploadFiles = (data) => {
    return request({
        url: `/api/v1/files`,
        method: 'post',
        headers: { "Content-Type": "multipart/form-data" },
        data
    })
}
