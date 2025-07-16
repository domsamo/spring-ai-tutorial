package com.fbc.ai.service;

import com.fbc.ai.exception.DocumentProcessingException;

import java.io.File;

/**
 * 다양한 형식의 문서에서 텍스트를 추출하는 서비스 인터페이스입니다.
 * 현재는 PDF 파일 지원, 향후 다른 형식도 추가 가능합니다.
 */
public interface DocumentProcessingService {

    /**
     * PDF 파일로부터 텍스트를 추출합니다.
     *
     * @param pdfFile PDF 파일 객체
     * @return 추출된 텍스트
     * @throws DocumentProcessingException 텍스트 추출 실패 시
     */
    String extractTextFromPdf(File pdfFile);

    // 향후 다른 문서 형식 지원을 위한 메서드 추가 가능
    // String extractTextFromDocx(File docxFile);
    // String extractTextFromTxt(File txtFile);
}