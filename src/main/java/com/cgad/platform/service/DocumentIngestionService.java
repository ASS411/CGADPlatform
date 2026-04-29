package com.cgad.platform.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 文档摄入服务
 *
 * 递归字符切片策略（Recursive Character Text Splitter）：
 *   - 优先在段落边界切分
 *   - 段落过长时在换行符切分
 *   - 仍过长时在句号/空格切分
 *   - 每个切片有 overlap 重叠区域，保证语义连续性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP_SIZE = 100;

    public void ingestDocument(String content, String documentName) {
        log.info("Ingesting document: {}, content length: {}", documentName, content.length());
        Document document = Document.from(content);
        ingestDocuments(List.of(document));
        log.info("Document ingested successfully: {}", documentName);
    }

    public void ingestDocuments(List<Document> documents) {
        DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, OVERLAP_SIZE);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);
        log.info("Ingested {} documents", documents.size());
    }

    public void ingestText(String text, String sourceName) {
        Document document = Document.from(text);
        document.metadata().put("source", sourceName);

        DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, OVERLAP_SIZE);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);
        log.info("Ingested text from source: {}, length: {}", sourceName, text.length());
    }
}
