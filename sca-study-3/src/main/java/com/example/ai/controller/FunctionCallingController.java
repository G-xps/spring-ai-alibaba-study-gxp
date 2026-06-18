package com.example.ai.controller;

import com.example.ai.function.OrderQueryFunction;
import com.example.ai.function.WeatherFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
//import org.springframework.ai.chat.model.ToolCall;
import org.springframework.ai.model.function.FunctionCallback;
//import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 4.1 Function Calling — 函数调用。
 * <p>
 * 演示 AI 模型调用外部 Java 函数（天气查询、订单查询）。
 * 函数通过 @Bean + @Description 注册，模型自动判断何时调用。
 */
@RestController
@RequestMapping("/ai/function")
public class FunctionCallingController {

    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final Function<WeatherFunction.WeatherRequest, WeatherFunction.WeatherResponse> weatherFn;
    private final Function<OrderQueryFunction.OrderRequest, OrderQueryFunction.OrderResponse> orderFn;
    private final ObjectMapper objectMapper;

    public FunctionCallingController(
            ChatClient.Builder builder,
            ChatModel chatModel,
            Function<WeatherFunction.WeatherRequest, WeatherFunction.WeatherResponse> weatherFn,
            Function<OrderQueryFunction.OrderRequest, OrderQueryFunction.OrderResponse> orderFn) {
        this.chatClient = builder
                .defaultSystem("你是一个智能助手，可以查询天气和订单信息。请根据用户问题调用对应的工具，并用中文回答。")
                .build();
        this.chatModel = chatModel;
        this.weatherFn = weatherFn;
        this.orderFn = orderFn;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 单函数：天气查询
     * GET /ai/function/weather?city=杭州
     */
    @GetMapping("/weather")
    public String weather(@RequestParam(defaultValue = "杭州") String city) {
        return chatClient.prompt()
                .user("查询 %s 今天的天气".formatted(city))
                .functions("currentWeather") // 注册 weather 函数
                .call()
                .content();
    }

    /**
     * 单函数：订单查询
     * GET /ai/function/order?orderId=1001
     */
    @GetMapping("/order")
    public String order(@RequestParam(defaultValue = "1001") String orderId) {
        return chatClient.prompt()
                .user("查询订单 %s 的状态".formatted(orderId))
                .functions("queryOrder")        // 注册 order 函数
                .call()
                .content();
    }

    /**
     * 多函数：一次对话中同时注册天气和订单两个函数，模型按需选择
     * GET /ai/function/multi?question=杭州今天天气如何，并帮我查一下订单1001的状态
     */
    @GetMapping("/multi")
    public String multi(@RequestParam(defaultValue = "杭州今天天气如何，并帮我查一下订单1001的状态") String question) {
        return chatClient.prompt()
                .user(question)
                .functions("currentWeather", "queryOrder")   // 同时注册两个函数
                .call()
                .content();
    }
}
