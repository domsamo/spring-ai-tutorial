package com.fbc.ai.controller.image;

import com.fbc.ai.domain.dto.ImageRequestDTO;
import com.fbc.ai.service.ImageService;
import org.springframework.ai.image.ImageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 이미지 생성 AI Controller - DALL-2, DALL-3
 */
@RestController
@RequestMapping("/api/v1/rag/image")
public class ImageGenerationController {
    private final ImageService imageService;

    public ImageGenerationController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/generate", consumes = "application/json; charset=UTF-8")
    public List<String> generate(@RequestBody ImageRequestDTO request) throws IOException {

        //String message = request.get("message"); // Map에서 "message" 키의 값을 가져옴
        ImageResponse imageResponse = imageService.getImageGen(request);

        //String imageUrl= imageResponse.getResult().getOutput().getUrl();
        //response.sendRedirect(imageUrl);
        List<String> imageUrls = imageResponse.getResults().stream()
                .map(result->result.getOutput().getUrl())
                .toList();

        return imageUrls;
    }
}
