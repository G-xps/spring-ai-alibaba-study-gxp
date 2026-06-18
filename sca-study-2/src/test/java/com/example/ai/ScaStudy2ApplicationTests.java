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
 * 集成测试：测试 sca-study-2 多模态接口。运行前需设置 QWEN_API_KEY。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScaStudy2ApplicationTests {

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

    // ===== ImageModel =====

    @Test
    void testImageGenerate() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/image/generate?input=一只可爱的猫咪", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/image/generate ===");
        System.out.println(response.getBody());
    }

    @Test
    void testImageResponse() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/image/response?input=山水画", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).contains("图片 URL");
        System.out.println("=== /ai/image/response ===");
        System.out.println(response.getBody());
    }

    // ===== EmbeddingModel =====

    @Test
    void testEmbeddingSingle() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/embedding/single?text=Spring AI 是框架", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).contains("向量维度");
        System.out.println("=== /ai/embedding/single ===");
        System.out.println(response.getBody());
    }

    @Test
    void testEmbeddingBatch() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/embedding/batch?text1=今天天气好&text2=明天会下雨", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).contains("批量嵌入结果");
        System.out.println("=== /ai/embedding/batch ===");
        System.out.println(response.getBody());
    }

    @Test
    void testEmbeddingDimensions() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/embedding/dimensions", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/embedding/dimensions ===");
        System.out.println(response.getBody());
    }

    // ===== AudioModel =====

    @Test
    void testAudioTts() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/audio/tts?text=你好", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/audio/tts ===");
        System.out.println(response.getBody());
    }

    @Test
    void testAudioTranscribe() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/ai/audio/transcribe?fileUrl=https://example.com/test.wav", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotBlank();
        System.out.println("=== /ai/audio/transcribe ===");
        System.out.println(response.getBody());
    }
}
