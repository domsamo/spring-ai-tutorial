package com.fbc.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * OpenAI API 설정
 */
@Slf4j
@Configuration
public class OpenAiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model:gpt-3.5-turbo}")
    private String defaultModel;

    @Value("classpath:/prompt.txt")
    private Resource txtPrompt;

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatCLientBuilder) {
//        return chatCLientBuilder
//                .build();

// --- default System prompt ---
//        return chatCLientBuilder
//                .defaultSystem("시스템 메시지")
//                .build();

// --- txt Prompt default System prompt
//        return chatCLientBuilder
//                .defaultSystem(txtPrompt)
//                .build();

// --- Adviser 지정
        return chatCLientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    /**
     * OpenAI API 클라이언트 빈 등록
     */
    @Bean
    public OpenAiApi openAiApi() {
        log.debug("OpenAI API 클라이언트 초기화, 기본 모델: {}", defaultModel);
        return OpenAiApi.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * 기본 모델명 반환
     */
    public String getDefaultModel() {
        return defaultModel;
    }

    /**
     * hotel_vector_store 테이블용 VectorStore
     * @param jdbcTemplate
     * @param embeddingModel
     * @return
     */
    @Bean
    public VectorStore hotelVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("hotel_vector_store")
                .dimensions(1536)
                .build();
    }
}
