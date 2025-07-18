package com.fbc.ai.service.image;

import com.fbc.ai.domain.dto.ImageRequestDTO;
import com.fbc.ai.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 이미지 생성 AI Service - OpenAI(DALL-2, DALL-3)
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {
    @Value("classpath:/image_system.message")
    private Resource imageSystemMessage;

    @Value("classpath:/math_system.message")
    private Resource mathSystemMessage;

    @Value("${google.api-key:#{null}}")
    private String googleApiKey;

    private final OpenAiImageModel openAiImageModel; // dall-e-2 기본 이미지 생성 모델
    private final ChatModel chatModel;

    public ImageServiceImpl(OpenAiImageModel openAiImageModel, ChatModel chatModel) {
        this.openAiImageModel = openAiImageModel;
        this.chatModel = chatModel;
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

    /**
     * AI로 부터 이미지를 분석하여 Text 데이터를 추출한다.
     * @param imageFile
     * @param message
     * @return
     * @throws IOException
     */
    @Override
    public String analyzeImage(MultipartFile imageFile, String message) throws IOException {
        String contentType = imageFile.getContentType();
        if (!MimeTypeUtils.IMAGE_PNG_VALUE.equals(contentType) &&
                !MimeTypeUtils.IMAGE_JPEG_VALUE.equals(contentType)) {
            throw new IllegalArgumentException("지원되지 않는 이미지 형식입니다.");
        }

        var media = new Media(MimeType.valueOf(contentType), imageFile.getResource());
        var userMessage = new UserMessage(message, media);
        var systemMessage = new SystemMessage(imageSystemMessage);
        return chatModel.call(userMessage, systemMessage);
    }

    /**
     * AI로 부터 이미지를 분석하여 Text 데이터를 추출한다.
     * @param imageFile
     * @param message
     * @return
     * @throws IOException
     */
    @Override
    public String analyzeImageMath(MultipartFile imageFile, String message) throws IOException {
        String contentType = imageFile.getContentType();
        if (!MimeTypeUtils.IMAGE_PNG_VALUE.equals(contentType) &&
                !MimeTypeUtils.IMAGE_JPEG_VALUE.equals(contentType)) {
            throw new IllegalArgumentException("지원되지 않는 이미지 형식입니다.");
        }

        var media = new Media(MimeType.valueOf(contentType), imageFile.getResource());
        var userMessage = new UserMessage(message, media);
        var systemMessage = new SystemMessage(mathSystemMessage);
        return chatModel.call(userMessage, systemMessage);
    }

    @Override
    public String extractKeyYouTubeSearch(String analysisText) {
        String keyword=null;
        if(analysisText.indexOf("핵심 키워드:")!=-1){
            //핵심 키워드: 세제곱근, 제곱근, 곱셈
            keyword=analysisText.substring(analysisText.indexOf("핵심 키워드:")).split(":")[1].trim();
        }
        //세제곱근, 제곱근, 곱셈
        return keyword;
    }

    /**
     * Google Cloud(https://console.cloud.google.com) : YouTube Data API v3
     * 하루 10,000건 사용 가능
     * @param query
     * @return
     */
    @Override
    public List<String> searchYouTubeVideos(String query) {
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q=EBS " +
                query + "&order=relevance&key=" + googleApiKey;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        log.info(response.getBody());

        List<String> videoUrls = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONArray items = jsonResponse.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String videoId = item.getJSONObject("id").getString("videoId");
            videoUrls.add(videoId);
        }
        return videoUrls;
    }
}
