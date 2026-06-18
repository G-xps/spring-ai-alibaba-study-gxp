package com.example.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟机票下单服务。
 */
@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final Map<Integer, Booking> orders = new ConcurrentHashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(1000);

    public Booking book(String flightNo, String passenger, int price) {
        int orderId = idGen.incrementAndGet();
        Booking booking = new Booking(orderId, flightNo, passenger, price, "已支付");
        orders.put(orderId, booking);
        log.info("下单成功：订单号={}，航班={}，乘客={}", orderId, flightNo, passenger);
        return booking;
    }

    public Booking queryOrder(int orderId) {
        return orders.get(orderId);
    }

    public record Booking(int orderId, String flightNo, String passenger, int price, String status) {
        @Override
        public String toString() {
            return "订单 %s：%s %s ¥%d %s".formatted(orderId, flightNo, passenger, price, status);
        }
    }
}
