package com.fbc.ai.service.rag;

import com.fbc.ai.document.DocumentParser;
import com.fbc.ai.exception.DocumentProcessingException;
import com.fbc.ai.service.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 문서 업로드, 검색, 그리고 검색 결과를 활용한 LLM 응답 생성을 담당합니다.
 */
@Slf4j
@Service
public class RagServiceImpl implements RagService {

    private final JdbcClient jdbcClient;
    private final WebClient webClient;
    private final String vectorApiKey;
    private final String defaultBucketId;
    private final String defaultWebhookUrl;
    private final VectorStore vectorStore;
    private final List<DocumentParser> parsers;

    public RagServiceImpl(
            JdbcClient jdbcClient, @Value("${vector.api.key:123}") String vectorApiKey,
            @Value("${default.bucket.id:123}") String defaultBucketId,
            @Value("{webhook.url:#{null}}") String defaultWebhookUrl,
            VectorStore vectorStore, List<DocumentParser> parsers
    ) {
        this.jdbcClient = jdbcClient;
        this.vectorApiKey = vectorApiKey;
        this.defaultBucketId = defaultBucketId;
        this.defaultWebhookUrl = defaultWebhookUrl;
        this.vectorStore = vectorStore;
        this.parsers = parsers;
        this.webClient = WebClient.builder()
                .baseUrl("https://live-stargate.sionic.im/api/v2")
                .build();
    }


    @Override
    public Mono<String> uploadFile(MultipartFile file) throws Exception{
        return uploadFile(file, defaultBucketId);
    }

    public Mono<String> uploadFile(MultipartFile file, String bucketId) throws Exception {
        log.info("문서 등록 요청 시작: {}, bucketId: {}", file.getOriginalFilename(), bucketId);

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        DocumentParser appropriateParser = findParserFor(extension);

        // 파일 업로드 (여러 확장자 지원)
        try {
            if (appropriateParser != null) {
                // 파서는 이제 Document 리스트(청크 리스트)를 반환
                List<Document> parsedChunks = appropriateParser.parse(file);

                // 각 청크의 메타데이터에 공통 정보(bucketId) 추가
                // Map<String, Object> baseMetadata = createBaseMetadata(file);

                for (Document chunk : parsedChunks) {
                    chunk.getMetadata().put("bucket_id", bucketId);
                    chunk.getMetadata().put("file_name", file.getOriginalFilename());
                    chunk.getMetadata().put("content_type", file.getContentType());
                }

                // 1000글자 단위로 자른다.
                // # 2.단계 : 문서분할(Split Documents)
                TokenTextSplitter splitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
                List<Document> splitDocuments = splitter.apply(parsedChunks);

                // # 3.단계 : 임베딩(Embedding) -> 4.단계 : DB에 저장(백터스토어 생성)
                vectorStore.accept(splitDocuments); // OpenAI 임베딩을 거친다.
                return Mono.just("success");
            } else {
                return Mono.error(new DocumentProcessingException("지원하지 않는 파일 형식입니다: " + file.getOriginalFilename()));
            }

        } catch (Exception e) {
            log.error("문서 업로드 준비 중 오류: {}", e.getMessage(), e);
            return Mono.error(new DocumentProcessingException("문서 업로드 준비 중 오류: " + e.getMessage(), e));
        }
    }

    private Map<String, Object> createBaseMetadata(MultipartFile file) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("file_name", file.getOriginalFilename());
        metadata.put("content_type", file.getContentType());
        return metadata;
    }

    private DocumentParser findParserFor(String extension) {
        for (DocumentParser parser : parsers) {
            if (parser.supports(extension)) {
                return parser;
            }
        }
        return null;
    }

    /**
     * API를 통해 문서 등록을 요청합니다.
     *
     * @param file 업로드할 파일
     * @param bucketId 버킷 ID (기본값: application.properties의 설정값)
     * @param parserType 파서 타입 (기본값: "DEFAULT")
     * @param webhookUrl 웹훅 URL (선택사항)
     * @return 생성된 문서 ID를 포함한 Mono
     */
