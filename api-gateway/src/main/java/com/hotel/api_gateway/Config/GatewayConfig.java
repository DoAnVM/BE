package com.hotel.api_gateway.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${services.user-service:lb://user-service}")
    private String userServiceUrl;

    @Value("${services.booking-service:lb://booking-service}")
    private String bookingServiceUrl;

    @Value("${services.review-service:lb://review-service}")
    private String reviewServiceUrl;

    @Value("${services.hotel-service:lb://hotel-service}")
    private String hotelServiceUrl;

    @Value("${services.room-service:lb://room-service}")
    private String roomServiceUrl;

    @Value("${services.payment-service:lb://payment-service}")
    private String paymentServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service
                .route("user-service", r -> r.path("/user-service/**")
                        .filters(f -> f.rewritePath("/user-service/(?<segment>.*)", "/${segment}"))
                        .uri(userServiceUrl))
                // Booking Service
                .route("booking-service", r -> r.path("/booking-service/**")
                        .filters(f -> f.rewritePath("/booking-service/(?<segment>.*)", "/${segment}"))
                        .uri(bookingServiceUrl))
                // Payment Service
                .route("payment-service", r -> r.path("/payment-service/**")
                        .filters(f -> f.rewritePath("/payment-service/(?<segment>.*)", "/${segment}"))
                        .uri(paymentServiceUrl))
                // Review Service
                .route("review-service", r -> r.path("/review-service/**")
                        .filters(f -> f.rewritePath("/review-service/(?<segment>.*)", "/${segment}"))
                        .uri(reviewServiceUrl))
                // Hotel Service
                .route("hotel-service", r -> r.path("/hotel-service/**")
                        .filters(f -> f.rewritePath("/hotel-service/(?<segment>.*)", "/${segment}"))
                        .uri(hotelServiceUrl))
                //Room Service
                .route("room-service", r -> r.path("/room-service/**")
                        .filters(f -> f.rewritePath("/room-service/(?<segment>.*)", "/${segment}"))
                        .uri(roomServiceUrl))
                .build();
    }
}