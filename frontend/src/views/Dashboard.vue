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
      </div>
    </div>

    <div class="feature-grid">
      <div class="feature-card wide" @click="$router.push('/analysis')">
        <div class="feature-icon" style="background: linear-gradient(135deg, #ec4899, #f472b6)">
          <el-icon :size="28"><DataAnalysis /></el-icon>
        </div>
        <div class="feature-info">
          <h3>文档分析仪表盘</h3>
          <p>综合分析：关键词云 + 情感雷达图 + RAG 知识库摄入，可视化看板一目了然</p>
        </div>
        <el-icon class="feature-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="feature-card" @click="$router.push('/document')">
        <div class="feature-icon" style="background: linear-gradient(135deg, #6366f1, #818cf8)">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="feature-info">
          <h3>智能文档分析</h3>
          <p>上传研报或合同，自动生成摘要、提取关键条款，支持 JSON/Excel 导出</p>
        </div>
        <el-icon class="feature-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="feature-card" @click="$router.push('/translation')">
        <div class="feature-icon" style="background: linear-gradient(135deg, #0ea5e9, #38bdf8)">
          <el-icon :size="28"><ChatDotRound /></el-icon>
        </div>
        <div class="feature-info">
          <h3>多语言翻译润色</h3>
          <p>针对跨境电商等专业领域进行高质量翻译，支持多种输出风格</p>
        </div>
        <el-icon class="feature-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="feature-card" @click="$router.push('/keyword')">
        <div class="feature-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24)">
          <el-icon :size="28"><PriceTag /></el-icon>
        </div>
        <div class="feature-info">
          <h3>关键词提取</h3>
          <p>智能提取文本关键词与标签，分析主题和情感倾向</p>
        </div>
        <el-icon class="feature-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="feature-card" @click="$router.push('/rewrite')">
        <div class="feature-icon" style="background: linear-gradient(135deg, #10b981, #34d399)">
          <el-icon :size="28"><EditPen /></el-icon>
        </div>
        <div class="feature-info">
          <h3>AI 文本改写</h3>
          <p>支持简化、扩写、正式化、口语化、创意化等多种改写风格</p>
        </div>
        <el-icon class="feature-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="feature-card wide" @click="$router.push('/chat')">
        <div class="feature-icon" style="background: linear-gradient(135deg, #8b5cf6, #a78bfa)">
          <el-icon :size="28"><ChatLineSquare /></el-icon>
        </div>
        <div class="feature-info">
          <h3>AI 智能对话</h3>
          <p>与 AI 进行多轮对话，支持文档分析、翻译润色、知识问答等场景，上下文连续理解</p>
        </div>
        <el-icon class="feature-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="tech-stack">
      <h3>技术栈</h3>
      <div class="tech-tags">
        <el-tag effect="plain" round>Spring Boot 3.2</el-tag>
        <el-tag effect="plain" round type="success">LangChain4j</el-tag>
        <el-tag effect="plain" round type="warning">DashScope / OpenAI</el-tag>
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
  max-width: 960px;
  margin: 0 auto;
}

.welcome-banner {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  border-radius: 16px;
  padding: 40px 36px;
  color: white;
  position: relative;
  overflow: hidden;
  margin-bottom: 28px;
}

.welcome-text h1 {
  font-size: 1.6rem;
  font-weight: 700;
  margin-bottom: 8px;
}

.welcome-text p {
  opacity: 0.75;
  font-size: 0.95rem;
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
  opacity: 0.08;
  background: white;
}

.c1 {
  width: 200px;
  height: 200px;
  top: -60px;
  right: -40px;
}

.c2 {
  width: 120px;
  height: 120px;
  bottom: -30px;
  right: 80px;
}

.c3 {
  width: 80px;
  height: 80px;
  top: 20px;
  right: 160px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}

.feature-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid #f0f0f0;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  border-color: transparent;
}

.feature-card.wide {
  grid-column: span 2;
}

.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.feature-info {
  flex: 1;
  min-width: 0;
}

.feature-info h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.feature-info p {
  font-size: 0.85rem;
  color: #909399;
  line-height: 1.5;
}

.feature-arrow {
  color: #c0c4cc;
  flex-shrink: 0;
  transition: transform 0.2s;
}

.feature-card:hover .feature-arrow {
  transform: translateX(4px);
  color: #6366f1;
}

.tech-stack {
  background: white;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #f0f0f0;
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

@media (max-width: 768px) {
  .feature-grid {
    grid-template-columns: 1fr;
  }
  .feature-card.wide {
    grid-column: span 1;
  }
}
</style>
