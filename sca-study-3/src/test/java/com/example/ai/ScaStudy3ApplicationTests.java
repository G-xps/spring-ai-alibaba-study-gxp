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
 * 集成测试：测试 sca-study-3 第四阶段各模块接口。
 * 运行前需设置 QWEN_API_KEY 环境变量。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScaStudy3ApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder()
                    .setConnectTimeout(Duration.ofSeconds(60))
                    .setReadTimeout(Duration.ofSeconds(600));
        }
    }

    @Test
    void contextLoads() {
    }

    // ===== 4.1 Function Calling =====

    @Test
    void testFunctionWeather() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/function/weather?city=杭州", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/function/weather ===");
        System.out.println(response.getBody());
    }

    @Test
    void testFunctionOrder() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/function/order?orderId=1001", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/function/order ===");
        System.out.println(response.getBody());
    }

    @Test
    void testFunctionMulti() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/function/multi?question=杭州天气如何，并查一下订单1001", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/function/multi ===");
        System.out.println(response.getBody());
    }

    @Test
    void testFunctionDebug() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/function/debug?question=杭州天气如何", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).contains("步骤");
        System.out.println("=== /ai/function/debug ===");
        System.out.println(response.getBody());
    }

    // ===== Tool Calling（新 API） =====

    @Test
    void testToolWeather() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/tool/weather?city=杭州", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/tool/weather ===");
        System.out.println(response.getBody());
    }

    @Test
    void testToolDatetime() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/tool/datetime", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/tool/datetime ===");
        System.out.println(response.getBody());
    }

    @Test
    void testToolMixed() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/tool/mixed?question=今天几号，杭州天气如何", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/tool/mixed ===");
        System.out.println(response.getBody());
    }


    @Test
    void testToolContext() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/tool/context?question=今天几号杭州天气如何", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/tool/context ===");
        System.out.println(response.getBody());
    }
    // ===== 4.2 Chat Memory =====

    @Test
    void testChatMemoryConversationV2() {
        // 第一轮对话
        ResponseEntity<String> r1 = restTemplate
                .postForEntity("/ai/chat/memory/v2?conversationId=v2-test&message=你好，我叫小明", null, String.class);
        assertThat(r1.getStatusCode().value()).isEqualTo(200);
        assertThat(r1.getBody()).isNotBlank();
        System.out.println("=== /ai/chat/memory/v2 [turn 1] ===");
        System.out.println(r1.getBody());

        // 第二轮对话（应记住上一轮说的"小明"）
        ResponseEntity<String> r2 = restTemplate
                .postForEntity("/ai/chat/memory/v2?conversationId=v2-test&message=我叫什么名字", null, String.class);
        assertThat(r2.getStatusCode().value()).isEqualTo(200);
        assertThat(r2.getBody()).isNotBlank();
        System.out.println("=== /ai/chat/memory/v2 [turn 2] ===");
        System.out.println(r2.getBody());
    }

    @Test
    void testChatMemoryConversation() {
        // 第一轮对话
        ResponseEntity<String> r1 = restTemplate
                .postForEntity("/ai/chat/memory?conversationId=test-1&message=你好，我叫小明", null, String.class);
        assertThat(r1.getStatusCode().value()).isEqualTo(200);
        assertThat(r1.getBody()).isNotBlank();
        System.out.println("=== /ai/chat/memory [turn 1] ===");
        System.out.println(r1.getBody());

        // 第二轮对话（应记住上一轮说的"小明"）
        ResponseEntity<String> r2 = restTemplate
                .postForEntity("/ai/chat/memory?conversationId=test-1&message=我叫什么名字", null, String.class);
        assertThat(r2.getStatusCode().value()).isEqualTo(200);
        assertThat(r2.getBody()).isNotBlank();
        System.out.println("=== /ai/chat/memory [turn 2] ===");
        System.out.println(r2.getBody());

        // 查看历史记录
        ResponseEntity<String> history = restTemplate
                .getForEntity("/ai/chat/memory/history?conversationId=test-1", String.class);
        assertThat(history.getStatusCode().value()).isEqualTo(200);
        assertThat(history.getBody()).isNotBlank();
        assertThat(history.getBody()).contains("历史消息");
        System.out.println("=== /ai/chat/memory/history ===");
        System.out.println(history.getBody());
    }

    // ===== 4.4 Function + Memory + Streaming =====

    @Test
    void testAssistantSync() {
        // 第一轮：查杭州天气
        ResponseEntity<String> r1 = restTemplate
                .postForEntity("/ai/assistant/sync?conversationId=assistant-test&message=杭州今天天气如何", null, String.class);
        assertThat(r1.getStatusCode().value()).isEqualTo(200);
        assertThat(r1.getBody()).isNotBlank();
        System.out.println("=== /ai/assistant/sync [turn 1] ===");
        System.out.println(r1.getBody());

        // 第二轮：追问"明天呢？"，模型应记住杭州
        ResponseEntity<String> r2 = restTemplate
                .postForEntity("/ai/assistant/sync?conversationId=assistant-test&message=明天呢", null, String.class);
        assertThat(r2.getStatusCode().value()).isEqualTo(200);
        assertThat(r2.getBody()).isNotBlank();
        System.out.println("=== /ai/assistant/sync [turn 2] ===");
        System.out.println(r2.getBody());
    }

    @Test
    void testAssistantFull() {
        // 第一轮：查杭州天气
        ResponseEntity<String> r1 = restTemplate
                .postForEntity("/ai/assistant/full?conversationId=assistant-test&message=杭州今天天气如何", null, String.class);
        assertThat(r1.getStatusCode().value()).isEqualTo(200);
        assertThat(r1.getBody()).isNotBlank();
        System.out.println("=== /ai/assistant/sync [turn 1] ===");
        System.out.println(r1.getBody());

        // 第二轮：追问"明天呢？"，模型应记住杭州
        ResponseEntity<String> r2 = restTemplate
                .postForEntity("/ai/assistant/full?conversationId=assistant-test&message=明天呢", null, String.class);
        assertThat(r2.getStatusCode().value()).isEqualTo(200);
        assertThat(r2.getBody()).isNotBlank();
        System.out.println("=== /ai/assistant/sync [turn 2] ===");
        System.out.println(r2.getBody());
    }
    // ===== 4.3 Vector Store =====

    @Test
    void testVectorStore() {
        // 存储文档
        ResponseEntity<String> store = restTemplate
                .postForEntity("/ai/vector/store?text=Spring Boot 是 Java 主流的微服务框架", null, String.class);
        assertThat(store.getStatusCode().value()).isEqualTo(200);
        assertThat(store.getBody()).isNotBlank();
        assertThat(store.getBody()).contains("已存入");
        System.out.println("=== /ai/vector/store ===");
        System.out.println(store.getBody());

        // 搜索
        ResponseEntity<String> search = restTemplate
                .getForEntity("/ai/vector/search?query=Java 框架", String.class);
        assertThat(search.getStatusCode().value()).isEqualTo(200);
        assertThat(search.getBody()).isNotBlank();
        System.out.println("=== /ai/vector/search ===");
        System.out.println(search.getBody());

        // 列出文档
        ResponseEntity<String> list = restTemplate
                .getForEntity("/ai/vector/list", String.class);
        assertThat(list.getStatusCode().value()).isEqualTo(200);
        assertThat(list.getBody()).isNotBlank();
        System.out.println("=== /ai/vector/list ===");
        System.out.println(list.getBody());
    }
}
