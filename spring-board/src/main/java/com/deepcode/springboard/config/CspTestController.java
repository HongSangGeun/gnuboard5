package com.deepcode.springboard.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * CSP 테스트 페이지 컨트롤러
 */
@Controller
public class CspTestController {

    @GetMapping("/csp-test")
    public String cspTest() {
        return "csp-test";
    }
}
