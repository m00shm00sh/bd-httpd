package com.moshy.jchirp.config;

import com.moshy.jchirp.filters.PolkaWebhookFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.moshy.jchirp.filters.JwtAuthFilter;
import com.moshy.jchirp.filters.RefreshTokenFilter;

import lombok.AllArgsConstructor;


abstract class SecurityConfigBase {
    protected static void doSetupChain(
            HttpSecurity builder,
            String[] pathPatterns,
            Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeHttpRequestsCustomizer,
            @Nullable Filter authFilter,
            @Nullable AuthenticationProvider authProvider
    ) throws Exception {
        builder
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .securityMatcher(pathPatterns);
        if (authFilter != null)
            builder.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        if (authProvider != null)
            builder.authenticationProvider(authProvider);
        builder
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        builder.authorizeHttpRequests(authorizeHttpRequestsCustomizer);
        builder.exceptionHandling(send401OnNoAuth);
    }

    private static final Customizer<ExceptionHandlingConfigurer<HttpSecurity>> send401OnNoAuth = e ->
            e.authenticationEntryPoint((req, res, ex) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())
            );

    protected static String[] paths(String... args) {
        return args;
    }
}
@Configuration
@EnableWebSecurity
@AllArgsConstructor
class SecurityConfig extends SecurityConfigBase {
    private final JwtAuthFilter jwtFilter;
    private final RefreshTokenFilter refreshFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordConfig passwordConfig;
    private final PolkaWebhookFilter polkaFilter;

    @Bean
    public SecurityFilterChain filterChainNone(HttpSecurity http) throws Exception {
        doSetupChain(http, paths("/admin/**", "/app/**", "/api/login"),
            a -> a
                    .requestMatchers(HttpMethod.GET, "/api/healthz", "/app/**", "/admin/**")
                        .permitAll()
                    .requestMatchers(HttpMethod.POST, "/admin/reset", "/api/login")
                        .permitAll(),
                null, null
        );
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChainChirps(HttpSecurity http) throws Exception {
        doSetupChain(http, paths("/api/chirps", "/api/chirps/{chirp}"),
               a -> a
                   .requestMatchers(HttpMethod.GET, "/api/chirps", "/api/chirps/{chirp}")
                       .permitAll()
                   .requestMatchers(HttpMethod.POST, "/api/chirps")
                       .authenticated()
                   .requestMatchers(HttpMethod.DELETE, "/api/chirps/{chirp}")
                       .authenticated(),
                jwtFilter, authenticationProvider()
        );
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChainUsers(HttpSecurity http) throws Exception {
        doSetupChain(http, paths("/api/users"),
                a -> a
                    .requestMatchers(HttpMethod.POST, "/api/users")
                        .permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/users")
                        .authenticated(),
                jwtFilter, authenticationProvider()
        );
        return http.build();
    }
    
    @Bean
    public SecurityFilterChain filterChainRefresh(HttpSecurity http) throws Exception {
        doSetupChain(http, paths("/api/refresh", "/api/revoke"),
                a -> a
                .requestMatchers(HttpMethod.POST,"/api/refresh", "/api/revoke")
                    .authenticated(),
                refreshFilter, null
        );
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChainPolka(HttpSecurity http) throws Exception {
        doSetupChain(http, paths("/api/polka/webhooks"),
            a -> a
                .requestMatchers(HttpMethod.POST, "/api/polka/webhooks")
                    .authenticated(),
                polkaFilter, null
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
