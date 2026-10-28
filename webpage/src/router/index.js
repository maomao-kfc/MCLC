/* 路由 */

import {
	createRouter,
	createWebHistory
} from 'vue-router'

import Home from '@/pages/Home.vue'
import Guests from '@/pages/Guests.vue'
import Schedule from '@/pages/Schedule.vue'
import Hotel from '@/pages/Hotel.vue'
import Faq from '@/pages/Faq.vue'
import PreMeeting from '@/pages/PreMeeting.vue'
import ArticleList from '@/pages/ArticleList.vue'
import ArticleDetail from '@/pages/ArticleDetail.vue'
import { useRegisterStore } from '@/stores/register'
import { ElMessage } from 'element-plus'

// 路由页面导入
const routes = [{
		path: '/',
		name: 'Home',
		component: () => import('@/pages/Home.vue'),
		meta: {
			title: '首页 - 学术会议官网'
		}
	},
	{
		path: '/guests',
		name: 'Guests',
		component: () => import('@/pages/Guests.vue'),
		meta: {
			title: '嘉宾介绍 - 学术会议官网'
		}
	},
	{
		path: '/schedule',
		name: 'Schedule',
		component: () => import('@/pages/Schedule.vue'),
		meta: {
			title: '会议日程 - 学术会议官网'
		}
	},
	{
		path: '/hotel',
		name: 'Hotel',
		component: () => import('@/pages/Hotel.vue'),
		meta: {
			title: '会议及住宿 - 学术会议官网'
		}
	},
	{
		path: '/faq',
		name: 'Faq',
		component: () => import('@/pages/Faq.vue'),
		meta: {
			title: '常见问题 - 学术会议官网'
		}
	},
	{
		path: '/pre-meeting',
		name: 'PreMeeting',
		component: () => import('@/pages/PreMeeting.vue'),
		meta: {
			title: '会前会 - 学术会议官网'
		}
	},
	{
		path: '/submit',
		name: 'Submit',
		component: () => import('@/pages/Submit.vue'),
		meta: {
			title: '投稿 - 学术会议官网'
		}
	},
	{
		path: '/first-draft',
		name: 'FirstDraft',
		component: () => import('@/pages/FirstDraft.vue'),
		meta: {
			title: '论文投稿 - 学术会议官网'
		}
	},
	{
		path: '/payment',
		name: 'Payment',
		component: () => import('@/pages/Payment.vue'),
		meta: {
			title: '支付 - 学术会议官网'
		}
	},
	{
		path: '/my-registration',
		name: 'MyRegistration',
		component: () => import('@/pages/MyRegistration.vue'),
		meta: {
			title: '我的注册 - 学术会议官网'
		}
	},
	{
		path: '/review-file',
		name: 'ReviewFile',
		component: () => import('@/pages/ReviewFile.vue'),
		meta: {
			title: '审稿决议 - 学术会议官网'
		}
	},
	{
		path: '/register',
		name: 'Register',
		component: () => import('@/pages/Register.vue'),
		meta: {
			title: '参会注册'
		},
		children: [
			{ path: '', redirect: '/register/enroll' },
			{ path: 'enroll', component: () => import('@/pages/register/Enroll.vue') },
			{ 
				path: 'consent', 
				component: () => import('@/pages/register/Consent.vue'),
				meta: { requiresEnroll: true }
			},
			{ path: 'contact', component: () => import('@/pages/register/Contact.vue') },
			{ path: 'survey', component: () => import('@/pages/register/Survey.vue') },
			{ path: 'signup', component: () => import('@/pages/register/Signup.vue') },
			{ path: 'skills', component: () => import('@/pages/register/Skills.vue') },
			{ path: 'expert', component: () => import('@/pages/register/Expert.vue') },
			{ path: 'confirm', component: () => import('@/pages/register/Confirm.vue') }
		]
	},
	{
		path: '/email-configuration',
		name: 'EmailConfiguration',
		component: () => import('@/pages/EmailConfiguration.vue'),
		meta: {
			title: '邮箱模板配置 - 学术会议官网'
		}
	},
	{
		path: '/article',
		name: 'ArticleList',
		component: ArticleList,
		meta: {
			title: '嘉宾演讲集锦 | MCLC 2026'
		}
	},
	//文章详情页动态路由
	{
		path: '/article/:id',
		name: 'ArticleDetail',
		component: ArticleDetail,
		meta: {
			title: '演讲详情 | MCLC 2026'
		}
	},
	// 404页面兜底
	{
		path: '/:pathMatch(.*)*',
		redirect: '/'
	}
]

const router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes,
	// 滚动行为：切换路由回到顶部
	scrollBehavior() {
		return {
			top: 0
		}
	}
})

// 路由守卫：动态修改页面标题和权限校验
router.beforeEach((to, from, next) => {
	if (to.meta.title) {
		document.title = to.meta.title
	}
	
	if (to.meta.requiresEnroll) {
		const registerStore = useRegisterStore()
		const enroll = registerStore.state.enroll
		if (!enroll.firstName || !enroll.lastName || !enroll.email) {
			ElMessage.error('请先完成报名信息')
			next('/register/enroll')
			return
		}
	}
	next()
})

export default router