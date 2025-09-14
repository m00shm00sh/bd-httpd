package com.moshy.jchirp.config;

import com.moshy.jchirp.filters.PolkaWebhookFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.moshy.jchirp.filters.JwtAuthFilter;
import com.moshy.jchirp.filters.RefreshTokenFilter;

import lombok.AllArgsConstructor;


@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;
    private final RefreshTokenFilter refreshFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordConfig passwordConfig;
    private final PolkaWebhookFilter polkaFilter;

    @Bean
    public SecurityFilterChain filterChainNone(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .securityMatcher("/admin/**", "/app/**", "/api/login")
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a ->
                a
                    .requestMatchers(HttpMethod.GET, "/api/healthz", "/app/**", "/admin/**")
                        .permitAll()
                    .requestMatchers(HttpMethod.POST, "/admin/reset", "/api/login")
                        .permitAll()
            );
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChainChirps(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .securityMatcher("/api/chirps", "/api/chirps/{chirp}")
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(a ->
                a
                    .requestMatchers(HttpMethod.GET, "/api/chirps", "/api/chirps/{chirp}")
                        .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/chirps")
                        .authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/chirps/{chirp}")
                        .authenticated()
            )
            .exceptionHandling(e ->
                e
                    .authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())
                    )
            );
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChainUsers(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .securityMatcher("/api/users")
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(a ->
                a
                    .requestMatchers(HttpMethod.POST, "/api/users")
                        .permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/users")
                        .authenticated()
            )
            .exceptionHandling(e ->
                e
                    .authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())
                    )
            );
        return http.build();
    }
    
    @Bean
    public SecurityFilterChain filterChainRefresh(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .securityMatcher("/api/refresh", "/api/revoke")
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(refreshFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(a ->
                a
                    .requestMatchers(HttpMethod.POST,"/api/refresh", "/api/revoke")
                        .authenticated()
            )
            .exceptionHandling(e ->
                e
                    .authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())
                    )
            );

        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChainPolka(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .securityMatcher("/api/polka/webhooks")
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(polkaFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(a ->
                a.requestMatchers(HttpMethod.POST, "/api/polka/webhooks").authenticated()
            )
            .exceptionHandling(e ->
                e
                    .authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())
                    )
            );
        return http.build();
    }

    // attaches passwordEncoder to UserDetailsService
    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordConfig.passwordEncoder());
        return provider;
    }

    // gives login endpoint an authentication manager to forward logins to
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
