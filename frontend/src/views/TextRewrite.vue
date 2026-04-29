<template>
  <div class="rewrite-page">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never" class="input-card">
          <template #header>
            <div class="section-title">
              <el-icon><EditPen /></el-icon>
              原文
            </div>
          </template>
          <el-form :model="form" label-position="top">
            <el-form-item>
              <el-input
                v-model="form.sourceText"
                type="textarea"
                :rows="10"
                placeholder="输入需要改写的文本..."
              />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="改写风格">
                  <el-select v-model="form.style" style="width:100%">
                    <el-option label="简化 - 去繁就简" value="simplify" />
                    <el-option label="扩写 - 补充细节" value="expand" />
                    <el-option label="正式 - 书面用语" value="formal" />
                    <el-option label="口语 - 轻松自然" value="casual" />
                    <el-option label="创意 - 新颖独特" value="creative" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="目标受众">
                  <el-select v-model="form.audience" style="width:100%">
                    <el-option label="大众读者" value="general" />
                    <el-option label="专业人士" value="professional" />
                    <el-option label="学生" value="student" />
                    <el-option label="高管" value="executive" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="额外要求（可选）">
              <el-input
                v-model="form.additionalRequirements"
                placeholder="例如：保留所有数据引用、不超过200字..."
              />
            </el-form-item>
            <el-button type="primary" :loading="loading" @click="handleRewrite">
              <el-icon><MagicStick /></el-icon>
              开始改写
            </el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card v-if="!result" shadow="never" class="empty-card">
          <el-empty description="配置改写选项后点击开始" />
        </el-card>

        <template v-if="result">
          <el-card shadow="never" class="result-card">
            <div class="section-title">
              <el-icon><Finished /></el-icon>
              改写结果
              <el-tag v-if="result.qualityScore" :type="qualityType" size="small" round class="score-tag">
                质量 {{ result.qualityScore }}分
              </el-tag>
            </div>
            <div class="rewrite-result">
              {{ result.rewrittenText }}
            </div>
            <div class="result-actions">
              <el-button size="small" @click="handleCopy">
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
            </div>
          </el-card>

          <el-card shadow="never" class="result-card" v-if="result.changeSummary || result.changePoints?.length">
            <div class="section-title">
              <el-icon><List /></el-icon>
              修改说明
            </div>
            <p v-if="result.changeSummary" class="change-summary">{{ result.changeSummary }}</p>
            <div v-if="result.changePoints?.length" class="change-points">
              <div v-for="(point, i) in result.changePoints" :key="i" class="change-point">
                <el-icon color="#6366f1"><Check /></el-icon>
                <span>{{ point }}</span>
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
import { EditPen, MagicStick, Finished, CopyDocument, List, Check } from '@element-plus/icons-vue'
import { rewriteText } from '../api'

const form = reactive({
  sourceText: '',
  style: 'simplify',
  audience: 'general',
  additionalRequirements: ''
})

const loading = ref(false)
const result = ref(null)

const qualityType = computed(() => {
  const s = result.value?.qualityScore || 0
  if (s >= 80) return 'success'
  if (s >= 60) return 'warning'
  return 'danger'
})

async function handleRewrite() {
  if (!form.sourceText.trim()) {
    ElMessage.warning('请输入原文内容')
    return
  }
  loading.value = true
  result.value = null
  try {
    const res = await rewriteText(form)
    result.value = res.data
    ElMessage.success('改写完成')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleCopy() {
  try {
    await navigator.clipboard.writeText(result.value.rewrittenText)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.rewrite-page {
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

.score-tag {
  margin-left: auto;
}

.rewrite-result {
  margin-top: 12px;
  padding: 16px;
  background: #f0fdf4;
  border-radius: 8px;
  border-left: 3px solid #10b981;
  line-height: 1.8;
  color: #303133;
  font-size: 0.95rem;
}

.result-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.change-summary {
  margin: 12px 0 8px;
  color: #606266;
  font-size: 0.9rem;
  line-height: 1.6;
}

.change-points {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.change-point {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 0.9rem;
  color: #606266;
  line-height: 1.5;
}
</style>
