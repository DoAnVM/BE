package com.hotel.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {

        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("API Gateway is running...");
	}

}
