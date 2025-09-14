package com.moshy.jchirp.config;

import com.moshy.jchirp.ApiConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;


@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry r) {
        r.addViewController("/app").setViewName("forward:/app/index.html");
        r.addViewController("/app/").setViewName("forward:/app/index.html");
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        final var userDir = System.getProperty("user.dir");
        registry
            .addResourceHandler("/app/**")
            .addResourceLocations("file:" + userDir + "/../static/");
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new StaticHitCount());
    }
}

@Component
@Slf4j
class StaticHitCount implements HandlerInterceptor {
    private final static AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest req, @NonNull HttpServletResponse resp, @NonNull Object handler) {
        final var uri = req.getRequestURI();
        if (matcher.match("/app/**", uri)) {
            // the redirect triggers a double hit we need to guard against
            if (uri.equals("/app") || uri.equals("/app/"))
                return true;
            log.info("Hit: {}", uri);
            ApiConfig.fileserverHits.incrementAndGet();
        }
        return true;
    }
}