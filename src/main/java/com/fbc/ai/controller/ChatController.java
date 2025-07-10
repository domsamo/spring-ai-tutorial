package com.fbc.ai.controller;

import com.fbc.ai.domain.dto.ApiResponseDto;
import com.fbc.ai.domain.dto.ChatRequestDto;
import com.fbc.ai.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Chat API 컨트롤러
 *
 * LLM API를 통해 채팅 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat API", description = "OpenAI API를 통한 채팅 기능")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 사용자의 메시지를 받아 OpenAI API로 응답 생성
     */
    @Operation(
            summary = "LLM 채팅 메시지 전송",
            description = "사용자의 메시지를 받아 OpenAI API를 통해 응답을 생성합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "LLM 응답 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping("/query")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> sendMessage(
            @Parameter(description = "채팅 요청 객체", required = true)
            @RequestBody ChatRequestDto request
    ) {
        log.info("Chat API 요청 받음: model={}", request.getModel());

        // 유효성 검사
        if (request.getQuery() == null || request.getQuery().isBlank()) {
            log.warn("빈 질의가 요청됨");
            return ResponseEntity.badRequest().body(
                    new ApiResponseDto<>(false, "질의가 비어있습니다.")
            );
        }

        try {
            // 시스템 프롬프트 지정
            String systemMessage = "You are a helpful AI assistant.";

            // AI 응답 생성
            ChatResponse response = chatService.openAiChat(
                    request.getQuery(),
                    systemMessage,
                    request.getModel()
            );

            if (response != null) {
                log.debug("LLM 응답 생성: {}", response);
                Map<String, Object> data = new HashMap<>();
                data.put("answer", response.getResult().getOutput().getText());
                return ResponseEntity.ok(
                        new ApiResponseDto<>(true, data)
                );
            } else {
                log.error("LLM 응답 생성 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new ApiResponseDto<>(false, "LLM 응답 생성 중 오류 발생")
                );
            }
        } catch (Exception e) {
            log.error("Chat API 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponseDto<>(false, e.getMessage() != null ? e.getMessage() : "알 수 없는 오류 발생")
            );
        }
    }
}
