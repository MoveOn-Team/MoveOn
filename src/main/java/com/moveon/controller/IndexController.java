package com.moveon.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class IndexController {
    @GetMapping(value = "/")
    public String index(HttpSession session) {

        log.info("{}.index Start!", this.getClass().getName());

        String target = (session.getAttribute("SS_USER_NO") == null)
                ? "redirect:/user/login"
                : "redirect:/recommend/recommendList";

        log.info("{}.index End! {}", this.getClass().getName(), target);

        return target;
    }
}
