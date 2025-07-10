package com.fbc.ai.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 채팅 요청 데이터 모델
 */
@Schema(description = "채팅 요청 데이터 모델")
public class ChatRequestDto {
    @Schema(description = "사용자 질문", example = "안녕하세요")
    private final String query;

    @Schema(description = "사용할 LLM 모델", example = "gpt-3.5-turbo", defaultValue = "gpt-3.5-turbo")
    private final String model;

    @JsonCreator
    public ChatRequestDto(
            @JsonProperty("query") String query,
            @JsonProperty(value = "model", defaultValue = "gpt-3.5-turbo") String model) {
        this.query = query;
        this.model = model != null ? model : "gpt-3.5-turbo";
    }

    public ChatRequestDto(String query) {
        this(query, null);
    }

    public String getQuery() {
        return query;
    }

    public String getModel() {
        return model;
    }
}
