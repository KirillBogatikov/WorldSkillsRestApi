package org.ws.mts.service;

import java.util.List;

import org.ws.mts.models.SearchQuery;
import org.ws.mts.models.User;

public interface UserService {
	public List<User> search(List<SearchQuery> queries) throws Exception;
	public boolean exists(String id) throws Exception;
}
