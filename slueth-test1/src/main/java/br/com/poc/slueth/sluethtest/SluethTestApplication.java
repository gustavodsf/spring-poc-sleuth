package br.com.poc.slueth.sluethtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class SluethTestApplication {

	public static void main(String[] args) {

		SpringApplication.run(SluethTestApplication.class, args);
	}

	@Bean WebClient webClient() {
		return WebClient.create();
	}
}
