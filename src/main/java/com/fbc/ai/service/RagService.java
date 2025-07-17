package com.fbc.ai.service;

import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

/**
 * 문서 업로드, 검색, 그리고 검색 결과를 활용한 LLM 응답 생성을 담당합니다.
 */
public interface RagService {

    /**
     * 기본 파라미터를 사용하여 문서를 업로드합니다.
     *
     * @param file 업로드할 파일
     * @return 생성된 문서 ID를 포함한 Mono
     */
    Mono<String> uploadFile(MultipartFile file) throws Exception;


    /**
     * 버킷 ID를 지정하여 문서를 업로드합니다.
     *
     * @param file 업로드할 파일
     * @param bucketId 버킷 ID
     * @return 생성된 문서 ID를 포함한 Mono
     */
    Mono<String> uploadFile(MultipartFile file, String bucketId) throws Exception;


    /**
     * 질문과 관련된 문서 데이터를 검색합니다.
     * 
     * @param question 사용자 질문
     * @return 검색된 문서 내용
     */
    String findSimilarData(String question);

}
