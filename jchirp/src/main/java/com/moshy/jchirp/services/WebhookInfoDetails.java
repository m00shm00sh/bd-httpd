package com.moshy.jchirp.services;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class WebhookInfoDetails implements UserDetails {
    private final String username;
    private final String password;

    private final List<? extends GrantedAuthority> authorities;


    public WebhookInfoDetails(String k) {
        this.username = "ApiKey";
        this.password = k;

        this.authorities = new ArrayList<>();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
