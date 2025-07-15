package com.fbc.ai.controller.chat;

import com.fbc.ai.domain.dto.*;
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
import java.util.List;
import java.util.Map;

/**
 * Chat API 컨트롤러
 *
 * LLM API를 통해 채팅 기능을 제공.
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

    /** ----------------------------------------------------------------------------
     * 기본 OpenAI API로 응답 생성(Get)
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
    @GetMapping("/chat")
    public String chatMessage(
            @Parameter(description = "채팅 메시지", required = true)
            @RequestParam("query") String query) {

        ChatResponse response = chatService.chatWithResponse(query);
        return response.getResult().getOutput().getText();
    }

    /**
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> chatMessage(
                    @Parameter(description = "채팅 메시지", required = true)
                    @RequestParam("query") String query) {

        ChatResponse response = chatService.chatWithResponse(query);

        if (response != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("answer", response.getResult().getOutput().getText());

            // 메타데이터 추출
            String model = response.getMetadata().getModel();
            ApiResponseMetaDto metadata = chatService.extractMetadata(response, model);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, data, metadata)
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponseDto<>(false, "LLM 응답 생성 중 오류 발생")
            );
        }
    }
    */

    /** ----------------------------------------------------------------------------
     * PlaceHolder 연동
     */
    @Operation(
            summary = "[PlaceHolder 연동] LLM 채팅 메시지 전송",
            description = "사용자의 메시지를 받아 OpenAI API를 통해 응답을 생성합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "LLM 응답 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/chatplace")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> chatPlaceHolder(
            @Parameter(description = "채팅 메시지", required = true)
            @RequestParam("query") String query,

            @Parameter(description = "톤(friendly)", required = true)
            @RequestParam("tone") String tone,

            @Parameter(description = "주제(자바)", required = true)
            @RequestParam("subject") String subject
            ) {

        ChatResponse response = chatService.chatPlaceHolderWithResponse(subject, tone, query);

        if (response != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("answer", response.getResult().getOutput().getText());

            // 메타데이터 추출
            String model = response.getMetadata().getModel();
            ApiResponseMetaDto metadata = chatService.extractMetadata(response, model);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, data, metadata)
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponseDto<>(false, "LLM 응답 생성 중 오류 발생")
            );
        }
    }


    /** ----------------------------------------------------------------------------
     * 기본 OpenAI API로 응답 생성(Get)
     */
    @Operation(
            summary = "[ChatResponse] LLM 채팅 메시지 json 전송",
            description = "OpenAI API 응답(ChatResponse)을 json으로 전송합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "LLM 응답 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/chatjson")
    public ChatResponse chatResponseJson(
            @Parameter(description = "채팅 메시지", required = true)
            @RequestParam("query") String query) {

        return chatService.chatJson(query);
    }

    /**
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> chatResponseJson(
            @Parameter(description = "채팅 메시지", required = true)
            @RequestParam("query") String query) {

        ChatResponse response = chatService.chatJson(query);
        if (response != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("answer", response.getResult().getOutput().getText());
            data.put("rawResponse", response);

            // 메타데이터 추출
            String model = response.getMetadata().getModel();
            ApiResponseMetaDto metadata = chatService.extractMetadata(response, model);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, data, metadata)
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponseDto<>(false, "LLM 응답 생성 중 오류 발생")
            );
        }
    }
    */

    /** ----------------------------------------------------------------------------
     * User Prompt - PlaceHolder 연동
     */
    @Operation(
            summary = "[User Prompt - PlaceHolder 연동] 요리 레시피",
            description = "사용자의 메시지를 받아 레시피에 대한 응답 처리."
    )
    @ApiResponse(
            responseCode = "200",
            description = "LLM 응답 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/recipe")
    public Answer chatRecipe(
            @Parameter(description = "채팅 메시지(요리순서,재료)", required = true)
            @RequestParam("query") String query,

            @Parameter(description = "요리이름(햄버거)", required = true)
            @RequestParam("foodName") String foodName
    ) {
        return chatService.recipe(foodName, query);
    }


    /** ----------------------------------------------------------------------------
     * Structured Output - List
     */
    @Operation(
            summary = "[Structured Output - List]",
            description = "AI 응답을 List 형태로 변환 응답 처리."
    )
    @ApiResponse(
            responseCode = "200",
            description = "LLM 응답 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/chatList")
    public List<String> chatList(
            @Parameter(description = "채팅 메시지(미국의 주요 도시를 알려줘)", required = true)
            @RequestParam("query") String query
    ) {
        return chatService.chatList(query);
    }

    /** ----------------------------------------------------------------------------
     * Structured Output - Map
     */
    @Operation(
            summary = "[Structured Output - Map]",
            description = "AI 응답을 List 형태로 변환 응답 처리."
    )
    @ApiResponse(
            responseCode = "200",
            description = "LLM 응답 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/chatMap")
    public Map<String, String> chatMap(
            @Parameter(description = "채팅 메시지(국가와 그 국가의 수도를 5개 만들어줘)", required = true)
            @RequestParam("query") String query
    ) {
        return chatService.chatMap(query);
    }

    /** ----------------------------------------------------------------------------
     * Structured Output - Map
     */
    @Operation(
            summary = "[Structured Output - Customer Object]",
            description = "AI 응답을 사용자 정의 Object 형태로 변환 응답 처리."
    )
    @ApiResponse(
            responseCode = "200",
            description = "LLM 응답 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/chatMovie")
    public List<Movie> chatMovie(
            @Parameter(description = "영화감독이름(봉준호)", required = true)
            @RequestParam("directorName") String directorName
    ) {
        return chatService.chatMovie(directorName);
    }






    /** ----------------------------------------------------------------------------
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

                // 메타데이터 추출
                ApiResponseMetaDto metadata = chatService.extractMetadata(response, request.getModel());
                log.debug("메타데이터 추출: {}", metadata);

                return ResponseEntity.ok(
                        new ApiResponseDto<>(true, data, metadata)
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
