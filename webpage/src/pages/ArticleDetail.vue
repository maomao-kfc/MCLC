<template>
	<div class="article-page">
		<!-- 文章顶部头部 -->
		<div class="article-header">
			<div class="issue-tag">NO.{{ articleInfo.id }}</div>
			<h1 class="main-title">{{ articleInfo.title }}</h1>
			<p class="sub-title">{{ articleInfo.subTitle }}</p>
			<p class="en-title">{{ articleInfo.enSubTitle }}</p>
			<p class="meta-info">
				{{ articleInfo.publishTime }} | {{ articleInfo.author }}
			</p>
		</div>

		<!-- 讲者介绍 -->
		<div class="speaker-section" v-if="articleInfo.speaker">
			<div class="module-title">讲者介绍</div>
			<div class="speaker-content">
				<img :src="articleInfo.speaker.avatar" :alt="articleInfo.speaker.name" class="speaker-avatar" />
				<div class="speaker-text">
					<h3 class="speaker-name">{{ articleInfo.speaker.name }}</h3>
					<p class="speaker-desc">{{ articleInfo.speaker.title }}</p>
				</div>
			</div>
		</div>

		<!-- 文章核心内容区 -->
		<div class="article-content">
			<template v-for="(item, index) in articleInfo.content" :key="index">
				<div v-if="item.type === 'video'" class="video-section">
					<video class="article-video" :src="item.src" :poster="item.poster" controls preload="metadata">
						您的浏览器不支持视频播放，请更换浏览器重试
					</video>
					<p v-if="item.desc" class="video-desc">{{ item.desc }}</p>
				</div>

				<!-- 三级小标题 -->
				<h3 v-if="item.type === 'title' && item.level === 3" class="h3-title">
					{{ item.text }}
				</h3>

				<!-- 四级小标题 -->
				<h4 v-if="item.type === 'title' && item.level === 4" class="h4-title">
					{{ item.text }}
				</h4>

				<!-- 正文文本 -->
				<p v-if="item.type === 'text'" class="content-text">
					{{ item.text }}
				</p>

				<!-- 列表模块 -->
				<ul v-if="item.type === 'list'" class="content-list">
					<li v-for="(li, i) in item.list" :key="i" class="list-item">
						{{ li }}
					</li>
				</ul>

				<!-- 图片模块 -->
				<div v-if="item.type === 'image'" class="img-section" :style="{ maxWidth: item.maxWidth || '100%' }">
					<img :src="item.src" :alt="item.alt" class="content-img" @click="showBigImg(item.src)" />
					<p class="img-desc">{{ item.alt }}</p>
				</div>

				<!-- 参考文献模块 -->
				<div v-if="item.type === 'reference'" class="reference-section">
					<div class="module-title">参考文献与学习资料</div>
					<ol class="reference-list">
						<li v-for="(ref, i) in item.list" :key="i" class="reference-item">
							{{ ref }}
						</li>
					</ol>
				</div>
			</template>
		</div>

		<!-- 返回列表按钮 -->
		<div class="back-btn-wrapper">
			<el-button type="primary" class="back-btn" @click="$router.back()">
				← 返回文章列表
			</el-button>
		</div>

		<!-- 图片放大弹窗 -->
		<div v-if="bigImgUrl" class="img-modal" @click="bigImgUrl = ''">
			<img :src="bigImgUrl" alt="大图预览" class="big-img" />
		</div>
	</div>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue'
	import {
		useRoute
	} from 'vue-router'
	import {
		articleList
	} from '@/data/article.js'

	const route = useRoute()
	const bigImgUrl = ref('')

	// 获取文章ID，兼容params/query
	const articleId = computed(() => route.params.id || route.query.id || 1)

	// 匹配文章数据
	const articleInfo = computed(() => {
		return articleList.zh.find(item => item.id === Number(articleId.value)) || articleList.zh[0]
	})

	// 图片放大
	const showBigImg = (src) => {
		bigImgUrl.value = src
	}
</script>

