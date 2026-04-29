<template>
  <div class="chat-assistant">
    <el-card class="chat-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><ChatLineSquare /></el-icon>
          <span>AI 智能对话助手</span>
          <div class="header-actions">
            <el-switch
              v-model="useRag"
              active-text="RAG"
              inactive-text=""
              size="small"
              style="--el-switch-on-color: #6366f1"
            />
            <el-switch
              v-model="useStream"
              active-text="流式"
              inactive-text=""
              size="small"
              style="--el-switch-on-color: #10b981"
            />
          </div>
        </div>
        <p class="card-desc">多轮对话 · RAG 知识库检索 · 流式打字机输出 · Markdown 渲染</p>
      </template>

      <div class="chat-container">
        <div class="chat-messages" ref="messagesRef">
          <div v-if="messages.length === 0" class="welcome-section">
            <div class="welcome-icon">🤖</div>
            <h3>你好！我是 CGAD AI 助手</h3>
            <p>我可以帮你分析文档、翻译文本、润色文章，或回答各种问题。</p>
            <div class="quick-actions">
              <el-button round size="small" @click="sendQuickMessage('帮我分析一份合同的注意事项')">
                📄 分析合同注意事项
              </el-button>
              <el-button round size="small" @click="sendQuickMessage('跨境电商常用的英文术语有哪些？')">
                🌐 跨境电商术语
              </el-button>
              <el-button round size="small" @click="sendQuickMessage('请润色以下文本：本项目旨在提升数据处理效率')">
                ✨ 润色文本
              </el-button>
            </div>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', msg.role]"
          >
            <div class="message-avatar">
              {{ msg.role === 'user' ? '👤' : '🤖' }}
            </div>
            <div class="message-content">
              <div class="message-role">
                {{ msg.role === 'user' ? '你' : 'AI 助手' }}
                <el-tag v-if="msg.ragUsed" size="small" type="success" round class="rag-badge">RAG</el-tag>
              </div>
              <div v-if="msg.role === 'assistant'" class="message-text markdown-body" v-html="renderMarkdown(msg.content)"></div>
              <div v-else class="message-text">{{ msg.content }}</div>
            </div>
          </div>

          <div v-if="loading && !streamingContent" class="message-item assistant">
            <div class="message-avatar">🤖</div>
            <div class="message-content">
              <div class="message-role">AI 助手</div>
              <div class="message-text typing">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>

          <div v-if="streamingContent" class="message-item assistant">
            <div class="message-avatar">🤖</div>
            <div class="message-content">
              <div class="message-role">AI 助手 <span class="streaming-badge">生成中...</span></div>
              <div class="message-text markdown-body" v-html="renderMarkdown(streamingContent)"></div>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入你的问题..."
            resize="none"
            @keydown.enter.exact.prevent="handleSend"
          />
          <div class="input-actions">
            <span class="token-info" v-if="totalTokens > 0">
              Token 消耗：约 {{ totalTokens }}
            </span>
            <el-button type="primary" :loading="loading" @click="handleSend" :disabled="!inputText.trim()">
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
            <el-button @click="handleClear">
              <el-icon><Delete /></el-icon>
              清空
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineSquare, Promotion, Delete } from '@element-plus/icons-vue'
import { chat } from '../api'
import { marked } from 'marked'
import hljs from 'highlight.js'

