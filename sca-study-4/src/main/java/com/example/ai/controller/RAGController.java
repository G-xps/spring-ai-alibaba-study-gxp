package com.example.ai.controller;

import com.example.ai.service.DocumentService;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 5.1 ETL 管道 — 文档读取 → 分割 → 嵌入 → 存储。
 * <p>
 * 将 knowledge 目录下的文档加载到向量库中，
 * 支持手动添加和搜索预览。
 */
@RestController
@RequestMapping("/ai/rag")
public class RAGController {

    private final DocumentService documentService;

    public RAGController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 启动时自动加载知识库
     */
    @PostConstruct
    public void init() {
        documentService.loadKnowledgeBase();
    }

    /**
     * 手动添加文档到知识库
     * POST /ai/rag/document?content=Spring AI 是一个 Java AI 框架
     */
    @PostMapping("/document")
    public String addDocument(@RequestParam(defaultValue = "Spring AI Alibaba 是一个 Java AI 框架") String content) {
        String docId = UUID.randomUUID().toString().substring(0, 8);
        documentService.addDocument(docId, content);
        return "文档已存入，ID：%s，内容：%s".formatted(docId, content);
    }

    /**
     * 搜索知识库
     * GET /ai/rag/search?query=Spring AI 有哪些功能
     */
    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "Spring AI 有哪些功能") String query) {
        List<Document> results = documentService.search(query, 5);
        if (results.isEmpty()) {
            return "未找到与「%s」相关的内容".formatted(query);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== 搜索结果：「%s」===\n".formatted(query));
        for (int i = 0; i < results.size(); i++) {
            Document doc = results.get(i);
            sb.append("[%s] 来源=%s，相似度=%.2f\n  内容：%s\n\n"
                    .formatted(i + 1, doc.getMetadata().get("source"), doc.getScore(), doc.getText()));
        }
        return sb.toString();
    }

    /**
     * 查看当前知识库中的所有文档
     * GET /ai/rag/documents
     */
    @GetMapping("/documents")
    public String documents() {
        List<Document> all = documentService.listAll();
        if (all.isEmpty()) {
            return "知识库为空，请先 POST /ai/rag/document 添加文档";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== 知识库中共 %s 条文档片段 ===\n\n".formatted(all.size()));
        for (int i = 0; i < all.size(); i++) {
            Document doc = all.get(i);
            sb.append("[%s] ID=%s，来源=%s\n  内容：%s\n\n"
                    .formatted(i + 1, doc.getId(), doc.getMetadata().get("source"), doc.getText()));
        }
        return sb.toString();
    }
}
