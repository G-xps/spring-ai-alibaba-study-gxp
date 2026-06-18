package com.example.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * VectorStore 配置 — 将 SimpleVectorStore 暴露为 Bean。
 * <p>
 * DocumentService 和 RagChatController 共用同一个向量库实例，
 * 确保文档存入后能在问答时检索到。
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public VectorStore vectorStore(SimpleVectorStore simpleVectorStore) {
        return simpleVectorStore;
    }
}
