<template>
  <div class="analysis-dashboard">
    <el-row :gutter="16" class="input-row">
      <el-col :span="16">
        <el-card shadow="never" class="input-card">
          <template #header>
            <div class="section-title">
              <el-icon><Document /></el-icon>
              文档分析仪表盘
            </div>
          </template>
          <el-input
            v-model="content"
            type="textarea"
            :rows="6"
            placeholder="粘贴待分析的文档内容..."
          />
          <div class="action-bar">
            <el-button type="primary" :loading="analyzing" @click="handleFullAnalysis">
              <el-icon><DataAnalysis /></el-icon>
              综合分析
            </el-button>
            <el-button :loading="ingesting" @click="handleIngest">
              <el-icon><Upload /></el-icon>
              摄入知识库
            </el-button>
            <el-button @click="handleClear">清空</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="status-card">
          <template #header>
            <div class="section-title">
              <el-icon><Monitor /></el-icon>
              分析状态
            </div>
          </template>
          <div v-if="!hasResult" class="empty-state">
            <el-empty description="提交文档后查看分析结果" :image-size="60" />
          </div>
          <div v-else class="status-info">
            <div class="status-item">
              <span class="label">文档类型</span>
              <el-tag size="small">{{ analysisResult?.documentType || '未知' }}</el-tag>
            </div>
            <div class="status-item">
              <span class="label">重要程度</span>
              <el-rate :model-value="summaryResult?.importanceLevel || 0" disabled />
            </div>
            <div class="status-item">
              <span class="label">情感倾向</span>
              <el-tag :type="sentimentTagType" size="small">{{ sentimentResult?.sentiment || '-' }}</el-tag>
            </div>
            <div class="status-item">
              <span class="label">关键词数</span>
              <span class="value">{{ keywordResult?.keywords?.length || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <template v-if="hasResult">
      <el-row :gutter="16" class="chart-row">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="section-title">
                <el-icon><PriceTag /></el-icon>
                关键词云
              </div>
            </template>
            <div ref="wordCloudRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="section-title">
                <el-icon><TrendCharts /></el-icon>
                情感分析雷达图
              </div>
            </template>
            <div ref="radarRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="detail-row">
        <el-col :span="12">
          <el-card shadow="never" class="detail-card">
            <template #header>
              <div class="section-title">
                <el-icon><Memo /></el-icon>
                文档摘要
              </div>
            </template>
            <div class="summary-content" v-if="analysisResult?.summary">
              {{ analysisResult.summary }}
            </div>
            <div class="topics-list" v-if="summaryResult?.keyTopics?.length">
              <el-tag v-for="topic in summaryResult.keyTopics" :key="topic" size="small" round class="topic-tag">
                {{ topic }}
              </el-tag>
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="never" class="detail-card">
            <template #header>
              <div class="section-title">
                <el-icon><Warning /></el-icon>
                关键条款与风险
              </div>
            </template>
            <div class="clauses-list" v-if="analysisResult?.keyClauses?.length">
              <div v-for="(clause, i) in analysisResult.keyClauses" :key="i" class="clause-item">
                <el-tag size="small" :type="getClauseTagType(clause.clauseType)">{{ clause.clauseType }}</el-tag>
                <span class="clause-content">{{ clause.clauseContent }}</span>
              </div>
            </div>
            <div class="risk-section" v-if="analysisResult?.riskAssessment">
              <h4>风险评估</h4>
              <p>{{ analysisResult.riskAssessment }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document, DataAnalysis, Upload, Monitor, PriceTag,
  TrendCharts, Memo, Warning
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import 'echarts-wordcloud'
import { analyzeDocument, extractKeywords } from '../api'
import request from '../utils/request'

const content = ref('')
const analyzing = ref(false)
const ingesting = ref(false)
const analysisResult = ref(null)
const keywordResult = ref(null)
const summaryResult = ref(null)
const sentimentResult = ref(null)

const wordCloudRef = ref(null)
const radarRef = ref(null)
let wordCloudChart = null
let radarChart = null

const hasResult = computed(() => analysisResult.value || keywordResult.value)

const sentimentTagType = computed(() => {
  const s = sentimentResult.value?.sentiment || ''
  if (s.includes('正面') || s.includes('积极')) return 'success'
  if (s.includes('负面') || s.includes('消极')) return 'danger'
  return 'warning'
})

async function handleFullAnalysis() {
  if (!content.value.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }
  analyzing.value = true
  try {
    const [analysisRes, keywordRes, summaryRes, sentimentRes] = await Promise.allSettled([
      analyzeDocument({ content: content.value, documentType: '' }),
      extractKeywords({ text: content.value, maxKeywords: 20, domain: '' }),
      request({ url: '/extract/summary', method: 'post', data: { content: content.value } }),
      request({ url: '/extract/sentiment', method: 'post', data: { content: content.value } })
    ])

    if (analysisRes.status === 'fulfilled') analysisResult.value = analysisRes.value.data
    if (keywordRes.status === 'fulfilled') keywordResult.value = keywordRes.value.data
    if (summaryRes.status === 'fulfilled') summaryResult.value = summaryRes.value.data
    if (sentimentRes.status === 'fulfilled') sentimentResult.value = sentimentRes.value.data

    ElMessage.success('综合分析完成')
    await nextTick()
    renderCharts()
  } catch (e) {
    console.error(e)
  } finally {
    analyzing.value = false
  }
}

async function handleIngest() {
  if (!content.value.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }
  ingesting.value = true
  try {
    await request({ url: '/rag/ingest', method: 'post', data: { content: content.value, sourceName: 'dashboard-upload' } })
    ElMessage.success('文档已摄入向量知识库')
  } catch (e) {
    console.error(e)
  } finally {
    ingesting.value = false
  }
}

function handleClear() {
  content.value = ''
  analysisResult.value = null
  keywordResult.value = null
  summaryResult.value = null
  sentimentResult.value = null
  wordCloudChart?.dispose()
  radarChart?.dispose()
  wordCloudChart = null
  radarChart = null
}

function renderCharts() {
  renderWordCloud()
  renderRadar()
}

function renderWordCloud() {
  if (!wordCloudRef.value || !keywordResult.value?.keywords?.length) return
  wordCloudChart?.dispose()
  wordCloudChart = echarts.init(wordCloudRef.value)

  const data = keywordResult.value.keywords.map(kw => ({
    name: kw.keyword,
    value: Math.round(kw.relevance * 100),
    textStyle: {
      color: getRandomColor()
    }
  }))

  wordCloudChart.setOption({
    tooltip: { show: true, formatter: '{b}: {c}' },
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      left: 'center',
      top: 'center',
      width: '90%',
      height: '90%',
      sizeRange: [14, 48],
      rotationRange: [-30, 30],
      rotationStep: 15,
      gridSize: 8,
      drawOutOfBound: false,
      layoutAnimation: true,
      textStyle: {
        fontFamily: 'Inter, sans-serif',
        fontWeight: 'bold'
      },
      emphasis: {
        textStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(99, 102, 241, 0.5)',
          color: '#6366f1'
        }
      },
      data
    }]
  })
}

