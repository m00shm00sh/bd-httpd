package com.moshy.jchirp.services;

import com.moshy.jchirp.entities.User;
import com.moshy.jchirp.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Primary
@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;

    private final PasswordEncoder encoder;

    public User findUserById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UsernameNotFoundException("no match for id"));
    }

    public User findUserByEmail(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("no match for email"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = findUserById(UUID.fromString(username));
        return new UserInfoDetails(user);
    }
    public User addUser(User u) {
        u.setPassword(encoder.encode(u.getPassword()));
        u = repository.saveAndFlush(u);
        repository.refresh(u);
        return u;
    }
}
