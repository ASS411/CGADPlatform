<template>
  <div class="dashboard">
    <div class="welcome-banner">
      <div class="welcome-text">
        <h1>欢迎使用 CGAD Platform</h1>
        <p>基于 LangChain4j 的智能内容生成与数据处理平台</p>
      </div>
      <div class="welcome-decoration">
        <div class="decoration-circle c1"></div>
        <div class="decoration-circle c2"></div>
        <div class="decoration-circle c3"></div>
        <div class="floating-dots">
          <span v-for="i in 6" :key="i" :style="{ animationDelay: `${i * 0.3}s` }"></span>
        </div>
      </div>
    </div>

    <transition-group name="stagger" tag="div" class="feature-grid">
      <div class="feature-card wide" @click="$router.push('/analysis')" :key="1">
        <div class="feature-icon" style="background: linear-gradient(135deg, #ec4899, #f472b6)">
          <el-icon :size="28"><DataAnalysis /></el-icon>
        </div>
        <div class="feature-info">
          <h3>文档分析仪表盘</h3>
          <p>综合分析：关键词云 + 情感雷达图 + RAG 知识库摄入，可视化看板一目了然</p>
        </div>
        <div class="feature-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="feature-card" @click="$router.push('/document')" :key="2">
        <div class="feature-icon" style="background: linear-gradient(135deg, #6366f1, #818cf8)">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="feature-info">
          <h3>智能文档分析</h3>
          <p>上传研报或合同，自动生成摘要、提取关键条款，支持 JSON/Excel 导出</p>
        </div>
        <div class="feature-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="feature-card" @click="$router.push('/translation')" :key="3">
        <div class="feature-icon" style="background: linear-gradient(135deg, #0ea5e9, #38bdf8)">
          <el-icon :size="28"><ChatDotRound /></el-icon>
        </div>
        <div class="feature-info">
          <h3>多语言翻译润色</h3>
          <p>针对跨境电商等专业领域进行高质量翻译，支持多种输出风格</p>
        </div>
        <div class="feature-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="feature-card" @click="$router.push('/keyword')" :key="4">
        <div class="feature-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24)">
          <el-icon :size="28"><PriceTag /></el-icon>
        </div>
        <div class="feature-info">
          <h3>关键词提取</h3>
          <p>智能提取文本关键词与标签，分析主题和情感倾向</p>
        </div>
        <div class="feature-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="feature-card" @click="$router.push('/rewrite')" :key="5">
        <div class="feature-icon" style="background: linear-gradient(135deg, #10b981, #34d399)">
          <el-icon :size="28"><EditPen /></el-icon>
        </div>
        <div class="feature-info">
          <h3>AI 文本改写</h3>
          <p>支持简化、扩写、正式化、口语化、创意化等多种改写风格</p>
        </div>
        <div class="feature-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="feature-card wide" @click="$router.push('/chat')" :key="6">
        <div class="feature-icon" style="background: linear-gradient(135deg, #8b5cf6, #a78bfa)">
          <el-icon :size="28"><ChatLineSquare /></el-icon>
        </div>
        <div class="feature-info">
          <h3>AI 智能对话</h3>
          <p>与 AI 进行多轮对话，支持文档分析、翻译润色、知识问答等场景，上下文连续理解</p>
        </div>
        <div class="feature-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </transition-group>

    <div class="tech-stack">
      <h3>技术栈</h3>
      <div class="tech-tags">
        <el-tag effect="plain" round>Spring Boot 3.2</el-tag>
        <el-tag effect="plain" round type="success">LangChain4j</el-tag>
        <el-tag effect="plain" round type="warning">DashScope / DeepSeek</el-tag>
        <el-tag effect="plain" round type="danger">Vue 3</el-tag>
        <el-tag effect="plain" round type="info">Element Plus</el-tag>
        <el-tag effect="plain" round>Apache POI</el-tag>
        <el-tag effect="plain" round type="success">PGVector</el-tag>
        <el-tag effect="plain" round type="warning">Redis</el-tag>
        <el-tag effect="plain" round>ECharts</el-tag>
        <el-tag effect="plain" round type="danger">SSE</el-tag>
        <el-tag effect="plain" round type="info">AiServices</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  Document, ChatDotRound, PriceTag, EditPen,
  ChatLineSquare, ArrowRight, DataAnalysis
} from '@element-plus/icons-vue'
</script>

