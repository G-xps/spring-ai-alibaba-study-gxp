package com.example.ai.controller;

import com.example.ai.tool.BookingTool;
import com.example.ai.tool.FlightSearchTool;
import com.example.ai.tool.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 6.3 综合项目：智能机票助手。
 * <p>
 * 集成了 WeatherTool + FlightSearchTool + BookingTool 三个工具，
 * 加上 Chat Memory 支持多轮对话。
 * <p>
 * 模拟场景：
 *   1. 查天气
 *   2. 查航班
 *   3. 下单购票
 *   4. 查订单状态
 */
@RestController
@RequestMapping("/ai/flight-assistant")
public class FlightAssistantController {

    private final ChatClient chatClient;
    private final WeatherTool weatherTool;
    private final FlightSearchTool flightSearchTool;
    private final BookingTool bookingTool;

    public FlightAssistantController(
            ChatClient.Builder builder,
            InMemoryChatMemory chatMemory,
            WeatherTool weatherTool,
            FlightSearchTool flightSearchTool,
            BookingTool bookingTool) {

        this.chatClient = builder
                .defaultSystem("""
                        你是一个智能机票助手，帮助用户查询天气、搜索航班、下单购票和查询订单。
                        根据用户问题调用合适的工具，按步骤完成操作，并用中文清晰回答。
                        """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();

        this.weatherTool = weatherTool;
        this.flightSearchTool = flightSearchTool;
        this.bookingTool = bookingTool;
    }

    /**
     * 智能机票助手（流式）
     * POST /ai/flight-assistant/chat?conversationId=test&message=明天从杭州到北京有哪些航班
     */
    @PostMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chat(
            @RequestParam(defaultValue = "default") String conversationId,
            @RequestParam String message) {

        return chatClient.prompt()
                .user(message)
                .tools(weatherTool, flightSearchTool, bookingTool)
                .advisors(a -> a
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

    /**
     * 智能机票助手（同步，方便测试）
     * POST /ai/flight-assistant/chat/sync?conversationId=test&message=明天从杭州到北京有哪些航班
     */
    @PostMapping("/chat/sync")
    public String chatSync(
            @RequestParam(defaultValue = "default") String conversationId,
            @RequestParam String message) {

        return chatClient.prompt()
                .user(message)
                .tools(weatherTool, flightSearchTool, bookingTool)
                .advisors(a -> a
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .content();
    }
}
