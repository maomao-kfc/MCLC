/**
 * 将 registerStore.state 映射为后端 RegisterUserForm 格式
 */
export function mapStateToRegisterUserForm(state) {
  const enroll = state.enroll || {}
  const contact = state.contact || {}
  const survey = state.survey?.questionnaire || {}
  const signup = state.signup || {}
  const billing = contact.billingAddress || {}

  const form = {}

  // ---- 基础信息 (enroll/contact) ----
  form.name = contact.firstName || enroll.firstName || ''
  form.surname = contact.lastName || enroll.lastName || ''
  form.email = contact.email || enroll.email || ''
  form.organization = contact.organization || ''
  form.mobilePhone = contact.phone || ''
  form.street = contact.street || ''
  form.postalCode = contact.zipCode || ''
  form.country = contact.country || ''

  // 注册类型: '0'=个人, '1'=团队
  form.registrationType = enroll.registerType || '0'

  // ---- 账单地址 ----
  if (contact.hasBillingAddress) {
    form.billOrganization = billing.organization || ''
    form.billVat = billing.vatNumber || ''
    form.billProcurement = billing.poNumber || ''
    form.billStreet = billing.street || ''
    form.billPostalCode = billing.zipCode || ''
    form.billCity = billing.city || ''
    form.billCountry = billing.country || ''
  }

  // ---- 问卷调查 ----
  form.registrationCategory = survey.registerType || ''
  form.registrationWhichMember = survey.hospitalVal || ''
  form.registrationCode = survey.specialCode || ''
  form.isMatchDrug = mapYesNo(survey.prescription)
  form.dietaryRequirements = buildDietary(survey)
  form.specialRequirements = (survey.selectedConferences || []).join(', ')
  form.isFirst = mapYesNo(survey.firstJoin)
  form.gender = mapGender(survey.gender)
  form.occupation = mapOccupation(survey.job)
  form.experience = survey.experience || ''
  form.fieldOfInterest = (survey.interestAreas || []).join(', ')
  form.other = survey.otherRemark || ''

  // ---- 注册费 ----
  form.registrationFee = signup.registerType || ''

  // ---- 签证邀请函 ----
  form.visaIsNeed = signup.visaIsNeed ? 1 : 0
  if (signup.visaIsNeed && signup.visaInfo) {
    const visa = signup.visaInfo
    form.visaName = visa.visaName || ''
    form.visaSurname = visa.visaSurname || ''
    form.visaPassport = visa.visaPassport || ''
    form.visaIssueTime = visa.visaIssueTime || ''
    form.visaEndTime = visa.visaEndTime || ''
    form.visaTime = visa.visaTime || ''
    form.visaArea = visa.visaArea || ''
  }

  // ---- 抵扣码 ----
  if (state.deductionVerified && state.deductionCode) {
    form.deductionCode = state.deductionCode
  }

  // ---- 注册标签 ----
  form.registerLabel = signup.registerLabel || signup.registerTypeName || ''

  // ---- 团队成员 ----
  const rawMembers = contact.teamMembers || []
  form.teamMembers = rawMembers
    .filter(m => (m.firstName || '').trim() || (m.lastName || '').trim() || (m.email || '').trim())
    .map(m => ({
      name: m.firstName || '',
      surname: m.lastName || '',
      email: m.email || '',
      mobilePhone: m.phone || ''
    }))

  return form
}

function mapYesNo(val) {
  if (val === 'yes') return 1
  if (val === 'no') return 0
  return null
}

function mapGender(val) {
  if (val === 'male') return '男'
  if (val === 'female') return '女'
  if (val === 'unknown') return '不愿披露'
  return val || ''
}

function mapOccupation(val) {
  const map = {
    eyeDoctor: '眼科医生',
    optometrist: '验光师',
    researcher: '研究员',
    student: '医学生',
    nurse: '护士'
  }
  return map[val] || val || ''
}

function buildDietary(survey) {
  if (survey.diet !== 'yes') return '无'
  const parts = []
  if (survey.dietRequirement) parts.push(survey.dietRequirement)
  return parts.join(', ') || '是'
}

