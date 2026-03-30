package com.example.tablecure.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                // Reviews endpoint — requires credentials (authenticated POST)
                registry.addMapping("/api/products/*/reviews")
                        .allowedOrigins(
                                "http://localhost:3000",
                                "https://tablecure.vercel.app",
                                "https://tablecure.com",
                                "https://www.tablecure.com"
                        )
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("Content-Type", "Authorization")
                        .allowCredentials(true);

                // Public product endpoints — no credentials needed, so wildcard origin is fine.
                // This prevents Spring from adding Vary: Origin, which would cause Cloudflare
                // to store separate cache entries per origin and break cache purge.
                registry.addMapping("/api/products/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET")
                        .allowedHeaders("Content-Type", "Authorization");

                // All other endpoints — specific origins with credentials
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:3000",
                                "https://tablecure.vercel.app",
                                "https://tablecure.com",
                                "https://www.tablecure.com"
                        )
                        .allowedMethods("GET", "PATCH", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("Content-Type", "Authorization")
                        .allowCredentials(true);
            }
        };
    }
}