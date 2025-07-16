package com.fbc.ai.service;

import com.fbc.ai.domain.dto.ImageRequestDTO;
import org.springframework.ai.image.ImageResponse;

public interface ImageService {

    /**
     * AI를 통해 이미지 생성
     * @param request
     * @return
     */
    ImageResponse getImageGen(ImageRequestDTO request);
}
