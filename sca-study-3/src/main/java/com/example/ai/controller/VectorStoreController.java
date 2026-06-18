package com.example.ai.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 4.3 Vector Store — 向量存储。
 * <p>
 * 演示文本嵌入 → 存储 → 语义搜索的完整流程。
 * 使用 SimpleVectorStore（内存版），无需外部服务即可演示。
 * <p>
 * 接口：
 *   POST /ai/vector/store?text=...       添加文档到向量库
 *   GET  /ai/vector/search?query=...      按语义搜索
 *   GET  /ai/vector/list                  列出所有文档 ID
 *   DELETE /ai/vector/delete?id=...       按 ID 删除文档
 */
@RestController
@RequestMapping("/ai/vector")
public class VectorStoreController {

    private final SimpleVectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    public VectorStoreController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 添加文档到向量库
     * POST /ai/vector/store?text=Spring AI 是一个 Java AI 框架
     */
    @PostMapping("/store")
    public String store(@RequestParam(defaultValue = "Spring AI 是一个 Java AI 框架") String text) {
        String docId = UUID.randomUUID().toString().substring(0, 8);
        Document doc = new Document(docId, text, Map.of("source", "user-input"));
        vectorStore.add(List.of(doc));
        return "文档已存入，ID：%s，内容：%s".formatted(docId, text);
    }

    /**
     * 按语义搜索最相似的文档
     * GET /ai/vector/search?query=Java 框架有哪些
     */
    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "Java 框架") String query) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(5).similarityThreshold(0.5).build()
        );

        if (results.isEmpty()) {
            return "未找到与「%s」相关的文档".formatted(query);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== 搜索结果：「%s」===\n".formatted(query));
        for (int i = 0; i < results.size(); i++) {
            Document doc = results.get(i);
            sb.append("[%s] ID=%s，内容=%s，metadata=%s\n"
                    .formatted(i + 1, doc.getId(), doc.getText(), doc.getMetadata()));
        }
        return sb.toString();
    }

    /**
     * 列出所有已存储的文档
     * GET /ai/vector/list
     */
    @GetMapping("/list")
    public String list() {
        // SimpleVectorStore 没有直接提供列出所有文档的 API，
        // 这里用一个空查询 + 低阈值来获取全部
        List<Document> all = vectorStore.similaritySearch(
                SearchRequest.builder().query("").topK(100).similarityThreshold(0.0).build()
        );

        if (all.isEmpty()) {
            return "向量库为空，请先通过 POST /ai/vector/store 添加文档";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== 向量库中共 %s 条文档 ===\n".formatted(all.size()));
        for (int i = 0; i < all.size(); i++) {
            Document doc = all.get(i);
            sb.append("[%s] ID=%s，内容=%s\n".formatted(i + 1, doc.getId(), doc.getText()));
        }
        return sb.toString();
    }

    /**
     * 按 ID 删除文档
     * DELETE /ai/vector/delete?id=xxx
     */
    @DeleteMapping("/delete")
    public String delete(@RequestParam String id) {
        vectorStore.delete(List.of(id));
        return "文档 %s 已删除".formatted(id);
    }
}
