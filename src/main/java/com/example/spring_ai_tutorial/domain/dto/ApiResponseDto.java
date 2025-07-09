package com.example.spring_ai_tutorial.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API 표준 응답 포맷
 *
 * 모든 API 응답에 사용되는 공통 응답 포맷입니다.
 */
@Schema(description = "API 표준 응답 포맷")
public class ApiResponseDto<T> {
    @Schema(description = "요청 처리 성공 여부")
    private final boolean success;

    @Schema(description = "응답 데이터 (성공 시)")
    private final T data;

    @Schema(description = "오류 메시지 (실패 시)")
    private final String error;

    public ApiResponseDto(boolean success, T data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public ApiResponseDto(boolean success, T data) {
        this(success, data, null);
    }

    public ApiResponseDto(boolean success, String error) {
        this(success, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}