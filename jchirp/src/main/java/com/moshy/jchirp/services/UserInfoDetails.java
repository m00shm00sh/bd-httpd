package com.moshy.jchirp.services;

import com.moshy.jchirp.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class UserInfoDetails implements UserDetails {
    private final String username;
    private final String password;

    private final User dbRow;

    private final List<? extends GrantedAuthority> authorities;


    public UserInfoDetails(User user) {
        this.username = user.getId().toString();
        this.password = user.getPassword();
        this.dbRow = user;

        var authorities = new ArrayList<GrantedAuthority>();
        if (user.isChirpyRed())
            authorities.add(new SimpleGrantedAuthority("red"));
        this.authorities = authorities;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
