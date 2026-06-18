package com.example.ai.config;

import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatMemory 配置。
 * <p>
 * 声明 InMemoryChatMemory 为 Spring Bean，供 ChatMemoryController 注入。
 */
@Configuration
public class ChatMemoryConfig {

    @Bean
    public InMemoryChatMemory inMemoryChatMemory() {
        return new InMemoryChatMemory();
    }
}
