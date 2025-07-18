# Spring AI Tutorial

Spring AI를 활용하여 LLM 호출부터 RAG 파이프라인 구축 실습 프로젝트입니다.

**언어**: Java  
**프레임워크**: Spring Boot 3.4.4        
**Spring AI 버전**: `1.0.0-M6`

## --- 환경 설정 ---
### 1. API Key 설정
**${OPENAI_API_KEY}** 값은 윈도우 시스템 환경변수에 직접 설정 

`application.yml` 
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```
### 2. 프로젝트 빌드 및 실행

```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

애플리케이션이 성공적으로 시작되면 다음 주소에서 접근 가능합니다.
- **메인 서버**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html



## --- AI Model ---
| Category          | Description                              |   Exsample   |
|:------------------|:-----------------------------------------|:------------:|
| Chat Models       | 자연어 처리 및 대화 기능을 제공하는 LLM 기반 모델           | AI 챗봇, Q&A 시스템 |
| Embedding Models  | 텍스트를 벡터로 변환하여 유사도 검색 및 문서 검색            | 문서 유사도 비교, RAG 서비스 |
| Image Models      | 주어진 프롬프트로 이미지 생성 및 수정 기능을 제공            | 이미지 생성, 이미지 편집 |
| Audio Models      | 음성을 텍스트로 변환(STT)하거나 텍스트를 음성으로 변환(TTS)  | 음성 인식, 음성 합성 |
| Moderation Models | 콘텐츠 검열 및 부적절한 텍스트/이미지를 감지하는 모델        | 텍스트 필터링, 부적절 콘텐츠 차단 |


### Chapter 1. Chat Model

#### 1) ChatClient 
- ChatClient 생성 (/api/vi/chat/chat)
```java
@Bean
public ChatClient chatClient(ChatClient.Builder chatCLientBuilder) {
    return chatCLientBuilder
            .build();
}
```

#### 2) Prompt

- Default System Prompt 설정 
```java
// OpenAiConfig.java

@Bean
public ChatClient chatClient(ChatClient.Builder chatCLientBuilder) {
    return chatCLientBuilder
            .defaultSystem("시스템 메시지")
            .build();
}
```

- Placeholder System Prompt (/api/vi/chat/chatplace)
```java
// OpenAiConfig.java

@Configuration
public class AppConfig {
  @Value("classpath:/prompt.txt")
  private Resource resource;

  private String prompt= """
          You are an AI assistant that specializes in {subject}.
          You respond in a {tone} voice with detailed explanations.
          """;
  @Bean
  public ChatClient chatBuilder(ChatClient.Builder chatBuilder){
    // 당신은 특정 {주제}에 특화된 AI 도우미입니다. 당신은 상세한 설명을 제공하는 {톤}과 음성으로 답변합니다.
    return chatBuilder.defaultSystem(resource).build();
  }
}
```
 
- ChatResponse (/api/vi/chat/chatjson)
  - LLM 채팅 메시지 json 전송
```java
@RestController
@GetMapping("/chatjson")
public ChatResponse chatJson(@RequestParam("message") String message){
    return chatClient.prompt()
        .user(message)
        .call()
        .chatResponse();
}
```
- Returning an Entity
```java
@RestController
public class RecipeController {

    @Autowired
    private ChatClient chatClient;

    @GetMapping("/generate-recipe")
    public Answer generateRecipe(String question) {
        return chatClient.prompt()
            .user(question)
            .call()
            .entity(Answer.class);
    }
}
``` 


#### 3) Structured Output

Spring AI에서 Structured Output은 LLM의 자유로운 텍스트 출력을 구조화된 형식으로 변환하는 것을 제공.

![Structured Output](https://github.com/domsamo/spring-ai-tutorial/blob/main/src/main/resources/templates/img/structured_output.png)

대표적으로 List의 형태나 Map형태의 Output 사용.

![Structured Output2](https://github.com/domsamo/spring-ai-tutorial/blob/main/src/main/resources/templates/img/structured_output2.png)

- List 형식 (/api/vi/chat/chatList)
```java
public List<String> chatlist(String query) {
    return chatClient.prompt()
          .user(query)
          .call()
          .entity(new ListOutputConverter(new DefaultConversionService()));
}
```

- Map 형식 (/api/vi/chat/chatMap)
```java
public Map<String, String> chatMap(String query) {
    return chatClient.prompt()
        .user(query)
        .call()
        .entity(new ParameterizedTypeReference<Map<String, String>>() { });
}
```

- User Object 형식 (/api/vi/chat/chatMovie)
```java
List<Movie> movieList = chatClient.prompt()
        .user(userSpec -> userSpec.text(template)
                .param("directorName", directorName)
                .param("format", "json"))
        .call()
        .entity(new ParameterizedTypeReference<List<Movie>>() {});
```

#### 4) Spring AI Advisors
- 메모리
```java
@Bean
public ChatClient chatClient(ChatClient.Builder chatCLientBuilder) {
    return chatCLientBuilder
            .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
            .build();
}    
```
 
- CommandLineAppStartupRunner
```java
public class CommandLineAppStartupRunner implements CommandLineRunner {

    private final ChatService chatService;

    public CommandLineAppStartupRunner(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void run(String... args) throws Exception {
        chatService.startChat();
    }
}
``` 
