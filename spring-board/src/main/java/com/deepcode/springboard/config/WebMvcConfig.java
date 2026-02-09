package com.deepcode.springboard.config;

import com.deepcode.springboard.visit.VisitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final VisitInterceptor visitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 접속자 로그 인터셉터 등록
        registry.addInterceptor(visitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/static/**",
                    "/webjars/**",
                    "/favicon.ico",
                    "/error"
                );
    }
}
