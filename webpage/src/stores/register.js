import { defineStore } from 'pinia'
import { reactive } from 'vue'

const STORAGE_KEY = 'registerData'

const defaultEnrollData = () => ({
  firstName: '',
  lastName: '',
  registerType: '0',
  email: ''
})

const loadFromStorage = () => {
  try {
    const stored = sessionStorage.getItem(STORAGE_KEY)
    return stored ? JSON.parse(stored) : null
  } catch {
    return null
  }
}

const storedData = loadFromStorage()

const saveToStorage = (data) => {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data))
}

export const useRegisterStore = defineStore('register', () => {
  const state = reactive({
    enroll: storedData?.enroll || defaultEnrollData(),
    consent: storedData?.consent || {
      agreePolicy1: false,
      agreePolicy2: false,
      agreeThirdParty: false,
      agreeBadgeScan: false
    },
    contact: storedData?.contact || {
      title: '',
      firstName: '',
      lastName: '',
      email: '',
      organization: '',
      phone: '',
      street: '',
      zipCode: '',
      city: '',
      state: '',
      country: '',
      billingAddress: {
        organization: '',
        vatNumber: '',
        poNumber: '',
        street: '',
        zipCode: '',
        city: '',
        state: '',
        country: ''
      },
      hasBillingAddress: false,
      teamMembers: [{ firstName: '', lastName: '', email: '', phone: '' }]
    },
    survey: storedData?.survey || { questionnaire: {} },
    signup: storedData?.signup || { registerType: '', registerTypeName: '', registerTypePrice: 0, registerTypePriceDisplay: '', memberCount: 1, discountRate: 1, finalPrice: 0, finalPriceDisplay: '', registerLabel: '' },
    deductionVerified: storedData?.deductionVerified || false,
    deductionCode: storedData?.deductionCode || '',
    picBank: storedData?.picBank || '',
    skills: storedData?.skills || [],
    expert: storedData?.expert || [],
    paymentMethod: storedData?.paymentMethod || 2
  })

  const setEnrollData = (data) => {
    Object.assign(state.enroll, data)
    saveToStorage(state)
  }

  const setConsentData = (data) => {
    Object.assign(state.consent, data)
    saveToStorage(state)
  }

  const setContactData = (data) => {
    Object.assign(state.contact, data)
    saveToStorage(state)
  }

  const setSurveyData = (data) => {
    state.survey = data
    saveToStorage(state)
  }

  const setSignupData = (data) => {
    state.signup = data
    saveToStorage(state)
  }

  const setSkillsData = (data) => {
    state.skills = data
    saveToStorage(state)
  }

  const setExpertData = (data) => {
    state.expert = data
    saveToStorage(state)
  }

  const setPaymentMethod = (data) => {
    state.paymentMethod = data
    saveToStorage(state)
  }

  const setDeductionVerified = (data) => {
    state.deductionVerified = data
    saveToStorage(state)
  }

  const setDeductionCode = (data) => {
    state.deductionCode = data
    saveToStorage(state)
  }

  const setPicBank = (data) => {
    state.picBank = data
    saveToStorage(state)
  }

  const clearRegisterData = () => {
    state.enroll = defaultEnrollData()
    state.consent = {
      agreePolicy1: false,
      agreePolicy2: false,
      agreeThirdParty: false,
      agreeBadgeScan: false
    }
    state.contact = {
      title: '',
      firstName: '',
      lastName: '',
      email: '',
      organization: '',
      phone: '',
      street: '',
      zipCode: '',
      city: '',
      state: '',
      country: ''
    }
    state.survey = { questionnaire: {} }
    state.signup = { registerType: '', registerTypeName: '', registerTypePrice: 0, registerTypePriceDisplay: '', memberCount: 1, discountRate: 1, finalPrice: 0, finalPriceDisplay: '', registerLabel: '' }
    state.deductionVerified = false
    state.deductionCode = ''
    state.picBank = ''
    state.skills = []
    state.expert = []
    state.paymentMethod = 2
    sessionStorage.removeItem(STORAGE_KEY)
  }

  const getAllRegisterData = () => {
    return {
      ...state.enroll,
      ...state.contact,
      ...state.signup,
      paymentMethod: state.paymentMethod
    }
  }

  return {
    state,
    setEnrollData,
    setConsentData,
    setContactData,
    setSurveyData,
    setSignupData,
    setSkillsData,
    setExpertData,
    setPaymentMethod,
    setDeductionVerified,
    setDeductionCode,
    setPicBank,
    clearRegisterData,
    getAllRegisterData
  }
})
