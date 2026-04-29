<template>
  <div class="translation-page">
    <el-card class="input-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><ChatDotRound /></el-icon>
          <span>多语言翻译与润色工具</span>
        </div>
        <p class="card-desc">针对特定行业术语进行高质量的文本翻译或润色，支持跨境电商等专业领域</p>
      </template>

      <el-form :model="form" label-position="top">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="功能模式">
              <el-radio-group v-model="form.mode">
                <el-radio-button value="translate">翻译</el-radio-button>
                <el-radio-button value="polish">润色</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="源语言">
              <el-select v-model="form.sourceLanguage" style="width: 100%">
                <el-option label="中文" value="中文" />
                <el-option label="英语" value="英语" />
                <el-option label="日语" value="日语" />
                <el-option label="韩语" value="韩语" />
                <el-option label="法语" value="法语" />
                <el-option label="德语" value="德语" />
                <el-option label="西班牙语" value="西班牙语" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目标语言">
              <el-select v-model="form.targetLanguage" style="width: 100%">
                <el-option label="英语" value="英语" />
                <el-option label="中文" value="中文" />
                <el-option label="日语" value="日语" />
                <el-option label="韩语" value="韩语" />
                <el-option label="法语" value="法语" />
                <el-option label="德语" value="德语" />
                <el-option label="西班牙语" value="西班牙语" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="行业领域">
          <el-select v-model="form.domain" placeholder="选择行业领域（可选）" clearable style="width: 100%">
            <el-option label="跨境电商" value="跨境电商" />
            <el-option label="法律" value="法律" />
            <el-option label="金融" value="金融" />
            <el-option label="医疗" value="医疗" />
            <el-option label="科技" value="科技" />
            <el-option label="通用" value="通用" />
          </el-select>
        </el-form-item>

        <el-form-item label="输出风格">
          <el-radio-group v-model="form.style">
            <el-radio-button value="professional">专业正式</el-radio-button>
            <el-radio-button value="casual">轻松口语</el-radio-button>
            <el-radio-button value="academic">学术严谨</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="输入文本">
          <el-input
            v-model="form.text"
            type="textarea"
            :rows="8"
            placeholder="请输入待翻译或润色的文本..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            <el-icon><MagicStick /></el-icon>
            {{ form.mode === 'translate' ? '开始翻译' : '开始润色' }}
          </el-button>
          <el-button @click="handleSwap">
            <el-icon><Sort /></el-icon>
            交换语言
          </el-button>
          <el-button @click="handleClear">
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="result" class="result-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Finished /></el-icon>
          <span>{{ form.mode === 'translate' ? '翻译结果' : '润色结果' }}</span>
        </div>
      </template>

      <div class="result-container">
        <div class="result-block">
          <div class="result-label">
            <el-tag type="info" size="small">原文 ({{ form.sourceLanguage }})</el-tag>
          </div>
          <p class="result-text original">{{ form.text }}</p>
        </div>

        <div class="result-arrow">
          <el-icon :size="24" color="#4f46e5"><Right /></el-icon>
        </div>

        <div class="result-block">
          <div class="result-label">
            <el-tag type="success" size="small">{{ form.mode === 'translate' ? '译文' : '润色' }} ({{ form.targetLanguage }})</el-tag>
          </div>
          <p class="result-text translated">{{ result.translatedText }}</p>
        </div>
      </div>

      <div v-if="result.glossaryNotes && result.glossaryNotes.length" class="glossary-section">
        <el-divider content-position="left">术语注释</el-divider>
        <el-tag v-for="(note, i) in result.glossaryNotes" :key="i" type="warning" size="small" class="glossary-tag">
          {{ note }}
        </el-tag>
      </div>

      <div class="result-meta">
        <span v-if="result.detectedSourceLanguage">检测到源语言：{{ result.detectedSourceLanguage }}</span>
        <span v-if="result.confidence">置信度：{{ (result.confidence * 100).toFixed(0) }}%</span>
      </div>

      <div class="result-actions">
        <el-button size="small" @click="handleCopy">
          <el-icon><CopyDocument /></el-icon>
          复制结果
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, MagicStick, Sort, Delete,
  Finished, Right, CopyDocument
} from '@element-plus/icons-vue'
import { translate, polish } from '../api'

const form = reactive({
  mode: 'translate',
  sourceLanguage: '中文',
  targetLanguage: '英语',
  domain: '',
  style: 'professional',
  text: ''
})

const loading = ref(false)
const result = ref(null)

async function handleSubmit() {
  if (!form.text.trim()) {
    ElMessage.warning('请输入文本内容')
    return
  }
  loading.value = true
  result.value = null
  try {
    const apiFn = form.mode === 'translate' ? translate : polish
    const payload = {
      sourceText: form.text,
      sourceLanguage: form.sourceLanguage,
      targetLanguage: form.targetLanguage,
      domain: form.domain,
      style: form.style
    }
    const res = await apiFn(payload)
    result.value = res.data
    ElMessage.success(form.mode === 'translate' ? '翻译完成' : '润色完成')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSwap() {
  const temp = form.sourceLanguage
  form.sourceLanguage = form.targetLanguage
  form.targetLanguage = temp
}

function handleClear() {
  form.text = ''
  result.value = null
}

async function handleCopy() {
  try {
    await navigator.clipboard.writeText(result.value.translatedText)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.translation-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 960px;
}

.translation-page :deep(.el-card) {
  border: 1px solid #f0f0f0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: #1a1a2e;
}

.card-desc {
  color: #909399;
  font-size: 0.85rem;
  margin-top: 4px;
}

.result-container {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}

.result-block {
  flex: 1;
}

.result-label {
  margin-bottom: 0.5rem;
}

.result-text {
  line-height: 1.8;
  padding: 1rem;
  border-radius: 8px;
  min-height: 80px;
}

.result-text.original {
  background: #f8fafc;
  color: #606266;
  border-left: 3px solid #909399;
}

.result-text.translated {
  background: #f0fdf4;
  color: #303133;
  font-weight: 500;
  border-left: 3px solid #10b981;
}

.result-arrow {
  display: flex;
  align-items: center;
  padding-top: 2rem;
}

.result-actions {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
}

.glossary-section {
  margin-top: 1rem;
}

.glossary-tag {
  margin: 0.25rem;
}

.result-meta {
  margin-top: 0.8rem;
  display: flex;
  gap: 1.5rem;
  color: #909399;
  font-size: 0.85rem;
}

@media (max-width: 768px) {
  .result-container {
    flex-direction: column;
  }
  .result-arrow {
    justify-content: center;
    transform: rotate(90deg);
  }
}
</style>
