package com.hotel.api_gateway.Config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {
    // CORS is handled via globalcors in application.yaml
    // to prevent duplicate CORS headers with DedupeResponseHeader filter
}
