package com.gnuboard.springboard.common;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizer {
    private static final Safelist CONTENT_SAFELIST = Safelist.relaxed()
            .addTags("span", "div", "table", "thead", "tbody", "tfoot", "tr", "th", "td", "colgroup", "col")
            .addAttributes(":all", "class")
            .addAttributes("a", "target", "rel")
            .addProtocols("img", "src", "http", "https", "data");

    public String sanitizeRichText(String html) {
        if (html == null) {
            return "";
        }
        return Jsoup.clean(html, CONTENT_SAFELIST);
    }

    public String sanitizePlainText(String text) {
        if (text == null) {
            return "";
        }
        return Jsoup.clean(text, Safelist.none()).trim();
    }
}
