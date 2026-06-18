package com.example.ai.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Prompt 与 Prompt Template 使用演示。
 * 展示底层 ChatModel API 和模板引擎的用法。
 */
@RestController
@RequestMapping("/ai/prompt")
public class PromptController {

    private final ChatModel chatModel;

    public PromptController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 使用 PromptTemplate 创建带占位符的提示词
     * GET /ai/prompt/template?adjective=有趣的&topic=程序员
     */
    @GetMapping("/template")
    public String template(
            @RequestParam(defaultValue = "有趣的") String adjective,
            @RequestParam(defaultValue = "程序员") String topic) {

        PromptTemplate promptTemplate = new PromptTemplate("给我讲一个{adjective}的关于{topic}的笑话");
        Prompt prompt = promptTemplate.create(Map.of("adjective", adjective, "topic", topic));
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getContent();
    }

    /**
     * 多 Message 组装（System + User）
     * GET /ai/prompt/multi?name=小爱&topic=云计算&input=帮我解释一下这个概念
     */
    @GetMapping("/multi")
    public String multi(
            @RequestParam(defaultValue = "小爱") String name,
            @RequestParam(defaultValue = "云计算") String topic,
            @RequestParam(defaultValue = "帮我解释一下这个概念") String input) {

        // 使用 SystemPromptTemplate 创建系统消息
        String systemText = """
                你是{name}，一个精通{topic}的资深技术专家。
                请用通俗易懂的语言回答问题，开头先介绍自己。
                """;
        Message systemMessage = new SystemPromptTemplate(systemText)
                .createMessage(Map.of("name", name, "topic", topic));

        // 用户消息
        Message userMessage = new UserMessage(input);

        // 组装 Prompt 并调用
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getContent();
    }

    /**
     * 获取模型响应的元数据（Token 用量等）
     * GET /ai/prompt/metadata?input=用一句话介绍什么是微服务
     */
    @GetMapping("/metadata")
    public String metadata(@RequestParam(defaultValue = "用一句话介绍什么是微服务") String input) {
        ChatResponse response = chatModel.call(new Prompt(input));

        return """
                回答：%s
                ---
                Token 用量：
                输入：%s
                输出：%s
                总计：%s
                """.formatted(
                    response.getResult().getOutput().getContent(),
                    response.getMetadata().getUsage() != null ? response.getMetadata().getUsage().getPromptTokens() : "未知",
                    response.getMetadata().getUsage() != null ? response.getMetadata().getUsage().getGenerationTokens() : "未知",
                    response.getMetadata().getUsage() != null ? response.getMetadata().getUsage().getTotalTokens() : "未知"
                );
    }
}