<style scoped>
.dashboard {
  width: 100%;
}

.welcome-banner {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 40%, #334155 100%);
  border-radius: var(--radius-lg);
  padding: 40px 36px;
  color: white;
  position: relative;
  overflow: hidden;
  margin-bottom: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.welcome-text h1 {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #fff, #cbd5e1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-text p {
  opacity: 0.75;
  font-size: 0.95rem;
  max-width: 500px;
}

.welcome-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 300px;
  height: 100%;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
  background: white;
  animation: float 6s ease-in-out infinite;
}

.c1 {
  width: 200px;
  height: 200px;
  top: -60px;
  right: -40px;
  animation-delay: 0s;
}

.c2 {
  width: 120px;
  height: 120px;
  bottom: -30px;
  right: 80px;
  animation-delay: -2s;
}

.c3 {
  width: 80px;
  height: 80px;
  top: 20px;
  right: 160px;
  animation-delay: -4s;
}

.floating-dots {
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
}

.floating-dots span {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(99, 102, 241, 0.6);
  border-radius: 50%;
  animation: floatDot 3s ease-in-out infinite;
}

.floating-dots span:nth-child(1) { top: 20%; right: 30%; }
.floating-dots span:nth-child(2) { top: 40%; right: 60%; }
.floating-dots span:nth-child(3) { top: 60%; right: 20%; }
.floating-dots span:nth-child(4) { top: 30%; right: 70%; }
.floating-dots span:nth-child(5) { top: 70%; right: 50%; }
.floating-dots span:nth-child(6) { top: 50%; right: 80%; }

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-10px) scale(1.05); }
}

@keyframes floatDot {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.3; }
  50% { transform: translateY(-15px) scale(1.5); opacity: 0.8; }
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}

.feature-card {
  background: white;
  border-radius: var(--radius-md);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid #f0f0f0;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.02), rgba(139, 92, 246, 0.02));
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
  border-color: transparent;
}

.feature-card:hover::before {
  opacity: 1;
}

.feature-card.wide {
  grid-column: span 2;
}

.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  transition: transform var(--transition-normal);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-card:hover .feature-icon {
  transform: scale(1.05) rotate(-5deg);
}

.feature-info {
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.feature-info h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 6px;
  transition: color var(--transition-fast);
}

.feature-card:hover .feature-info h3 {
  color: var(--primary-color);
}

.feature-info p {
  font-size: 0.85rem;
  color: #909399;
  line-height: 1.5;
}

.feature-arrow {
  color: #c0c4cc;
  flex-shrink: 0;
  transition: all var(--transition-normal);
  position: relative;
  z-index: 1;
}

.feature-card:hover .feature-arrow {
  transform: translateX(6px);
  color: var(--primary-color);
}

.tech-stack {
  background: white;
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid #f0f0f0;
  transition: all var(--transition-normal);
}

.tech-stack:hover {
  box-shadow: var(--shadow-md);
}

.tech-stack h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 12px;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stagger-move {
  transition: transform var(--transition-normal);
}

.stagger-enter-active {
  transition: all var(--transition-slow);
  transition-delay: calc(0.08s * var(--index, 0));
}

.stagger-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

@media (max-width: 768px) {
  .feature-grid {
    grid-template-columns: 1fr;
  }
  .feature-card.wide {
    grid-column: span 1;
  }
  .welcome-text h1 {
    font-size: 1.4rem;
  }
}
</style>
