package com.sw.SWAPI.damain;

import lombok.Data;

@Data
public class User {

    private String userName;
    private String login_ID;
    private String email;
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getLogin_ID() {
		return login_ID;
	}
	public void setLogin_ID(String login_ID) {
		this.login_ID = login_ID;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
    
}
