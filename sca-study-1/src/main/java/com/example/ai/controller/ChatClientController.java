package com.example.ai.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * ChatClient 进阶功能演示：
 * 1. 完整结构化响应 — 包含 Token 用量、FinishReason、角色等元数据
 * 2. 流式事件流（SSE）— 带事件类型的结构化推送
 * 3. 实体映射 — 将内容转为 POJO
 * 4. System Prompt 角色设定
 */
@RestController
@RequestMapping("/ai/chat-client")
public class ChatClientController {

    private final ChatClient chatClient;

    public ChatClientController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // ================ 完整结构化响应 ================

    /**
     * 返回完整的 ChatResponse，包含：
     * - output.text       → 模型回复内容
     * - metadata.usage    → Token 用量（输入/输出/总计）
     * - result.finishReason → 结束原因
     * - result.role       → 角色
     * <p>
     * GET /ai/chat-client/response?input=用一句话介绍微服务
     */
    @GetMapping("/response")
    public ChatClientResponse response(@RequestParam(defaultValue = "用一句话介绍微服务") String input) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(input)
                .call()
                .chatResponse();

        Generation result = chatResponse.getResult();
        return new ChatClientResponse(
                result.getOutput().getContent(),
                result.getOutput().getMessageType().name(),
                result.getMetadata().getFinishReason(), // 模型结束原因，如 max_tokens、stop、length 等
                chatResponse.getMetadata().getUsage() != null
                        ? new TokenUsage(
                        chatResponse.getMetadata().getUsage().getPromptTokens(),
                        chatResponse.getMetadata().getUsage().getGenerationTokens(),
                        chatResponse.getMetadata().getUsage().getTotalTokens())
                        : null
        );
    }

    // ================ 实体映射 ================

    /**
     * 结构化输出到 POJO：适合已知数据结构的场景。
     * 与 /response 的区别：这里只提取内容映射为 Java 对象，不包含元数据。
     * <p>
     * GET /ai/chat-client/entity?actor=周星驰
     */
    @GetMapping("/entity")
    public ActorFilms entity(@RequestParam(defaultValue = "周星驰") String actor) {
        return chatClient.prompt()
                .user("列出 {actor} 最出名的 5 部电影，每部电影给一句话简介。".replace("{actor}", actor))
                .call()
                .entity(ActorFilms.class);
    }

    /**
     * 泛型实体列表映射
     * GET /ai/chat-client/movies?actor=刘德华
     */
    @GetMapping("/movies")
    public List<ActorFilms> movies(@RequestParam(defaultValue = "刘德华") String actor) {
        return chatClient.prompt()
                .user("列出 {actor} 的 5 部代表作，每部给一句话简介。"
                        .replace("{actor}", actor))
                .call()
                .entity(new org.springframework.core.ParameterizedTypeReference<List<ActorFilms>>() {});
    }

    // ================ System Prompt ================

    /**
     * 使用 System Prompt 指定 AI 角色
     * GET /ai/chat-client/role?input=介绍一下自己&role=你是鲁迅，用民国文风回答
     */
    @GetMapping("/role")
    public String role(
            @RequestParam(defaultValue = "介绍一下你自己") String input,
            @RequestParam(defaultValue = "你是一位耐心的 Java 技术导师") String role) {
        return chatClient.prompt()
                .system(role)
                .user(input)
                .call()
                .content();
    }

    // ================ 流式响应（Studio 风格 SSE） ================

    /**
     * 流式响应，SSE 事件格式。
     * 每个事件包含 type 和 data，便于前端按事件类型处理。
     * <p>
     * 事件类型：
     * - "start"    → 开始事件，包含角色信息
     * - "delta"    → 内容片段
     * - "finish"   → 结束事件，包含结束原因（注：流式结束时无法获取 Token 用量，
     *               若需完整用量信息请调用 /ai/chat-client/response）
     * <p>
     * GET /ai/chat-client/stream-sse?input=用 50 字介绍 Spring AI
     */
    @GetMapping(value = "/stream-sse-simple", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> streamSseSim(@RequestParam(defaultValue = "用 50 字介绍 Spring AI") String input) {

        return chatClient.prompt()
                .user(input)
                .stream()
                .content()
                .map(content -> "event: delta\ndata: " + jsonEscape(content) + "\n\n")
                .startWith("event: start\ndata: {\"role\":\"assistant\"}\n\n")
                .concatWithValues("event: finish\ndata: {\"finishReason\":\"stop\"}\n\n");
    }

    /**
     * 流式响应，SSE 事件格式。
     * 每个事件包含 type 和 data，便于前端按事件类型处理。
     * <p>
     * 事件类型：
     * - "start"    → 开始事件，包含角色信息
     * - "delta"    → 内容片段
     * - "finish"   → 结束事件，包含 FinishReason 和 Token 用量
     * <p>
     * GET /ai/chat-client/stream-sse?input=用 50 字介绍 Spring AI
     */
    @GetMapping(value = "/stream-sse", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> streamSse(@RequestParam(defaultValue = "用 50 字介绍 Spring AI") String input) {

        return chatClient.prompt()
                .system("你是一个介绍性小助手")
                .user(input)
                .stream()
                .chatResponse()
                .flatMap(this::toSseEvents)
                .startWith("event: start\ndata: {\"role\":\"assistant\"}\n\n");
    }

    /**
     * 基础的流式响应（纯文本 SSE）
     * GET /ai/chat-client/stream?input=用 50 字介绍 Spring AI
     */
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> stream(@RequestParam(defaultValue = "用 50 字介绍 Spring AI") String input) {
        return chatClient.prompt()
                .user(input)
                .stream()
                .content();
    }

    // ================ 内部类 ================

    /** 完整结构化响应 DTO */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ChatClientResponse(
            String content,
            String role,
            String finishReason,
            TokenUsage usage
    ) {}

    /** Token 用量 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TokenUsage(
            Long promptTokens,
            Long completionTokens,
            Long totalTokens
    ) {}

    /** 实体映射用：演员电影作品 */
    public record ActorFilms(String actor, List<Film> movies) {
        public record Film(String title, String year, String description) {}
    }

    // ================ 工具方法 ================

    /** 将 ChatResponse chunk 转换为 SSE 事件列表（delta + finish） */
    private Flux<String> toSseEvents(ChatResponse chunk) {
        List<String> events = new java.util.ArrayList<>();

        buildDeltaEvent(chunk).ifPresent(events::add);
        buildFinishEvent(chunk).ifPresent(events::add);

        return Flux.fromIterable(events);
    }

    /** 内容片段 → delta 事件 */
    private java.util.Optional<String> buildDeltaEvent(ChatResponse chunk) {
        String content = chunk.getResult().getOutput().getContent();
        if (content == null || content.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of("event: delta\ndata: " + jsonEscape(content) + "\n\n");
    }

    /** 结束原因 → finish 事件（携带用量） */
    private java.util.Optional<String> buildFinishEvent(ChatResponse chunk) {
        Generation result = chunk.getResult();
        String finishReason = result.getMetadata() != null
                ? result.getMetadata().getFinishReason() : null;

        if (finishReason == null || "NULL".equalsIgnoreCase(finishReason)) {
            return java.util.Optional.empty();
        }

        String usageJson = buildUsageJson(chunk);
        String comma = usageJson.isEmpty() ? "" : ",";
        return java.util.Optional.of("event: finish\ndata: {\"finishReason\":\"%s\"%s%s}\n\n"
                .formatted(finishReason, comma, usageJson));
    }

    /** 从 chunk 中提取 Token 用量 JSON 片段 */
    private String buildUsageJson(ChatResponse chunk) {
        var usage = chunk.getMetadata() != null && chunk.getMetadata().getUsage() != null
                ? chunk.getMetadata().getUsage() : null;
        if (usage == null) {
            return "";
        }
        return "\"usage\":{\"promptTokens\":%s,\"completionTokens\":%s,\"totalTokens\":%s}"
                .formatted(usage.getPromptTokens(), usage.getGenerationTokens(), usage.getTotalTokens());
    }

    /** 转义 JSON 字符串中的特殊字符 */
    private static String jsonEscape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
