package com.sw.SWAPI.damain;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;



@Data
public class ConfigureField {

    private String token;

    private String host;

    private int port;

    private String loginName;

    private String passWord;

}
