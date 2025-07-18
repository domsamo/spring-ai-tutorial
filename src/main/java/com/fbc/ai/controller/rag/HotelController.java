package com.fbc.ai.controller.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 기반 호텔 챗봇 Controller
 */
@Slf4j
@RestController
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
        log.info("question : {}", question);
        // Fetch similar movies using vector store
        List<Document> results = hotelVectorStore.similaritySearch(SearchRequest.builder()
                                .query(question)
                                .similarityThreshold(0.5)
                                .topK(1).build());

        log.info("조회건수 : {}", results.size());
        for(Document document : results) {
            log.info("조회데이터 : {}", document.getText());
        }

        String template = """
                당신은 어느 호텔 직원입니다. 문맥에 따라 고객의 질문에 정중하게 답변해 주십시오. 
                컨텍스트가 질문에 대답할 수 없는 경우 '모르겠습니다'라고 대답하세요.
                               
                컨텍스트:
                {context}
                
                질문: 
                {question}
                 
                답변:
                """;

        return chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text(template)
                        .param("context", results)
                        .param("question", question))
                .stream()
                .content();
    }
}
