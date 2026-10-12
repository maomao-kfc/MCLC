<template>
  <div class="register-page">
    <div class="section">
      <div class="section-header">{{ t('register.surveyTitle') }}</div>
      <p class="desc" v-html="t('register.surveySelectCategoryDesc')"></p>
      <p class="important">{{ t('register.surveyImportant') }}</p>

      <div class="form-item">
        <label class="label required">{{ t('register.surveySelectCategory') }}</label>
        <el-radio-group @change="typeChangeEve" v-model="form.registerType" class="radio-grid">
          <el-radio label="member">{{ t('register.surveyRegisterTypeOphthalmologist') }}</el-radio>
          <el-radio label="nonMember">{{ t('register.surveyRegisterTypeNonMember') }}</el-radio>
          <el-radio label="scientist">{{ t('register.surveyRegisterTypeScientist') }}</el-radio>
          <el-radio label="resident">{{ t('register.surveyRegisterTypeResident') }}</el-radio>
        </el-radio-group>

      </div>
    </div>
<!--  社团  -->
    <div class="section" v-if="form.registerType === 'member'">
      <div class="form-item">
        <label class="label required">{{ t('register.surveySociety') }}</label>
        <el-select :placeholder="t('register.surveySelectSociety')" v-model="form.hospitalVal" >
           <el-option v-for="item in hospital" :label="item?.labelZh" :value="item?.value" />
        </el-select>
      </div>
    </div>
