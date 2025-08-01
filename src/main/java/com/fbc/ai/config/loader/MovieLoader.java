package com.fbc.ai.config.loader;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;

@Slf4j
@Configuration
public class MovieLoader {
    private final VectorStore movieVectorStore;
    private final JdbcClient jdbcClient;

    @Value("classpath:movie_plots_korean.txt")
    Resource resource;

    public MovieLoader(VectorStore movieVectorStore, JdbcClient jdbcClient) {
        this.movieVectorStore = movieVectorStore;
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void init() throws Exception {
//        Integer count=jdbcClient.sql("select count(*) from movie_vector")
//                .query(Integer.class)
//                .single();
//        log.info("No of Records in the PG Vector Store = {}", count);
//        if(count==0){
//            List<Document> documents = Files.lines(resource.getFile().toPath())
//                    .map(Document::new)
//                    .collect(Collectors.toList());
//            TextSplitter textSplitter = new TokenTextSplitter();
//            for(Document document : documents) {
//                List<Document> splitteddocs = textSplitter.split(document);
//                log.info("before adding document: {}", document.getText());
//                movieVectorStore.add(splitteddocs); //임베딩
//                log.info("Added document: {}", document.getText());
//                Thread.sleep(1000); // 1초
//            }
//            log.info("Application is ready to Serve the Requests");
//        }
    }
}
