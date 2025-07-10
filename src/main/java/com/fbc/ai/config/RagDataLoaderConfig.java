package com.fbc.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

@Slf4j
@Configuration
public class RagDataLoaderConfig {
    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;

    // # 0. PDF 경로(resources 아래)
    @Value("classpath:/SPRi_AI_Brief_7월호_산업동향.pdf")
    private Resource pdfResource;

    public RagDataLoaderConfig(VectorStore vectorStore, JdbcClient jdbcClient) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void init(){
        Integer count=jdbcClient.sql("select count(*) from vector_store")
                .query(Integer.class)
                .single();

        log.info("No of Records in the PG Vector Store={}", count);

        if(count==0){
            log.info("Loading.....");
            // PDF Reader
            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPageTopMargin(0)
                    .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                            .withNumberOfTopTextLinesToDelete(0)
                            .build())
                    .withPagesPerDocument(1)
                    .build();

            // # 1.단계 : 문서로드(Load Documents)
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource,config);

            List<Document> documents=pdfReader.get();

            // 1000글자 단위로 자른다.
            // # 2.단계 : 문서분할(Split Documents)
            TokenTextSplitter splitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
            List<Document> splitDocuments=splitter.apply(documents);
            log.info("splitDocuments.size() : {}", splitDocuments.size()); // 45
            log.info("splitDocuments.get(0) : {}", splitDocuments.get(0)); // 25

            // # 3.단계 : 임베딩(Embedding) -> 4.단계 : DB에 저장(백터스토어 생성)
            vectorStore.accept(splitDocuments); // OpenAI 임베딩을 거친다.
            log.info("Application is ready to Serve the Requests");
        }
    }
}
