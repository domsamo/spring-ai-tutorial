package com.fbc.ai.service.search;

import com.fbc.ai.domain.dto.Recipe;
import com.fbc.ai.service.SearchService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Value("${google.api-key:#{null}}")
    private String googleApiKey;

    private final ChatModel chatModel;
    private final String googleCx = "06d12ede226144e51";
    private final RestTemplate restTemplate = new RestTemplate();

    public SearchServiceImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    // 레시피 생성 메서드
    @Override
    public String createRecipe(Recipe recipe) {
        String template = """
            제목: 요리 제목을 제공해 주세요.
            다음 재료를 사용하여 요리법을 만들고 싶습니다: {ingredients}.
            선호하는 요리 유형은 {cuisine}입니다.
            다음 식이 제한을 고려해 주세요: {dietaryRestrictions}.
            재료 목록과 조리법을 포함한 상세한 요리법을 제공해 주세요.
    """;
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Map<String, Object> params = Map.of(
                "ingredients", recipe.getIngredients(), // 재료
                "cuisine", recipe.getCuisine(), // 요리
                "dietaryRestrictions", recipe.getDietaryRestrictions() // 식이제한
        );

        Prompt prompt = promptTemplate.create(params);
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    // Google Custom Search API를 사용하여 관련 URL 검색
    @Override
    public List<String> searchRecipeUrls(String query) throws IOException {

        // API URI 생성
        URI apiUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/customsearch/v1")
                .queryParam("key", googleApiKey)
                .queryParam("cx", googleCx)
                .queryParam("q", query)
                .build()
                .toUri();
        log.info(apiUrl.toString());
        // API 호출
        String response = restTemplate.getForObject(apiUrl, String.class);

        // JSON 응답 파싱
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        log.info(jsonResponse.toString());

        JsonArray itemsArray = jsonResponse.getAsJsonArray("items");

        // itemsArray가 null인지 확인
        List<String> urls = new ArrayList<>();
        if (itemsArray != null) {
            for (JsonElement item : itemsArray) {
                urls.add(item.getAsJsonObject().get("link").getAsString());
            }
        } else {
            log.info("No search results found for the query: {}", query);
        }
        return urls;
    }

    // 레시피와 링크를 함께 제공하는 메서드
    @Override
    public Map<String, Object> createRecipeWithUrls(Recipe recipe) throws IOException {
        String recipeContent = createRecipe(recipe); // 레시피 설명 생성
        List<String> urls = searchRecipeUrls(recipe.getIngredients()); // Google Custom Search API로 URL 검색
        return Map.of("recipe", recipeContent, "urls", urls);
    }
}
