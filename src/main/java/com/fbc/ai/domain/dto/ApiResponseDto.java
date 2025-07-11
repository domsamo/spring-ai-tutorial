package com.fbc.ai.domain.dto;

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

    @Schema(description = "OpenAI API 메타데이터")
    private final ApiResponseMetaDto meta;

    public ApiResponseDto(boolean success, T data, String error, ApiResponseMetaDto meta) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.meta = meta;
    }

    public ApiResponseDto(boolean success, T data, String error) {
        this(success, data, error, null);
    }

    public ApiResponseDto(boolean success, T data) {
        this(success, data, null, null);
    }

    public ApiResponseDto(boolean success, T data, ApiResponseMetaDto meta) {
        this(success, data, null, meta);
    }

    public ApiResponseDto(boolean success, String error) {
        this(success, null, error, null);
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

    public ApiResponseMetaDto getMeta() {
        return meta;
    }
}
