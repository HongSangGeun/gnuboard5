package com.deepcode.springboard.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizer {
    private static final Safelist CONTENT_SAFELIST = Safelist.relaxed()
            .addTags("span", "div", "table", "thead", "tbody", "tfoot", "tr", "th", "td", "colgroup", "col")
            .addAttributes(":all", "class")
            .addAttributes("a", "target", "rel");

    public String sanitizeRichText(String html) {
        if (html == null) {
            return "";
        }

        // Parse the original HTML
        Document dirty = Jsoup.parse(html);

        // Clean using Safelist
        Cleaner cleaner = new Cleaner(CONTENT_SAFELIST);
        Document clean = cleaner.clean(dirty);

        // Manually restore img src attributes for relative URLs
        // (Safelist removes them due to protocol restrictions)
        Elements dirtyImages = dirty.select("img[src]");
        Elements cleanImages = clean.select("img");

        for (int i = 0; i < Math.min(dirtyImages.size(), cleanImages.size()); i++) {
            Element dirtyImg = dirtyImages.get(i);
            Element cleanImg = cleanImages.get(i);
            String src = dirtyImg.attr("src");

            // Only allow safe protocols: relative URLs (/...), safe data image URLs, http, https
            if (src.startsWith("/") ||
                src.startsWith("http://") || src.startsWith("https://")) {
                cleanImg.attr("src", src);
            } else if (src.startsWith("data:image/") && !src.contains("svg") &&
                       src.matches("^data:image/(png|jpeg|jpg|gif|webp|bmp);base64,[A-Za-z0-9+/=]+$")) {
                cleanImg.attr("src", src);
            }
        }

        clean.outputSettings().prettyPrint(false);
        return clean.body().html();
    }

    public String sanitizePlainText(String text) {
        if (text == null) {
            return "";
        }
        return Jsoup.clean(text, Safelist.none()).trim();
    }
}
