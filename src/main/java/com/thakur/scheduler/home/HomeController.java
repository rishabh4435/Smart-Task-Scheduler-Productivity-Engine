package com.thakur.scheduler.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @ResponseBody
    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui/index.html";
    }
}