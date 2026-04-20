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
//引入文章页面
import ArticleList from '@/pages/ArticleList.vue'
import ArticleDetail from '@/pages/ArticleDetail.vue'

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
			title: '酒店信息 - 学术会议官网'
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
		path: '/register',
		name: 'Register',
		component: () => import('@/pages/Register.vue'),
		meta: {
			title: '参会注册'
		}
	},
	// {
	// 	path: '/login',
	// 	name: 'Login',
	// 	component: () => import('@/pages/Login.vue'),
	// 	meta: {
	// 		title: '用户登录'
	// 	}
	// },
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

// 路由守卫：动态修改页面标题
router.beforeEach((to) => {
	if (to.meta.title) {
		document.title = to.meta.title
	}
})

export default router