package com.example.ai.tool;

import com.example.ai.service.BookingService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 机票下单与查询工具。
 */
@Component
public class BookingTool {

    private final BookingService bookingService;

    public BookingTool(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Tool(description = "下单购买机票，参数为航班号、乘客姓名、价格，返回订单信息")
    public String bookFlight(String flightNo, String passenger, int price) {
        var booking = bookingService.book(flightNo, passenger, price);
        return booking.toString();
    }

    @Tool(description = "根据订单号查询订单状态，参数为订单号（如 1001）")
    public String queryOrder(int orderId) {
        var booking = bookingService.queryOrder(orderId);
        if (booking == null) {
            return "未找到订单 %s".formatted(orderId);
        }
        return booking.toString();
    }
}
