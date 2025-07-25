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

}
