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
 * 集成测试：测试 sca-study-4 RAG 各接口。
 * 运行前需设置 QWEN_API_KEY 环境变量。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScaStudy4ApplicationTests {

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

    // ===== 5.1 ETL 管道 =====

    @Test
    void testAddDocument() {
        ResponseEntity<String> response = restTemplate
                .postForEntity("/ai/rag/document?content=测试文档内容", null, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).contains("已存入");
        System.out.println("=== /ai/rag/document ===");
        System.out.println(response.getBody());
    }

    @Test
    void testSearch() {
        // 先加一条
        restTemplate.postForEntity("/ai/rag/document?content=Spring AI Alibaba 是一个 Java 框架", null, String.class);

        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/rag/search?query=Java 框架", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/rag/search ===");
        System.out.println(response.getBody());
    }

    // ===== 5.2 RAG 问答 =====

    @Test
    void testRagChat() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/rag/chat?question=Spring AI Alibaba 是什么", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/rag/chat ===");
        System.out.println(response.getBody());
    }

    @Test
    void testRagChatMemory() {
        // 第一轮：问 Spring AI Alibaba 是什么
        ResponseEntity<String> r1 = restTemplate
                .getForEntity("/ai/rag/chat-memory?conversationId=rag-test&question=Spring AI Alibaba 是什么", String.class);
        assertThat(r1.getStatusCode().value()).isEqualTo(200);
        assertThat(r1.getBody()).isNotBlank();
        System.out.println("=== /ai/rag/chat-memory [turn 1] ===");
        System.out.println(r1.getBody());

        // 第二轮：追问"它有哪些特性"，模型应记住上一轮讨论的主题，直接回答
        ResponseEntity<String> r2 = restTemplate
                .getForEntity("/ai/rag/chat-memory?conversationId=rag-test&question=它有哪些特性", String.class);
        assertThat(r2.getStatusCode().value()).isEqualTo(200);
        assertThat(r2.getBody()).isNotBlank();
        System.out.println("=== /ai/rag/chat-memory [turn 2] ===");
        System.out.println(r2.getBody());
    }

    // ===== 5.3 自定义 Advisor =====

    @Test
    void testCustomAdvisor() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/rag/custom?question=Spring AI Alibaba 有哪些模型", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        // SourceTracingAdvisor 会在结果中追加【参考资料】
        assertThat(response.getBody()).contains("通义千问");
        System.out.println("=== /ai/rag/custom ===");
        System.out.println(response.getBody());
    }
}
