package com.example.tablecure.cloudflare;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

    /**
     * Purges Cloudflare cache for all URLs related to a specific product:
     *   /api/products          (list — imageUrl shown here too)
     *   /api/products/{id}     (single product)
     *   /api/products/{id}/details
     */
    public void purgeProduct(Long productId) {
        purge(List.of(
                apiBaseUrl + "/api/products",
                apiBaseUrl + "/api/products/" + productId,
                apiBaseUrl + "/api/products/" + productId + "/details"
        ));
    }

    private void purge(List<String> urls) {
        if (zoneId.isBlank() || apiToken.isBlank()) {
            log.debug("Cloudflare env vars not set — skipping cache purge");
            return;
        }
        try {
            restClient.post()
                    .uri("https://api.cloudflare.com/client/v4/zones/" + zoneId + "/purge_cache")
                    .header("Authorization", "Bearer " + apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("files", urls))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Cloudflare cache purged: {}", urls);
        } catch (Exception e) {
            log.warn("Cloudflare cache purge failed (non-critical): {}", e.getMessage());
        }
    }
}
