package com.fbc.ai.service.image;

import com.fbc.ai.domain.dto.ImageRequestDTO;
import com.fbc.ai.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;

/**
 * 이미지 생성 AI Service - OpenAI(DALL-2, DALL-3)
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    private final OpenAiImageModel openAiImageModel; // dall-e-2 기본 이미지 생성 모델

    public ImageServiceImpl(OpenAiImageModel openAiImageModel) {
        this.openAiImageModel = openAiImageModel;
    }


    @Override
    public ImageResponse getImageGen(ImageRequestDTO request) {
        log.info("request.getModel() : {}", request.getModel());
        OpenAiImageOptions options = null;

        if("dall-e-3".equals(request.getModel())){
            options =  OpenAiImageOptions.builder()
                    .model(request.getModel())
                    .quality("hd")              // DALL-E 3에서 사용 가능
                    .N(1)                   // DALL-E 3는 n=1만 지원
                    .height(1024)
                    .width(1024)
                    .build();
        }else{
            options =  OpenAiImageOptions.builder()
                    .model(request.getModel())
                    .N(request.getN())          // DALL-E 3는 n=1만 지원
                    .height(1024)
                    .width(1024)
                    .build();
        }

        ImageResponse imageResponse = openAiImageModel
                .call(new ImagePrompt(request.getMessage(), options));
        return  imageResponse;
    }
}
