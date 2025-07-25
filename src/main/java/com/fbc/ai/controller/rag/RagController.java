package com.fbc.ai.controller.rag;

import com.fbc.ai.config.OpenAiConfig;
import com.fbc.ai.domain.dto.*;
import com.fbc.ai.service.ApiMetaService;
import com.fbc.ai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

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
    private final OpenAiApi openAiApi;
    private final OpenAiConfig openAiConfig;
    private final ApiMetaService apiMetaService;

    // # 6.단계 : 프롬프트 생성(Create Prompt)
    private String prompt = """
            You are an assistant for question-answering tasks.
            Use the following pieces of retrieved context to answer the question.
            If you don't know the answer, just say that you don.t know.
            Answer in Korean.  

            #Question:
            {input}               

            #Context :
            {documents}

            #Answer:                                    
            """;
    /*
            질문에 답변하는 것입니다. 질문에 정확하게 답변하기 위해 문서에 있는 정보를 사용해야 합니다.
            만약 정보가 부족하거나 문서에서 답을 찾을 수 없다면, 알지 못한다고 간단히 답변하세요.
            답변은 한글로 해줘
     */

    public RagController(RagService ragService, OpenAiApi openAiApi, OpenAiConfig openAiConfig, ApiMetaService apiMetaService) {
        this.ragService = ragService;
        this.openAiApi = openAiApi;
        this.openAiConfig = openAiConfig;
        this.apiMetaService = apiMetaService;
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
    ) throws Exception {
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
     * PDF 문서 기반 질의응답 API
     *
     * 질문을 받아 PDF 문서에서 관련 정보를 검색하고 AI 모델을 통해 답변을 생성합니다.
     */
    @Operation(
            summary = "Vector DB 문서(PDF) 기반 질의응답",
            description = "질문을 받아 PDF 문서에서 관련 정보를 Vector DB에서 검색하고 AI 모델을 통해 답변을 생성합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "질의응답 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @GetMapping("/answer")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> answer(
            @Parameter(description = "질문 내용", required = true, example = "가디언 에이전트가 2030년까지 에이전틱 AI 시장에서 몇%를 차지할 것으로 예측해?")
            @RequestParam String question,
            @Parameter(description = "사용할 OpenAI 모델", required = false, example = "gpt-3.5-turbo")
            @RequestParam(required = false) String model
    ) {
        // 모델이 지정되지 않은 경우 기본 모델 사용
        String modelToUse = (model != null && !model.isBlank()) ? model : openAiConfig.getDefaultModel();
        log.info("PDF QA API 요청 받음: question={}, model={}", question, modelToUse);

        // 유효성 검사
        if (question == null || question.isBlank()) {
            log.warn("빈 질의가 요청됨");
            return ResponseEntity.badRequest().body(
                    new ApiResponseDto<>(false, "질의가 비어있습니다.")
            );
        }

        try {
            PromptTemplate template = new PromptTemplate(prompt);

            Map<String, Object> promptsParameters = new HashMap<>();
            promptsParameters.put("input", question);
            promptsParameters.put("documents", ragService.findSimilarData(question));

            // 모델 옵션 설정
            ChatOptions chatOptions = ChatOptions.builder()
                    .model(modelToUse)
                    .build();

            // 프롬프트 생성
            Prompt promptWithOptions = new Prompt(template.create(promptsParameters).getContents(), chatOptions);

            // 챗 모델 생성
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .build();

            // 응답 객체 저장
            ChatResponse response = chatModel.call(promptWithOptions);

            String answer = response.getResult().getOutput().getText();

            log.debug("AI 응답 생성 완료: {}", answer);

            // 메타데이터 추출
            ApiResponseMetaDto metadata = apiMetaService.extractMetadata(response, modelToUse);
            log.debug("메타데이터 추출: {}", metadata);

            Map<String, Object> data = new HashMap<>();
            data.put("answer", answer);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, data, metadata)
            );
        } catch (Exception e) {
            log.error("PDF QA API 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponseDto<>(false, e.getMessage() != null ? e.getMessage() : "알 수 없는 오류 발생")
            );
        }
    }


    /**
     * 사용자 질의에 대해 관련 문서를 검색하고 RAG 기반 응답을 생성합니다.
     */
//    @Operation(
//            summary = "RAG 질의 수행",
//            description = "사용자 질문에 대해 관련 문서를 검색하고 RAG 기반 응답을 생성합니다."
//    )
//    @ApiResponse(
//            responseCode = "200",
//            description = "질의 성공",
//            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
//    )
//    @ApiResponse(responseCode = "400", description = "잘못된 요청")
//    @ApiResponse(responseCode = "500", description = "서버 오류")
//    @PostMapping("/query")
//    public Mono<ResponseEntity<ApiResponseDto<QueryResponseDto>>> queryWithRag(
//            @Parameter(description = "질의 요청 객체", required = true)
//            @RequestBody QueryRequestDto request
//    ) {
//        log.info("RAG 질의 요청 받음: {}", request.getQuery());
//
//        // 유효성 검사
//        if (request.getQuery() == null || request.getQuery().isBlank()) {
//            log.warn("빈 질의가 요청됨");
//            return Mono.just(ResponseEntity.badRequest().body(
//                    new ApiResponseDto<QueryResponseDto>(false, "질의가 비어있습니다.")
//            ));
//        }
//
//        return ragService.generateAnswerWithContexts(request.getQuery(), request.getBucketIds())
//                .map(stormResponse -> {
//                    List<DocumentResponseDto> relevantDocuments = stormResponse.getContexts().stream()
//                            .map(DocumentDtoUtil::toDocumentResponseDto)
//                            .collect(Collectors.toList());
//
//                    QueryResponseDto queryResponse = new QueryResponseDto(
//                            stormResponse.getChat().getQuestion(),
//                            stormResponse.getChat().getAnswer(),
//                            relevantDocuments
//                    );
//
//                    return ResponseEntity.ok(new ApiResponseDto<>(true, queryResponse));
//                })
//                .onErrorResume(e -> {
//                    log.error("RAG 질의 처리 중 오류 발생", e);
//                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                            new ApiResponseDto<QueryResponseDto>(false, "질의 처리 중 오류가 발생했습니다: " + e.getMessage())
//                    ));
//                });
//    }
}
