package com.fbc.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Storm AI 답변 생성 응답
 */
@Schema(description = "Storm AI 답변 생성 응답")
public class StormAnswerResponseDto {
    @Schema(description = "채팅 정보")
    private final StormChatDto chat;
    
    @Schema(description = "참조된 컨텍스트 목록")
    private final List<StormContextDto> contexts;

    public StormAnswerResponseDto(StormChatDto chat, List<StormContextDto> contexts) {
        this.chat = chat;
        this.contexts = contexts;
    }

    public StormChatDto getChat() {
        return chat;
    }

    public List<StormContextDto> getContexts() {
        return contexts;
    }
}