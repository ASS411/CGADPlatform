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
              <el-input
                v-model="form.text"
                type="textarea"
                :rows="12"
                placeholder="粘贴或输入需要提取关键词的文本..."
              />
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
                  <el-button type="primary" :loading="loading" @click="handleExtract" style="width:100%">
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
import { PriceTag, Search, Memo } from '@element-plus/icons-vue'
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
  max-width: 1100px;
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
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #6366f1;
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
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
  transition: background 0.2s;
}

.keyword-item:hover {
  background: #f0f4ff;
}

.kw-text {
  font-weight: 600;
  color: #1a1a2e;
  min-width: 80px;
}

.kw-category {
  font-size: 0.75rem;
  color: #909399;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
  flex-shrink: 0;
}

.kw-progress {
  flex: 1;
  min-width: 80px;
}
</style>
