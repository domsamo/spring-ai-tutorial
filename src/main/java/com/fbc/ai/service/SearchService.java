package com.fbc.ai.service;

import com.fbc.ai.domain.dto.Recipe;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SearchService {
    // 레시피 생성 메서드
    String createRecipe(Recipe recipe);

    // Google Custom Search API를 사용하여 관련 URL 검색
    List<String> searchRecipeUrls(String query) throws IOException;

    // 레시피와 링크를 함께 제공하는 메서드
    Map<String, Object> createRecipeWithUrls(Recipe recipe) throws IOException;
}
