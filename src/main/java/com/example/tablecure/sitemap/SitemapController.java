package com.example.tablecure.sitemap;

import com.example.tablecure.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SitemapController {

    private final ProductRepository productRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // ── Static public pages ──────────────────────────────────
        addUrl(xml, frontendUrl + "/",          "1.0", "daily",  LocalDate.now().toString());
        addUrl(xml, frontendUrl + "/products",  "0.9", "daily",  LocalDate.now().toString());
        addUrl(xml, frontendUrl + "/about",     "0.5", "monthly", null);
        addUrl(xml, frontendUrl + "/contact",   "0.5", "monthly", null);

        // ── Dynamic product pages ────────────────────────────────
        List<Long> productIds = productRepository.findAllIds();
        for (Long id : productIds) {
            addUrl(xml, frontendUrl + "/products/" + id, "0.8", "weekly", null);
        }

        xml.append("</urlset>");
        return xml.toString();
    }

    private void addUrl(StringBuilder xml, String loc, String priority,
                        String changefreq, String lastmod) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");
        if (lastmod != null) {
            xml.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        }
        xml.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        xml.append("    <priority>").append(priority).append("</priority>\n");
        xml.append("  </url>\n");
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
