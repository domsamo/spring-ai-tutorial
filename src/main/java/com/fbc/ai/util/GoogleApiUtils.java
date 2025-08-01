package com.fbc.ai.util;

import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Google API 연동 유틸 클래스
 */
@Component
public class GoogleApiUtils {

    // 1. 값을 주입받을 인스턴스 필드
    @Value("${google.api-key:#{null}}")
    private String googleApiKey;

    // 2. 값을 저장할 static 필드
    private static String staticGoogleApiKey;

    // 3. @PostConstruct를 사용하여 Bean이 생성되고 의존성 주입이 완료된 후,
    // 인스턴스 필드의 값을 static 필드로 복사
    @PostConstruct
    private void init() {
        staticGoogleApiKey = this.googleApiKey;
    }

    // 4. static 메서드에서 static 필드를 사용할 수 있음
    public static List<String> searchYouTube(String movieTitle) throws Exception {

        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q="
                + movieTitle
                + "&order=relevance"
                + "&key=" + staticGoogleApiKey;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

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
