<template>
  <div class="keyword-page">
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card shadow="never" class="input-card">
          <template #header>
            <div class="section-title">
              <el-icon><PriceTag /></el-icon>
              输入文本
            </div>
          </template>
          <el-form :model="form" label-position="top">
            <el-form-item>
              <div class="textarea-wrapper">
                <el-input
                  v-model="form.text"
                  type="textarea"
                  :rows="12"
                  placeholder="粘贴或输入需要提取关键词的文本..."
                  resize="none"
                />
                <div class="textarea-actions">
                  <el-button text size="small" @click="form.text = ''">
                    <el-icon><Delete /></el-icon>
                    清空
                  </el-button>
                </div>
              </div>
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="关键词数量">
                  <el-input-number v-model="form.maxKeywords" :min="3" :max="30" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="领域提示">
                  <el-select v-model="form.domain" placeholder="可选" clearable style="width:100%">
                    <el-option label="科技" value="科技" />
                    <el-option label="金融" value="金融" />
                    <el-option label="法律" value="法律" />
                    <el-option label="医疗" value="医疗" />
                    <el-option label="教育" value="教育" />
                    <el-option label="电商" value="电商" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label=" ">
                  <el-button
                    type="primary"
                    size="large"
                    :loading="loading"
                    @click="handleExtract"
                    style="width:100%"
                  >
                    <el-icon><Search /></el-icon>
                    提取关键词
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card v-if="!result" shadow="never" class="empty-card">
          <el-empty description="输入文本后点击提取关键词" />
        </el-card>

        <template v-if="result">
          <el-card shadow="never" class="result-card" v-if="result.topicSummary">
            <div class="section-title">
              <el-icon><Memo /></el-icon>
              主题概括
            </div>
            <p class="topic-text">{{ result.topicSummary }}</p>
            <el-tag
              v-if="result.sentiment"
              :type="sentimentType"
              size="small"
              round
              class="sentiment-tag"
            >
              情感倾向：{{ result.sentiment }}
            </el-tag>
          </el-card>

          <el-card shadow="never" class="result-card" v-if="result.keywords && result.keywords.length">
            <div class="section-title">
              <el-icon><PriceTag /></el-icon>
              关键词列表
            </div>
            <div class="keyword-cloud">
              <div
                v-for="(kw, i) in result.keywords"
                :key="i"
                class="keyword-item"
                :style="{ fontSize: getKeywordSize(kw.relevance) + 'px' }"
              >
                <span class="kw-text">{{ kw.keyword }}</span>
                <span class="kw-category">{{ kw.category }}</span>
                <el-progress
                  :percentage="kw.relevance * 100"
                  :stroke-width="4"
                  :show-text="false"
                  :color="getRelevanceColor(kw.relevance)"
                  class="kw-progress"
                />
              </div>
            </div>
          </el-card>
        </template>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { PriceTag, Search, Memo, Delete } from '@element-plus/icons-vue'
import { extractKeywords } from '../api'

const form = reactive({
  text: '',
  maxKeywords: 10,
  domain: ''
})

const loading = ref(false)
const result = ref(null)

const sentimentType = computed(() => {
  if (!result.value?.sentiment) return 'info'
  const s = result.value.sentiment
  if (s.includes('正面') || s.includes('积极')) return 'success'
  if (s.includes('负面') || s.includes('消极')) return 'danger'
  return 'warning'
})

async function handleExtract() {
  if (!form.text.trim()) {
    ElMessage.warning('请输入文本内容')
    return
  }
  loading.value = true
  result.value = null
  try {
    const res = await extractKeywords(form)
    result.value = res.data
    ElMessage.success('提取完成')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function getKeywordSize(relevance) {
  return 13 + relevance * 6
}

function getRelevanceColor(relevance) {
  if (relevance >= 0.8) return '#6366f1'
  if (relevance >= 0.5) return '#0ea5e9'
  return '#94a3b8'
}
</script>

<style scoped>
.keyword-page {
  width: 100%;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 0.95rem;
  color: #1a1a2e;
}

.input-card, .result-card, .empty-card {
  border: 1px solid #f0f0f0;
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: box-shadow var(--transition-normal);
}

.input-card:hover, .result-card:hover {
  box-shadow: var(--shadow-md);
}

.empty-card {
  min-height: 400px;
  display: flex;
  align-items: center;
}

.result-card {
  margin-bottom: 16px;
}

.topic-text {
  font-size: 1rem;
  color: #303133;
  line-height: 1.7;
  margin: 12px 0 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--primary-color);
  transition: background var(--transition-fast);
}

.topic-text:hover {
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
}

.sentiment-tag {
  margin-top: 8px;
}

.keyword-cloud {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.keyword-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  cursor: default;
}

.keyword-item:hover {
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  transform: translateX(4px);
  box-shadow: var(--shadow-sm);
}

.kw-text {
  font-weight: 600;
  color: #1a1a2e;
  min-width: 80px;
}

.kw-category {
  font-size: 0.75rem;
  color: #909399;
  background: white;
  padding: 2px 10px;
  border-radius: 4px;
  flex-shrink: 0;
  border: 1px solid #e5e7eb;
}

.kw-progress {
  flex: 1;
  min-width: 80px;
}

.textarea-wrapper {
  position: relative;
}

.textarea-wrapper :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  resize: none;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.textarea-wrapper :deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.textarea-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
  opacity: 0.6;
  transition: opacity var(--transition-fast);
}

.textarea-wrapper:hover .textarea-actions {
  opacity: 1;
}
</style>
