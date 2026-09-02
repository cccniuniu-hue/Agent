package com.mindbridge.agent.security;

import com.mindbridge.agent.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
/**
 * 登录认证时的账号加载服务。
 *
 * <p>项目使用响应式 WebFlux，但 JPA 是阻塞访问，因此查询放到 boundedElastic 线程池。</p>
 */
public class CurrentUserDetailsService implements ReactiveUserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public CurrentUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.fromCallable(() -> userAccountRepository.findByUsername(username)
                        .map(CurrentUser::new)
                        .map(UserDetails.class::cast)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
