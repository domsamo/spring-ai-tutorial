package com.fbc.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.Map;

/**
 * Storm API 컨텍스트 정보
 */
@Schema(description = "Storm AI 컨텍스트 정보")
public class StormContextDto {
    @Schema(description = "문서 ID")
    private final String documentId;
    
    @Schema(description = "문서 내용")
    private final String content;
    
    @Schema(description = "메타데이터")
    private final Map<String, Object> metadata;

    public StormContextDto(String documentId, String content, Map<String, Object> metadata) {
        this.documentId = documentId;
        this.content = content;
        this.metadata = metadata;
    }

    public StormContextDto(String documentId, String content) {
        this(documentId, content, Collections.emptyMap());
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}