import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '控制台' }
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: () => import('../views/AnalysisDashboard.vue'),
    meta: { title: '文档分析仪表盘' }
  },
  {
    path: '/document',
    name: 'Document',
    component: () => import('../views/DocumentAnalyzer.vue'),
    meta: { title: '智能文档分析' }
  },
  {
    path: '/translation',
    name: 'Translation',
    component: () => import('../views/Translation.vue'),
    meta: { title: '多语言翻译与润色' }
  },
  {
    path: '/keyword',
    name: 'Keyword',
    component: () => import('../views/KeywordExtraction.vue'),
    meta: { title: '智能关键词提取' }
  },
  {
    path: '/rewrite',
    name: 'Rewrite',
    component: () => import('../views/TextRewrite.vue'),
    meta: { title: 'AI 文本改写' }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/ChatAssistant.vue'),
    meta: { title: 'AI 智能对话' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title} - CGAD Platform`
  next()
})

export default router
