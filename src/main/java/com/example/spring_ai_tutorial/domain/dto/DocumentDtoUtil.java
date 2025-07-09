package com.example.spring_ai_tutorial.domain.dto;

/**
 * Document DTO 관련 유틸리티 메서드
 */
public class DocumentDtoUtil {

    /**
     * StormContextDto를 DocumentResponseDto로 변환
     * 
     * @param contextDto 변환할 StormContextDto 객체
     * @return 변환된 DocumentResponseDto 객체
     */
    public static DocumentResponseDto toDocumentResponseDto(StormContextDto contextDto) {
        String content = contextDto.getContent();
        if (content.length() > 1000) {
            content = content.substring(0, 1000) + "...";
        }
        
        return new DocumentResponseDto(
            contextDto.getDocumentId(),
            content,
            contextDto.getMetadata()
        );
    }
    
    // 유틸리티 클래스이므로 인스턴스화 방지
    private DocumentDtoUtil() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }
}