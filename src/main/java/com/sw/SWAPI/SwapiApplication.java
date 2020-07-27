package com.sw.SWAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SwapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwapiApplication.class, args);
	}

}
