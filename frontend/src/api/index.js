import request from '../utils/request'

export function analyzeDocument(data) {
  return request({
    url: '/document/analyze',
    method: 'post',
    data
  })
}

export function analyzeFile(formData) {
  return request({
    url: '/document/analyze/file',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportExcel(data) {
  return request({
    url: '/document/analyze/export-excel',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

export function translate(data) {
  return request({
    url: '/translation/translate',
    method: 'post',
    data
  })
}

export function polish(data) {
  return request({
    url: '/translation/polish',
    method: 'post',
    data
  })
}

export function extractKeywords(data) {
  return request({
    url: '/keyword/extract',
    method: 'post',
    data
  })
}

export function rewriteText(data) {
  return request({
    url: '/rewrite',
    method: 'post',
    data
  })
}

export function chat(data) {
  return request({
    url: '/chat',
    method: 'post',
    data
  })
}

export function ingestDocument(data) {
  return request({
    url: '/rag/ingest',
    method: 'post',
    data
  })
}

export function askWithRag(data) {
  return request({
    url: '/rag/ask',
    method: 'post',
    data
  })
}

export function submitAsyncAnalysis(data) {
  return request({
    url: '/async/submit/analysis',
    method: 'post',
    data
  })
}

export function submitAsyncIngestion(data) {
  return request({
    url: '/async/submit/ingestion',
    method: 'post',
    data
  })
}

export function getTaskStatus(taskId) {
  return request({
    url: `/async/status/${taskId}`,
    method: 'get'
  })
}

export function extractSummary(data) {
  return request({
    url: '/extract/summary',
    method: 'post',
    data
  })
}

export function analyzeSentiment(data) {
  return request({
    url: '/extract/sentiment',
    method: 'post',
    data
  })
}
