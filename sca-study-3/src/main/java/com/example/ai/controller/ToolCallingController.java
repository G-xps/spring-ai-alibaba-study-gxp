package com.example.ai.controller;

import com.example.ai.function.CurrentDateTimeTool;
import com.example.ai.function.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Tool Calling 学习控制器。
 * <p>
 * 演示新版本推荐的 .tools() API（替代已废弃的 .functions()）。
 * 主要包含四种注册方式：
 *   .tools(String...)          — 按 bean name 引用 @Bean 函数
 *   .tools(Object...)          — 传入 @Tool 注解的 Bean，自动扫描
 *   .tools(ToolCallbackProvider) — 批量注册
 */
@RestController
@RequestMapping("/ai/tool")
public class ToolCallingController {

    private final ChatClient chatClient;
    private final CurrentDateTimeTool dateTimeTool;
    private final WeatherTool weatherTool;

    public ToolCallingController(
            ChatClient.Builder builder,
            CurrentDateTimeTool dateTimeTool,
            WeatherTool weatherTool) {
        this.chatClient = builder
                .defaultSystem("你是一个智能助手，可以根据问题调用对应的工具，并用中文回答。")
                .build();
        this.dateTimeTool = dateTimeTool;
        this.weatherTool = weatherTool;
    }

    /**
     * 方式一：.tools(Object...) — 传入 @Tool 注解的 Bean
     * <p>
     * WeatherTool 中的 queryWeather 方法有 @Tool 注解，框架自动扫描注册。
     * <p>
     * GET /ai/tool/weather?city=杭州
     */
    @GetMapping("/weather")
    public String weather(@RequestParam(defaultValue = "杭州") String city) {
        return chatClient.prompt()
                .user("查询 %s 今天的天气".formatted(city))
                .tools(weatherTool)          // Object... 方式，自动扫描 @Tool
                .call()
                .content();
    }

    /**
     * 方式二：.tools(Object...) — 另一个 @Tool 注解的 Bean
     * <p>
     * CurrentDateTimeTool 中有两个 @Tool 方法，框架自动扫描注册。
     * <p>
     * GET /ai/tool/datetime
     */
    @GetMapping("/datetime")
    public String datetime() {
        return chatClient.prompt()
                .user("今天几号？现在几点了？")
                .tools(dateTimeTool)         // Object... 方式，自动扫描 @Tool
                .call()
                .content();
    }

    /**
     * 方式三：混用多个 @Tool Bean
     * <p>
     * GET /ai/tool/mixed?question=今天几号，杭州天气如何
     */
    @GetMapping("/mixed")
    public String mixed(@RequestParam(defaultValue = "今天几号，杭州天气如何") String question) {
        return chatClient.prompt()
                .user(question)
                .tools(weatherTool, dateTimeTool)  // 多个 Object...
                .call()
                .content();
    }

    /**
     * 方式四：.toolContext(Map) — 传递上下文给工具
     * <p>
     * 某些场景下需要给工具传递额外上下文（如用户ID、请求来源等）。
     * <p>
     * GET /ai/tool/context?city=上海
     */
    @GetMapping("/context")
    public String context(@RequestParam(defaultValue = "上海") String question) {
        return chatClient.prompt()
                .user(question)
                .tools(weatherTool,dateTimeTool)
                .toolContext(Map.of("userId", "1"))
                .call()
                .content();
    }

}
