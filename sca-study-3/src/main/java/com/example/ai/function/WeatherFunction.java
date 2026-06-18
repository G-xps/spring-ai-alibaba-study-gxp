package com.example.ai.function;

import com.example.ai.service.WeatherService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * 天气查询 Function — 注册为 @Bean，供 AI 模型调用。
 * <p>
 * 模型收到用户问"天气"相关问题时，会自动调用此函数获取实时天气。
 */
@Configuration(proxyBeanMethods = false)
public class WeatherFunction {

    /**
     * 函数请求体：城市名称。
     */
    public record WeatherRequest(String city) {}

    /**
     * 函数响应体：天气描述。
     */
    public record WeatherResponse(String weatherInfo) {}

    @Bean
    @Description("根据城市名称查询实时天气信息，参数为城市名，如'杭州'")
    public Function<WeatherRequest, WeatherResponse> currentWeather(WeatherService weatherService) {
        return request -> {
            String result = weatherService.getWeather(request.city());
            return new WeatherResponse(result);
        };
    }
}
