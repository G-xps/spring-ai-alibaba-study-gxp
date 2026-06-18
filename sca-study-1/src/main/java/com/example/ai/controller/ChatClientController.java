package com.example.ai.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
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
 * 5. 模型参数控制 — options / defaultOptions
 * 6. Generation 元数据 — id、model 等
 */
@RestController
@RequestMapping("/ai/chat-client")
public class ChatClientController {

    private final ChatClient chatClient;
    private final ChatModel chatModel;

    public ChatClientController(ChatClient.Builder builder, ChatModel chatModel) {
        this.chatClient = builder
                // 全局默认 System Prompt：没有手动传 system() 时自动使用
                .defaultSystem("你是一个知识渊博的助手，回答简洁准确。")
                // 全局默认参数
                .defaultOptions(DashScopeChatOptions.builder()
                        .withTemperature(0.7)
                        .build())
                .build();
        this.chatModel = chatModel;
    }

    // ================ 完整结构化响应 ================

    /**
     * 返回完整的 ChatResponse，包含：
     * - output.text       → 模型回复内容
     * - output.role       → 角色
     * - metadata.usage    → Token 用量（输入/输出/总计）
     * - result.finishReason → 结束原因
     * - result.id         → 响应 ID
     * - result.model      → 模型名称
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
                result.getOutput().getText(),
                result.getOutput().getMessageType().name(),
                result.getMetadata().getFinishReason(),
                chatResponse.getMetadata().getUsage() != null
                        ? new TokenUsage(
                        chatResponse.getMetadata().getUsage().getPromptTokens(),
                        chatResponse.getMetadata().getUsage().getCompletionTokens(),
                        chatResponse.getMetadata().getUsage().getTotalTokens())
                        : null,
                chatResponse.getMetadata().getId(),
                chatResponse.getMetadata().getModel()
        );
    }

    // ================ 模型参数控制 ================

    /**
     * 演示 ChatClient 的 .options() — 每次调用时覆盖模型参数
     * <p>
     * GET /ai/chat-client/options?input=给我讲个笑话&temp=0.9&maxTokens=500
     */
    @GetMapping("/options")
    public String options(
            @RequestParam(defaultValue = "给我讲个笑话") String input,
            @RequestParam(defaultValue = "0.9") double temp,
            @RequestParam(defaultValue = "500") int maxTokens) {
        return chatClient.prompt()
                .user(input)
                .options(DashScopeChatOptions.builder()
                        .withTemperature(temp)
                        .build())
                .call()
                .content();
    }

    /**
     * 演示 ChatModel 底层传参 — Prompt 携带 ChatOptions
     * ChatClient 的 .options() 底层也是转成这个
     * <p>
     * GET /ai/chat-client/model-options?input=用一句话介绍 Spring&model=qwen-max&temp=0.3
     */
    @GetMapping("/model-options")
    public String modelOptions(
            @RequestParam(defaultValue = "用一句话介绍 Spring") String input,
            @RequestParam(defaultValue = "qwen-max") String model,
            @RequestParam(defaultValue = "0.3") double temp) {
        Prompt prompt = new Prompt(input, DashScopeChatOptions.builder()
                .withModel(model)
                .withTemperature(temp)
                .build());
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
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
            TokenUsage usage,
            String id,
            String model
    ) {}

    /** Token 用量 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TokenUsage(
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens
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
        String content = chunk.getResult().getOutput().getText();
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
        String metaJson = buildMetadataJson(chunk);
        // 合并用量和元数据，用逗号拼接
        String combined = joinJsonFields(usageJson, metaJson);
        String comma = combined.isEmpty() ? "" : ",";
        return java.util.Optional.of("event: finish\ndata: {\"finishReason\":\"%s\"%s%s}\n\n"
                .formatted(finishReason, comma, combined));
    }

    /** 从 chunk 中提取 Token 用量 JSON 片段 */
    private String buildUsageJson(ChatResponse chunk) {
        var usage = chunk.getMetadata() != null && chunk.getMetadata().getUsage() != null
                ? chunk.getMetadata().getUsage() : null;
        if (usage == null) {
            return "";
        }
        return "\"usage\":{\"promptTokens\":%s,\"completionTokens\":%s,\"totalTokens\":%s}"
                .formatted(usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
    }

    /** 从 chunk 中提取 id 和 model 元数据 JSON 片段（从 ChatResponseMetadata 获取） */
    private String buildMetadataJson(ChatResponse chunk) {
        String id = chunk.getMetadata() != null ? chunk.getMetadata().getId() : null;
        String model = chunk.getMetadata() != null ? chunk.getMetadata().getModel() : null;
        if (id == null && model == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (id != null) {
            sb.append("\"id\":\"").append(jsonEscape(id)).append("\"");
        }
        if (model != null) {
            if (sb.length() > 0) sb.append(",");
            sb.append("\"model\":\"").append(jsonEscape(model)).append("\"");
        }
        return sb.toString();
    }

    /** 拼接多个 JSON 字段片段（逗号分隔） */
    private String joinJsonFields(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (String f : fields) {
            if (!f.isEmpty()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(f);
            }
        }
        return sb.toString();
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
