import request from '@/utils/request'

export const getRegistrationTypeList = (countryType) => {
  return request({
    url: '/api/v1/registrationType',
    method: 'GET',
    params: { countryType }
  })
}
