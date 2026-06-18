package com.example.ai.function;

import com.example.ai.service.WeatherService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 天气查询工具 — 使用 @Tool 注解（推荐的新方式）。
 * <p>
 * 和旧的 @Configuration + @Bean + Function<T,R> 模式相比：
 * - 省去 Request/Response Record
 * - 省去 @Bean 方法
 * - 直接注入 Service，方法签名更自然
 * - 在 ChatClient 中用 .tools(weatherTool) 注册
 */
@Component
public class WeatherTool {

    private final WeatherService weatherService;

    public WeatherTool(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Tool(description = "根据城市名称查询实时天气信息，参数为城市名，如'北京'")
    public String queryWeather(String city, ToolContext toolContext) {
        System.out.println("WeatherTool工具上下文：" + toolContext.getContext());
        return weatherService.getWeather(city);
    }
}
