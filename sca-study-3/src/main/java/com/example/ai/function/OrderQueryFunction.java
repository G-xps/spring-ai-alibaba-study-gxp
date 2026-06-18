package com.example.ai.function;

import com.example.ai.service.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * 订单查询 Function — 注册为 @Bean，供 AI 模型调用。
 * <p>
 * 模型收到用户问"订单"相关问题时，会自动调用此函数查询订单状态。
 */
@Configuration(proxyBeanMethods = false)
public class OrderQueryFunction {

    /**
     * 函数请求体：订单 ID。
     */
    public record OrderRequest(String orderId) {}

    /**
     * 函数响应体：订单状态描述。
     */
    public record OrderResponse(String orderStatus) {}

    @Bean
    @Description("根据订单号查询订单状态，参数为订单编号，如'1001'")
    public Function<OrderRequest, OrderResponse> queryOrder(OrderService orderService) {
        return request -> {
            String result = orderService.queryOrder(request.orderId());
            return new OrderResponse(result);
        };
    }
}
