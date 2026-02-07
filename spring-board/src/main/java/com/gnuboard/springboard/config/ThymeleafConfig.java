package com.gnuboard.springboard.config;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.core.io.ResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ThymeleafConfig implements WebMvcConfigurer {
    private final G5ConfigService configService;
    private final ResourceLoader resourceLoader;

    public ThymeleafConfig(G5ConfigService configService, ResourceLoader resourceLoader) {
        this.configService = configService;
        this.resourceLoader = resourceLoader;
    }
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CsrfInterceptor())
                .addPathPatterns("/**");

        registry.addInterceptor(new MobileViewInterceptor(resourceLoader))
                .addPathPatterns("/**");

        registry.addInterceptor(new AdminAuthInterceptor(configService))
                .addPathPatterns("/mgmt/**");
    }
}
