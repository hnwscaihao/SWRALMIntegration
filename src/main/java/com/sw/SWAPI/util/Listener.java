package com.sw.SWAPI.util;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 定义事件监听器
 * @author Administrator
 *
 */
@Component
@PropertySource(value = {"classpath:sw.properties"})
public class Listener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${token}")
    private String token;

    @Value("${host}")
    private String host;

    @Value("${port}")
    private int port;


    @Override
    @SneakyThrows
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String url = "http://" + host + ":" + port;
        System.out.println("项目启动成功: " + url);
    }

}