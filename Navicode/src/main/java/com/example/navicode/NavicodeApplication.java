package com.example.navicode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NavicodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(NavicodeApplication.class, args);
	}

}
