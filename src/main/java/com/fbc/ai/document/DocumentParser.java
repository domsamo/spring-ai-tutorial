package com.fbc.ai.document;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentParser {

    /**
     * 해당 파서가 지원하는 파일 확장자인지 확인합니다.
     * @param fileExtension 파일 확장자 (예: "pdf", "docx")
     * @return 지원 여부
     */
    boolean supports(String fileExtension);

    /**
     * 파일을 파싱하여 Document 객체를 생성합니다.
     *
     * @param file 업로드된 MultipartFile
     * @return 분할된 Document 객체 리스트
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    List<Document> parse(MultipartFile file) throws IOException;

}
