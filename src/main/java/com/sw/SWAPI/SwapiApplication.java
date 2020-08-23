package com.sw.SWAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCaching
//@ComponentScan({"com.sw"})
public class SwapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwapiApplication.class, args);

	}

}
