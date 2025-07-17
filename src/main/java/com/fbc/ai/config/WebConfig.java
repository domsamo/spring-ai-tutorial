package com.fbc.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 1. 모든 경로에 대해
                .allowedOrigins("http://localhost:8080", "http://your-frontend-domain.com") // 2. 허용할 출처(Origin)를 명시합니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 3. 허용할 HTTP 메서드를 지정합니다.
                .allowedHeaders("*") // 4. 허용할 헤더를 지정합니다.
                .allowCredentials(true) // 5. 쿠키 등 자격 증명을 허용할지 여부입니다.
                .maxAge(3600); // 6. Pre-flight 요청의 캐시 시간을 설정합니다. (단위: 초)
    }
}
