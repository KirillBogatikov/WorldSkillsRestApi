package org.ws.mts.service;

import org.ws.mts.models.Credentials;
import org.ws.mts.models.Response;
import org.ws.mts.models.User;

public interface AuthService {
	public String userId(String token) throws Exception;
	public Response singup(User user) throws Exception;
	public Response login(Credentials credentials) throws Exception;
	public void logout(String token) throws Exception;
	public boolean checkToken(String token) throws Exception;
}