<style scoped>
	/* 全局重置 */
	* {
		margin: 0;
		padding: 0;
		box-sizing: border-box;
	}

	/* 页面整体容器 */
	.article-page {
		min-height: 100vh;
		padding: 16px 12px 80px;
		background: #f8f9fc;
		font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", sans-serif;
		color: #333;
		max-width: 750px;
		margin: 0 auto;
	}

	/* 文章头部 */
	.article-header {
		text-align: center;
		margin-bottom: 24px;
		padding: 24px 16px;
	}

	.issue-tag {
		display: inline-block;
		background: linear-gradient(90deg, #7b61ff 0%, #b39dff 100%);
		color: #fff;
		font-size: 12px;
		font-weight: 600;
		padding: 4px 12px;
		border-radius: 20px;
		margin-bottom: 12px;
	}

	.main-title {
		font-size: 20px;
		font-weight: 700;
		color: #0a1629;
		line-height: 1.4;
		margin-bottom: 8px;
	}

	.sub-title {
		font-size: 16px;
		font-weight: 500;
		color: #0f4088;
		margin-bottom: 6px;
	}

	.en-title {
		font-size: 14px;
		color: #555;
		font-style: italic;
		margin-bottom: 16px;
	}

	.meta-info {
		font-size: 12px;
		color: #777;
	}

	.speaker-section {
		margin-bottom: 24px;
		margin-bottom: 24px;
		padding: 0 8px;
	}

	.module-title {
		width: fit-content;
		margin: 0 auto 20px;
		padding: 8px 24px;
		background: linear-gradient(90deg, #7b61ff 0%, #a78bfa 100%);
		color: #fff;
		font-size: 16px;
		font-weight: 700;
		border-radius: 20px;
		text-align: center;
	}

	.speaker-content {
		display: flex;
		flex-direction: row;
		align-items: center;
		gap: 16px;
		padding: 0 8px;
		justify-content: center;
	}

	.speaker-avatar {
		width: 110px;
		height: 110px;
		border-radius: 50%;
		object-fit: cover;
		border: 3px solid #e8edff;
		flex-shrink: 0;
	}

	.speaker-text {
		//text-align: left;
		flex: 1;
		max-width: calc(100% - 96px);
		margin-left: -100px;
	}

	.speaker-name {
		font-size: 18px;
		font-weight: 600;
		color: #0a1629;
		margin-bottom: 6px;
	}

	.speaker-desc {
		font-size: 14px;
		color: #555;
		line-height: 1.5;
	}

	/* 新增：视频模块样式 */
	.video-section {
		margin: 20px auto;
		padding: 0 8px;
		width: 100%;
	}

	.article-video {
		width: 100%;
		height: auto;
		border-radius: 8px;
		background: #000;
	}

	.video-desc {
		font-size: 12px;
		color: #666;
		text-align: center;
		margin-top: 8px;
	}

	/* 标题层级 */
	.h3-title {
		font-size: 17px;
		font-weight: 600;
		color: #0f4088;
		margin: 20px 0 12px 8px;
		padding-left: 8px;
		border-left: 3px solid #7b61ff;
	}

	.h4-title {
		font-size: 16px;
		font-weight: 600;
		color: #1a2639;
		margin: 18px 0 10px 8px;
	}

	/* 正文文本（保留首行缩进+自然段） */
	.content-text {
		font-size: 15px;
		line-height: 1.75;
		color: #222;
		margin-bottom: 8px;
		text-align: justify;
		padding: 0 8px;
		text-indent: 2em;
	}

	/* 列表模块 */
	.content-list {
		padding-left: 32px;
		margin: 0 8px 16px;
		display: flex;
		flex-direction: column;
		gap: 10px;
	}

	.list-item {
		font-size: 15px;
		line-height: 1.7;
		color: #222;
	}

	/* 图片模块 */
	.img-section {
		margin: 20px auto;
		padding: 0 8px;
		width: 100%;
	}

	.content-img {
		width: 100%;
		height: auto;
		border-radius: 6px;
		cursor: pointer;
		image-rendering: crisp-edges;
		object-fit: contain;
		transition: transform 0.2s ease;
	}

	.content-img:hover {
		transform: scale(1.01);
	}

	.img-desc {
		font-size: 12px;
		color: #666;
		text-align: center;
		margin-top: 8px;
	}

	/* 参考文献模块 */
	.reference-section {
		margin-top: 32px;
		padding: 0 8px;
	}

	.reference-list {
		padding-left: 32px;
		margin: 0 auto;
		display: flex;
		flex-direction: column;
		gap: 10px;
		max-width: 90%;
	}

	.reference-item {
		font-size: 12px;
		line-height: 1.6;
		color: #555;
		word-break: break-all;
	}

	/* 返回按钮 */
	.back-btn-wrapper {
		margin-top: 40px;
		text-align: center;
	}

	.back-btn {
		background: linear-gradient(90deg, #7b61ff 0%, #a78bfa 100%);
		border: none;
		border-radius: 20px;
		padding: 8px 24px;
		font-size: 14px;
	}

	/* 图片放大弹窗 */
	.img-modal {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background: rgba(0, 0, 0, 0.85);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 9999;
	}

	.big-img {
		max-width: 95%;
		max-height: 95%;
		object-fit: contain;
	}
</style>