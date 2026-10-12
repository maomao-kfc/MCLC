import request from '@/utils/request'

export const addContribution = (data) => {
  return request({
    url: '/api/v1/paperSubmission',
    method: 'POST',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

export const getContributionList = () => {
  return request({
    url: '/api/v1/contribution',
    method: 'GET'
  })
}

export const expertReview = (id, reviewResult, rejectReason) => {
  return request({
    url: '/api/v1/paperSubmission/expertReview',
    method: 'POST',
    params: { id, reviewResult, rejectReason }
  })
}

export const checkDeductionCode = (discountCode) => {
  return request({
    url: '/api/v1/paperSubmission',
    method: 'GET',
    params: { discountCode, pageNum: 1, pageSize: 1 }
  })
}
