package com.fbc.ai.service;

import com.fbc.ai.domain.dto.ApiResponseMetaDto;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * OpenAI API 메타데이터 추출 서비스 인터페이스
 *
 * ChatResponse에서 메타데이터를 추출하여 ApiResponseMetaDto 객체로 변환합니다.
 */
public interface ApiMetaService {
    
    /**
     * ChatResponse에서 메타데이터를 추출하여 ApiResponseMetaDto 객체로 반환합니다.
     *
     * @param response ChatResponse 객체
     * @param model 사용된 모델명 (메타데이터에서 추출할 수 없는 경우 사용)
     * @return 메타데이터 객체, 추출 실패 시 기본값 반환
     */
    ApiResponseMetaDto extractMetadata(ChatResponse response, String model);
}