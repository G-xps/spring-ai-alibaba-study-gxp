package com.example.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟天气查询服务。
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private static final Map<String, String> WEATHER_DB = new ConcurrentHashMap<>();

    static {
        WEATHER_DB.put("北京", "晴，25°C，空气质量优");
        WEATHER_DB.put("上海", "多云，28°C，湿度较大");
        WEATHER_DB.put("杭州", "小雨，22°C，东风3级");
        WEATHER_DB.put("深圳", "晴转多云，30°C，适合户外活动");
        WEATHER_DB.put("广州", "雷阵雨，29°C，请携带雨具");
        WEATHER_DB.put("成都", "阴天，20°C，微风");
        WEATHER_DB.put("武汉", "小雨，18°C，降温注意保暖");
    }

    public String getWeather(String city) {
        log.info("查询天气：{}", city);
        String weather = WEATHER_DB.get(city);
        if (weather != null) {
            return "%s 今天天气：%s（查询时间：%s）".formatted(city, weather, LocalDate.now());
        }
        return "暂未收录 %s 的天气数据，请尝试其他城市".formatted(city);
    }
}
