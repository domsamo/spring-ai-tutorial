package com.fbc.ai.document.parser;

import com.fbc.ai.document.DocumentParser;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pdf Document Parser
 */
@Component
public class PdfDocumentParser implements DocumentParser {
    @Override
    public boolean supports(String fileExtension) {
        return "pdf".equalsIgnoreCase(fileExtension);
    }

    @Override
    public List<Document> parse(MultipartFile file) throws IOException {

        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPageTopMargin(0)
                    .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                            .withNumberOfTopTextLinesToDelete(0)
                            .build())
                    .withPagesPerDocument(1)
                    .build();
        // # 1.단계 : 문서로드(Load Documents)
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(file.getResource() ,config);
//        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(new ByteArrayResource(file.getBytes()) ,config);

        List<Document> documents = pdfReader.get();

        return documents;
    }

}
