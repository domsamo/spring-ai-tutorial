package com.fbc.ai.domain.dto;

import java.util.List;

/**
 * AI 연동 이미지 분석 데이터 처리용 VO
 */
public class ImageAnalysisVO {
    private String imageUrl;
    private String analysisText;
    private List<String> youtubeUrls; // 유튜브 URL 리스트 추가

    // 생성자에 youtubeUrls 추가
    public ImageAnalysisVO(String imageUrl, String analysisText, List<String> youtubeUrls) {
        this.imageUrl = imageUrl;
        this.analysisText = analysisText;
        this.youtubeUrls = youtubeUrls;
    }

    // Getters and Setters
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAnalysisText() {
        return analysisText;
    }

    public void setAnalysisText(String analysisText) {
        this.analysisText = analysisText;
    }

    public List<String> getYoutubeUrls() {
        return youtubeUrls;
    }

    public void setYoutubeUrls(List<String> youtubeUrls) {
        this.youtubeUrls = youtubeUrls;
    }
}