package com.blps.app.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
