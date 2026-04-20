<!-- 会前会页面 -->
<template>
	<div class="pre-meeting-wrapper">
		<div class="huiHead">
			<el-image src="/assets/image/preMeeting/huiQianHuiShouTu.png" fit="cover" />
		</div>
		<div class="pre-meeting-header">
			<h1 class="pre-meeting-title">{{ t('preMeeting.title') }}</h1>
			<h4 class="pre-meeting-date">{{ t('preMeeting.date') }}</h4>
		</div>
		<div class="pre-meeting-list">
			<div class="meeting-item" v-for="(item, index) in preMeetingList" :key="index">
				<div class="room-tag" :ref="el => setRoomTagRef(el, index)" :class="{ 'align-top': item.isMultiLine }">
					{{ item.room }}
				</div>
				<div class="meeting-topic" :ref="el => setTopicTagRef(el, index)">
					{{ item.topic }}
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
	import {
		ref,
		computed,
		onMounted,
		nextTick,
		watch
	} from 'vue'
	import {
		useI18n
	} from 'vue-i18n'

	const {
		t,
		locale,
		messages
	} = useI18n()

	// 会前会列表DOM引用
	const roomTagRefs = ref([])
	const topicTagRefs = ref([])
	const processedMeetingList = ref([])

	// 初始化会前会列表数据
	const initMeetingList = () => {
		const originList = messages.value[locale.value]?.preMeeting?.list || []
		processedMeetingList.value = originList.map(item => ({
			...item,
			lineCount: 1,
			isMultiLine: false
		}))
		roomTagRefs.value = []
		topicTagRefs.value = []
		calculateLineCounts()
	}

	// 计算标题行数
	const calculateLineCounts = async () => {
		await nextTick()
		await nextTick()
		const newList = processedMeetingList.value.map((item, index) => {
			const topicEl = topicTagRefs.value[index]
			let lineCount = 1
			let isMultiLine = false
			if (topicEl) {
				const style = window.getComputedStyle(topicEl)
				const lineHeight = parseFloat(style.lineHeight) || 30
				const height = topicEl.clientHeight
				const paddingTop = parseFloat(style.paddingTop) || 0
				const paddingBottom = parseFloat(style.paddingBottom) || 0
				const contentHeight = height - paddingTop - paddingBottom
				lineCount = Math.round(contentHeight / lineHeight)
				isMultiLine = lineCount > 1
			}
			return {
				...item,
				lineCount,
				isMultiLine
			}
		})
		processedMeetingList.value = newList
	}

	// DOM引用设置
	const setRoomTagRef = (el, index) => {
		if (el) roomTagRefs.value[index] = el
	}
	const setTopicTagRef = (el, index) => {
		if (el) topicTagRefs.value[index] = el
	}

	// 计算属性
	const preMeetingList = computed(() => processedMeetingList.value)

	// 监听语言变化
	watch(locale, () => {
		initMeetingList()
	})

	// 页面挂载
	onMounted(() => {
		initMeetingList()
	})
</script>

<style scoped>
	/* 把你原来App.vue里的会前会样式完整复制到这里 */
	.pre-meeting-wrapper {
		width: 100%;
		background: #ffffff;
		border-radius: 12px;
		padding: 0 0 40px 0;
		box-sizing: border-box;
		overflow: hidden;
	}

	.huiHead {
		width: 100%;
		margin-bottom: 20px;
	}

	.huiHead .el-image {
		width: 100%;
		height: auto;
	}

	.pre-meeting-wrapper {
		width: 100%;
		background: #ffffff;
		border-radius: 12px;
		padding: 0 0 40px 0;
		box-sizing: border-box;
		overflow: hidden;
	}

	.huiHead {
		width: 100%;
		margin-bottom: 20px;
	}

	.huiHead .el-image {
		width: 100%;
		height: auto;
	}

	.pre-meeting-header {
		text-align: center;
		margin-bottom: 40px;
		padding: 0 24px;
	}

	.pre-meeting-title {
		font-size: 42px;
		font-weight: 800;
		background: linear-gradient(90deg, #7952b3, #b886f0);
		-webkit-background-clip: text;
		-webkit-text-fill-color: transparent;
		margin: 0 0 8px 0;
		line-height: 1.2;
	}

	.pre-meeting-date {
		font-size: 24px;
		font-weight: 600;
		color: #7952b3;
		margin: 0;
		line-height: 1.2;
	}

	.pre-meeting-list {
		display: flex;
		flex-direction: column;
		gap: 20px;
		padding: 0 40px;
	}

	.meeting-item {
		display: flex;
		align-items: flex-start;
		gap: 20px;
		width: 100%;
	}

	.room-tag {
		flex-shrink: 0;
		min-width: 180px;
		padding: 10px 16px;
		background: linear-gradient(90deg, #9d75d6, #7952b3);
		border-radius: 8px;
		color: #ffffff;
		font-size: 18px;
		font-weight: 600;
		line-height: 1.4;
		display: flex;
		align-items: center;
		justify-content: center;
		height: auto;
	}

	.room-tag.align-top {
		align-items: flex-start !important;
		justify-content: center !important;
	}

	.meeting-topic {
		flex: 1;
		text-align: left;
		font-size: 15px;
		font-weight: 500;
		color: #333333;
		line-height: 1.5;
		padding-top: 8px;
		word-break: break-word;
		align-self: flex-start;
	}
</style>