# Spring AI Tutorial

Spring AI를 활용하여 LLM 호출부터 RAG 파이프라인 구축, Storm OpenAPI 연동까지 단계별로 학습할 수 있는 실습 프로젝트입니다.

**언어**: Java  
**프레임워크**: Spring Boot 3.4.4        
**Spring AI 버전**: `1.0.0-M6`

## AI Model
| Category          | Description                              |   Exsample   |
|:------------------|:-----------------------------------------|:------------:|
| Chat Models       | 자연어 처리 및 대화 기능을 제공하는 LLM 기반 모델           | AI 챗봇, Q&A 시스템 |
| Embedding Models  | 텍스트를 벡터로 변환하여 유사도 검색 및 문서 검색            | 문서 유사도 비교, RAG 서비스 |
| Image Models      | 주어진 프롬프트로 이미지 생성 및 수정 기능을 제공            | 이미지 생성, 이미지 편집 |
| Audio Models      | 음성을 텍스트로 변환(STT)하거나 텍스트를 음성으로 변환(TTS)  | 음성 인식, 음성 합성 |
| Moderation Models | 콘텐츠 검열 및 부적절한 텍스트/이미지를 감지하는 모델        | 텍스트 필터링, 부적절 콘텐츠 차단 |


### Chapter 1. Chat Model
- ChatClient 생성
```java
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatCLientBuilder) {
        return chatCLientBuilder
                .build();
    }
```

- Default System Prompt 설정
```java
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatCLientBuilder) {
        return chatCLientBuilder
                .defaultSystem("시스템 메시지")
                .build();
    }
```

- Placeholder System Prompt 
```java
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
 
- ChatResponse
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



### Chapter 2. 나만의 RAG 챗봇 만들기
- **이론**: 나만의 RAG 챗봇 설계하기
- **실습 1**: RAG 파이프라인 구축하기 - Data Indexing
- **실습 2**: RAG 파이프라인 구축하기 - Data Retrieval & Generation
- **브랜치**: `chapter2` (현재 브랜치)


## 참고 사항

### 브랜치 관리
각 Chapter 별로 실습할 때는 해당 브랜치로 전환해주세요.

```bash
# Chapter 1 실습
git switch chapter1_exercise

# Chapter 1 실습 후 완성 코드 확인
git switch chapter1_completed

# Chapter 2 실습
git switch chapter2

# Chapter 3 실습
git switch chapter3
```

## 프로젝트 설정

### 필요한 환경
- **Java**: 17 이상
- **API Keys**:
    - **Chapter 1-2**: OpenAI API Key (LLM 모델 및 임베딩 모델 사용)
    - **Chapter 3**: Storm API Key (Storm OpenAPI 사용)

### 1. API Key 설정

`application.properties` 파일에 직접 추가
```properties
# Chapter 1-2
spring.ai.openai.api-key=your-openai-api-key-here
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
