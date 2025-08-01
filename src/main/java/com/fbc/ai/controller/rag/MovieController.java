package com.fbc.ai.controller.rag;

import com.fbc.ai.util.GoogleApiUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Movie VectorStore 검색 및 Google API 검색 처리
 */
@Controller
public class MovieController {
    private final VectorStore movieVectorStore;
    private final ChatClient chatClient;

    public MovieController(VectorStore movieVectorStore, ChatClient.Builder chatClient) {
        this.movieVectorStore = movieVectorStore;
        this.chatClient = chatClient.build();
    }

    // 어떤 남자가 억울하게 감옥에 갇혔고, 그는 탈출을 계획합니다 - query ->임베딩[   ,,,,,,,   ]
    @PostMapping("/recommend")
    public String recommendMovies1(@RequestParam("query") String query, Model model) throws Exception {
        // Fetch similar movies using vector store
        List<Document> results = movieVectorStore.similaritySearch(
                SearchRequest.builder().
                        query(query).
                        similarityThreshold(0.85).
                        topK(1).
                        build());

        if (!results.isEmpty()) {
            Document topResult = results.get(0);
            String movieContent = topResult.getText();
            String title = movieContent.substring(movieContent.indexOf("(")+1, movieContent.lastIndexOf(")")); //(쇼생크탈출)

            // Use Jsoup to fetch the YouTube URL
            List<String> url = GoogleApiUtils.searchYouTube(title);
            model.addAttribute("title", title);

            // Add the movie details and YouTube URL to the model
            model.addAttribute("results", movieContent);
            model.addAttribute("youtubeUrls", url);
        }else{
            model.addAttribute("message", "No closely related movies found.");
        }

        model.addAttribute("query", query);
        return "movieRAG";  // Renders the 'movieRAG.html' view
    }
}
