<template>
  <el-container class="app-layout">
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="app-sidebar">
      <div class="sidebar-header" @click="isCollapsed = !isCollapsed">
        <div class="logo-icon" v-if="isCollapsed">C</div>
        <div class="logo-full" v-else>
          <span class="logo-text">CGAD</span>
          <span class="logo-sub">AI Platform</span>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        class="sidebar-menu"
        background-color="transparent"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>控制台</template>
        </el-menu-item>

        <el-sub-menu index="tools">
          <template #title>
            <el-icon><SetUp /></el-icon>
            <span>AI 工具箱</span>
          </template>
          <el-menu-item index="/analysis">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>分析仪表盘</template>
          </el-menu-item>
          <el-menu-item index="/document">
            <el-icon><Document /></el-icon>
            <template #title>文档分析</template>
          </el-menu-item>
          <el-menu-item index="/translation">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>翻译润色</template>
          </el-menu-item>
          <el-menu-item index="/keyword">
            <el-icon><PriceTag /></el-icon>
            <template #title>关键词提取</template>
          </el-menu-item>
          <el-menu-item index="/rewrite">
            <el-icon><EditPen /></el-icon>
            <template #title>文本改写</template>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/chat">
          <el-icon><ChatLineSquare /></el-icon>
          <template #title>AI 对话</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="collapse-btn" @click="isCollapsed = !isCollapsed">
          <el-icon :size="16">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="app-topbar" height="56px">
        <div class="topbar-left">
          <h2 class="page-title">{{ currentTitle }}</h2>
        </div>
        <div class="topbar-right">
          <el-tag type="success" effect="dark" round size="small">
            <el-icon><Connection /></el-icon>
            LangChain4j
          </el-tag>
        </div>
      </el-header>

      <el-main class="app-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Odometer, SetUp, DataAnalysis, Document, ChatDotRound, PriceTag,
  EditPen, ChatLineSquare, Fold, Expand, Connection
} from '@element-plus/icons-vue'

const route = useRoute()
const isCollapsed = ref(false)
const activeMenu = computed(() => route.path)

const titleMap = {
  '/dashboard': '控制台',
  '/analysis': '文档分析仪表盘',
  '/document': '智能文档分析',
  '/translation': '多语言翻译与润色',
  '/keyword': '智能关键词提取',
  '/rewrite': 'AI 文本改写',
  '/chat': 'AI 智能对话'
}

const currentTitle = computed(() => titleMap[route.path] || 'CGAD Platform')
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

:root {
  --primary-color: #6366f1;
  --primary-light: #818cf8;
  --primary-dark: #4f46e5;
  --success-color: #10b981;
  --warning-color: #f59e0b;
  --danger-color: #ef4444;
  --info-color: #0ea5e9;
  --bg-color: #f0f2f5;
  --card-bg: #ffffff;
  --sidebar-width: 220px;
  --sidebar-collapsed: 64px;
  --header-height: 56px;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.08);
  --transition-fast: 0.15s ease;
  --transition-normal: 0.25s ease;
  --transition-slow: 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.12);
  border-radius: 3px;
  transition: background var(--transition-fast);
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: var(--bg-color);
  overflow: hidden;
  color: #1a1a2e;
}

.app-layout {
  height: 100vh;
}

.app-sidebar {
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 50%, #334155 100%);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-slow);
  overflow: hidden;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
}

.sidebar-header {
  padding: 20px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition-fast);
}

.sidebar-header:hover {
  background: rgba(255, 255, 255, 0.04);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 1.1rem;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.4);
}

.logo-full {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.logo-text {
  font-size: 1.4rem;
  font-weight: 700;
  color: white;
  letter-spacing: -0.5px;
}

.logo-sub {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 500;
}

.sidebar-menu {
  flex: 1;
  border-right: none !important;
  padding: 8px;
}

.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu :deep(.el-sub-menu__title) {
  border-radius: var(--radius-sm);
  margin: 2px 0;
  height: 44px;
  line-height: 44px;
  transition: all var(--transition-fast);
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-sub-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  transform: translateX(2px);
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light)) !important;
  color: white !important;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.sidebar-menu .el-sub-menu.is-active > :deep(.el-sub-menu__title) {
  color: white !important;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.collapse-btn {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: white;
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.app-topbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid #ebeef5;
  box-shadow: var(--shadow-sm);
}

.page-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--primary-dark);
  letter-spacing: -0.3px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.topbar-right .el-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.app-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: var(--bg-color);
  display: flex;
  flex-direction: column;
}

.app-content > * {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.app-content > .chat-assistant,
.app-content > .translation-page,
.app-content > .document-analyzer,
.app-content > .keyword-page,
.app-content > .rewrite-page,
.app-content > .analysis-dashboard {
  max-width: 960px;
}

.app-content > .analysis-dashboard {
  max-width: 1200px;
}

.app-content > .dashboard {
  max-width: 960px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-normal);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: all var(--transition-slow);
}

.slide-enter-from {
  transform: translateX(-20px);
  opacity: 0;
}

.slide-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}
</style>
