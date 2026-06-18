package com.example.ai.tool;

import com.example.ai.service.FlightService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 航班搜索工具。
 */
@Component
public class FlightSearchTool {

    private final FlightService flightService;

    public FlightSearchTool(FlightService flightService) {
        this.flightService = flightService;
    }

    @Tool(description = "查询航班信息，参数为出发城市、到达城市、日期（如'杭州'、'北京'、'2026-06-20'）")
    public String searchFlights(String from, String to, String date) {
        var flights = flightService.searchFlights(from, to, date);
        if (flights.isEmpty()) {
            return "未找到 %s 到 %s 在 %s 的航班".formatted(from, to, date);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("找到 %s 到 %s 在 %s 的航班如下：\n".formatted(from, to, date));
        for (var f : flights) {
            sb.append("  %s %s-%s ¥%d\n".formatted(f.flightNo(), f.departureTime(), f.arrivalTime(), f.price()));
        }
        return sb.toString();
    }
}
