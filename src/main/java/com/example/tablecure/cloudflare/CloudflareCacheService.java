package com.example.tablecure.cloudflare;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CloudflareCacheService {

    private final RestClient restClient = RestClient.create();

    @Value("${cloudflare.zone-id:}")
    private String zoneId;

    @Value("${cloudflare.api-token:}")
    private String apiToken;

    @Value("${app.api-base-url:https://api.tablecure.com}")
    private String apiBaseUrl;

    public void purgeProduct(Long productId) {
        List<String> paths = List.of(
                "/api/products",
                "/api/products/" + productId,
                "/api/products/" + productId + "/details"
        );

        // Cloudflare allows mixing plain URL strings and header-targeted objects in one request.
        // We purge each URL twice:
        //   1. plain URL  — clears the no-Origin variant (direct curl / server-side calls)
        //   2. with Origin header — clears the www.tablecure.com variant cached from browser requests
        // This covers old Vary:Origin cache entries that may still exist from before the filter was deployed.
        List<Object> files = new ArrayList<>();
        for (String path : paths) {
            String url = apiBaseUrl + path;
            files.add(url);
            files.add(Map.of("url", url, "headers", Map.of("Origin", "https://www.tablecure.com")));
        }

        purge(files);
    }

    private void purge(List<Object> files) {
        if (zoneId.isBlank() || apiToken.isBlank()) {
            log.warn("Cloudflare env vars not set (CLOUDFLARE_ZONE_ID / CLOUDFLARE_API_TOKEN) — cache purge skipped");
            return;
        }
        try {
            var response = restClient.post()
                    .uri("https://api.cloudflare.com/client/v4/zones/" + zoneId + "/purge_cache")
                    .header("Authorization", "Bearer " + apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("files", files))
                    .retrieve()
                    .toEntity(String.class);
            // Log the full response body — Cloudflare can return HTTP 200 but success:false in the body
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Cloudflare purge response ({}): {}", response.getStatusCode(), response.getBody());
            } else {
                log.error("Cloudflare purge failed ({}) body: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Cloudflare purge exception: {}", e.getMessage());
        }
    }
}
