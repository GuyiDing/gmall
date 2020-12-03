package com.example.canatest;

import com.xpand.starter.canal.annotation.EnableCanalClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCanalClient
public class CanalTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(CanalTestApplication.class, args);
	}
}
