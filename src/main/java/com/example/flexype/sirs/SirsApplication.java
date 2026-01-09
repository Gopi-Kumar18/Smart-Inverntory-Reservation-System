package com.example.flexype.sirs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SirsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SirsApplication.class, args);
	}

}
