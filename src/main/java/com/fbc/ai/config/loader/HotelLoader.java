package com.fbc.ai.config.loader;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

/**
 * hotel_vector_store 테이블에 hotel_data.txt 파일을 로드하여 임베딩 처리
 */
@Slf4j
@Configuration
public class HotelLoader {
    @Qualifier("hotelVectorStore")
    private  final VectorStore hotelVectorStore;
    private final JdbcClient jdbcClient;

    @Value("classpath:hotel_data.txt")
    Resource resource;

    public HotelLoader(VectorStore vectorStore, VectorStore hotelVectorStore, JdbcClient jdbcClient) {
        this.hotelVectorStore = hotelVectorStore;
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void init() throws Exception {
//        Integer count=jdbcClient.sql("select count(*) from hotel_vector_store")
//                .query(Integer.class)
//                .single();
//        log.info("No of Records in the PG Vector Store="+count);
//        if(count==0){
//            List<Document> documents = Files.lines(resource.getFile().toPath())
//                    .map(Document::new)
//                    .collect(Collectors.toList());
//            TextSplitter textSplitter = new TokenTextSplitter();
//            for(Document document : documents) {
//                List<Document> splitteddocs = textSplitter.split(document);
//                log.info("before adding document: {}", document.getText());
//                hotelVectorStore.add(splitteddocs); //임베딩
//                log.info("Added document: {}", document.getText());
//                Thread.sleep(1000); // 1초
//            }
//            log.info("Application is ready to Serve the Requests");
//        }
    }
}
