package com.fbc.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API Answer 응답 포맷")
public class Answer {
    @Schema(description = "읃답내용")
    private String answer;

    public Answer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    @Override
    public String toString() {
        return "Answer{" +
                "answer='" + answer + '\'' +
                '}';
    }

}
