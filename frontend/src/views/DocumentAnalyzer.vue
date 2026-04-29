<template>
  <div class="document-analyzer">
    <el-card class="input-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>智能文档分析器</span>
        </div>
        <p class="card-desc">上传长篇研报或合同，自动生成摘要、提取关键条款并输出为 JSON 或 Excel</p>
      </template>

      <el-form :model="form" label-position="top">
        <el-form-item label="文档内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="请粘贴待分析的文档内容..."
          />
        </el-form-item>

        <el-form-item label="文档类型">
          <el-select v-model="form.documentType" placeholder="选择文档类型（可选）" clearable style="width: 100%">
            <el-option label="合同" value="合同" />
            <el-option label="研报" value="研报" />
            <el-option label="协议" value="协议" />
            <el-option label="公告" value="公告" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="分析选项">
          <el-checkbox v-model="form.generateSummary">生成摘要</el-checkbox>
          <el-checkbox v-model="form.extractKeyClauses">提取关键条款</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="analyzing" @click="handleAnalyze">
            <el-icon><MagicStick /></el-icon>
            开始分析
          </el-button>
          <el-button :loading="exporting" :disabled="!result" @click="handleExportExcel">
            <el-icon><Download /></el-icon>
            导出 Excel
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="result" class="result-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><DataAnalysis /></el-icon>
          <span>分析结果</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="摘要" name="summary">
          <div class="result-section" v-if="result.summary">
            <h4>文档摘要</h4>
            <p class="result-text">{{ result.summary }}</p>
          </div>
          <el-empty v-else description="未生成摘要" />
        </el-tab-pane>

        <el-tab-pane label="关键条款" name="clauses">
          <div class="result-section" v-if="result.keyClauses && result.keyClauses.length">
            <el-table :data="result.keyClauses" stripe border style="width: 100%">
              <el-table-column prop="clauseType" label="条款类型" width="150" />
              <el-table-column prop="clauseContent" label="条款内容" min-width="300" />
              <el-table-column prop="clauseValue" label="条款数值" width="150" />
              <el-table-column prop="location" label="位置" width="120" />
            </el-table>
          </div>
          <el-empty v-else description="未提取关键条款" />
        </el-tab-pane>

        <el-tab-pane label="关键要点" name="points">
          <div class="result-section" v-if="result.keyPoints && result.keyPoints.length">
            <el-timeline>
              <el-timeline-item
                v-for="(point, index) in result.keyPoints"
                :key="index"
                :color="'#4f46e5'"
              >
                {{ point }}
              </el-timeline-item>
            </el-timeline>
          </div>
          <el-empty v-else description="未提取关键要点" />
        </el-tab-pane>

        <el-tab-pane label="风险评估" name="risk">
          <div class="result-section" v-if="result.riskAssessment">
            <el-alert
              :title="result.riskAssessment"
              :type="getRiskLevel(result.riskAssessment)"
              :closable="false"
              show-icon
            />
          </div>
          <el-empty v-else description="未进行风险评估" />
        </el-tab-pane>

        <el-tab-pane label="JSON 原始数据" name="json">
          <pre class="json-viewer">{{ JSON.stringify(result, null, 2) }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, MagicStick, Download, DataAnalysis } from '@element-plus/icons-vue'
import { analyzeDocument, exportExcel } from '../api'

const form = reactive({
  content: '',
  documentType: '',
  generateSummary: true,
  extractKeyClauses: true
})

const analyzing = ref(false)
const exporting = ref(false)
const result = ref(null)
const activeTab = ref('summary')

async function handleAnalyze() {
  if (!form.content.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }
  analyzing.value = true
  result.value = null
  try {
    const res = await analyzeDocument(form)
    result.value = res.data
    activeTab.value = 'summary'
    ElMessage.success('分析完成')
  } catch (e) {
    console.error(e)
  } finally {
    analyzing.value = false
  }
}

async function handleExportExcel() {
  exporting.value = true
  try {
    const res = await exportExcel(form)
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'analysis-result.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error(e)
  } finally {
    exporting.value = false
  }
}

function getRiskLevel(text) {
  if (!text) return 'info'
  if (text.includes('高') || text.includes('严重')) return 'error'
  if (text.includes('中') || text.includes('一般')) return 'warning'
  return 'success'
}
</script>

<style scoped>
.document-analyzer {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 960px;
}

.document-analyzer :deep(.el-card) {
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

.result-section h4 {
  margin-bottom: 0.8rem;
  color: #303133;
}

.result-text {
  line-height: 1.8;
  color: #606266;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #6366f1;
}

.json-viewer {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 1.2rem;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 0.85rem;
  line-height: 1.6;
}
</style>
