package com.fbc.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OpenAI API 응답 메타데이터
 *
 * OpenAI API 호출 시 반환되는 메타데이터 정보를 저장합니다.
 */
@Schema(description = "OpenAI API 응답 메타데이터")
public class ApiResponseMetaDto {
    @Schema(description = "프롬프트 토큰 사용량")
    private final Integer promptTokens;

    @Schema(description = "완성 토큰 사용량")
    private final Integer completionTokens;

    @Schema(description = "총 토큰 사용량")
    private final Integer totalTokens;

    @Schema(description = "사용된 모델명")
    private final String model;

    public ApiResponseMetaDto(Integer promptTokens, Integer completionTokens, Integer totalTokens, String model) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.model = model;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public String getModel() {
        return model;
    }
}