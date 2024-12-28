package com.example.webservice1.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class ArticleController {
    @GetMapping("/")
    public String home(Model model) {
        //model.addAttribute("email","");
        return "/home";
    }
    @GetMapping("/custom-login")
    public String login(){
        log.info("login");
        return "oauth/loginform";
    }
}
