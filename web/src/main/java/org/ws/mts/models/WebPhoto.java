package org.ws.mts.models;

import java.util.List;

public class WebPhoto {
	private String id;
	private String name;
	private String url;
	private String owner_id;
	private List<String> users;

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
		return url;
	}

	public void setLink(String link) {
		this.url = link;
	}
	
	public String getOwner() {
		return owner_id;
	}

	public void setOwner(String owner) {
		this.owner_id = owner;
	}

	public List<String> getTrusted() {
		return users;
	}

	public void setTrusted(List<String> trusted) {
		this.users = trusted;
	}
	
}
