package com.mindbridge.agent.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
/**
 * 静态首页入口。
 *
 * <p>直接访问根路径时返回前端页面，方便本地演示不用单独启动前端服务。</p>
 */
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public Resource index() {
        return new ClassPathResource("static/index.html");
    }
}
