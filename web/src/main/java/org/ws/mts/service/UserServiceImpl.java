package org.ws.mts.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ws.mts.database.DatabaseContext;
import org.ws.mts.database.UsersRepository;
import org.ws.mts.models.DbUser;
import org.ws.mts.models.SearchQuery;
import org.ws.mts.models.User;

public class UserServiceImpl implements UserService {
	private UsersRepository usersRepo;
	
	public UserServiceImpl(DatabaseContext database) throws SQLException {
		this.usersRepo = new UsersRepository(database);
	}
		
	@Override
	public List<User> search(List<SearchQuery> queries) throws Exception {
		StringBuilder nameRegex = new StringBuilder();
		StringBuilder surnameRegex = new StringBuilder();
		StringBuilder phoneRegex = new StringBuilder();
		
		for(SearchQuery query : queries) {
			if(query.getNamePart() != null) {
				nameRegex.append("(").append(query.getNamePart()).append(".*)").append("|");
			}
			if(query.getSurnamePart() != null) {
				surnameRegex.append("(").append(query.getSurnamePart()).append(".*)").append("|");
			}
			if(query.getPhonePart() != null) {
				phoneRegex.append("(").append(query.getPhonePart()).append(".*)").append("|");
			}
		}
		
		int length = nameRegex.length();
		if(length > 1) {
			nameRegex.deleteCharAt(length - 1);
		}
		length = surnameRegex.length();
		if(length > 1) {
			surnameRegex.deleteCharAt(length - 1);
		}
		length = phoneRegex.length();
		if(length > 1) {
			phoneRegex.deleteCharAt(length - 1);
		}
		
		List<DbUser> dbUsers = usersRepo.search(nameRegex.toString(), surnameRegex.toString(), phoneRegex.toString());
		ArrayList<User> users = new ArrayList<>();
		
		for(DbUser dbUser : dbUsers) {
			User user = new User();
			
			user.setId(dbUser.getId());
			user.setName(dbUser.getName());
			user.setSurname(dbUser.getSurname());
			user.setPhone(dbUser.getPhone());
			
			users.add(user);
		}
		
		return users;
	}

	@Override
	public boolean exists(String id) throws Exception {
		return usersRepo.exists(id);
	}

}
