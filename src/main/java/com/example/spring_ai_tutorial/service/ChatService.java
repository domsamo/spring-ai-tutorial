package com.example.spring_ai_tutorial.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * OpenAI API를 사용하여 질의응답을 수행하는 서비스
 */
@Slf4j
@Service
public class ChatService {
    private final OpenAiApi openAiApi;

    public ChatService(OpenAiApi openAiApi) {
        this.openAiApi = openAiApi;
    }

    /**
     * OpenAI 챗 API를 이용하여 응답을 생성합니다.
     *
     * @param userInput 사용자 입력 메시지
     * @param systemMessage 시스템 프롬프트
     * @param model 사용할 LLM 모델명
     * @return 챗 응답 객체, 오류 시 null
     */
    public ChatResponse openAiChat(
            String userInput,
            String systemMessage,
            String model
    ) {
        log.debug("OpenAI 챗 호출 시작 - 모델: {}", model);
        try {
            // 메시지 구성
            List<org.springframework.ai.chat.messages.Message> messages = Arrays.asList(
                    new SystemMessage(systemMessage),
                    new UserMessage(userInput)
            );

            // 챗 옵션 설정
            ChatOptions chatOptions = ChatOptions.builder()
                    .model(model)
                    .build();

            // 프롬프트 생성
            Prompt prompt = new Prompt(messages, chatOptions);

            // 챗 모델 생성 및 호출
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .build();

            return chatModel.call(prompt);
        } catch (Exception e) {
            log.error("OpenAI 챗 호출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * OpenAI 챗 API를 이용하여 응답을 생성합니다. (기본 모델 사용)
     *
     * @param userInput 사용자 입력 메시지
     * @param systemMessage 시스템 프롬프트
     * @return 챗 응답 객체, 오류 시 null
     */
    public ChatResponse openAiChat(String userInput, String systemMessage) {
        return openAiChat(userInput, systemMessage, "gpt-3.5-turbo");
    }
}
