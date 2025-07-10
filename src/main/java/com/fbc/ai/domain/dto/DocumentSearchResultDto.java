package com.fbc.ai.domain.dto;

import java.util.Map;

/**
 * 문서 검색 결과 데이터
 */
public class DocumentSearchResultDto {
    private final String id;
    private final String content;
    private final Map<String, Object> metadata;
    private final double score;

    public DocumentSearchResultDto(String id, String content, Map<String, Object> metadata, double score) {
        this.id = id;
        this.content = content;
        this.metadata = metadata;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public double getScore() {
        return score;
    }
}