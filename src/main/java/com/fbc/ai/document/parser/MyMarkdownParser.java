package com.fbc.ai.document.parser;

import com.fbc.ai.document.DocumentParser;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class MyMarkdownParser implements DocumentParser {
    @Override
    public boolean supports(String fileExtension) {
        return false;
    }

    @Override
    public List<Document> parse(MultipartFile file) throws IOException {
        return List.of();
    }
}
