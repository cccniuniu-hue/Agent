package com.mindbridge.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
/**
 * 共享 WebClient.Builder。
 *
 * <p>模型服务、embedding 服务和 HTTP MCP 工具都复用这个 Builder，便于统一扩展超时、
 * 日志或代理设置。</p>
 */
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
