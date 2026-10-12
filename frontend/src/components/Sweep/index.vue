<template>
  <div>
    <div
      class="sweep-trigger"
      role="button"
      tabindex="0"
      aria-label="扫一扫"
      @click="open"
      @keydown.enter.prevent="open"
      @keydown.space.prevent="open"
    >
      <div class="i-svg:scan" />
    </div>

    <el-dialog
      v-model="visible"
      title="扫一扫"
      :width="dialogWidth"
      :close-on-click-modal="false"
      @close="close"
    >
      <div class="sweep-dialog">
        <div v-if="!hasCamera" class="sweep-error">
          <p>当前设备不支持摄像头或未授予摄像头权限</p>
        </div>
        <template v-else>
          <div class="sweep-viewport">
            <div id="qr-reader" class="sweep-reader" />
            <div class="sweep-border" />
          </div>
          <div v-if="scanResult" class="sweep-result">
            <div class="sweep-result__label">扫描结果：</div>
            <div class="sweep-result__content">{{ scanResult }}</div>
            <div class="sweep-result__actions">
              <el-button size="small" @click="handleCopy">复制</el-button>
              <el-button v-if="isUrl(scanResult)" size="small" type="primary" @click="handleOpenUrl">
                打开链接
              </el-button>
              <el-button size="small" @click="handleRescan">重新扫描</el-button>
            </div>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Html5Qrcode } from "html5-qrcode";

const visible = ref(false);
const scanResult = ref("");
const hasCamera = ref(true);
let html5QrCode: Html5Qrcode | null = null;

const windowWidth = ref(window.innerWidth);
const onResize = () => {
  windowWidth.value = window.innerWidth;
};
onMounted(() => window.addEventListener("resize", onResize));
onUnmounted(() => window.removeEventListener("resize", onResize));

const dialogWidth = computed(() => (windowWidth.value < 768 ? "100%" : "500px"));

const qrboxSize = computed(() => {
  const base = Math.min(windowWidth.value - 64, 320);
  return Math.max(200, base);
});

function open() {
  visible.value = true;
  scanResult.value = "";
  nextTick(() => {
    startScan();
  });
}

function close() {
  stopScan();
  visible.value = false;
  scanResult.value = "";
}

async function startScan() {
  try {
    const devices = await Html5Qrcode.getCameras();
    if (!devices || devices.length === 0) {
      hasCamera.value = false;
      return;
    }
    hasCamera.value = true;

    html5QrCode = new Html5Qrcode("qr-reader", {
      verbose: false,
      formatsToSupport: undefined,
    });

    // 优先使用后置摄像头
    const cameraId =
      devices.length > 1
        ? devices[devices.length - 1].id
        : devices[0].id;

    await html5QrCode.start(
      cameraId,
      {
        fps: 10,
        qrbox: { width: qrboxSize.value, height: qrboxSize.value },
      },
      (decodedText: string) => {
        html5QrCode?.pause(true);
        if (isUrl(decodedText)) {
          stopScan();
          visible.value = false;
          window.open(decodedText, "_blank");
        } else {
          scanResult.value = decodedText;
        }
      },
      () => {
        // 扫描中，每帧回调，忽略
      }
    );
  } catch (error: any) {
    if (error?.message?.includes("NotAllowedError") || error === "NotAllowedError") {
      hasCamera.value = false;
    } else {
      hasCamera.value = false;
      console.error("扫码初始化失败:", error);
    }
  }
}

function stopScan() {
  if (html5QrCode?.isScanning) {
    html5QrCode
      .stop()
      .then(() => {
        html5QrCode?.clear();
      })
      .catch(() => {});
  }
}

async function handleCopy() {
  try {
    await navigator.clipboard.writeText(scanResult.value);
    ElMessage.success("已复制到剪贴板");
  } catch {
    ElMessage.error("复制失败");
  }
}

function handleOpenUrl() {
  window.open(scanResult.value, "_blank");
}

async function handleRescan() {
  scanResult.value = "";
  if (html5QrCode) {
    await html5QrCode.resume();
  }
}

function isUrl(text: string) {
  return /^https?:\/\/.+/.test(text);
}
</script>

<style lang="scss" scoped>
.sweep-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.sweep-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.sweep-error {
  padding: 40px 0;
  color: var(--el-text-color-secondary);
  text-align: center;
}

.sweep-viewport {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  max-width: 320px;
  overflow: hidden;
  border-radius: 12px;
}

.sweep-reader {
  width: 100%;
  height: 100%;
}

.sweep-border {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  border: 2px solid var(--el-color-primary);
  border-radius: 12px;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.35) inset;
}

.sweep-result {
  width: 100%;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.sweep-result__label {
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.sweep-result__content {
  margin-bottom: 12px;
  font-size: 14px;
  word-break: break-all;
  color: var(--el-text-color-primary);
}

.sweep-result__actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 767px) {
  .sweep-dialog {
    gap: 12px;
  }
}
</style>
