package com.example.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集成测试：测试 sca-study-5 AI Agent 与机票助手接口。
 * 运行前需设置 QWEN_API_KEY 环境变量。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScaStudy5ApplicationTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder()
                    .setConnectTimeout(Duration.ofSeconds(60))
                    .setReadTimeout(Duration.ofSeconds(600));
        }
    }
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    // ===== 6.1 多工具 Agent =====

    @Test
    void testAgentWeather() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/agent/chat?question=杭州今天天气怎么样，明天从杭州到北京有哪些航班", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        // 应同时包含天气和航班信息
        assertThat(response.getBody()).containsAnyOf("天气", "℃", "航班", "CA");
        System.out.println("=== /ai/agent/chat ===");
        System.out.println(response.getBody());
    }

    // ===== 6.3 智能机票助手 =====

    @Test
    void testFlightAssistantSync() {
        // 第一轮：搜索航班
        ResponseEntity<String> r1 = restTemplate
                .postForEntity("/ai/flight-assistant/chat/sync?conversationId=fa-test&message=从杭州到北京的航班有哪些", null, String.class);
        assertThat(r1.getStatusCode().value()).isEqualTo(200);
        assertThat(r1.getBody()).isNotBlank();
        System.out.println("=== /ai/flight-assistant [turn 1] ===");
        System.out.println(r1.getBody());

        // 第二轮：下单买最便宜的
        ResponseEntity<String> r2 = restTemplate
                .postForEntity("/ai/flight-assistant/chat/sync?conversationId=fa-test&message=帮我订最便宜的那班，我叫张三", null, String.class);
        assertThat(r2.getStatusCode().value()).isEqualTo(200);
        assertThat(r2.getBody()).isNotBlank();
        System.out.println("=== /ai/flight-assistant [turn 2] ===");
        System.out.println(r2.getBody());
    }
}
