package com.example.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 4.2 Chat Memory — 对话记忆。
 * <p>
 * 演示带记忆的多轮对话：同一 conversationId 自动维护上下文，
 * 不同 conversationId 会话隔离。
 */
@RestController
@RequestMapping("/ai/chat")
public class ChatMemoryController {

    /**
     * 内存会话存储，每个 conversationId 对应一个 ChatClient。
     */
    private final Map<String, ChatClient> sessionMap = new ConcurrentHashMap<>();

    private final ChatClient.Builder builder;
    private final InMemoryChatMemory chatMemory;
    private final ChatClient chatClientV2;

    public ChatMemoryController(ChatClient.Builder builder, InMemoryChatMemory chatMemory) {
        this.builder = builder;
        this.chatMemory = chatMemory;
        this.chatClientV2 = builder
                .defaultSystem("你是一个友好的智能助手，根据对话历史连贯地回答用户问题。")
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    /**
     * 带记忆的多轮对话（方式一：per-session ChatClient）
     * POST /ai/chat/memory?conversationId=abc&message=你好
     */
    @PostMapping("/memory")
    public String chat(
            @RequestParam(defaultValue = "default") String conversationId,
            @RequestParam String message) {

        ChatClient client = sessionMap.computeIfAbsent(conversationId, id ->
                builder
                        .defaultSystem("你是一个友好的智能助手，根据对话历史连贯地回答用户问题。")
                        .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory, id, 10))
                        .build()
        );

        return client.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 带记忆的多轮对话（方式二：注入单例 ChatClient，调用时动态传入 conversationId）
     * <p>
     * 对比方式一：ChatClient 注入为单例，不需要 sessionMap 管理。
     * conversationId 通过 .advisors(param) 在每次调用时动态传入。
     * <p>
     * POST /ai/chat/memory/v2?conversationId=abc&message=你好
     */
    @PostMapping("/memory/v2")
    public String chatV2(
            @RequestParam(defaultValue = "default") String conversationId,
            @RequestParam String message) {

        return chatClientV2.prompt("你是一个脾气暴躁的智能助手，根据对话历史连贯地回答用户问题。")
                .user(message)
                .advisors(a -> a
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .content();
    }

    /**
     * 查看指定会话的历史记录
     * GET /ai/chat/memory/history?conversationId=abc
     */
    @GetMapping("/memory/history")
    public String history(@RequestParam(defaultValue = "default") String conversationId) {
        List<Message> messages = chatMemory.get(conversationId, Integer.MAX_VALUE);
        if (messages.isEmpty()) {
            return "会话 %s 暂无历史记录".formatted(conversationId);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== 会话 [%s] 历史消息 ===\n".formatted(conversationId));
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            sb.append("[%s] %s: %s\n".formatted(i, msg.getMessageType(), msg.getText()));
        }
        return sb.toString();
    }
}
