package com.example.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟订单查询服务。
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final Map<String, String> ORDER_DB = new ConcurrentHashMap<>();

    static {
        ORDER_DB.put("1001", "已支付，等待发货，金额 ¥299.00");
        ORDER_DB.put("1002", "已发货，快递单号 SF1234567890");
        ORDER_DB.put("1003", "已完成，签收时间 2026-06-15");
        ORDER_DB.put("1004", "已取消，取消原因：用户主动取消");
        ORDER_DB.put("1005", "待支付，请在 30 分钟内完成付款");
    }

    public String queryOrder(String orderId) {
        log.info("查询订单：{}", orderId);
        String status = ORDER_DB.get(orderId);
        if (status != null) {
            return "订单 %s 状态：%s（查询时间：%s）".formatted(orderId, status, LocalDateTime.now());
        }
        return "未找到订单 %s，请检查订单号是否正确".formatted(orderId);
    }
}
