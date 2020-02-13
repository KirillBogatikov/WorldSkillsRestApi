package org.ws.mts.models;

import java.util.Date;

public class DbToken {
	private String user;
	private String token;
	private Date dieTime;
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public Date getDieTime() {
		return dieTime;
	}
	
	public void setDieTime(Date dieTime) {
		this.dieTime = dieTime;
	}
		
}
