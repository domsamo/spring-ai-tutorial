package com.fbc.ai.controller.image;

import com.fbc.ai.domain.dto.ImageAnalysisVO;
import com.fbc.ai.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/image-text")
public class ImageTextGenController {

    @Value("${upload.path}")
    private String uploadPath;

    private final ImageService imageService;

    public ImageTextGenController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<ImageAnalysisVO> getMultimodalResponse(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(defaultValue = "이 이미지에 무엇이 있나요?") String message)
            throws IOException {

        uploadFile(imageFile);
        String filename = imageFile.getOriginalFilename();
        String imageUrl = "/uploads/" + filename;

        // Analyze the image
        String analysisText = imageService.analyzeImage(imageFile, message);
        ImageAnalysisVO response = new ImageAnalysisVO(imageUrl, analysisText, null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mathanalyze")
    public ResponseEntity<ImageAnalysisVO> getMultimodalResponseMath(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(defaultValue = "이 이미지에 무엇이 있나요?") String message)
            throws IOException {

        uploadFile(imageFile);
        String filename = imageFile.getOriginalFilename();
        String imageUrl = "/uploads/" + filename;

        // Analyze the image
        String analysisText = imageService.analyzeImageMath(imageFile, message);

        // 세제곱근, 제곱근, 곱셈
        String searchKeyword = imageService.extractKeyYouTubeSearch(analysisText);
        List<String> youtubeUrls = imageService.searchYouTubeVideos(searchKeyword);

        ImageAnalysisVO response = new ImageAnalysisVO(imageUrl, analysisText, youtubeUrls);
        return ResponseEntity.ok(response);
    }

    private void uploadFile(MultipartFile imageFile) throws IOException {
        // Ensure the upload directory exists
        File uploadDirectory = new File(uploadPath);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // Save the uploaded file to the specified upload path
        String filename = imageFile.getOriginalFilename();
        Path filePath = Paths.get(uploadPath, filename);
        Files.write(filePath, imageFile.getBytes());
    }
}
