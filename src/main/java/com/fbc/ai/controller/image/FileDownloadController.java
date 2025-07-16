package com.fbc.ai.controller.image;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * File Download Controller
 */
@RestController
public class FileDownloadController {

    private final RestTemplate restTemplate;

    public FileDownloadController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/download-file")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String url) {
        try {
            // URL을 URI 객체로 변환
            URI uri = new URI(url);

            // RestTemplate로 요청 보내기
            ResponseEntity<byte[]> response = restTemplate.getForEntity(uri, byte[].class);

            // URL에서 파일 이름 추출
            String fileName = extractFileName(url);

            // 파일 다운로드용 헤더 설정
            HttpHeaders downloadHeaders = new HttpHeaders();
            downloadHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            downloadHeaders.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

            return new ResponseEntity<>(response.getBody(), downloadHeaders, HttpStatus.OK);

        } catch (Exception e) {
            // 상세 오류 메시지 반환
            return ResponseEntity.badRequest()
                    .body(("Failed to download file: " + e.getMessage()).getBytes());
        }
    }

    // 파일 이름 추출 메서드
    private String extractFileName(String url) {
        // URI에서 경로의 마지막 부분 추출
        String path = URI.create(url).getPath();  // .png?__X
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
