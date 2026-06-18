package com.example.ai.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 文档服务 — 实现 RAG 的 ETL 管道。
 * <p>
 * 读取 knowledge 目录下的文档 → 分割 → 嵌入 → 存入向量库。
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final SimpleVectorStore vectorStore;

    public DocumentService(SimpleVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 启动时自动从 classpath 加载知识库文档。
     */
    @PostConstruct
    public void loadKnowledgeBase() {
        log.info("开始加载知识库文档...");

        for (String name : List.of("knowledge/spring-ai-alibaba.txt", "knowledge/ai-models.txt", "knowledge/rag-intro.txt")) {
            log.info("读取文档：{}", name);
            try {
                TextReader reader = new TextReader("classpath:" + name);
                reader.getCustomMetadata().put("source", name);
                List<Document> docs = reader.get();

                TokenTextSplitter splitter = TokenTextSplitter.builder()
                        .withChunkSize(200)
                        .build();
                List<Document> split = splitter.apply(docs);
                vectorStore.add(split);
                log.info("文档 {} 已处理，拆分为 {} 个片段", name, split.size());
            } catch (Exception e) {
                log.warn("读取文档 {} 失败：{}", name, e.getMessage());
            }
        }

        log.info("知识库加载完成");
    }

    public void addDocument(String id, String content) {
        Document doc = new Document(id, content, Map.of("source", "api-input"));
        vectorStore.add(List.of(doc));
    }

    public List<Document> search(String query, int topK) {
        return vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(0.5)
                        .build());
    }

    public List<Document> listAll() {
        return vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.builder()
                        .query("")
                        .topK(100)
                        .similarityThreshold(0.0)
                        .build());
    }
}
