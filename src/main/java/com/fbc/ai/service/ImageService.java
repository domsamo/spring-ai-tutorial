package com.fbc.ai.service;

import com.fbc.ai.domain.dto.ImageRequestDTO;
import org.springframework.ai.image.ImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {

    /**
     * AI를 통해 이미지 생성
     * @param request
     * @return
     */
    ImageResponse getImageGen(ImageRequestDTO request);

    String analyzeImage(MultipartFile imageFile, String message) throws IOException;

    String extractKeyYouTubeSearch(String analysisText);

    List<String> searchYouTubeVideos(String searchKeyword);

    String analyzeImageMath(MultipartFile imageFile, String message) throws IOException;
}
