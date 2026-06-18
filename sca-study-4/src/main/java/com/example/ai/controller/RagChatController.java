package com.example.ai.controller;

import com.example.ai.service.DocumentService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

/**
 * 5.2 RAG 问答 — QuestionAnswerAdvisor 实现检索增强生成。
 * <p>
 * 用户提问时，自动从 VectorStore 检索相关文档，组装到 Prompt 中，
 * 让模型基于检索到的知识回答问题。
 */
@RestController
@RequestMapping("/ai/rag")
public class RagChatController {

    private final ChatClient chatClient;
    private final ChatClient chatMemoryClient;
    private final DocumentService documentService;

    public RagChatController(
            ChatClient.Builder builder,
            VectorStore vectorStore,
            DocumentService documentService,
            InMemoryChatMemory chatMemory) {

        // 不带记忆的 RAG
        this.chatClient = builder
                .defaultSystem("你是一个 AI 知识助手，根据提供的参考资料回答问题。" +
                        "如果参考资料中没有相关信息，请如实说明。")
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore,
                        SearchRequest.builder()
                                .topK(3)
                                .similarityThreshold(0.5)
                                .build()))
                .build();

        // 带记忆的 RAG：QuestionAnswerAdvisor + MessageChatMemoryAdvisor 组合
        this.chatMemoryClient = builder
                .defaultSystem("你是一个 AI 知识助手，根据提供的参考资料回答问题。" +
                        "如果参考资料中没有相关信息，请如实说明。")
                .defaultAdvisors(
                        new QuestionAnswerAdvisor(vectorStore,
                                SearchRequest.builder()
                                        .topK(3)
                                        .similarityThreshold(0.5)
                                        .build()),
                        new MessageChatMemoryAdvisor(chatMemory))
                .build();

        this.documentService = documentService;
    }

    /**
     * RAG 问答 — 基于知识库回答
     * GET /ai/rag/chat?question=Spring AI Alibaba 是什么
     */
    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "Spring AI Alibaba 是什么") String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * RAG 问答 + 多轮对话记忆
     * GET /ai/rag/chat-memory?conversationId=test&question=Spring AI Alibaba 是什么
     */
    @GetMapping("/chat-memory")
    public String chatWithMemory(
            @RequestParam(defaultValue = "default") String conversationId,
            @RequestParam(defaultValue = "Spring AI Alibaba 是什么") String question) {
        return chatMemoryClient.prompt()
                .user(question)
                .advisors(a -> a
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .content();
    }
}
