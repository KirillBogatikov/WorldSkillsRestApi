package org.ws.mts.models;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class WebPhoto {
	private String id;
	private String name;
	@SerializedName("url")
	private String link;
	@SerializedName("owner_id")
	private String owner;
	@SerializedName("users")
	private List<String> trusted;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public List<String> getTrusted() {
		return trusted;
	}

	public void setTrusted(List<String> trusted) {
		this.trusted = trusted;
	}
	
}
