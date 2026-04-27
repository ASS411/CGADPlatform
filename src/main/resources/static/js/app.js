const API_BASE = '';

const loadingOverlay = document.getElementById('loading-overlay');

function showLoading() {
    loadingOverlay.style.display = 'flex';
}

function hideLoading() {
    loadingOverlay.style.display = 'none';
}

function showTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(section => {
        section.classList.remove('active');
    });
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.getElementById(`${tabName}-section`).classList.add('active');
        }


document.getElementById('analyze-btn').addEventListener('click', analyzeDocument);
document.getElementById('export-btn').addEventListener('click', exportExcel);
document.getElementById('translate-btn').addEventListener('click', translateText);
document.getElementById('copy-btn').addEventListener('click', copyResult);
document.getElementById('swap-lang').addEventListener('click', swapLanguages);

document.getElementById('source-text').addEventListener('input', function() {
    document.getElementById('char-count').textContent = this.value.length + ' 字符';
});

let lastAnalysisResult = null;

async function analyzeDocument() {
    const content = document.getElementById('doc-content').value.trim();
    if (!content) {
        alert('请输入文档内容');
        return;
    }

    const request = {
        content: content,
        documentType: document.getElementById('doc-type').value,
        extractKeyClauses: document.getElementById('extract-clauses').checked,
        generateSummary: document.getElementById('generate-summary').checked
    };

    showLoading();
    try {
        const response = await fetch(API_BASE + '/api/document/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });

        const result = await response.json();

        if (result.code === 200) {
            lastAnalysisResult = result.data;
            displayAnalysisResult(result.data);
            document.getElementById('export-btn').disabled = false;
        } else {
            alert('分析失败: ' + result.message);
        }
    } catch (error) {
        alert('请求失败: ' + error.message);
    } finally {
        hideLoading();
    }
}

function displayAnalysisResult(data) {
    const resultCard = document.getElementById('doc-result');
    resultCard.style.display = 'block';

    document.getElementById('result-summary').textContent = data.summary || '无';
    document.getElementById('result-type').textContent = data.documentType || '未识别';
    document.getElementById('result-risk').textContent = data.riskAssessment || '无';

    const pointsList = document.getElementById('result-points');
    pointsList.innerHTML = '';
    if (data.keyPoints && data.keyPoints.length > 0) {
        data.keyPoints.forEach(point => {
            const li = document.createElement('li');
            li.textContent = point;
            pointsList.appendChild(li);
        });
    } else {
        pointsList.innerHTML = '<li>无关键要点</li>';
    }

    const tbody = document.querySelector('#clauses-table tbody');
    tbody.innerHTML = '';
    if (data.keyClauses && data.keyClauses.length > 0) {
        data.keyClauses.forEach(clause => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${clause.clauseType || '-'}</td>
                <td>${clause.clauseContent || '-'}</td>
                <td>${clause.clauseValue || '-'}</td>
                <td>${clause.location || '-'}</td>
            `;
            tbody.appendChild(tr);
        });
    } else {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">无关键条款</td></tr>';
    }

    resultCard.scrollIntoView({ behavior: 'smooth' });
}

async function exportExcel() {
    if (!lastAnalysisResult) {
        alert('请先分析文档');
        return;
    }

    showLoading();
    try {
        const response = await fetch(API_BASE + '/api/document/analyze/export-excel', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                content: document.getElementById('doc-content').value,
                documentType: document.getElementById('doc-type').value
            })
        });

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'analysis-result.xlsx';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
    } catch (error) {
        alert('导出失败: ' + error.message);
    } finally {
        hideLoading();
    }
}

async function translateText() {
    const sourceText = document.getElementById('source-text').value.trim();
    if (!sourceText) {
        alert('请输入需要翻译的文本');
        return;
    }

    const polishMode = document.getElementById('polish-mode').checked;
    const endpoint = polishMode ? '/api/translation/polish' : '/api/translation/translate';

    const request = {
        sourceText: sourceText,
        sourceLanguage: document.getElementById('source-lang').value,
        targetLanguage: document.getElementById('target-lang').value,
        domain: document.getElementById('domain').value,
        style: document.getElementById('style').value
    };

    showLoading();
    try {
        const response = await fetch(API_BASE + endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });

        const result = await response.json();

        if (result.code === 200) {
            document.getElementById('target-text').value = result.data.translatedText || '';
            
            if (result.data.detectedSourceLanguage) {
                document.getElementById('detected-lang').textContent = 
                    '检测到: ' + result.data.detectedSourceLanguage;
            }

            displayGlossaryNotes(result.data.glossaryNotes);
            document.getElementById('copy-btn').disabled = false;
        } else {
            alert('翻译失败: ' + result.message);
        }
    } catch (error) {
        alert('请求失败: ' + error.message);
    } finally {
        hideLoading();
    }
}

function displayGlossaryNotes(notes) {
    const notesCard = document.getElementById('glossary-notes');
    const notesList = document.getElementById('notes-list');

    if (notes && notes.length > 0) {
        notesCard.style.display = 'block';
        notesList.innerHTML = '';
        notes.forEach(note => {
            const li = document.createElement('li');
            li.textContent = note;
            notesList.appendChild(li);
        });
    } else {
        notesCard.style.display = 'none';
    }
}

function copyResult() {
    const targetText = document.getElementById('target-text').value;
    if (!targetText) return;

    navigator.clipboard.writeText(targetText).then(() => {
        const btn = document.getElementById('copy-btn');
        const originalText = btn.textContent;
        btn.textContent = '已复制!';
        setTimeout(() => {
            btn.textContent = originalText;
        }, 2000);
    }).catch(err => {
        alert('复制失败: ' + err);
    });
}

function swapLanguages() {
    const sourceLang = document.getElementById('source-lang');
    const targetLang = document.getElementById('target-lang');
    
    if (sourceLang.value === 'auto') {
        alert('自动检测模式下无法交换语言');
        return;
    }

    const temp = sourceLang.value;
    sourceLang.value = targetLang.value;
    targetLang.value = temp;

    const sourceText = document.getElementById('source-text');
    const targetText = document.getElementById('target-text');
    const tempText = sourceText.value;
    sourceText.value = targetText.value;
    targetText.value = tempText;

    document.getElementById('char-count').textContent = sourceText.value.length + ' 字符';
}
