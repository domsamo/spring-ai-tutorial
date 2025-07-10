package com.fbc.ai.service;

import com.fbc.ai.domain.dto.StormAnswerResponseDto;
import com.fbc.ai.domain.dto.StormChatDto;
import com.fbc.ai.domain.dto.StormContextDto;
import com.fbc.ai.exception.DocumentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 문서 업로드, 검색, 그리고 검색 결과를 활용한 LLM 응답 생성을 담당합니다.
 */
@Service
public class RagService {
    private final Logger logger = LoggerFactory.getLogger(RagService.class);
    private final WebClient webClient;
    private final String stormApiKey;
    private final String defaultBucketId;
    private final String defaultWebhookUrl;

    public RagService(
            @Value("${storm.api.key:123}") String stormApiKey,
            @Value("${storm.default.bucket.id:123}") String defaultBucketId,
            @Value("{webhook.url:#{null}}") String defaultWebhookUrl
    ) {
        this.stormApiKey = stormApiKey;
        this.defaultBucketId = defaultBucketId;
        this.defaultWebhookUrl = defaultWebhookUrl;
        this.webClient = WebClient.builder()
                .baseUrl("https://live-stargate.sionic.im/api/v2")
                .build();
    }

    /**
     * Storm API를 통해 문서 등록을 요청합니다.
     *
     * @param file 업로드할 파일
     * @param bucketId 버킷 ID (기본값: application.properties의 설정값)
     * @param parserType 파서 타입 (기본값: "DEFAULT")
     * @param webhookUrl 웹훅 URL (선택사항)
     * @return 생성된 문서 ID를 포함한 Mono
     */
    public Mono<String> uploadFile(
            MultipartFile file,
            String bucketId,
            String parserType,
            String webhookUrl
    ) {
        logger.info("문서 등록 요청 시작: {}, bucketId: {}", file.getOriginalFilename(), bucketId);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        // 파일 업로드 (여러 확장자 지원)
        try {
            bodyBuilder.part("file", file.getBytes())
                    .headers(headers -> 
                            headers.setContentDispositionFormData("file", 
                                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "document")
                    );
            bodyBuilder.part("bucketId", bucketId);
            bodyBuilder.part("parserType", parserType);
            if (webhookUrl != null) {
                bodyBuilder.part("webhookUrl", webhookUrl);
            }

            logger.debug("요청 데이터: bucketId={}, parserType={}, webhookUrl={}", bucketId, parserType, webhookUrl);
            logger.debug("파일 정보: name={}, size={}, contentType={}", 
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            return webClient.post()
                    .uri("/documents/by-file")
                    .header("storm-api-key", stormApiKey)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        logger.debug("Storm API 응답: {}", response);

                        // 응답에서 documentId 추출
                        Map<String, Object> data = (Map<String, Object>) response.get("data");
                        if (data == null) {
                            throw new DocumentProcessingException("응답에 data 필드가 없습니다: " + response);
                        }
                        
                        String documentId = (String) data.get("documentId");
                        if (documentId == null) {
                            throw new DocumentProcessingException("data에서 documentId를 찾을 수 없습니다: " + data);
                        }

                        logger.info("문서 등록 요청 완료: documentId={}", documentId);
                        return documentId;
                    })
                    .onErrorMap(e -> {
                        logger.error("문서 등록 요청 실패: {}", e.getMessage(), e);
                        if (e instanceof WebClientResponseException) {
                            WebClientResponseException wcre = (WebClientResponseException) e;
                            logger.error("응답 상태: {}", wcre.getStatusCode());
                            logger.error("응답 본문: {}", wcre.getResponseBodyAsString());
                        }
                        return new DocumentProcessingException("문서 등록 요청 실패: " + e.getMessage(), e);
                    });
        } catch (Exception e) {
            logger.error("문서 업로드 준비 중 오류: {}", e.getMessage(), e);
            return Mono.error(new DocumentProcessingException("문서 업로드 준비 중 오류: " + e.getMessage(), e));
        }
    }

    /**
     * 기본 파라미터를 사용하여 문서를 업로드합니다.
     *
     * @param file 업로드할 파일
     * @return 생성된 문서 ID를 포함한 Mono
     */
    public Mono<String> uploadFile(MultipartFile file) {
        return uploadFile(file, defaultBucketId, "DEFAULT", defaultWebhookUrl);
    }

    /**
     * 버킷 ID를 지정하여 문서를 업로드합니다.
     *
     * @param file 업로드할 파일
     * @param bucketId 버킷 ID
     * @return 생성된 문서 ID를 포함한 Mono
     */
    public Mono<String> uploadFile(MultipartFile file, String bucketId) {
        return uploadFile(file, bucketId, "DEFAULT", defaultWebhookUrl);
    }

    /**
     * 질문에 대한 답변을 생성하며, 참고한 정보 출처도 함께 제공합니다.
     *
     * @param question 사용자 질문
     * @param bucketIds 검색할 버킷 ID 목록 (null이면 모든 버킷에서 검색)
     * @return Storm AI 답변 응답을 포함한 Mono
     */
    public Mono<StormAnswerResponseDto> generateAnswerWithContexts(
            String question,
            List<String> bucketIds
    ) {
        logger.info("답변 생성 시작: question={}, bucketIds={}", question, bucketIds);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("question", question);
        if (bucketIds != null) {
            requestBody.put("bucketIds", bucketIds);
        }

        return webClient.post()
                .uri("/answer")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("storm-api-key", stormApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    logger.debug("Storm API 응답: {}", response);

                    // 응답 파싱
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    if (data == null) {
                        throw new DocumentProcessingException("응답 데이터가 올바르지 않습니다");
                    }
                    
                    Map<String, Object> chatData = (Map<String, Object>) data.get("chat");
                    if (chatData == null) {
                        throw new DocumentProcessingException("채팅 데이터가 올바르지 않습니다");
                    }
                    
                    List<Map<String, Object>> contextsData = (List<Map<String, Object>>) data.get("contexts");
                    if (contextsData == null) {
                        contextsData = new ArrayList<>();
                    }

                    String answer = (String) chatData.get("answer");
                    if (answer == null) {
                        throw new DocumentProcessingException("답변을 찾을 수 없습니다");
                    }

                    StormChatDto chat = new StormChatDto(
                            chatData.get("question") != null ? chatData.get("question").toString() : question,
                            answer
                    );

                    List<StormContextDto> contexts = contextsData.stream()
                            .map(contextMap -> {
                                String documentId = contextMap.get("id") != null ? 
                                        contextMap.get("id").toString() : "unknown";
                                String content = contextMap.get("context") != null ? 
                                        contextMap.get("context").toString() : "";
                                
                                Map<String, Object> metadata = new HashMap<>(contextMap);
                                metadata.remove("context");
                                metadata.remove("id");
                                
                                return new StormContextDto(documentId, content, metadata);
                            })
                            .collect(Collectors.toList());

                    StormAnswerResponseDto stormResponse = new StormAnswerResponseDto(chat, contexts);
                    logger.info("답변 생성 완료: {}개 컨텍스트 참조", stormResponse.getContexts().size());
                    return stormResponse;
                })
                .onErrorMap(e -> {
                    logger.error("답변 생성 실패: {}", e.getMessage(), e);
                    return new DocumentProcessingException("답변 생성 실패: " + e.getMessage(), e);
                });
    }
}