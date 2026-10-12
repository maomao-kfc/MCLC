import request from '@/utils/request'

export const getArchitecturePage = (params) => {
    return request({
        url: '/api/v1/architecture',
        method: 'GET',
        params
    })
}
