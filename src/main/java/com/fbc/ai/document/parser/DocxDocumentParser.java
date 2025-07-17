package com.fbc.ai.document.parser;

import com.fbc.ai.document.DocumentParser;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class DocxDocumentParser implements DocumentParser {
    @Override
    public boolean supports(String fileExtension) {
        return "docx".equalsIgnoreCase(fileExtension);
    }

    @Override
    public List<Document> parse(MultipartFile file) throws IOException {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(file.getResource());
        return tikaDocumentReader.read();
    }
}
