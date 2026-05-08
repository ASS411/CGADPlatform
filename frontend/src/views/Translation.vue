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
          <div class="textarea-wrapper">
            <el-input
              v-model="form.text"
              type="textarea"
              :rows="8"
              placeholder="请输入待翻译或润色的文本..."
              resize="none"
            />
            <div class="textarea-actions">
              <el-button text size="small" @click="handleSwap">
                <el-icon><Sort /></el-icon>
                交换语言
              </el-button>
              <el-button text size="small" @click="handleClear">
                <el-icon><Delete /></el-icon>
                清空
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleSubmit"
            style="width: 200px"
          >
            <el-icon><MagicStick /></el-icon>
            {{ form.mode === 'translate' ? '开始翻译' : '开始润色' }}
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
}

.translation-page :deep(.el-card) {
  border: 1px solid #f0f0f0;
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: box-shadow var(--transition-normal);
}

.translation-page :deep(.el-card:hover) {
  box-shadow: var(--shadow-md);
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
  align-items: stretch;
  gap: 1.25rem;
  position: relative;
}

.result-block {
  flex: 1;
  transition: transform var(--transition-normal);
}

.result-block:hover {
  transform: translateY(-2px);
}

.result-label {
  margin-bottom: 0.5rem;
}

.result-text {
  line-height: 1.8;
  padding: 1.25rem;
  border-radius: var(--radius-sm);
  min-height: 80px;
  transition: all var(--transition-fast);
  position: relative;
}

.result-text.original {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  color: #606266;
  border-left: 3px solid #94a3b8;
}

.result-text.original:hover {
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
}

.result-text.translated {
  background: linear-gradient(135deg, #f0fdf4, #ecfdf5);
  color: #303133;
  font-weight: 500;
  border-left: 3px solid var(--success-color);
}

.result-text.translated:hover {
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
}

.result-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 2rem;
  color: var(--primary-color);
  animation: arrowPulse 2s ease-in-out infinite;
}

@keyframes arrowPulse {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.1); opacity: 1; }
}

.result-actions {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
}

.result-actions .el-button {
  transition: all var(--transition-fast);
}

.result-actions .el-button:hover {
  transform: translateY(-1px);
}

.glossary-section {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px dashed #e5e7eb;
}

.glossary-tag {
  margin: 0.3rem;
  transition: transform var(--transition-fast);
}

.glossary-tag:hover {
  transform: scale(1.05);
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
    padding-top: 0;
  }
  .result-block:hover {
    transform: translateY(-1px);
  }
}
</style>