function renderRadar() {
  if (!radarRef.value) return
  radarChart?.dispose()
  radarChart = echarts.init(radarRef.value)

  const emotions = ['正面', '中性', '负面']
  const emotionValues = [0, 0, 0]
  const sentiment = sentimentResult.value?.sentiment || ''
  const confidence = sentimentResult.value?.confidence || 0.5

  if (sentiment.includes('正面') || sentiment.includes('积极')) {
    emotionValues[0] = Math.round(confidence * 100)
    emotionValues[1] = Math.round((1 - confidence) * 40)
    emotionValues[2] = Math.round((1 - confidence) * 20)
  } else if (sentiment.includes('负面') || sentiment.includes('消极')) {
    emotionValues[2] = Math.round(confidence * 100)
    emotionValues[1] = Math.round((1 - confidence) * 40)
    emotionValues[0] = Math.round((1 - confidence) * 20)
  } else {
    emotionValues[1] = Math.round(confidence * 100)
    emotionValues[0] = Math.round((1 - confidence) * 50)
    emotionValues[2] = Math.round((1 - confidence) * 50)
  }

  const categories = ['客观性', '专业性', '信息密度', '可读性', '情感强度']
  const categoryValues = [70, 75, 80, 65, emotionValues[0] - emotionValues[2] + 50]

  radarChart.setOption({
    tooltip: {},
    legend: {
      data: ['情感维度', '文档质量'],
      bottom: 0,
      textStyle: { fontSize: 12 }
    },
    radar: {
      indicator: [
        ...emotions.map(e => ({ name: e, max: 100 })),
        ...categories.map(c => ({ name: c, max: 100 }))
      ],
      shape: 'polygon',
      splitNumber: 4,
      axisName: { color: '#606266', fontSize: 11 },
      splitArea: {
        areaStyle: {
          color: ['rgba(99, 102, 241, 0.02)', 'rgba(99, 102, 241, 0.05)',
                  'rgba(99, 102, 241, 0.08)', 'rgba(99, 102, 241, 0.12)']
        }
      }
    },
    series: [{
      type: 'radar',
      data: [
        {
          value: [...emotionValues, ...categoryValues.slice(0, 2)],
          name: '情感维度',
          areaStyle: { color: 'rgba(239, 68, 68, 0.15)' },
          lineStyle: { color: '#ef4444' },
          itemStyle: { color: '#ef4444' }
        },
        {
          value: [...Array(3).fill(0), ...categoryValues],
          name: '文档质量',
          areaStyle: { color: 'rgba(99, 102, 241, 0.15)' },
          lineStyle: { color: '#6366f1' },
          itemStyle: { color: '#6366f1' }
        }
      ]
    }]
  })
}

