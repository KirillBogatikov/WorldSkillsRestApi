package org.ws.mts.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class WebUpdatePhoto {
	@SerializedName("_method")
    @JsonProperty("_method")
	private String method;
	private String name;
	@SerializedName("photo")
    @JsonProperty("photo")
	private String imageBase64;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageBase64() {
		return imageBase64;
	}

	public void setImageBase64(String imageBase64) {
		this.imageBase64 = imageBase64;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
}
