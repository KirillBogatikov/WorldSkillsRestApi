package org.ws.mts.models;

public class Response {
	private Object content;
	private Status status;
	
	public Response(Object content, Status status) {
		this.content = content;
		this.status = status;
	}
	
	@SuppressWarnings("unchecked")
	public <E> E getContent() {
		return (E)content;
	}
	
	public Status getStatus() {
		return status;
	}
}
