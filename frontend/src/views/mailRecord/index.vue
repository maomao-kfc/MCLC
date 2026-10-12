<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" :inline="true" class="search-form">
      <el-form-item label="业务类型">
        <el-select v-model="queryParams.businessType" placeholder="全部" clearable style="width: 160px">
          <el-option label="论文审稿通知" value="PAPER_SUBMIT" />
          <el-option label="论文审核通过" value="PAPER_PASS" />
          <el-option label="论文审核拒绝" value="PAPER_REJECT" />
          <el-option label="注册成功通知" value="USER_REGISTER" />
          <el-option label="密码重置" value="RESET_PASSWORD" />
          <el-option label="会议提醒" value="MEETING_NOTICE" />
          <el-option label="管理员错误通知" value="ADMIN_ERROR_NOTIFY" />
        </el-select>
      </el-form-item>
      <el-form-item label="发送状态">
        <el-select v-model="queryParams.sendStatus" placeholder="全部" clearable style="width: 160px">
          <el-option label="待发送" :value="0" />
          <el-option label="已提交至邮件服务器" :value="1" />
          <el-option label="发送失败" :value="2" />
          <el-option label="重试中" :value="3" />
          <el-option label="已取消" :value="4" />
          <el-option label="退信-投递失败" :value="5" />
          <el-option label="待重试" :value="6" />
          <el-option label="重试成功" :value="7" />
          <el-option label="重试失败" :value="8" />
        </el-select>
      </el-form-item>
      <el-form-item label="投递状态">
        <el-select v-model="queryParams.deliveryStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="等待投递" :value="0" />
          <el-option label="已投递" :value="1" />
          <el-option label="已退信" :value="2" />
          <el-option label="投递中" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="收件邮箱">
        <el-input v-model="queryParams.receiver" placeholder="搜索邮箱" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="邮件标题">
        <el-input v-model="queryParams.subject" placeholder="搜索标题" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格（树形展示：原始邮件为父节点，重试记录为子节点） -->
    <el-table v-loading="loading" :data="tableData" border stripe row-key="id" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
      <el-table-column label="ID" prop="id" width="70" />
      <el-table-column label="类型" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.recordType === 'RETRY'" type="warning" size="small">
            重试#{{ scope.row.retrySequence }}
          </el-tag>
          <el-tag v-else type="" size="small">原始</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务类型" align="center" width="130">
        <template #default="scope">
          <el-tag type="info">{{ scope.row.businessTypeDesc || scope.row.businessType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务ID" prop="businessId" width="80" />
      <el-table-column label="收件人" prop="receiverName" width="100" show-overflow-tooltip />
      <el-table-column label="收件邮箱" prop="receiver" min-width="200" show-overflow-tooltip />
      <el-table-column label="邮件标题" prop="subject" min-width="200" show-overflow-tooltip />
      <el-table-column label="发送状态" align="center" width="130">
        <template #default="scope">
          <el-tooltip v-if="scope.row.deliveryStatus === 2 || scope.row.sendStatus === 2 || scope.row.sendStatus === 5 || scope.row.sendStatus === 8" :content="scope.row.bounceReason || scope.row.errorMessage || (scope.row.deliveryStatus === 2 ? '退信-投递失败' : scope.row.sendStatus === 5 ? '退信-投递失败' : scope.row.sendStatus === 8 ? '重试失败' : '发送失败')" placement="top">
            <el-tag :type="getEffectiveStatusInfo(scope.row).type" style="cursor: pointer">{{ getEffectiveStatusInfo(scope.row).label }}</el-tag>
          </el-tooltip>
          <el-tag v-else :type="getEffectiveStatusInfo(scope.row).type">{{ getEffectiveStatusInfo(scope.row).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="投递状态" align="center" width="110">
        <template #default="scope">
          <el-tag v-if="scope.row.deliveryStatus != null" :type="getDeliveryStatusInfo(scope.row.deliveryStatus).type">
            {{ scope.row.deliveryStatusDesc || getDeliveryStatusInfo(scope.row.deliveryStatus).label }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="退信/错误原因" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          <el-text v-if="scope.row.bounceReason" type="danger" size="small">{{ scope.row.bounceReason }}</el-text>
          <el-text v-else-if="scope.row.errorMessage" type="warning" size="small">{{ scope.row.errorMessage }}</el-text>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="重试次数" prop="retryCount" align="center" width="80" />
      <el-table-column label="耗时(ms)" align="center" width="90">
        <template #default="scope">
          <span v-if="scope.row.consumeTime != null">{{ scope.row.consumeTime }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="发送时间" prop="sendTime" width="160" />
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="操作" fixed="right" width="300">
        <template #default="scope">
          <el-button
            v-if="scope.row.sendStatus === 1 || scope.row.sendStatus === 7"
            type="danger"
            size="small"
            link
            @click="handleMarkBounce(scope.row)"
          >
            标记退信
          </el-button>
          <el-button
            v-if="(scope.row.sendStatus === 2 || scope.row.sendStatus === 5 || scope.row.sendStatus === 8 || scope.row.deliveryStatus === 2) && scope.row.recordType !== 'RETRY' && (scope.row.retryCount == null || scope.row.retryCount < 5)"
            type="warning"
            size="small"
            link
            :loading="scope.row.retrying"
            @click="handleRetry(scope.row)"
          >
            重试发送
          </el-button>
          <el-button
            v-if="scope.row.sendStatus === 0"
            type="danger"
            size="small"
            link
            @click="handleCancel(scope.row)"
          >
            取消
          </el-button>
          <el-button type="primary" size="small" link @click="handleViewDetail(scope.row)">
            查看详情
          </el-button>
          <el-button
            v-if="scope.row.deliveryStatus === 2"
            type="warning"
            size="small"
            link
            @click="handleViewBounceDetail(scope.row)"
          >
            退信详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 15px"
      @size-change="handleSearch"
      @current-change="handleSearch"
    />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" title="邮件发送记录详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detailDialog.data.id }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ detailDialog.data.businessTypeDesc || detailDialog.data.businessType }}</el-descriptions-item>
        <el-descriptions-item label="业务ID">{{ detailDialog.data.businessId }}</el-descriptions-item>
        <el-descriptions-item label="收件人">{{ detailDialog.data.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="收件邮箱" :span="2">{{ detailDialog.data.receiver }}</el-descriptions-item>
        <el-descriptions-item label="邮件标题" :span="2">{{ detailDialog.data.subject }}</el-descriptions-item>
        <el-descriptions-item label="发送状态">
          <el-tag :type="getEffectiveStatusInfo(detailDialog.data).type">
            {{ detailDialog.data.deliveryStatus === 2 ? '已退信' : (detailDialog.data.sendStatusDesc || getEffectiveStatusInfo(detailDialog.data).label) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="记录类型">
          <template v-if="detailDialog.data.recordType === 'RETRY'">
            <el-tag type="warning">重试 #{{ detailDialog.data.retrySequence }}</el-tag>
          </template>
          <template v-else>
            <el-tag>原始邮件</el-tag>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ detailDialog.data.retryCount }}</el-descriptions-item>
        <el-descriptions-item label="发送耗时">{{ detailDialog.data.consumeTime }}ms</el-descriptions-item>
        <el-descriptions-item label="发送账号">{{ detailDialog.data.sender }}</el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.data.messageId" label="Message-ID" :span="2">
          <el-text size="small" type="info">{{ detailDialog.data.messageId }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.data.errorMessage" label="错误信息" :span="2">
          <el-text type="danger">{{ detailDialog.data.errorMessage }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.data.bounceReason" label="退信原因" :span="2">
          <el-text type="danger">{{ detailDialog.data.bounceReason }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.data.deliveryStatus != null" label="投递状态">
          <el-tag :type="getDeliveryStatusInfo(detailDialog.data.deliveryStatus).type">
            {{ detailDialog.data.deliveryStatusDesc || getDeliveryStatusInfo(detailDialog.data.deliveryStatus).label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.data.bounceType" label="退信类型">
          <el-tag type="danger">{{ detailDialog.data.bounceType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.data.bounceCode" label="退信代码">
          <el-text type="warning">{{ detailDialog.data.bounceCode }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.data.bouncePermanent != null" label="永久退信">
          <el-tag :type="detailDialog.data.bouncePermanent ? 'danger' : 'info'">
            {{ detailDialog.data.bouncePermanent ? '是（禁止重发）' : '否（可重发）' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.data.bounceMessageId" label="退信Message-ID" :span="2">
          <el-text size="small" type="info">{{ detailDialog.data.bounceMessageId }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.data.rootRecordId" label="原始记录ID">
          <el-link type="primary" @click="handleViewParentRecord(detailDialog.data.rootRecordId)">
            {{ detailDialog.data.rootRecordId }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item v-else-if="detailDialog.data.parentRecordId" label="父记录ID">
          <el-link type="primary" @click="handleViewParentRecord(detailDialog.data.parentRecordId)">
            {{ detailDialog.data.parentRecordId }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.data.imapCheckCount != null" label="IMAP检查次数">
          {{ detailDialog.data.imapCheckCount }}
        </el-descriptions-item>
        <el-descriptions-item label="发送时间">{{ detailDialog.data.sendTime }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailDialog.data.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detailDialog.data.content" style="margin-top: 15px">
        <el-divider content-position="left">邮件正文</el-divider>
        <div class="email-content-preview" v-html="detailDialog.data.content" />
      </div>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 标记退信弹窗 -->
    <el-dialog v-model="bounceDialog.visible" title="标记退信" width="500px">
      <el-form :model="bounceDialog" label-width="100px">
        <el-form-item label="收件邮箱">
          <span>{{ bounceDialog.receiver }}</span>
        </el-form-item>
        <el-form-item label="邮件标题">
          <span>{{ bounceDialog.subject }}</span>
        </el-form-item>
        <el-form-item label="退信原因" required>
          <el-input
            v-model="bounceDialog.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入退信原因，例如：550 User not found: xxx@126.com"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bounceDialog.visible = false">取消</el-button>
        <el-button type="danger" :loading="bounceDialog.loading" @click="confirmMarkBounce">确认标记退信</el-button>
      </template>
    </el-dialog>

    <!-- 退信详情弹窗 -->
    <el-dialog v-model="bounceDetailDialog.visible" title="退信详情" width="750px">
      <div v-loading="bounceDetailDialog.loading">
        <template v-if="bounceDetailDialog.records.length > 0">
          <el-collapse v-model="bounceDetailDialog.activeNames">
            <el-collapse-item
              v-for="(record, index) in bounceDetailDialog.records"
              :key="record.id"
              :title="`退信记录 #${record.id} - ${record.bounceType || '未知类型'}`"
              :name="record.id"
            >
              <el-descriptions :column="2" border size="small">
                <!-- 退信分类 -->
                <el-descriptions-item label="退信ID">{{ record.id }}</el-descriptions-item>
                <el-descriptions-item label="退信类型">
                  <el-tag type="danger">{{ record.bounceType }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="永久退信">
                  <el-tag :type="record.bouncePermanent ? 'danger' : 'info'">
                    {{ record.bouncePermanent ? '是（禁止重发）' : '否（可重发）' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="退信代码">{{ record.bounceCode || '-' }}</el-descriptions-item>

                <!-- 匹配信息 -->
                <el-descriptions-item label="匹配策略">
                  <el-tag type="warning">{{ getMatchStrategyLabel(record.matchStrategy) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="匹配置信度">
                  <template v-if="record.matchConfidence != null">
                    <el-progress :percentage="record.matchConfidence" :stroke-width="12" :text-inside="true" style="width: 120px" />
                  </template>
                  <template v-else>-</template>
                </el-descriptions-item>

                <!-- 原始邮件信息（真正发生投递失败的邮件） -->
                <el-descriptions-item label="原始收件人" :span="2">
                  <el-text type="danger">{{ record.originalRecipient || record.bouncedRecipient || '-' }}</el-text>
                  <el-tag v-if="record.recipientSource" size="small" type="info" style="margin-left: 8px">{{ record.recipientSource }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="原始发件人">{{ record.originalSender || '-' }}</el-descriptions-item>
                <el-descriptions-item label="原始主题" :span="2">{{ record.originalSubject || '-' }}</el-descriptions-item>
                <el-descriptions-item label="原始Message-ID" :span="2">
                  <el-text size="small" type="info">{{ record.originalMessageId || '-' }}</el-text>
                </el-descriptions-item>

                <!-- 退信通知邮件信息 -->
                <el-descriptions-item label="退信发送者">{{ record.bounceSender || '-' }}</el-descriptions-item>
                <el-descriptions-item label="退信收件人">{{ record.bounceRecipient || '-' }}</el-descriptions-item>
                <el-descriptions-item label="退信主题" :span="2">{{ record.bounceSubject || '-' }}</el-descriptions-item>
                <el-descriptions-item label="退信Message-ID" :span="2">
                  <el-text size="small" type="info">{{ record.bounceMessageId || '-' }}</el-text>
                </el-descriptions-item>

                <!-- DSN标准字段 -->
                <el-descriptions-item label="DSN状态码">{{ record.dsnStatus || '-' }}</el-descriptions-item>
                <el-descriptions-item label="DSN Action">{{ record.dsnAction || '-' }}</el-descriptions-item>
                <el-descriptions-item v-if="record.dsnDiagnosticCode" label="DSN诊断码" :span="2">
                  <el-text size="small" type="warning">{{ record.dsnDiagnosticCode }}</el-text>
                </el-descriptions-item>
                <el-descriptions-item v-if="record.finalRecipient" label="Final-Recipient" :span="2">
                  <el-text size="small">{{ record.finalRecipient }}</el-text>
                </el-descriptions-item>
                <el-descriptions-item v-if="record.originalRecipientDsn" label="Original-Recipient(DSN)">
                  <el-text size="small">{{ record.originalRecipientDsn }}</el-text>
                </el-descriptions-item>

                <!-- 退信原因 -->
                <el-descriptions-item label="退信原因" :span="2">
                  <el-text type="danger">{{ record.bounceReason }}</el-text>
                </el-descriptions-item>

                <!-- Header辅助字段 -->
                <el-descriptions-item v-if="record.inReplyTo" label="In-Reply-To" :span="2">
                  <el-text size="small" type="info">{{ record.inReplyTo }}</el-text>
                </el-descriptions-item>
                <el-descriptions-item v-if="record.referencesHeader" label="References" :span="2">
                  <el-text size="small" type="info">{{ record.referencesHeader }}</el-text>
                </el-descriptions-item>

                <!-- 处理状态 -->
                <el-descriptions-item label="处理状态">
                  <el-tag :type="record.processed ? 'success' : 'warning'">
                    {{ record.processed ? '已处理' : '未处理' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="退信日期">{{ record.bounceDate || '-' }}</el-descriptions-item>
                <el-descriptions-item v-if="record.processResult" label="处理结果" :span="2">{{ record.processResult }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ record.createTime }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ record.updateTime }}</el-descriptions-item>
              </el-descriptions>
            </el-collapse-item>
          </el-collapse>
        </template>
        <el-empty v-else description="暂无退信记录" />
      </div>
      <template #footer>
        <el-button @click="bounceDetailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onActivated } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getMailSendRecordPage,
  getMailSendRecordDetail,
  retryMailSendRecord,
  cancelMailSendRecord,
  markBounce,
  type MailSendRecordQuery,
  type MailSendRecordPageVO,
  type MailSendRecordDetail,
} from "@/api/portal/mailSendRecord";
import {
  listBounceBySendRecordId,
  type MailBounceRecord,
} from "@/api/portal/mailBounceRecord";

defineOptions({ name: "MailRecord", inheritAttrs: false });

const loading = ref(false);
const tableData = ref<MailSendRecordPageVO[]>([]);
const total = ref(0);

const queryParams = reactive<MailSendRecordQuery>({
  pageNum: 1,
  pageSize: 20,
  businessType: undefined,
  businessId: undefined,
  receiver: undefined,
  sendStatus: undefined,
  deliveryStatus: undefined,
  subject: undefined,
  startTime: undefined,
  endTime: undefined,
});

/**
 * 综合发送状态+投递状态的有效状态信息
 * 优先级：delivery_status=BOUNCED(2) > send_status=BOUNCE_DETECTED(5) > send_status
 * 解决问题：send_status=SUCCESS(1) 但 delivery_status=BOUNCED(2) 时，应显示"已退信"而非"已提交至邮件服务器"
 */
function getEffectiveStatusInfo(row: { sendStatus?: number | null; deliveryStatus?: number | null }) {
  // 投递状态=已退信(2)时，覆盖发送状态显示
  if (row.deliveryStatus === 2) {
    return { label: "已退信", type: "danger" };
  }
  // 发送状态=退信检测到(5)时，显示退信
  if (row.sendStatus === 5) {
    return { label: "退信-投递失败", type: "danger" };
  }
  // 重试相关状态
  if (row.sendStatus === 6) {
    return { label: "待重试", type: "warning" };
  }
  if (row.sendStatus === 7) {
    return { label: "重试成功", type: "success" };
  }
  if (row.sendStatus === 8) {
    return { label: "重试失败", type: "danger" };
  }
  // 其他情况按send_status显示
  return getSendStatusInfo(row.sendStatus);
}

/** 发送状态映射（兼容数字和枚举名称两种格式） */
function getSendStatusInfo(status: number | string | undefined | null) {
  const map: Record<number, { label: string; type: string }> = {
    0: { label: "待发送", type: "info" },
    1: { label: "已提交至邮件服务器", type: "success" },
    2: { label: "发送失败", type: "danger" },
    3: { label: "重试中", type: "warning" },
    4: { label: "已取消", type: "info" },
    5: { label: "退信-投递失败", type: "danger" },
    6: { label: "待重试", type: "warning" },
    7: { label: "重试成功", type: "success" },
    8: { label: "重试失败", type: "danger" },
  };
  // 枚举名称 → 数字映射
  const nameMap: Record<string, number> = {
    PENDING: 0, SUCCESS: 1, FAILED: 2, RETRYING: 3, CANCELLED: 4, BOUNCE_DETECTED: 5,
    RETRY_PENDING: 6, RETRY_SUCCESS: 7, RETRY_FAILED: 8,
  };
  if (status == null) return { label: "未知", type: "info" };
  // 数字格式
  if (typeof status === "number") return map[status] || { label: "未知", type: "info" };
  // 枚举名称字符串格式
  const num = nameMap[status];
  if (num !== undefined) return map[num];
  // 字符串数字格式
  const parsed = parseInt(String(status), 10);
  if (!isNaN(parsed) && map[parsed]) return map[parsed];
  return { label: "未知", type: "info" };
}

/** 投递状态映射 */
function getDeliveryStatusInfo(status: number | null | undefined) {
  const map: Record<number, { label: string; type: string }> = {
    0: { label: "等待投递", type: "info" },
    1: { label: "已投递", type: "success" },
    2: { label: "已退信", type: "danger" },
    3: { label: "投递中", type: "warning" },
  };
  if (status == null) return { label: "未知", type: "info" };
  return map[status] || { label: "未知", type: "info" };
}

/** 匹配策略标签映射 */
function getMatchStrategyLabel(strategy: string | null | undefined) {
  const map: Record<string, string> = {
    MESSAGE_ID: "Message-ID精确匹配",
    IN_REPLY_TO: "In-Reply-To匹配",
    REFERENCES: "References匹配",
    FINAL_RECIPIENT_TIME_WINDOW: "Final-Recipient+时间窗口匹配",
    ORIGINAL_RECIPIENT_TIME_WINDOW: "Original-Recipient+时间窗口匹配",
    RECEIVER_TIME_WINDOW: "收件人+时间窗口匹配",
    UNMATCHED: "未匹配",
  };
  if (!strategy) return "-";
  return map[strategy] || strategy;
}

/** 详情弹窗 */
const detailDialog = reactive({
  visible: false,
  data: {} as MailSendRecordDetail,
});

/** 查询列表 */
async function handleSearch() {
  loading.value = true;
  try {
    const data = await getMailSendRecordPage(queryParams);
    tableData.value = data.list || [];
    total.value = data.total ?? 0;
  } finally {
    loading.value = false;
  }
}

/** 重置查询 */
function handleReset() {
  queryParams.businessType = undefined;
  queryParams.businessId = undefined;
  queryParams.receiver = undefined;
  queryParams.sendStatus = undefined;
  queryParams.deliveryStatus = undefined;
  queryParams.subject = undefined;
  queryParams.startTime = undefined;
  queryParams.endTime = undefined;
  queryParams.pageNum = 1;
  handleSearch();
}

/** 重试发送 */
async function handleRetry(row: MailSendRecordPageVO) {
  await ElMessageBox.confirm(
    `确认重试发送邮件给 ${row.receiver}？已重试 ${row.retryCount} 次。`,
    "重试确认",
    { type: "warning" }
  );
  (row as any).retrying = true;
  try {
    await retryMailSendRecord(row.id);
    ElMessage.success("重试发送已提交");
    handleSearch();
  } finally {
    (row as any).retrying = false;
  }
}

/** 取消发送 */
async function handleCancel(row: MailSendRecordPageVO) {
  await ElMessageBox.confirm(
    `确认取消发送邮件给 ${row.receiver}？`,
    "取消确认",
    { type: "warning" }
  );
  await cancelMailSendRecord(row.id);
  ElMessage.success("已取消");
  handleSearch();
}

/** 查看详情 */
async function handleViewDetail(row: MailSendRecordPageVO) {
  const data = await getMailSendRecordDetail(row.id);
  detailDialog.data = data as MailSendRecordDetail;
  detailDialog.visible = true;
}

/** 标记退信弹窗 */
const bounceDialog = reactive({
  visible: false,
  loading: false,
  recordId: 0,
  receiver: "",
  subject: "",
  reason: "",
});

/** 打开标记退信弹窗 */
function handleMarkBounce(row: MailSendRecordPageVO) {
  bounceDialog.recordId = row.id;
  bounceDialog.receiver = row.receiver;
  bounceDialog.subject = row.subject;
  bounceDialog.reason = "";
  bounceDialog.visible = true;
}

/** 确认标记退信 */
async function confirmMarkBounce() {
  if (!bounceDialog.reason.trim()) {
    ElMessage.warning("请输入退信原因");
    return;
  }
  bounceDialog.loading = true;
  try {
    await markBounce(bounceDialog.recordId, bounceDialog.reason.trim());
    ElMessage.success("已标记为退信-投递失败");
    bounceDialog.visible = false;
    handleSearch();
  } finally {
    bounceDialog.loading = false;
  }
}

/** 退信详情弹窗 */
const bounceDetailDialog = reactive({
  visible: false,
  loading: false,
  records: [] as MailBounceRecord[],
  activeNames: [] as number[],
});

/** 查看退信详情 */
async function handleViewBounceDetail(row: MailSendRecordPageVO) {
  bounceDetailDialog.visible = true;
  bounceDetailDialog.loading = true;
  bounceDetailDialog.records = [];
  bounceDetailDialog.activeNames = [];
  try {
    const data = await listBounceBySendRecordId(row.id);
    bounceDetailDialog.records = (data as MailBounceRecord[]) || [];
    if (bounceDetailDialog.records.length > 0) {
      bounceDetailDialog.activeNames = [bounceDetailDialog.records[0].id];
    }
  } finally {
    bounceDetailDialog.loading = false;
  }
}

/** 查看父记录（重发邮件的原始退信记录） */
async function handleViewParentRecord(parentRecordId: number) {
  detailDialog.visible = false;
  const data = await getMailSendRecordDetail(parentRecordId);
  detailDialog.data = data as MailSendRecordDetail;
  detailDialog.visible = true;
}

onMounted(() => {
  handleSearch();
});

/** 页面被 keep-alive 缓存后，从菜单再次进入时自动刷新数据 */
onActivated(() => {
  handleSearch();
});
</script>

<style scoped>
.search-form {
  margin-bottom: 15px;
}
.email-content-preview {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
  background: #fafafa;
}
</style>