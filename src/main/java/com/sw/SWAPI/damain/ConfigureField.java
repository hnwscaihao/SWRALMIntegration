package com.sw.SWAPI.damain;

import lombok.Data;


/**
 * @author: liuxiaoguang
 * @Date: 2020/7/16 15:28
 * @Description: 基础配置类
 */
@Data
public class ConfigureField {

    private String token;

    private String host;

    private int port;

    private String loginName;

    private String passWord;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

}
