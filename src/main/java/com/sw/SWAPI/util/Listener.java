package com.sw.SWAPI.util;

import connect.Connection;
import connect.IntegrityFactory;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.sw.SWAPI.damain.ConfigureField;

/**
 * 定义事件监听器
 * @author Administrator
 *
 */
@Component
@PropertySource(value = {"classpath:sw.properties"})
public class Listener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${token}")
    public String token;

    @Value("${host}")
    private String host;

    @Value("${port}")
    private int port;

    @Value("${loginName}")
    private String loginName;

    @Value("${passWord}")
    private String passWord;

    @Override
    @SneakyThrows
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String url = "http://" + host + ":" + port;
        ConfigureField configureField = new ConfigureField();
        configureField.setHost(host);
        configureField.setPort(port);
        configureField.setLoginName(loginName);
        configureField.setPassWord(passWord);
        MKSCommand.conn = new Connection(configureField);
        System.out.println("项目启动成功: " + url);

    }


}