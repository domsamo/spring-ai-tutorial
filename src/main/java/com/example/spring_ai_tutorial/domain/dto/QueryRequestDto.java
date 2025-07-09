package com.example.spring_ai_tutorial.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 질의 요청 데이터 모델
 */
@Schema(description = "질의 요청 데이터 모델")
public class QueryRequestDto {
    @Schema(description = "사용자 질문", example = "인공지능이란 무엇인가요?")
    private final String query;

    @Schema(description = "검색할 버킷 ID 목록 (선택사항)", example = "[\"bucket1\", \"bucket2\"]")
    private final List<String> bucketIds;

    @Schema(description = "최대 검색 결과 수", example = "3", defaultValue = "3")
    private final int maxResults;

    @Schema(description = "사용할 LLM 모델", example = "gpt-3.5-turbo", defaultValue = "gpt-3.5-turbo")
    private final String model;

    public QueryRequestDto(String query, List<String> bucketIds, int maxResults, String model) {
        this.query = query;
        this.bucketIds = bucketIds;
        this.maxResults = maxResults;
        this.model = model;
    }

    public QueryRequestDto(String query, List<String> bucketIds, int maxResults) {
        this(query, bucketIds, maxResults, "gpt-3.5-turbo");
    }

    public QueryRequestDto(String query, List<String> bucketIds) {
        this(query, bucketIds, 3, "gpt-3.5-turbo");
    }

    public QueryRequestDto(String query) {
        this(query, null, 3, "gpt-3.5-turbo");
    }

    public String getQuery() {
        return query;
    }

    public List<String> getBucketIds() {
        return bucketIds;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public String getModel() {
        return model;
    }
}