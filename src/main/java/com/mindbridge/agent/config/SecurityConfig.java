package com.mindbridge.agent.config;

import com.mindbridge.agent.security.CurrentUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;

@Configuration
@EnableWebFluxSecurity
/**
 * WebFlux 安全配置。
 *
 * <p>项目使用 HTTP Basic 简化演示登录；管理员接口要求 ADMIN 角色，
 * 普通 API 要求已登录用户。</p>
 */
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http,
            ReactiveAuthenticationManager authenticationManager
    ) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authenticationManager(authenticationManager)
                .httpBasic(Customizer.withDefaults())
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers("/actuator/health", "/h2-console/**").permitAll()
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/reports/**").hasRole("ADMIN")
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().permitAll())
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(
            CurrentUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