const renderer = new marked.Renderer()
renderer.code = function(code, lang) {
  const language = lang || ''
  let highlighted
  try {
    if (language && hljs.getLanguage(language)) {
      highlighted = hljs.highlight(code, { language }).value
    } else {
      highlighted = hljs.highlightAuto(code).value
    }
  } catch {
    highlighted = code.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  }
  return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>`
}

marked.setOptions({
  renderer,
  breaks: true,
  gfm: true
})

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const totalTokens = ref(0)
const messagesRef = ref(null)
const useRag = ref(false)
const useStream = ref(true)
const streamingContent = ref('')

function renderMarkdown(content) {
  if (!content) return ''
  try {
    return marked.parse(content)
  } catch {
    return content.replace(/\n/g, '<br>')
  }
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true

  await nextTick()
  scrollToBottom()

  if (useStream.value) {
    await handleStreamChat(text)
  } else {
    await handleNormalChat(text)
  }
}

async function handleNormalChat(text) {
  try {
    const history = messages.value.slice(0, -1).map(m => ({
      role: m.role,
      content: m.content
    }))
    const res = await chat({
      messages: history,
      userMessage: text
    })
    messages.value.push({ role: 'assistant', content: res.data.reply })
    totalTokens.value += res.data.tokensUsed
  } catch (e) {
    console.error('Normal chat error:', e)
    messages.value.push({ role: 'assistant', content: '抱歉，发生了错误，请稍后重试。' })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

async function handleStreamChat(text) {
  streamingContent.value = ''

  try {
    if (useRag.value) {
      await streamWithRag(text)
    } else {
      await streamNormal(text)
    }
  } catch (e) {
    console.error('Stream chat error:', e)
    if (streamingContent.value) {
      messages.value.push({ role: 'assistant', content: streamingContent.value, ragUsed: useRag.value })
    } else {
      messages.value.push({ role: 'assistant', content: '抱歉，发生了错误，请稍后重试。' })
    }
  } finally {
    streamingContent.value = ''
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

async function streamNormal(text) {
  const history = messages.value.slice(0, -1).map(m => ({
    role: m.role,
    content: m.content
  }))

  const response = await fetch('/api/chat/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      messages: history,
      userMessage: text
    })
  })

  if (!response.ok) {
    const errText = await response.text()
    throw new Error(`HTTP ${response.status}: ${errText}`)
  }

  await processSSEStream(response)
}

async function streamWithRag(text) {
  const response = await fetch('/api/chat/stream/rag', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question: text })
  })

  if (!response.ok) {
    const errText = await response.text()
    throw new Error(`HTTP ${response.status}: ${errText}`)
  }

  await processSSEStream(response, true)
}

async function processSSEStream(response, ragUsed = false) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed) continue

      if (trimmed.startsWith('event:')) {
        continue
      }

      if (trimmed.startsWith('data:')) {
        const data = trimmed.slice(5).trim()
        if (!data) continue

        if (data === '[DONE]') continue

        try {
          const parsed = JSON.parse(data)
          if (parsed.tokens !== undefined) continue
          if (parsed.error) {
            throw new Error(parsed.error)
          }
          continue
        } catch (parseErr) {
          // not JSON, it's the actual text token
        }

        streamingContent.value += data
        await nextTick()
        scrollToBottom()
      }
    }
  }

  if (streamingContent.value) {
    messages.value.push({
      role: 'assistant',
      content: streamingContent.value,
      ragUsed
    })
  }
}

function sendQuickMessage(text) {
  inputText.value = text
  handleSend()
}

function handleClear() {
  messages.value = []
  totalTokens.value = 0
  streamingContent.value = ''
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-assistant {
  display: flex;
  flex-direction: column;
  max-width: 960px;
}

.chat-assistant :deep(.el-card) {
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

.header-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-desc {
  color: #909399;
  font-size: 0.85rem;
  margin-top: 4px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 300px);
  min-height: 500px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  text-align: center;
}

.welcome-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.welcome-section h3 {
  color: #1a1a2e;
  margin-bottom: 0.5rem;
}

.welcome-section p {
  color: #909399;
  margin-bottom: 1.5rem;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.message-item {
  display: flex;
  gap: 0.75rem;
  max-width: 85%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.assistant {
  align-self: flex-start;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
  background: #f8fafc;
}

.message-item.user .message-avatar {
  background: #eef2ff;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.message-role {
  font-size: 0.75rem;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 6px;
}

.message-item.user .message-role {
  text-align: right;
}

.rag-badge {
  font-size: 0.65rem;
}

.streaming-badge {
  color: #10b981;
  font-size: 0.7rem;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.message-text {
  padding: 0.75rem 1rem;
  border-radius: 12px;
  line-height: 1.7;
  font-size: 0.95rem;
}

.message-item.user .message-text {
  background: #6366f1;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item.assistant .message-text {
  background: #f8fafc;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.markdown-body :deep(h1), .markdown-body :deep(h2), .markdown-body :deep(h3) {
  margin: 0.8em 0 0.4em;
  color: #1a1a2e;
}

.markdown-body :deep(h1) { font-size: 1.3em; }
.markdown-body :deep(h2) { font-size: 1.15em; }
.markdown-body :deep(h3) { font-size: 1.05em; }

.markdown-body :deep(p) {
  margin: 0.5em 0;
}

.markdown-body :deep(ul), .markdown-body :deep(ol) {
  padding-left: 1.5em;
  margin: 0.5em 0;
}

.markdown-body :deep(code) {
  background: rgba(0, 0, 0, 0.06);
  padding: 0.15em 0.4em;
  border-radius: 4px;
  font-size: 0.88em;
  font-family: 'Fira Code', monospace;
}

.message-item.user .markdown-body :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.markdown-body :deep(pre) {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 1em;
  border-radius: 8px;
  overflow-x: auto;
  margin: 0.8em 0;
}

.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
  font-size: 0.85em;
}

.markdown-body :deep(blockquote) {
  border-left: 3px solid #6366f1;
  padding: 0.5em 1em;
  margin: 0.5em 0;
  background: rgba(99, 102, 241, 0.05);
  border-radius: 0 6px 6px 0;
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 0.8em 0;
}

.markdown-body :deep(th), .markdown-body :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 0.5em 0.8em;
  text-align: left;
}

.markdown-body :deep(th) {
  background: #f8fafc;
  font-weight: 600;
}

.typing {
  display: flex;
  gap: 0.3rem;
  padding: 1rem;
}

.typing .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #c0c4cc;
  animation: typing 1.4s infinite both;
}

.typing .dot:nth-child(2) { animation-delay: 0.2s; }
.typing .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.chat-input-area {
  border-top: 1px solid #ebeef5;
  padding-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.5rem;
}

.token-info {
  color: #909399;
  font-size: 0.8rem;
  margin-right: auto;
}
</style>
