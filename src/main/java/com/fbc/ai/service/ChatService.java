package com.fbc.ai.service;

import com.fbc.ai.domain.dto.Answer;
import com.fbc.ai.domain.dto.ApiResponseMetaDto;
import com.fbc.ai.domain.dto.Movie;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;
import java.util.Map;

/**
 * Interface for Chat Service
 */
public interface ChatService {
    
    /**
     * 기본 채팅 기능
     *
     * @param query
     * @return
     */
    String chat(String query);
    
    /**
     * 기본 채팅 기능 (ChatResponse 반환)
     *
     * @param query
     * @return
     */
    ChatResponse chatWithResponse(String query);
    
    /**
     * PlaceHoder를 사용한 LLM 호출
     * @param subject
     * @param tone
     * @param query
     * @return
     */
    String chatPlaceHolder(String subject, String tone, String query);
    
    /**
     * PlaceHoder를 사용한 LLM 호출 (ChatResponse 반환)
     * @param subject
     * @param tone
     * @param query
     * @return
     */
    ChatResponse chatPlaceHolderWithResponse(String subject, String tone, String query);
    
    /**
     * ChatResponse 객체 json 리턴
     * @param query
     * @return
     */
    ChatResponse chatJson(String query);
    
    /**
     * User Prompt 처리 및 ResponseEntity 처리
     * @param foodName
     * @param query
     * @return
     */
    Answer recipe(String foodName, String query);
    
    /**
     * List 형태로 응답 데이터 Conversion
     *
     * @param query
     * @return
     */
    List<String> chatList(String query);
    
    /**
     * Map 형태로 응답 데이터 Conversion
     *
     * @param query
     * @return
     */
    Map<String, String> chatMap(String query);
    
    /**
     * 사용자 정의 응답 데이터 Conversion
     *
     * @param directorName
     * @return
     */
    List<Movie> chatMovie(String directorName);
    
    /**
     * AI Chat - simple
     * @param message
     * @return
     */
    String getResponse(String message);
    
    /**
     * 시스템 input으로 AI 채팅 처리
     */
    void startChat();
    
    /**
     * OpenAI 챗 API를 이용하여 응답을 생성합니다.
     *
     * @param userInput 사용자 입력 메시지
     * @param systemMessage 시스템 프롬프트
     * @param model 사용할 LLM 모델명
     * @return 챗 응답 객체, 오류 시 null
     */
    ChatResponse openAiChat(String userInput, String systemMessage, String model);
    
    /**
     * OpenAI 챗 API를 이용하여 응답을 생성합니다. (기본 모델 사용)
     *
     * @param userInput 사용자 입력 메시지
     * @param systemMessage 시스템 프롬프트
     * @return 챗 응답 객체, 오류 시 null
     */
    ChatResponse openAiChat(String userInput, String systemMessage);
    
    /**
     * ChatResponse에서 메타데이터를 추출하여 ApiResponseMetaDto 객체로 반환합니다.
     *
     * @param response ChatResponse 객체
     * @param model 사용된 모델명 (메타데이터에서 추출할 수 없는 경우 사용)
     * @return 메타데이터 객체, 추출 실패 시 기본값 반환
     */
    ApiResponseMetaDto extractMetadata(ChatResponse response, String model);
    
    /**
     * 모델을 지정하여 응답 생성
     * @param message
     * @return
     */
    String getResponseWithModel(String message);
    
    /**
     * 옵션을 지정하여 응답 생성
     * @param message
     * @return
     */
    String getResponseOptions(String message);
}