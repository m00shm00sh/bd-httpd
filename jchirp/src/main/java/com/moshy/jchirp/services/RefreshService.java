package com.moshy.jchirp.services;

import com.moshy.jchirp.repositories.RefreshRepository;
import com.moshy.jchirp.repositories.UserRepository;

import lombok.AllArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service("LookupByRefresh")
public class RefreshService implements UserDetailsService {
    private final RefreshRepository repository;
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = repository.findByTokenAndExpiresAtGreaterThanAndRevokedAtIsNull(username, LocalDateTime.now())
            .orElseThrow(() -> new UsernameNotFoundException("token not found"));
        var uid = user.getUser().getId();
        return userRepository.findById(uid)
            .map(UserInfoDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }
}
