package com.fbc.ai.service.chat;

import com.fbc.ai.config.OpenAiConfig;
import com.fbc.ai.domain.dto.Answer;
import com.fbc.ai.domain.dto.ApiResponseMetaDto;
import com.fbc.ai.domain.dto.Movie;
import com.fbc.ai.service.ApiMetaService;
import com.fbc.ai.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * OpenAI API를 사용하여 질의응답을 수행하는 서비스 구현체
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatModel chatModel;
    private final ChatClient chatClient;
    private final OpenAiApi openAiApi;
    private final OpenAiConfig openAiConfig;
    private final ApiMetaService apiMetaService;

    public ChatServiceImpl(ChatModel chatModel, ChatClient chatClient, OpenAiApi openAiApi, OpenAiConfig openAiConfig, ApiMetaService apiMetaService) {
        this.chatModel = chatModel;
        this.chatClient = chatClient;
        this.openAiApi = openAiApi;
        this.openAiConfig = openAiConfig;
        this.apiMetaService = apiMetaService;
    }

    /**
     * 기본 채팅 기능
     *
     * @param query
     * @return
     */
    public String chat(String query) {
        return chatClient.prompt()  // 프롬프트 생성
                .user(query)        // 사용자 메시지
                .call()             // 호출
                .content();         // 요청정보를 받는 부분
    }

    /**
     * 기본 채팅 기능 (ChatResponse 반환)
     *
     * @param query
     * @return
     */
    public ChatResponse chatWithResponse(String query) {
        return chatClient.prompt()  // 프롬프트 생성
                .user(query)        // 사용자 메시지
                .call()             // 호출
                .chatResponse();    // ChatResponse 반환
    }

    /**
     * PlaceHoder를 사용한 LLM 호출
     * @param subject
     * @param tone
     * @param query
     * @return
     */
    public String chatPlaceHolder(String subject, String tone, String query) {
        return chatClient.prompt()  // 프롬프트 생성
                .user(query)
                .system(sp->
                    sp.param("subject", subject)
                            .param("tone", tone)
                    )
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
    }

    /**
     * PlaceHoder를 사용한 LLM 호출 (ChatResponse 반환)
     * @param subject
     * @param tone
     * @param query
     * @return
     */
    public ChatResponse chatPlaceHolderWithResponse(String subject, String tone, String query) {
        return chatClient.prompt()  // 프롬프트 생성
                .user(query)
                .system(sp->
                    sp.param("subject", subject)
                            .param("tone", tone)
                    )
                .call()
                .chatResponse();
    }

    /**
     * ChatResponse 객체 json 리턴
     * @param query
     * @return
     */
    public ChatResponse chatJson(String query) {
        return chatClient.prompt()  // 프롬프트 생성
                .user(query)        // 사용자 메시지
                .call()             // 호출
                .chatResponse();
    }

    /**
     * User Prompt for Recipe
     */
    private final String recipePrompt = """
        Answer for {foodName} for {query}?
        Answer in Korean.  
    """;

    /**
     * User Prompt 처리 및 ResponseEntity 처리
     * @param foodName
     * @param query
     * @return
     */
    public Answer recipe(String foodName, String query) {
        return chatClient.prompt()
                .user(userSpec -> userSpec.text(recipePrompt)
                    .param("foodName", foodName)
                    .param("query", query)
                ).call()
                .entity(Answer.class);
    }

    /**
     * List 형태로 응답 데이터 Conversion
     *
     * @param query
     * @return
     */
    public List<String> chatList(String query) {
        return chatClient.prompt()
                .user(query)
                .call()
                .entity(new ListOutputConverter(new DefaultConversionService()));
    }

    /**
     * Map 형태로 응답 데이터 Conversion
     *
     * @param query
     * @return
     */
    public Map<String, String> chatMap(String query) {
        return chatClient.prompt()
                .user(query)
                .call()
                .entity(new ParameterizedTypeReference<Map<String, String>>() { });
    }

    /**
     * 사용자 정의 응답 데이터 Conversion
     *
     * @param directorName
     * @return
     */
    public List<Movie> chatMovie(String directorName) {
        // "{directorName}가 감독한 영화 목록을 생성하세요. 감독이 알려지지 않은 경우, null을 반환하세요.
                //각 영화는 제목과 개봉 연도를 포함해야 합니다. {format}"
        String template= """
             "Generate a list of movies directed by {directorName}. If the director is unknown, return null.
             한국 영화는 한글로 표기해줘.
             Each movie should include a title and release year. {format}"
         """;

        List<Movie> movieList = chatClient.prompt()
                .user(userSpec -> userSpec.text(template)
                        .param("directorName", directorName)
                        .param("format", "json"))
                .call()
                .entity(new ParameterizedTypeReference<List<Movie>>() {});

        return movieList;
    }


    /**
     * AI Chat - simple
     * @param message
     * @return
     */
    public String getResponse(String message){
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 시스템 input으로 AI 채팅 처리
     */
    public void startChat(){
        Scanner scanner = new Scanner(System.in);
        log.info("Enter your message:");
        while (true){
            String message = scanner.nextLine();
            if(message.equals("exit")){
                log.info("Exiting chat...");
                break;
            }

            String response = getResponse(message);
            log.info("Bot: {}", response);
        }
        scanner.close();
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
        return openAiChat(userInput, systemMessage, openAiConfig.getDefaultModel());
    }

    /**
     * ChatResponse에서 메타데이터를 추출하여 ApiResponseMetaDto 객체로 반환합니다.
     *
     * @param response ChatResponse 객체
     * @param model 사용된 모델명 (메타데이터에서 추출할 수 없는 경우 사용)
     * @return 메타데이터 객체, 추출 실패 시 기본값 반환
     */
    public ApiResponseMetaDto extractMetadata(ChatResponse response, String model) {
        return apiMetaService.extractMetadata(response, model);
    }

    public String getResponseWithModel(String message){
        return chatModel.call(message);
    }

    public String getResponseOptions(String message){
        ChatResponse response = chatModel.call(
                new Prompt(
                        message,
                        OpenAiChatOptions.builder()
                                .model("gpt-4o")
                                .temperature(0.4)
                                .build()
                ));
        return response.getResult().getOutput().getText();
    }
}
