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
 * 集成测试：测试 sca-study-1 所有接口。运行前需设置 QWEN_API_KEY。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScaStudy1ApplicationTests {

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

    // ===== 第一阶段：基础入门 =====

    @Test
    void contextLoads() {
    }

    @Test
    void testChatDefault() {
        ResponseEntity<String> response = restTemplate.getForEntity("/ai/chat", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/chat ===");
        System.out.println(response.getBody());
    }

    // ===== 第二阶段：ChatClient 进阶 =====

    /** 完整结构化响应：包含 content、role、finishReason、usage */
    @Test
    void testResponse() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/chat-client/response?input=用一句话介绍微服务", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).contains("content");
        assertThat(response.getBody()).contains("finishReason");
        assertThat(response.getBody()).contains("model");
        System.out.println("=== /ai/chat-client/response ===");
        System.out.println(response.getBody());
    }

    /** 实体映射：将内容转为 POJO */
    @Test
    void testChatClientEntity() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/chat-client/entity?actor=周星驰", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/chat-client/entity ===");
        System.out.println(response.getBody());
    }

    /** 实体映射（泛型列表） */
    @Test
    void testChatClientMovies() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/chat-client/movies?actor=刘德华", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/chat-client/movies ===");
        System.out.println(response.getBody());
    }

    /** System Prompt 角色设定 */
    @Test
    void testChatClientRole() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/chat-client/role?input=介绍一下你自己&role=你是鲁迅，用民国文风回答", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/chat-client/role ===");
        System.out.println(response.getBody());
    }

    /** 普通流式响应（纯文本 SSE） */
    @Test
    void testStream() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/chat-client/stream?input=用 10 字介绍 Spring", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/chat-client/stream ===");
        System.out.println(response.getBody());
    }

    /** 结构化流式响应（带 start/delta/finish 事件） */
    @Test
    void testStreamSse() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/chat-client/stream-sse?input=用 10 字介绍 Spring", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).contains("event: start");
        assertThat(response.getBody()).contains("event: delta");
        assertThat(response.getBody()).contains("event: finish");
        System.out.println("=== /ai/chat-client/stream-sse ===");
        System.out.println(response.getBody());
    }

    /** 结构化流式响应（带 start/delta/finish 事件） */
    @Test
    void testStreamSseSimple() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/chat-client/stream-sse-simple?input=用 10 字介绍 Spring", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).contains("event: start");
        assertThat(response.getBody()).contains("event: delta");
        assertThat(response.getBody()).contains("event: finish");
        System.out.println("=== /ai/chat-client/stream-sse-simple ===");
        System.out.println(response.getBody());
    }

    // ===== 第二阶段：Prompt Template =====

    @Test
    void testPromptTemplate() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/prompt/template?adjective=冷笑话&topic=产品经理", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/prompt/template ===");
        System.out.println(response.getBody());
    }

    @Test
    void testPromptMulti() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/prompt/multi?name=小爱&topic=Java&input=什么是多线程", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/prompt/multi ===");
        System.out.println(response.getBody());
    }

    @Test
    void testPromptMetadata() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/prompt/metadata?input=用一句话介绍 Spring", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).contains("Token 用量");
        System.out.println("=== /ai/prompt/metadata ===");
        System.out.println(response.getBody());
    }

    // ===== 新增补充功能 =====

    @Test
    void testOptions() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/chat-client/options?input=给我讲个笑话&temp=0.9&maxTokens=500", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/chat-client/options ===");
        System.out.println(response.getBody());
    }

    @Test
    void testModelOptions() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/chat-client/model-options?input=用一句话介绍 Spring&model=qwen-plus&temp=0.3", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/chat-client/model-options ===");
        System.out.println(response.getBody());
    }

    @Test
    void testPromptFile() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/prompt/file?name=小智&topic=微服务&input=什么是服务发现", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/prompt/file ===");
        System.out.println(response.getBody());
    }
}
