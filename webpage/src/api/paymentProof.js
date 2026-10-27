/* 支付凭证接口封装（用户端：提交凭证、查询状态） */

import request from '@/utils/request'

/**
 * 提交支付凭证
 * @param {File} file 凭证截图文件（jpg/png/jpeg）
 * @param {Object} form 表单数据 { registrationId, declaredPayTime, declaredAmount, declaredTradeNo, remark }
 */
export const submitPaymentProof = (file, form) => {
  const formData = new FormData()
  formData.append('file', file)
  // @RequestPart("form") 需要 JSON 类型的 part
  formData.append('form', new Blob([JSON.stringify(form)], { type: 'application/json' }))
  return request({
    url: '/api/v1/paymentProof/submit',
    method: 'POST',
    data: formData,
    timeout: 120000 // 含OCR识别，超时放宽到2分钟
  })
}

/**
 * 查询凭证状态
 * @param {Number} registrationId 注册用户ID
 */
export const getProofStatus = (registrationId) => {
  return request({
    url: `/api/v1/paymentProof/status/${registrationId}`,
    method: 'GET'
  })
}