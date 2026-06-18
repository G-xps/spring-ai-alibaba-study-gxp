package com.example.ai.controller;

import com.example.ai.function.CurrentDateTimeTool;
import com.example.ai.function.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 4.4 综合实战：函数调用 + 对话记忆 + 流式返回。
 * <p>
 * 模拟真实场景：
 *   - 第一轮：模型调用函数查天气
 *   - 第二轮：追问"明天呢？"，模型记住城市名，再次调用函数查天气
 * 所有回复均以流式（SSE）返回，打字机效果。
 */
@RestController
@RequestMapping("/ai/assistant")
public class AssistantController {

    private final ChatClient chatClient;

    private final CurrentDateTimeTool dateTimeTool;
    private final WeatherTool weatherTool;
    public AssistantController(
            ChatClient.Builder builder,
            InMemoryChatMemory chatMemory,
            WeatherTool weatherTool,
            CurrentDateTimeTool dateTimeTool) {

        this.chatClient = builder
                .defaultSystem("""
                        你是一个智能天气助手，可以查询实时天气和当前日期时间。
                        回答要简洁友好，说明当前天气状况。
                        """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();

        this.dateTimeTool = dateTimeTool;
        this.weatherTool = weatherTool;
    }

    /**
     * 带记忆 + 函数调用 + 流式返回
     * <p>
     * 测试步骤：
     *   1) POST /ai/assistant/chat?conversationId=test&message=杭州今天天气如何
     *   2) POST /ai/assistant/chat?conversationId=test&message=明天呢
     * <p>
     * 第二步模型应记住"杭州"，再次函数调用查天气。
     */
    @PostMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chat(
            @RequestParam(defaultValue = "default") String conversationId,
            @RequestParam String message) {

        return chatClient.prompt()
                .user(message)
                .tools("currentWeather")
                .advisors(a -> a
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

    /**
     * 带记忆 + 多函数 + 流式返回
     * <p>
     * 比 /chat 多了日期时间工具，可以让模型查天气的同时也报日期。
     * POST /ai/assistant/full?conversationId=test&message=杭州今天天气如何，今天几号
     */
    @PostMapping(value = "/full", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> full(
            @RequestParam(defaultValue = "default") String conversationId,
            @RequestParam String message) {

        return chatClient.prompt()
                .user(message)
                .tools(weatherTool, dateTimeTool)
                .advisors(a -> a
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

    /**
     * 纯测试 — 同样功能但同步返回，方便测试断言
     * POST /ai/assistant/sync?conversationId=test&message=杭州今天天气如何
     */
    @PostMapping("/sync")
    public String sync(
            @RequestParam(defaultValue = "default") String conversationId,
            @RequestParam String message) {

        return chatClient.prompt()
                .user(message)
                .tools("currentWeather")
                .advisors(a -> a
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .content();
    }
}
