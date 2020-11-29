package com.atguigu.gmall.web.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @title: PassportController
 * @Author LiuXianKun
 * @Date: 2020/11/26 21:30
 */
@Controller
@RequestMapping
public class PassportController {
    @GetMapping("login.html")
    public String login(HttpServletRequest httpServletRequest) {
        String originUrl = httpServletRequest.getParameter("originUrl");
        httpServletRequest.setAttribute("originUrl", originUrl);
        return "login";
    }
}
