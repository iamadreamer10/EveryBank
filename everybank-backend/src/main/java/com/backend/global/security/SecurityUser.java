package com.backend.global.security;

import com.backend.domain.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Builder
@Getter
@NoArgsConstructor
public class SecurityUser implements UserDetails {

    private Long id;
    private String email;
    private String nickname;
    private String password;

    public SecurityUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.password = user.getPassword();
    }

    public SecurityUser(Long id, String email, String nickname, String password) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("AUTHORITY"));
    }

    @Override
    public String getUsername() {
        return "";
    }
}
