package com.fbc.ai.controller;

import com.fbc.ai.domain.dto.*;
import com.fbc.ai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG(Retrieval-Augmented Generation) API 컨트롤러
 *
 * 문서 업로드 및 질의응답 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rag")
@Tag(name = "RAG API", description = "Retrieval-Augmented Generation 기능을 위한 API")
public class RagController {
    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * AI Agent 에게 문서를 등록합니다.
     */
    @Operation(
            summary = "문서 등록",
            description = "파일이 벡터 스토어에 저장되며, 추후 질의시 컨텍스트로 활용됩니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "문서 등록 요청 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ApiResponseDto<DocumentUploadResultDto>>> uploadDocument(
            @Parameter(description = "업로드할 파일", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "버킷 ID (선택사항, 기본값은 설정된 기본 버킷)")
            @RequestParam(value = "bucketId", required = false) String bucketId
    ) {
        log.info("문서 등록 요청 받음: {}", file.getOriginalFilename());

        // 파일 유효성 검사
        if (file.isEmpty()) {
            log.warn("빈 파일이 업로드됨");
            return Mono.just(ResponseEntity.badRequest().body(
                    new ApiResponseDto<>(false, "업로드된 파일이 비어있습니다.")
            ));
        }

        Mono<String> documentIdMono;
        if (bucketId != null) {
            documentIdMono = ragService.uploadFile(file, bucketId);
        } else {
            documentIdMono = ragService.uploadFile(file);
        }

        return documentIdMono
                .map(documentId -> {
                    log.info("문서 등록 요청 성공: {}", documentId);
                    DocumentUploadResultDto resultDto = new DocumentUploadResultDto(
                            documentId,
                            "문서 등록이 성공적으로 요청되었습니다."
                    );
                    return ResponseEntity.ok(new ApiResponseDto<>(true, resultDto));
                })
                .onErrorResume(e -> {
                    log.error("문서 처리 중 오류 발생", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            new ApiResponseDto<DocumentUploadResultDto>(false, "문서 처리 중 오류가 발생했습니다: " + e.getMessage())
                    ));
                });
    }

    /**
     * 사용자 질의에 대해 관련 문서를 검색하고 RAG 기반 응답을 생성합니다.
     */
    @Operation(
            summary = "RAG 질의 수행",
            description = "사용자 질문에 대해 관련 문서를 검색하고 RAG 기반 응답을 생성합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "질의 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping("/query")
    public Mono<ResponseEntity<ApiResponseDto<QueryResponseDto>>> queryWithRag(
            @Parameter(description = "질의 요청 객체", required = true)
            @RequestBody QueryRequestDto request
    ) {
        log.info("RAG 질의 요청 받음: {}", request.getQuery());

        // 유효성 검사
        if (request.getQuery() == null || request.getQuery().isBlank()) {
            log.warn("빈 질의가 요청됨");
            return Mono.just(ResponseEntity.badRequest().body(
                    new ApiResponseDto<QueryResponseDto>(false, "질의가 비어있습니다.")
            ));
        }

        return ragService.generateAnswerWithContexts(request.getQuery(), request.getBucketIds())
                .map(stormResponse -> {
                    List<DocumentResponseDto> relevantDocuments = stormResponse.getContexts().stream()
                            .map(DocumentDtoUtil::toDocumentResponseDto)
                            .collect(Collectors.toList());

                    QueryResponseDto queryResponse = new QueryResponseDto(
                            stormResponse.getChat().getQuestion(),
                            stormResponse.getChat().getAnswer(),
                            relevantDocuments
                    );

                    return ResponseEntity.ok(new ApiResponseDto<>(true, queryResponse));
                })
                .onErrorResume(e -> {
                    log.error("RAG 질의 처리 중 오류 발생", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            new ApiResponseDto<QueryResponseDto>(false, "질의 처리 중 오류가 발생했습니다: " + e.getMessage())
                    ));
                });
    }
}
