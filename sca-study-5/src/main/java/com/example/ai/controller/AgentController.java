package com.example.ai.controller;

import com.example.ai.tool.FlightSearchTool;
import com.example.ai.tool.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

/**
 * 6.1 多工具协同 Agent。
 * <p>
 * 注册多个工具，模型根据用户问题按需选择调用，支持多步推理。
 * 例如：查天气 → 模型判断是否需要查航班 → 自动连续调用。
 */
@RestController
@RequestMapping("/ai/agent")
public class AgentController {

    private final ChatClient chatClient;
    private final WeatherTool weatherTool;
    private final FlightSearchTool flightSearchTool;

    public AgentController(
            ChatClient.Builder builder,
            WeatherTool weatherTool,
            FlightSearchTool flightSearchTool) {

        this.chatClient = builder
                .defaultSystem("""
                        你是一个智能出行助手，可以帮助用户查询天气和航班信息。
                        根据用户的问题调用合适的工具，并用中文回答。
                        如果需要多个信息（比如天气和航班），可以依次调用工具获取。
                        """)
                .build();

        this.weatherTool = weatherTool;
        this.flightSearchTool = flightSearchTool;
    }

    /**
     * Agent 问答 — 多工具协同，问题应涉及多个工具才能体现 Agent 决策
     * GET /ai/agent/chat?question=杭州今天天气怎么样，明天杭州到北京有哪些航班
     */
    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "杭州今天天气怎么样，明天从杭州到北京有哪些航班") String question) {
        return chatClient.prompt()
                .user(question)
                .tools(weatherTool, flightSearchTool)
                .call()
                .content();
    }
}
