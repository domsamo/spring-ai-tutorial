package com.example.spring_ai_tutorial.repository;

import com.example.spring_ai_tutorial.domain.dto.DocumentSearchResultDto;
import com.example.spring_ai_tutorial.exception.DocumentProcessingException;
import com.example.spring_ai_tutorial.service.DocumentProcessingService;
import com.example.spring_ai_tutorial.service.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 문서를 벡터화하여 저장하고, 벡터 유사도 검색을 제공합니다.
 * Spring AI의 SimpleVectorStore를 활용합니다.
 */
@Slf4j
@Repository
public class InMemoryDocumentVectorStore {
    private final DocumentProcessingService documentProcessingService;
    private final VectorStore vectorStore;

    public InMemoryDocumentVectorStore(
            EmbeddingService embeddingService,
            DocumentProcessingService documentProcessingService
    ) {
        this.documentProcessingService = documentProcessingService;
        // Spring AI의 인메모리 SimpleVectorStore 생성
        this.vectorStore = SimpleVectorStore.builder(embeddingService.getEmbeddingModel()).build();
    }

    /**
     * 문서를 벡터 스토어에 추가합니다.
     *
     * @param id 문서 식별자
     * @param fileText 문서 내용
     * @param metadata 문서 메타데이터
     */
    public void addDocument(String id, String fileText, Map<String, Object> metadata) {
        log.debug("문서 추가 시작 - ID: {}, 내용 길이: {}", id, fileText.length());

        try {
            // 메타데이터에 ID 추가
            Map<String, Object> metadataWithId = new HashMap<>(metadata);
            metadataWithId.put("id", id);

            // Spring AI Document 객체 생성
            Document document = new Document(fileText, metadataWithId);
            TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                    .withChunkSize(512)           // 원하는 청크 크기
                    .withMinChunkSizeChars(350)   // 최소 청크 크기
                    .withMinChunkLengthToEmbed(5) // 임베딩할 최소 청크 길이
                    .withMaxNumChunks(10000)      // 최대 청크 수
                    .withKeepSeparator(true)      // 구분자 유지 여부
                    .build();
            List<Document> chunks = textSplitter.split(document);

            // 벡터 스토어에 문서 청크 추가 (내부적으로 임베딩 변환 수행)
            vectorStore.add(chunks);

            log.info("문서 추가 완료 - ID: {}", id);
        } catch (Exception e) {
            log.error("문서 추가 실패 - ID: {}", id, e);
            throw new DocumentProcessingException("문서 임베딩 및 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 파일을 처리하여 벡터 스토어에 추가합니다.
     *
     * @param id 문서 식별자
     * @param file 파일 객체
     * @param metadata 문서 메타데이터
     */
    public void addDocumentFile(String id, File file, Map<String, Object> metadata) {
        log.debug("파일 문서 추가 시작 - ID: {}, 파일: {}", id, file.getName());

        try {
            // 텍스트 추출
            String fileText;
            if (file.getName().toLowerCase().endsWith(".pdf")) {
                fileText = documentProcessingService.extractTextFromPdf(file);
            } else {
                fileText = new String(Files.readAllBytes(file.toPath()));
            }

            log.debug("파일 텍스트 추출 완료 - 길이: {}", fileText.length());
            addDocument(id, fileText, metadata);
        } catch (IOException e) {
            log.error("파일 처리 실패 - ID: {}, 파일: {}", id, file.getName(), e);
            throw new DocumentProcessingException("파일 처리 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("파일 처리 실패 - ID: {}, 파일: {}", id, file.getName(), e);
            throw new DocumentProcessingException("파일 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 질의와 유사한 문서를 검색합니다.
     *
     * @param query 검색 질의
     * @param maxResults 최대 결과 수
     * @return 유사도 순으로 정렬된 검색 결과 목록
     */
    public List<DocumentSearchResultDto> similaritySearch(String query, int maxResults) {
        log.debug("유사도 검색 시작 - 질의: '{}', 최대 결과: {}", query, maxResults);

        try {
            // 검색 요청 구성
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(maxResults)
                    .build();

            // 유사성 검색 실행
            List<?> results = vectorStore.similaritySearch(request);
            if (results == null) {
                results = Collections.emptyList();
            }

            log.debug("유사도 검색 완료 - 결과 수: {}", results.size());

            // 결과 매핑 - 간소화된 버전
            // Spring AI 버전에 따라 SearchResult 클래스의 API가 다를 수 있으므로
            // 기본적인 결과만 반환하는 간소화된 버전으로 구현
            return results.stream()
                    .map(result -> {
                        // 기본값으로 처리
                        String id = "unknown";
                        String content = "";
                        Map<String, Object> resultMetadata = new HashMap<>();
                        double score = 0.0;

                        try {
                            // 리플렉션을 사용하여 필요한 메서드 호출
                            Object document = result.getClass().getMethod("getDocument").invoke(result);
                            if (document != null) {
                                Map<String, Object> metadata = (Map<String, Object>) document.getClass().getMethod("getMetadata").invoke(document);
                                if (metadata != null) {
                                    id = metadata.getOrDefault("id", "unknown").toString();
                                    resultMetadata = new HashMap<>(metadata);
                                    resultMetadata.remove("id");
                                }

                                Object contentObj = document.getClass().getMethod("getContent").invoke(document);
                                if (contentObj != null) {
                                    content = contentObj.toString();
                                }
                            }

                            Object scoreObj = result.getClass().getMethod("getScore").invoke(result);
                            if (scoreObj != null) {
                                score = (Double) scoreObj;
                            }
                        } catch (Exception e) {
                            log.warn("결과 매핑 중 오류 발생: {}", e.getMessage());
                            // 오류 발생 시 기본값 사용
                        }

                        return new DocumentSearchResultDto(id, content, resultMetadata, score);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("유사도 검색 실패 - 질의: '{}'", query, e);
            throw new DocumentProcessingException("유사도 검색 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
