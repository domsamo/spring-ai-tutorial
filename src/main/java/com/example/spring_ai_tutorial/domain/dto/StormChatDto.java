package com.example.spring_ai_tutorial.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Storm API 응답 타입들
 */
@Schema(description = "Storm AI 채팅 응답")
public class StormChatDto {
    @Schema(description = "질문")
    private final String question;
    
    @Schema(description = "답변")
    private final String answer;

    public StormChatDto(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}