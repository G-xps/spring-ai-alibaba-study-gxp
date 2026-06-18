package com.example.ai.tool;

import com.example.ai.service.WeatherService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 天气查询工具。
 */
@Component
public class WeatherTool {

    private final WeatherService weatherService;

    public WeatherTool(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Tool(description = "根据城市名称查询实时天气信息，参数为城市名，如'杭州'")
    public String queryWeather(String city) {
        return weatherService.getWeather(city);
    }
}