//    public Mono<String> uploadFile(
//            MultipartFile file,
//            String bucketId,
//            String parserType,
//            String webhookUrl
//    ) {
//        log.info("문서 등록 요청 시작: {}, bucketId: {}", file.getOriginalFilename(), bucketId);
//
//        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
//
//        // 파일 업로드 (여러 확장자 지원)
//        try {
//            bodyBuilder.part("file", file.getBytes())
//                    .headers(headers ->
//                            headers.setContentDispositionFormData("file",
//                                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "document")
//                    );
//            bodyBuilder.part("bucketId", bucketId);
//            bodyBuilder.part("parserType", parserType);
//            if (webhookUrl != null) {
//                bodyBuilder.part("webhookUrl", webhookUrl);
//            }
//
//            log.info("요청 데이터: bucketId={}, parserType={}, webhookUrl={}", bucketId, parserType, webhookUrl);
//            log.info("파일 정보: name={}, size={}, contentType={}",
//                    file.getOriginalFilename(), file.getSize(), file.getContentType());
//
//            return webClient.post()
//                    .uri("/documents/by-file")
//                    .header("vector-api-key", vectorApiKey)
//                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .map(response -> {
//                        log.info("API 응답: {}", response);
//
//                        // 응답에서 documentId 추출
//                        Map<String, Object> data = (Map<String, Object>) response.get("data");
//                        if (data == null) {
//                            throw new DocumentProcessingException("응답에 data 필드가 없습니다: " + response);
//                        }
//
//                        String documentId = (String) data.get("documentId");
//                        if (documentId == null) {
//                            throw new DocumentProcessingException("data에서 documentId를 찾을 수 없습니다: " + data);
//                        }
//
//                        log.info("문서 등록 요청 완료: documentId={}", documentId);
//                        return documentId;
//                    })
//                    .onErrorMap(e -> {
//                        log.error("문서 등록 요청 실패: {}", e.getMessage(), e);
//                        if (e instanceof WebClientResponseException) {
//                            WebClientResponseException wcre = (WebClientResponseException) e;
//                            log.error("응답 상태: {}", wcre.getStatusCode());
//                            log.error("응답 본문: {}", wcre.getResponseBodyAsString());
//                        }
//                        return new DocumentProcessingException("문서 등록 요청 실패: " + e.getMessage(), e);
//                    });
//        } catch (Exception e) {
//            log.error("문서 업로드 준비 중 오류: {}", e.getMessage(), e);
//            return Mono.error(new DocumentProcessingException("문서 업로드 준비 중 오류: " + e.getMessage(), e));
//        }
//    }

    /**
     * 질문에 대한 답변을 생성하며, 참고한 정보 출처도 함께 제공합니다.
     *
     * @param question 사용자 질문
     * @param bucketIds 검색할 버킷 ID 목록 (null이면 모든 버킷에서 검색)
     * @return Storm AI 답변 응답을 포함한 Mono
     */
//    public Mono<StormAnswerResponseDto> generateAnswerWithContexts(
//            String question,
//            List<String> bucketIds
//    ) {
//        log.info("답변 생성 시작: question={}, bucketIds={}", question, bucketIds);
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("question", question);
//        if (bucketIds != null) {
//            requestBody.put("bucketIds", bucketIds);
//        }
//
//        return webClient.post()
//                .uri("/answer")
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .header("storm-api-key", vectorApiKey)
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .map(response -> {
//                    log.info("Storm API 응답: {}", response);
//
//                    // 응답 파싱
//                    Map<String, Object> data = (Map<String, Object>) response.get("data");
//                    if (data == null) {
//                        throw new DocumentProcessingException("응답 데이터가 올바르지 않습니다");
//                    }
//
//                    Map<String, Object> chatData = (Map<String, Object>) data.get("chat");
//                    if (chatData == null) {
//                        throw new DocumentProcessingException("채팅 데이터가 올바르지 않습니다");
//                    }
//
//                    List<Map<String, Object>> contextsData = (List<Map<String, Object>>) data.get("contexts");
//                    if (contextsData == null) {
//                        contextsData = new ArrayList<>();
//                    }
//
//                    String answer = (String) chatData.get("answer");
//                    if (answer == null) {
//                        throw new DocumentProcessingException("답변을 찾을 수 없습니다");
//                    }
//
//                    StormChatDto chat = new StormChatDto(
//                            chatData.get("question") != null ? chatData.get("question").toString() : question,
//                            answer
//                    );
//
//                    List<StormContextDto> contexts = contextsData.stream()
//                            .map(contextMap -> {
//                                String documentId = contextMap.get("id") != null ?
//                                        contextMap.get("id").toString() : "unknown";
//                                String content = contextMap.get("context") != null ?
//                                        contextMap.get("context").toString() : "";
//
//                                Map<String, Object> metadata = new HashMap<>(contextMap);
//                                metadata.remove("context");
//                                metadata.remove("id");
//
//                                return new StormContextDto(documentId, content, metadata);
//                            })
//                            .collect(Collectors.toList());
//
//                    StormAnswerResponseDto stormResponse = new StormAnswerResponseDto(chat, contexts);
//                    log.info("답변 생성 완료: {}개 컨텍스트 참조", stormResponse.getContexts().size());
//                    return stormResponse;
//                })
//                .onErrorMap(e -> {
//                    log.error("답변 생성 실패: {}", e.getMessage(), e);
//                    return new DocumentProcessingException("답변 생성 실패: " + e.getMessage(), e);
//                });
//    }

    /**
     * 질문과 관련된 문서 데이터를 검색합니다.
     *
     * @param question 사용자 질문
     * @return 검색된 문서 내용
     */
    public String findSimilarData(String question) {
        log.info("유사 문서 검색 시작: question={}", question);

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(2)  // 상위 2개 문서 검색
                .build();

        List<Document> documents = vectorStore.similaritySearch(request);
        log.info("검색된 문서 수: {}", documents.size());

        String result = documents
                .stream()
                .map(document -> document.getText())
                .collect(Collectors.joining("\n\n"));

        log.info("검색된 문서 내용 길이: {}", result.length());
        return result;
    }
}
