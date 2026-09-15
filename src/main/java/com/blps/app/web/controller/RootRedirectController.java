package com.blps.app.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {

    @GetMapping("/")
    public String redirectToTasklist() {
        return "redirect:/camunda/app/tasklist/default/#/?processDefinitionKey=Process_00qvb5l";
    }
}
