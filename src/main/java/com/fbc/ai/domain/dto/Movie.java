package com.fbc.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 Object 응답 포맷 record")
public record Movie(String title, String year) {
}
