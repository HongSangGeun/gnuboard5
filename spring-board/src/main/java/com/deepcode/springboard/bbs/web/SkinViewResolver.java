package com.deepcode.springboard.bbs.web;

import com.deepcode.springboard.bbs.domain.Board;
import com.deepcode.springboard.config.MobileViewInterceptor;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class SkinViewResolver {
    private final ResourceLoader resourceLoader;

    public SkinViewResolver(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String resolve(Board board, String view) {
        String skin = resolveSkin(board);
        if (!skin.isBlank()) {
            // Priority 1: Mobile specific skin path
            if (isMobileRequest()) {
                String candidate = "mobile/skin/board/" + skin + "/" + view;
                if (resourceLoader.getResource("classpath:/templates/" + candidate + ".html").exists()) {
                    return candidate;
                }
            }

            // Priority 2: Standard/PC skin path
            String candidate = "skin/board/" + skin + "/" + view;
            if (resourceLoader.getResource("classpath:/templates/" + candidate + ".html").exists()) {
                return candidate;
            }

            // Priority 3: Fallback location (bbs/...)
            candidate = "bbs/" + skin + "/" + view;
            if (resourceLoader.getResource("classpath:/templates/" + candidate + ".html").exists()) {
                return candidate;
            }
        }
        // Fallback: Default templates in bbs/{view}.html
        return "bbs/" + view;
    }

    private String resolveSkin(Board board) {
        String defaultSkin = sanitize(board.getBoSkin());
        if (!isMobileRequest()) {
            return defaultSkin;
        }
        String mobileSkin = sanitize(board.getBoMobileSkin());
        if (!mobileSkin.isBlank()) {
            return mobileSkin;
        }
        return defaultSkin;
    }

    private boolean isMobileRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return false;
        }
        Object flag = attrs.getAttribute(MobileViewInterceptor.ATTR_IS_MOBILE_VIEW, RequestAttributes.SCOPE_REQUEST);
        return Boolean.TRUE.equals(flag);
    }

    private String sanitize(String skin) {
        if (skin == null) {
            return "";
        }
        String trimmed = skin.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (!trimmed.matches("[a-zA-Z0-9_-]+")) {
            return "";
        }
        return trimmed;
    }
}
