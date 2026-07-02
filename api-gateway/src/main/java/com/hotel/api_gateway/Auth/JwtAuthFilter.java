package com.hotel.api_gateway.Auth;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/user-service/auth/")) {
            return chain.filter(exchange);
        }

        // Handle WebSocket connections - get token from query param
        if (path.contains("/ws")) {
            String tokenFromQuery = exchange.getRequest().getQueryParams().getFirst("token");
            if (tokenFromQuery != null && !tokenFromQuery.isEmpty()) {
                if (jwtUtil.isTokenValid(tokenFromQuery)) {
                    Claims claims = jwtUtil.extractAllClaims(tokenFromQuery);
                    String username = claims.getSubject();
                    int userId = (Integer) claims.get("userId");
                    List<String> roles = (List<String>) claims.get("authorities");

                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .headers(headers -> {
                                headers.remove("X-Auth-Username");
                                headers.remove("X-Auth-Roles");
                                headers.remove("X-Auth-UserId");
                                headers.add("X-Auth-Username", username);
                                headers.add("X-Auth-Roles", String.join(",", roles));
                                headers.add("X-Auth-UserId", String.valueOf(userId));
                            })
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                }
            }
            // Allow WebSocket to pass through for SockJS handshake
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Claims claims = jwtUtil.extractAllClaims(token);
        String username = claims.getSubject();
        int userId = (Integer) claims.get("userId");
        List<String> roles = (List<String>) claims.get("authorities");

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove("X-Auth-Username");
                    headers.remove("X-Auth-Roles");
                    headers.remove("X-Auth-UserId");
                    headers.add("X-Auth-Username", username);
                    headers.add("X-Auth-Roles", String.join(",", roles));
                    headers.add("X-Auth-UserId", String.valueOf(userId));
                })
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1; // ưu tiên cao
    }
}
