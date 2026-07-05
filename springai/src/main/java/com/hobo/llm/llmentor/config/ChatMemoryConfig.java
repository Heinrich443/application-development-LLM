package com.hobo.llm.llmentor.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 实现 ChatMemory 的灵活切换
 */
// @Configuration
public class ChatMemoryConfig {

    @Value("${spring.ai.chat.memory.type:inmemory}")
    private String memoryType;

    @Bean("inMemoryChatMemory")
    public ChatMemory inMemoryChatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();
    }

    @Bean("jdbcChatMemory")
    public ChatMemory jdbcChatMemory(JdbcChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(10)
                .build();
    }

    @Primary
    @Bean("chatMemory")
    public ChatMemory chatMemory(
            @Value("${spring.ai.chat.memory.type:inmemory}") String type,
            ChatMemory inMemoryChatMemory,
            ChatMemory jdbcChatMemory) {
        return "jdbc".equalsIgnoreCase(type) ? jdbcChatMemory : inMemoryChatMemory;
    }
}
