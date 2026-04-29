package com.cgad.platform.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PGVector 向量存储配置
 *
 * RAG（Retrieval Augmented Generation）的核心组件：
 *   - EmbeddingStore：向量存储，用于存储和检索文档的向量表示
 *   - PGVector：PostgreSQL 的向量扩展，支持高维向量的相似度搜索
 *   - 工作流程：文档 → 切片 → 向量化 → 存入 PGVector → 查询时检索相关片段
 *
 * 向量搜索原理：
 *   - 文本通过 Embedding 模型转换为高维向量（如384维）
 *   - 语义相近的文本，向量距离也相近
 *   - 查询时将问题也转为向量，找到最近的文档片段
 */
@Slf4j
@Configuration
public class PgVectorConfig {

    @Value("${langchain4j.pgvector.host:localhost}")
    private String host;

    @Value("${langchain4j.pgvector.port:5432}")
    private int port;

    @Value("${langchain4j.pgvector.database:cgad}")
    private String database;

    @Value("${langchain4j.pgvector.user:postgres}")
    private String user;

    @Value("${langchain4j.pgvector.password:postgres}")
    private String password;

    @Value("${langchain4j.pgvector.table:document_embeddings}")
    private String table;

    @Value("${langchain4j.pgvector.dimension:384}")
    private int dimension;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("Initializing PGVector EmbeddingStore: {}:{}/{}", host, port, database);
        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(user)
                .password(password)
                .table(table)
                .dimension(dimension)
                .dropTableFirst(false)
                .build();
    }
}
