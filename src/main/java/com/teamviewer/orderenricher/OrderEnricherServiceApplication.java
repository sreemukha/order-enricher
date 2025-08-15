package com.teamviewer.orderenricher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OrderEnricherServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderEnricherServiceApplication.class, args);
	}

}
