package org.ws.mts.service;

import java.util.List;

import org.ws.mts.models.Photo;
import org.ws.mts.models.Response;

public interface PhotoService {
	public Response upload(byte[] imageContent, String user) throws Exception;
	public Response update(String id, byte[] imageContent, String name) throws Exception;
	public boolean checkAccess(String user, String photo) throws Exception;
	public List<Photo> list(String user) throws Exception;
	public Photo get(String photo) throws Exception;
	public void delete(String photo) throws Exception;
	public List<String> share(List<String> photos, String user) throws Exception;
	public byte[] content(String id) throws Exception;
}
