package com.fbc.ai.controller.rag;

import com.fbc.ai.config.OpenAiConfig;
import com.fbc.ai.domain.dto.ApiResponseDto;
import com.fbc.ai.domain.dto.ApiResponseMetaDto;
import com.fbc.ai.service.ApiMetaService;
import com.fbc.ai.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PDF 문서 기반 질의응답 API 컨트롤러
 *
 * PDF 문서에서 추출한 정보를 기반으로 질문에 답변하는 API를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/pdf")
@Tag(name = "PDF QA API", description = "PDF 문서 기반 질의응답 API")
public class PdfController {

    private final OpenAiApi openAiApi;
    private final OpenAiConfig openAiConfig;
    private final VectorStore vectorStore;
    private final ChatService chatService;
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

    public PdfController(OpenAiApi openAiApi, OpenAiConfig openAiConfig, VectorStore vectorStore, ChatService chatService, ApiMetaService apiMetaService) {
        this.openAiApi = openAiApi;
        this.openAiConfig = openAiConfig;
        this.vectorStore = vectorStore;
        this.chatService = chatService;
        this.apiMetaService = apiMetaService;
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
            promptsParameters.put("documents", findSimilarData(question));

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
            org.springframework.ai.chat.model.ChatResponse response = chatModel.call(promptWithOptions);

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
     * 질문과 관련된 문서 데이터를 검색합니다.
     * 
     * @param question 사용자 질문
     * @return 검색된 문서 내용
     */
    private String findSimilarData(String question) {
        log.debug("유사 문서 검색 시작: question={}", question);

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(2)  // 상위 2개 문서 검색
                .build();

        List<Document> documents = vectorStore.similaritySearch(request);
        log.debug("검색된 문서 수: {}", documents.size());

        String result = documents
                .stream()
                .map(document -> document.getText())
                .collect(Collectors.joining("\n\n"));

        log.debug("검색된 문서 내용 길이: {}", result.length());
        return result;
    }
}
