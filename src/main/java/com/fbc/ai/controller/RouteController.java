package com.fbc.ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Html View Forwarding Controller
 */
@Controller
public class RouteController {

    @GetMapping("/pdfRag")
    public String korea(){
        return "rag"; // rag.html
    }

    @GetMapping("/askview")
    public String askview(){
        return "ask";
    }

    @GetMapping("/image")
    public String image(){
        return "image"; // image.html
    }

    @GetMapping("/imagevoice")
    public String imagevoice(){
        return "imagev"; // image.html
    }

    @GetMapping("/imageview")
    public String imageview(){
        return "imageview";
    }

    @GetMapping("/imagemath")
    public String imagemath(){
        return "imagemath";
    }

    @GetMapping("/hotel")
    public String hotel()  {
        return "hotel";
    }

    @GetMapping("/recipeview")
    public String recipeview(){
        return "recipe";
    }

    @GetMapping("/sql")
    public String index(){
        return "sql";
    }

    @GetMapping("/stt")
    public String stt(){
        return "stt";
    }

    @GetMapping("/audioPlay")
    public String audioPlay(){
        return "tts";
    }

    @GetMapping("/movie")
    public String getRecommendationForm() {
        return "movieRAG";  // returns the HTML file 'recommend.html'
    }

}
