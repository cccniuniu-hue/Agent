package com.mindbridge.agent.security;

import com.mindbridge.agent.domain.UserAccount;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security 使用的当前用户对象。
 *
 * <p>在 UserDetails 中保留数据库用户 id 和显示名，业务接口可直接拿到当前账号信息。</p>
 */
public class CurrentUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String displayName;
    private final boolean enabled;
    private final Set<GrantedAuthority> authorities;

    public CurrentUser(UserAccount user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.displayName = user.getDisplayName();
        this.enabled = user.isEnabled();
        this.authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
