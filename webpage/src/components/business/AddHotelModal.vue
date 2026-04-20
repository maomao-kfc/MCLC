<template>
  <el-dialog 
    v-model="dialogVisible" 
    :title="t('hotel.dialogTitle')" 
    width="600px"
    :before-close="handleClose"
  >
    <el-form 
      :model="hotelForm" 
      :rules="hotelRules" 
      ref="hotelFormRef" 
      label-width="120px" 
      label-position="right"
    >
      <el-form-item :label="t('hotel.form.name')" prop="name">
        <el-input v-model="hotelForm.name" :placeholder="t('hotel.form.namePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('hotel.form.linkman')" prop="linkman">
        <el-input v-model="hotelForm.linkman" :placeholder="t('hotel.form.linkmanPlaceholder')" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">{{ t('hotel.cancelBtn') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ t('hotel.submitBtn') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['update:modelValue', 'submit'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const hotelFormRef = ref()

const hotelForm = reactive({
  name: '',
  linkman: ''
})

const hotelRules = computed(() => ({
  name: [{ required: true, message: t('hotel.form.nameRequired'), trigger: 'blur' }],
  linkman: [{ required: true, message: t('hotel.form.linkmanRequired'), trigger: 'blur' }]
}))

const handleClose = (done) => {
  ElMessageBox.confirm(t('hotel.closeConfirm'))
    .then(() => {
      done()
      resetForm()
    })
    .catch(() => {})
}

const handleCancel = () => {
  dialogVisible.value = false
  resetForm()
}

const handleSubmit = () => {
  hotelFormRef.value.validate((valid) => {
    if (valid) {
      emit('submit', { ...hotelForm })
      ElMessage.success(t('hotel.submitSuccess'))
      dialogVisible.value = false
      resetForm()
    }
  })
}

const resetForm = () => {
  hotelFormRef.value?.resetFields()
  hotelForm.name = ''
  hotelForm.linkman = ''
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>