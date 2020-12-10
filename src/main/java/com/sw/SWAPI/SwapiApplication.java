package com.sw.SWAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: 应用初始化
 */
@SpringBootApplication
@EnableCaching
public class SwapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwapiApplication.class, args);

	}

}
