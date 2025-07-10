package com.fbc.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
