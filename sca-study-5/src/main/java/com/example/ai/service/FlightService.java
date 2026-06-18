package com.example.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 模拟航班查询服务。
 */
@Service
public class FlightService {

    private static final Logger log = LoggerFactory.getLogger(FlightService.class);

    public List<Flight> searchFlights(String from, String to, String date) {
        log.info("查询航班：{} → {}，日期：{}", from, to, date);
        // 模拟返回数据
        return List.of(
                new Flight("CA1234", from, to, date, "08:00", "10:30", 680),
                new Flight("MU5678", from, to, date, "12:00", "14:20", 520),
                new Flight("CZ9012", from, to, date, "16:30", "18:55", 750),
                new Flight("HU3456", from, to, date, "20:00", "22:15", 460)
        );
    }

    public record Flight(
            String flightNo,
            String from,
            String to,
            String date,
            String departureTime,
            String arrivalTime,
            int price
    ) {
        @Override
        public String toString() {
            return "%s %s→%s %s %s-%s ¥%d".formatted(flightNo, from, to, date, departureTime, arrivalTime, price);
        }
    }
}
