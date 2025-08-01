# RAG(Retrieval-Augmented Generation)

검색 증강 생성 

이 기술은 대규모 언어 모델(LLM)의 성능을 향상시키기 위해 사용되며, 외부 지식 소스를 활용하여 LLM이 더 정확하고 최신의 정보를 기반으로 응답을 생성하도록 도움.

### Chapter 2. Rag Model

#### 1) DataLoader

- 시스템 기동시 데이터 로드하여 VectoreStore에 임베딩 처리  
```java
public class RagDataLoader {
    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;

    // # 0. PDF 경로(resources 아래)
    @Value("classpath:/SPRi_AI_Brief_7월호_산업동향.pdf")
    private Resource pdfResource;

    public RagDataLoader(VectorStore vectorStore, JdbcClient jdbcClient) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void init() {
    }
}
```

#### 2) 파일 업로드를 통한 VectoreStore에 임베딩 처리  

- 문서 등록 및 임베딩 (/api/v1/rag/documents) 
```java
@RequestMapping("/api/v1/rag")
@Tag(name = "RAG API", description = "Retrieval-Augmented Generation 기능을 위한 API")
public class RagController {
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ApiResponseDto<DocumentUploadResultDto>>> uploadDocument(
            @Parameter(description = "업로드할 파일", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "버킷 ID (선택사항, 기본값은 설정된 기본 버킷)")
            @RequestParam(value = "bucketId", required = false) String bucketId
    ) throws Exception {
        
    }
}
```

- RAG 검색 (/api/v1/rag/answer)
```java
@GetMapping("/answer")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> answer(
            @Parameter(description = "질문 내용", required = true, example = "가디언 에이전트가 2030년까지 에이전틱 AI 시장에서 몇%를 차지할 것으로 예측해?")
            @RequestParam String question,
            @Parameter(description = "사용할 OpenAI 모델", required = false, example = "gpt-3.5-turbo")
            @RequestParam(required = false) String model
    ) {
}
``` 

#### 2) 다중 벡터 테이블 

- 신규(hotel_vector_store) 벡터 테이블 참조

```sql
-- Table: public.hotel_vector_store

-- DROP TABLE IF EXISTS public.hotel_vector_store;

CREATE TABLE IF NOT EXISTS public.hotel_vector_store
(
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    content text COLLATE pg_catalog."default",
    metadata json,
    embedding vector(1536),
    CONSTRAINT hotel_vector_store_pkey PRIMARY KEY (id)
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.hotel_vector_store
    OWNER to spring;
-- Index: hotel_vector_store_embedding_idx

-- DROP INDEX IF EXISTS public.hotel_vector_store_embedding_idx;

CREATE INDEX IF NOT EXISTS hotel_vector_store_embedding_idx
    ON public.hotel_vector_store USING hnsw
    (embedding vector_cosine_ops)
    TABLESPACE pg_default;
```
 
```java
// AiConfig.java
@Bean
public VectorStore hotelVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
    return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            .vectorTableName("hotel_vector_store")
            .dimensions(1536)
            .build();
}
```

- RAG 데이터 로딩
```java
@Configuration
public class HotelLoader {
    @Qualifier("hotelVectorStore")
    private final VectorStore hotelVectorStore;
    private final JdbcClient jdbcClient;

    @Value("classpath:hotel_data.txt")
    Resource resource;

    public HotelLoader(VectorStore vectorStore, VectorStore hotelVectorStore, JdbcClient jdbcClient) {
        this.hotelVectorStore = hotelVectorStore;
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void init() throws Exception {

    }
}    
```

- RAG 기반 호텔 쳇봇 (http://localhost:8080/hotel)

상용 AI UI가 Steam 처리하는 방식(Flux 처리) 
```java
@RequestMapping("/api/v1/rag/hotel")
public class HotelController {
    @Qualifier("hotelVectorStore")
    private final VectorStore hotelVectorStore;
    private final ChatClient chatClient;

    public HotelController(VectorStore hotelVectorStore, ChatClient.Builder chatClient) {
        this.hotelVectorStore = hotelVectorStore;
        this.chatClient = chatClient.build();
    }

    @GetMapping("/question")
    public Flux<String> hotelQuestion(@RequestParam("question") String question, Model model) throws Exception {

    }
}
```

script stream 처리
```javascript
async function processStream(reader, contentElement) {
    const decoder = new TextDecoder('utf-8');   // bytes -> String 변환
    try {
        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            contentElement.innerHTML += decoder.decode(value, { stream: true });    //stream: true => string 변환이 성공된 것들은 바로 처리
            chatBox.scrollTop = chatBox.scrollHeight;
        }
    } catch (error) {
        console.error('Error processing stream:', error);
        contentElement.innerHTML += '<br><span class="text-danger">[Stream interrupted]</span>';
    }
}
```

### Chapter 2-1. 플롯 유사성을 기반으로 한 영화 추천 시스템 (http://localhost:8080/movie)

- Table 생성
```sql
-- pgvector_movie.sql

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS movie_vector(
                                           id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1536)
    );

CREATE INDEX ON movie_vector USING HNSW (embedding vector_cosine_ops);
```

- DataLoader
```text
# MovieLoader
```

- RAG조횜 및 Google Api 조회

```java
@PostMapping("/recommend")
public String recommendMovies1(@RequestParam("query") String query, Model model) throws Exception {
    // Fetch similar movies using vector store
    List<Document> results = movieVectorStore.similaritySearch(
            SearchRequest.builder().
                    query(query).
                    similarityThreshold(0.85).
                    topK(1).
                    build());

    if (!results.isEmpty()) {
        Document topResult = results.get(0);
        String movieContent = topResult.getText();
        String title = movieContent.substring(movieContent.indexOf("(")+1, movieContent.lastIndexOf(")")); //(쇼생크탈출)

        // Use Jsoup to fetch the YouTube URL
        List<String> url = GoogleApiUtils.searchYouTube(title);
        model.addAttribute("title", title);

        // Add the movie details and YouTube URL to the model
        model.addAttribute("results", movieContent);
        model.addAttribute("youtubeUrls", url);
    }else{
        model.addAttribute("message", "No closely related movies found.");
    }

    model.addAttribute("query", query);
    return "movieRAG";  // Renders the 'movieRAG.html' view
}
```

- GoogleApiUtils (컴포넌트를 유틸 클래스와 같이 사용)
 
```java
@Component
public class GoogleApiUtils {

    // 1. 값을 주입받을 인스턴스 필드
    @Value("${google.api-key:#{null}}")
    private String googleApiKey;

    // 2. 값을 저장할 static 필드
    private static String staticGoogleApiKey;

    // 3. @PostConstruct를 사용하여 Bean이 생성되고 의존성 주입이 완료된 후,
    // 인스턴스 필드의 값을 static 필드로 복사
    @PostConstruct
    private void init() {
        staticGoogleApiKey = this.googleApiKey;
    }

    // 4. static 메서드에서 static 필드를 사용할 수 있음
    public static List<String> searchYouTube(String movieTitle) throws Exception {
        return null;
    }
}    
``` 