package com.fbc.ai.controller.search;

import com.fbc.ai.domain.dto.Recipe;
import com.fbc.ai.service.SearchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class RecipeController {
    private final SearchService searchService;

    public RecipeController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/recipe")
    public Map<String, Object> recipe(@RequestBody Recipe recipe) throws IOException {
        return searchService.createRecipeWithUrls(recipe);
    }
}
