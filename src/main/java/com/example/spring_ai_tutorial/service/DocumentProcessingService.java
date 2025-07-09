package com.example.spring_ai_tutorial.service;

import com.example.spring_ai_tutorial.exception.DocumentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * 다양한 형식의 문서에서 텍스트를 추출하는 서비스입니다.
 * 현재는 PDF 파일 지원, 향후 다른 형식도 추가 가능합니다.
 */
@Slf4j
@Service
public class DocumentProcessingService {

    /**
     * PDF 파일로부터 텍스트를 추출합니다.
     *
     * @param pdfFile PDF 파일 객체
     * @return 추출된 텍스트
     * @throws DocumentProcessingException 텍스트 추출 실패 시
     */
    public String extractTextFromPdf(File pdfFile) {
        log.debug("PDF 텍스트 추출 시작: {}", pdfFile.getName());

        try (PDDocument document = PDDocument.load(pdfFile)) {
            logger.debug("PDF 문서 로드 성공: {}페이지", document.getNumberOfPages());
            String text = new PDFTextStripper().getText(document);
            logger.debug("PDF 텍스트 추출 완료: {} 문자", text.length());
            return text;
        } catch (IOException e) {
            logger.error("PDF 텍스트 추출 실패", e);
            throw new DocumentProcessingException("PDF에서 텍스트 추출 실패: " + e.getMessage(), e);
        }
    }

    // 향후 다른 문서 형식 지원을 위한 메서드 추가 가능
    // public String extractTextFromDocx(File docxFile) { ... }
    // public String extractTextFromTxt(File txtFile) { ... }
}