function getRandomColor() {
  const colors = ['#6366f1', '#8b5cf6', '#0ea5e9', '#10b981', '#f59e0b', '#ef4444', '#ec4899', '#14b8a6']
  return colors[Math.floor(Math.random() * colors.length)]
}

function getClauseTagType(type) {
  if (!type) return 'info'
  if (type.includes('金额') || type.includes('付款')) return 'warning'
  if (type.includes('违约') || type.includes('风险')) return 'danger'
  if (type.includes('日期') || type.includes('期限')) return 'success'
  return 'info'
}

onBeforeUnmount(() => {
  wordCloudChart?.dispose()
  radarChart?.dispose()
})
</script>

<style scoped>
.analysis-dashboard {
  max-width: 1200px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 0.95rem;
  color: #1a1a2e;
}

.input-card, .status-card, .chart-card, .detail-card {
  border: 1px solid #f0f0f0;
}

.action-bar {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-item .label {
  color: #909399;
  font-size: 0.85rem;
}

.status-item .value {
  font-weight: 600;
  color: #1a1a2e;
}

.chart-row {
  margin-top: 16px;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.detail-row {
  margin-top: 16px;
}

.summary-content {
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #6366f1;
  line-height: 1.8;
  color: #303133;
  font-size: 0.9rem;
}

.topics-list {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.topic-tag {
  cursor: default;
}

.clauses-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.clause-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 6px;
}

.clause-content {
  font-size: 0.85rem;
  color: #606266;
  line-height: 1.5;
}

.risk-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.risk-section h4 {
  font-size: 0.9rem;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.risk-section p {
  font-size: 0.85rem;
  color: #606266;
  line-height: 1.6;
  padding: 8px 12px;
  background: #fef2f2;
  border-radius: 6px;
  border-left: 3px solid #ef4444;
}
</style>
