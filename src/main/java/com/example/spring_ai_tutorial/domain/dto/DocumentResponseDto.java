package com.example.spring_ai_tutorial.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * 문서 응답 데이터
 */
@Schema(description = "문서 응답 데이터")
public class DocumentResponseDto {
    @Schema(description = "문서 ID")
    private final String id;

    @Schema(description = "문서 내용 (일부)")
    private final String content;

    @Schema(description = "문서 메타데이터")
    private final Map<String, Object> metadata;

    public DocumentResponseDto(String id, String content, Map<String, Object> metadata) {
        this.id = id;
        this.content = content;
        this.metadata = metadata;
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
}