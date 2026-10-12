import request from '@/utils/request'

export const getEmailTemplatePage = (queryParams) => {
  return request({
    url: '/api/v1/email-templates',
    method: 'GET',
    params: queryParams
  })
}

export const getEmailTemplateForm = (id) => {
  return request({
    url: `/api/v1/email-templates/${id}/form`,
    method: 'GET'
  })
}

export const saveEmailTemplate = (formData) => {
  return request({
    url: '/api/v1/email-templates',
    method: 'POST',
    data: formData
  })
}

export const updateEmailTemplate = (id, formData) => {
  return request({
    url: `/api/v1/email-templates/${id}`,
    method: 'PUT',
    data: formData
  })
}

export const deleteEmailTemplates = (ids) => {
  return request({
    url: `/api/v1/email-templates/${ids}`,
    method: 'DELETE'
  })
}
