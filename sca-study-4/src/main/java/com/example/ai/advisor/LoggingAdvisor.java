package com.example.ai.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.*;

/**
 * 自定义 Advisor — 日志记录型。
 * <p>
 * 演示 Advisor 生命周期：
 *   before阶段 → chain.nextAroundCall() → after阶段
 */
public class LoggingAdvisor implements CallAroundAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
//        log.info("=".repeat(40));
//        log.info("[before] LoggingAdvisor 收到请求");
//        log.info("[before] 用户问题：{}", advisedRequest.userText());

        // 调用链继续执行
        AdvisedResponse response = chain.nextAroundCall(advisedRequest);

//        log.info("[after] AI 回复：{}", response.response().getResult().getOutput().getText());
//        log.info("=".repeat(40));

        return response;
    }
}
