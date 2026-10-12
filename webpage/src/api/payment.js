import request from '@/utils/request'

const paymentMethodMap = { 1: 'credit_card', 2: 'bank_transfer', 3: 'wechat', 4: 'alipay' }

export const createPaymentIntent = (data) => {
  return request({
    url: '/api/v1/payment/create-intent',
    method: 'POST',
    data: {
      ...data,
      paymentMethod: paymentMethodMap[data.paymentMethod] || data.paymentMethod
    }
  })
}

export const getPaymentStatus = (registrationId) => {
  return request({
    url: `/api/v1/payment/status/${registrationId}`,
    method: 'GET'
  })
}

export const getBankTransferInfo = () => {
  return request({
    url: '/api/v1/payment/bank-info',
    method: 'GET'
  })
}

export const confirmBankTransfer = (registrationId) => {
  return request({
    url: `/api/v1/payment/confirm/${registrationId}`,
    method: 'POST'
  })
}

export const refund = (registrationId) => {
  return request({
    url: `/api/v1/payment/refund/${registrationId}`,
    method: 'POST'
  })
}

export const getPaymentConfig = () => {
  return request({
    url: '/api/v1/payment/config',
    method: 'GET'
  })
}
