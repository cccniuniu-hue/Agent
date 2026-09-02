package com.mindbridge.agent.controller;

import com.mindbridge.agent.security.CurrentUser;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
/**
 * 当前登录账号信息接口。
 *
 * <p>前端通过角色判断展示学生聊天界面还是管理员后台。</p>
 */
public class ProfileController {

    @GetMapping
    public Map<String, Object> profile(@AuthenticationPrincipal CurrentUser currentUser) {
        return Map.of(
                "id", currentUser.getId(),
                "username", currentUser.getUsername(),
                "displayName", currentUser.getDisplayName(),
                "roles", currentUser.getAuthorities());
    }
}
