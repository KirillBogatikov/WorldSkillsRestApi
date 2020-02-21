package org.ws.mts.models;

public class WebUpdatePhoto {
	private String _method;
	private String name;
	private String photo;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageBase64() {
		return photo;
	}

	public void setImageBase64(String imageBase64) {
		this.photo = imageBase64;
	}

	public String getMethod() {
		return _method;
	}

	public void setMethod(String method) {
		this._method = method;
	}
	
}
