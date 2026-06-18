package com.example.ai.controller;

import com.example.ai.advisor.LoggingAdvisor;
import com.example.ai.advisor.SourceTracingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 5.3 自定义 Advisor 演示。
 * <p>
 * Advisor 的执行顺序（洋葱模型，先注册的在内层）：
 *   外: LoggingAdvisor        → 记录请求和回复
 *       SourceTracingAdvisor  → 追加引用来源
 *   内: QuestionAnswerAdvisor → RAG 检索（用 SourceTracing 修改后的文本）
 */
@RestController
@RequestMapping("/ai/rag")
public class CustomAdvisorController {

    private final ChatClient chatClient;

    public CustomAdvisorController(
            ChatClient.Builder builder,
            VectorStore vectorStore) {

        this.chatClient = builder
                .defaultSystem("你是一个 AI 知识助手，根据提供的参考资料回答问题。" +
                        "如果参考资料中没有相关信息，请如实说明。")
                .defaultAdvisors(
                        new LoggingAdvisor(),
                        new SourceTracingAdvisor(vectorStore),
                        new QuestionAnswerAdvisor(vectorStore,
                                SearchRequest.builder()
                                        .topK(3)
                                        .similarityThreshold(0.5)
                                        .build())
                        )
                .build();
    }

    /**
     * 使用自定义 Advisor 的 RAG 问答
     * GET /ai/rag/custom?question=Spring AI Alibaba 有哪些模型
     */
    @GetMapping("/custom")
    public String custom(@RequestParam(defaultValue = "Spring AI Alibaba 有哪些模型") String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
