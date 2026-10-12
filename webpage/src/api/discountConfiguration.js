import request from '@/utils/request'

export const getDiscountConfigurationList = () => {
  return request({
    url: '/api/v1/discountConfiguration',
    method: 'GET'
  })
}