<!--    -->
    <div class="section" v-if="form.registerType === 'scientist' || form.registerType === 'resident'  ">
      <div class="form-item">
        <label class="label required">{{ t('register.surveyUploadProof') }}</label>
        <el-upload
            ref="uploadRef"
            v-model:file-list="form.fileList"
            class="upload-demo"
            :limit="1"
            v-loading="isUploading"
            :auto-upload="false"
            :on-exceed="onExceed"
            :on-change="handleChange"
        >
          <el-button  type="primary">{{ t('register.surveyUploadBtn') }}</el-button>
        </el-upload>
      </div>
    </div>
    <!-- 3. 特殊代码模块 -->
    <div class="section">
      <div class="section-header">{{ t('register.surveySpecialCode') }}</div>
      <div class="form-item">
        <label class="label">{{ t('register.surveyRegCode') }}</label>
        <p class="tip">{{ t('register.surveyRegCodeDesc') }}</p>
        <el-input v-model="form.specialCode" :placeholder="t('register.surveyEnterCode')" />
      </div>
    </div>

    <!-- 4. 处方医生模块 -->
    <div class="section">
      <div class="section-header">{{ t('register.surveyPrescriber') }}</div>
      <div class="form-item">
        <label class="label required">{{ t('register.surveyPrescriberQuestion') }}</label>
        <el-radio-group v-model="form.prescription">
          <el-radio label="yes">{{ t('register.yes') }}</el-radio>
          <el-radio label="no">{{ t('register.no') }}</el-radio>
        </el-radio-group>
      </div>
    </div>

    <!-- 5. 饮食要求模块 -->
    <div class="section">
      <div class="section-header">{{ t('register.surveyDietary') }}</div>
      <div class="form-item">
        <label class="label required">{{ t('register.surveyDietaryQuestion') }}</label>
        <el-radio-group v-model="form.diet">
          <el-radio label="yes">{{ t('register.yes') }}</el-radio>
          <el-radio label="no">{{ t('register.no') }}</el-radio>
        </el-radio-group>
      </div>
    </div>
    <div class="section" v-if="form.diet === 'yes'">
      <div class="form-item">
        <label class="label gdpr-notice">{{ t('register.surveyDietaryGDPR') }}</label>
      </div>
      <div class="form-item">
        <el-checkbox v-model="form.foodDetail" :label="t('register.surveyDietaryAgree')" size="large" />
      </div>
      <div v-if="form.foodDetail" class="diet-select-section">
        <label class="label diet-required">{{ t('register.surveyDietarySelect') }}</label>
        <el-radio-group v-model="form.dietRequirement" class="diet-radio-group">
          <el-radio label="不含麸质" class="diet-radio">{{ t('register.surveyGlutenFree') }}</el-radio>
          <el-radio label="素食" class="diet-radio">{{ t('register.surveyVegetarian') }}</el-radio>
          <el-radio label="纯素食" class="diet-radio">{{ t('register.surveyVegan') }}</el-radio>
          <el-radio label="犹太洁食" class="diet-radio">{{ t('register.surveyKosher') }}</el-radio>
          <el-radio label="清真" class="diet-radio">{{ t('register.surveyHalal') }}</el-radio>
          <el-radio label="不含猪肉" class="diet-radio">{{ t('register.surveyNoPork') }}</el-radio>
          <el-radio label="其他" class="diet-radio">{{ t('register.surveyOther') }}</el-radio>
        </el-radio-group>
      </div>
    </div>

    <!-- 6. 附加信息模块 -->
    <div class="section">
      <div class="section-header">{{ t('register.surveyAdditionalInfo') }}</div>

      <div class="form-item">
        <label class="label required">{{ t('register.surveyFirstTime') }}</label>
        <el-radio-group v-model="form.firstJoin">
          <el-radio label="yes">{{ t('register.yes') }}</el-radio>
          <el-radio label="no">{{ t('register.no') }}</el-radio>
        </el-radio-group>
      </div>

      <div class="form-group" v-if="form.firstJoin === 'no'">
        <label class="label required">{{ t('register.surveyPrevAttended') }}</label>
        <div class="checkbox-grid conference-grid">
          <el-checkbox-group v-model="form.selectedConferences">
            <el-checkbox label="WOC2024 加拿大温哥华">{{ t('register.surveyWOC2024') }}</el-checkbox>
            <el-checkbox label="WOC2022虚拟会议">{{ t('register.surveyWOC2022') }}</el-checkbox>
            <el-checkbox label="WOC2020虚拟会议">{{ t('register.surveyWOC2020') }}</el-checkbox>
            <el-checkbox label="WOC2016 墨西哥瓜达拉哈拉">{{ t('register.surveyWOC2016') }}</el-checkbox>
            <el-checkbox label="WOC2014 日本东京">{{ t('register.surveyWOC2014') }}</el-checkbox>
            <el-checkbox label="WOC2010 德国柏林">{{ t('register.surveyWOC2010') }}</el-checkbox>
            <el-checkbox label="WOC2008 香港，中国">{{ t('register.surveyWOC2008') }}</el-checkbox>
            <el-checkbox label="WOC2002 澳大利亚悉尼">{{ t('register.surveyWOC2002') }}</el-checkbox>
            <el-checkbox label="2018年世界奥运锦标赛，西班牙巴塞罗那">{{ t('register.surveyWOC2018') }}</el-checkbox>
            <el-checkbox label="2012年世界时装周，阿布扎比，阿拉伯联合酋长国">{{ t('register.surveyWOC2012') }}</el-checkbox>
            <el-checkbox label="2006年世界奥委会，巴西圣保罗">{{ t('register.surveyWOC2006') }}</el-checkbox>
            <el-checkbox label="其他">{{ t('register.surveyOther') }}</el-checkbox>
          </el-checkbox-group>
        </div>
      </div>

      <div class="form-item">
        <label class="label required">{{ t('register.surveyGender') }}</label>
        <el-select v-model="form.gender" placeholder="" class="w-full">
          <el-option :label="t('register.surveyMale')" value="male" />
          <el-option :label="t('register.surveyFemale')" value="female" />
          <el-option :label="t('register.surveyDisclose')" value="unknown" />
        </el-select>
      </div>

      <div class="form-item">
        <label class="label">{{ t('register.surveyProfession') }}</label>
        <el-select v-model="form.job" placeholder="" class="w-full">
          <el-option :label="t('register.surveyOphthalmologist')" value="eyeDoctor" />
          <el-option :label="t('register.surveyOptometrist')" value="optometrist" />
          <el-option :label="t('register.surveyResearcher')" value="researcher" />
          <el-option :label="t('register.surveyMedicalStudent')" value="student" />
          <el-option :label="t('register.surveyNurse')" value="nurse" />
        </el-select>
      </div>

      <div class="form-item">
        <label class="label required">{{ t('register.surveyExperience') }}</label>
        <el-select v-model="form.experience" placeholder="" class="w-full">
          <el-option :label="t('register.surveyExp02')" value="0-2" />
          <el-option :label="t('register.surveyExp35')" value="3-5" />
          <el-option :label="t('register.surveyExp610')" value="6-10" />
          <el-option :label="t('register.surveyExp10plus')" value="10+" />
        </el-select>
      </div>

      <div class="form-item">
        <label class="label required">{{ t('register.surveyInterest') }}</label>
        <p class="tip">{{ t('register.surveyInterestMax5') }}</p>
        <el-checkbox-group v-model="form.interestAreas" class="checkbox-grid">
          <el-checkbox label="视觉科学进展">{{ t('register.surveyInterestVisualScience') }}</el-checkbox>
          <el-checkbox label="白内障和屈光手术">{{ t('register.surveyInterestCataract') }}</el-checkbox>
          <el-checkbox label="角膜">{{ t('register.surveyInterestCornea') }}</el-checkbox>
          <el-checkbox label="青光眼">{{ t('register.surveyInterestGlaucoma') }}</el-checkbox>
          <el-checkbox label="视网膜和葡萄膜炎">{{ t('register.surveyInterestRetina') }}</el-checkbox>
          <el-checkbox label="小儿眼科和斜视">{{ t('register.surveyInterestPediatric') }}</el-checkbox>
          <el-checkbox label="神经眼科学">{{ t('register.surveyInterestNeuro') }}</el-checkbox>
          <el-checkbox label="眼外伤">{{ t('register.surveyInterestTrauma') }}</el-checkbox>
          <el-checkbox label="眼眶、眼整形">{{ t('register.surveyInterestOrbit') }}</el-checkbox>
          <el-checkbox label="近视">{{ t('register.surveyInterestMyopia') }}</el-checkbox>
          <el-checkbox label="流行病学">{{ t('register.surveyInterestEpidemiology') }}</el-checkbox>
          <el-checkbox label="视觉康复">{{ t('register.surveyInterestRehab') }}</el-checkbox>
          <el-checkbox label="其他">{{ t('register.surveyOther') }}</el-checkbox>
        </el-checkbox-group>
        <div v-if="form.interestAreas.includes('其他')" class="remark-section">
          <label class="required-label">{{ t('register.surveyInterestPlease') }}</label>
          <el-input v-model="form.otherRemark" class="remark-input" placeholder="" />
        </div>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="btn-group">
      <el-button class="back-btn" @click="handleBack">{{ t('register.back') }}</el-button>
      <el-button class="next-btn" @click="handleNext">{{ t('register.next') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ElRadio, ElRadioGroup, ElInput, ElSelect, ElOption, ElCheckbox, ElCheckboxGroup, ElButton } from 'element-plus'
import hospital from "@/utils/hospital";
import { uploadFiles } from "@/api/upload"
import { useRegisterStore } from '@/stores/register'

const router = useRouter()
const { t } = useI18n()
const registerStore = useRegisterStore()

const uploadRef = ref(null)
const isUploading = ref(false)

const defaultForm = () => ({
  hospitalVal: '',
  registerType: '',
  specialCode: '',
  prescription: '',
  diet: '',
  firstJoin: '',
  gender: '',
  job: '',
  experience: '',
  interestAreas: [],
  fileList: [],
  foodDetail: false,
  dietRequirement: '',
  selectedConferences: [],
  otherRemark: ''
})

const savedData = registerStore.state.survey?.questionnaire || {}
const form = ref({
  ...defaultForm(),
  ...savedData
})

watch(() => form.value, (newVal) => {
  registerStore.setSurveyData({ questionnaire: { ...newVal } })
}, { deep: true })
const typeChangeEve = (v) => {

}
const onExceed = (v) => {
  console.log("v",v)
  ElMessage.warning(t("register.surveyUploadLimit"));
}
async function handleChange(uploadFile) {
  console.log("1231231")
  const file = uploadFile.raw;
  console.log("file", file);
  if (file) {
    try {
      isUploading.value = true;
      let data = new FormData();
      data.append("file1", file);
      const res = await uploadFiles(data);
      console.log("res uploadFile" ,res)
      if(res?.code === "00000"){
        ElMessage.success(t("register.surveyUploadSuccess"));
      }else{
        uploadRef.value.clearFiles()
        throw new Error(t("register.surveyUploadFail"));
      }
    } catch(e) {
      uploadRef.value.clearFiles()
      ElMessage.error(t("register.surveyUploadFail"));
    } finally {
      isUploading.value = false;
    }
  }
}

const handleBack = () => {
  router.push('/register/contact')
}

const handleNext = () => {
  if (!form.value.registerType) {
    ElMessage.error(t('register.surveyValidation.selectCategory'))
    return
  }
  if (form.value.registerType === 'member' && !form.value.hospitalVal) {
    ElMessage.error(t('register.surveyValidation.selectSociety'))
    return
  }
  if ((form.value.registerType === 'scientist' || form.value.registerType === 'resident') && form.value.fileList.length === 0) {
    ElMessage.error(t('register.surveyValidation.uploadProof'))
    return
  }
  if (!form.value.prescription) {
    ElMessage.error(t('register.surveyValidation.selectPrescriber'))
    return
  }
  if (!form.value.diet) {
    ElMessage.error(t('register.surveyValidation.selectDietary'))
    return
  }
  if (form.value.diet === 'yes' && !form.value.foodDetail) {
    ElMessage.error(t('register.surveyValidation.agreeDietary'))
    return
  }
  if (form.value.diet === 'yes' && form.value.foodDetail && !form.value.dietRequirement) {
    ElMessage.error(t('register.surveyValidation.selectDietaryType'))
    return
  }
  if (!form.value.firstJoin) {
    ElMessage.error(t('register.surveyValidation.selectFirstTime'))
    return
  }
  if (form.value.firstJoin === 'no' && form.value.selectedConferences.length === 0) {
    ElMessage.error(t('register.surveyValidation.selectPrevAttended'))
    return
  }
  if (!form.value.gender) {
    ElMessage.error(t('register.surveyValidation.selectGender'))
    return
  }
  if (!form.value.experience) {
    ElMessage.error(t('register.surveyValidation.selectExperience'))
    return
  }
  if (form.value.interestAreas.length === 0) {
    ElMessage.error(t('register.surveyValidation.selectInterest'))
    return
  }
  if (form.value.interestAreas.includes('其他') && !form.value.otherRemark) {
    ElMessage.error(t('register.surveyValidation.specifyInterest'))
    return
  }
  registerStore.setSurveyData({ questionnaire: { ...form.value } })
  ElMessage.success(t('register.surveyDataSaved'))
  router.push('/register/signup')
}
</script>

<style scoped>
.register-page {
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.section {
  margin-bottom: 32px;
}

.section-header {
  background-color: #009688;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  padding: 10px 16px;
  border-radius: 6px;
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  font-size: 15px;
  color: #333;
  margin-bottom: 6px;
}

.link {
  color: #7d7ef4;
  text-decoration: underline;
}

.important {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 16px;
}

.form-item {
  margin-bottom: 20px;
}

.label {
  display: block;
  font-size: 15px;
  color: #ff7f24;
  font-weight: 500;
  margin-bottom: 10px;
}

.label.required::after {
  content: ' *';
  color: #f53f3f;
}

.tip {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.radio-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.w-full {
  width: 100%;
}

.btn-group {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
}

.conference-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 10px;
}

.remark-section {
  max-width: 100%;
  margin: 20px 0;
}

.required-label {
  color: #ff8000;
  font-size: 15px;
  font-weight: 600;
  display: block;
  margin-bottom: 10px;
}

.remark-input :deep(.el-input__wrapper) {
  border: 2px solid #993399;
  border-radius: 12px;
  box-shadow: none;
}

.gdpr-notice {
  background-color: #7E2081;
  color: #fff;
  padding: 12px 15px;
  border-radius: 10px;
  font-size: 14px;
  margin-bottom: 15px;
}

.diet-select-section {
  margin-top: 15px;
}

.diet-required {
  background-color: #7E2081;
  color: #fff;
  padding: 12px 15px;
  border-radius: 10px;
  font-size: 14px;
  margin-bottom: 15px;
  display: block;
}

.diet-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.diet-radio {
  margin-right: 0;
}

.back-btn,
.next-btn {
  background-color: #7d7ef4;
  border: none;
  color: #fff;
  padding: 10px 50px;
  font-size: 16px;
  border-radius: 6px;
  cursor: pointer;
}
</style>
