package com.example.ai.controller;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 第三阶段：多模态 — EmbeddingModel 嵌入模型演示。
 * <p>
 * 将文本转换为向量（Embedding），用于语义搜索、文本相似度比较等场景。
 */
@RestController
@RequestMapping("/ai/embedding")
public class EmbeddingModelController {

    private final EmbeddingModel embeddingModel;

    public EmbeddingModelController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 单条文本嵌入
     * GET /ai/embedding/single?text=Spring AI Alibaba 是一个框架
     */
    @GetMapping("/single")
    public String single(@RequestParam(defaultValue = "Spring AI Alibaba 是一个框架") String text) {
        float[] vector = embeddingModel.embed(text);
        List<Float> list = new ArrayList<>();
        for (float v : vector) {
            list.add(v);
        }

        return """
        原文：%s
        向量维度：%s
        全部值：%s
        """.formatted(text, vector.length, list);
    }

    /**
     * 批量文本嵌入
     * GET /ai/embedding/batch?text1=今天天气很好&text2=明天可能会下雨&text3=Spring Boot 是 Java 框架
     */
    @GetMapping("/batch")
    public String batch(
            @RequestParam(defaultValue = "今天天气很好") String text1,
            @RequestParam(defaultValue = "明天可能会下雨") String text2,
            @RequestParam(defaultValue = "Spring Boot 是 Java 框架") String text3) {

        List<String> texts = List.of(text1, text2, text3);
        EmbeddingResponse response = embeddingModel.embedForResponse(texts);
        StringBuilder sb = new StringBuilder();

        sb.append("=== 批量嵌入结果 ===\n");

        response.getResults().forEach(embedding -> {
            int idx = embedding.getIndex();
            float[] output = embedding.getOutput();

            List<Float> list = new ArrayList<>();
            for (float v : output) {
                list.add(v);
            }

            sb.append("文本[%s]「%s」：向量维度 %s，值 %s\n\n"
                    .formatted(idx, texts.get(idx), output.length, list));
        });

        return sb.toString();
    }

    /**
     * 获取向量维度
     * GET /ai/embedding/dimensions
     */
    @GetMapping("/dimensions")
    public String dimensions() {
        int dim = embeddingModel.dimensions();
        return "当前嵌入模型的向量维度为：" + dim;
    }
}
