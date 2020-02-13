package org.ws.mts.service;

import java.sql.SQLException;
import java.util.Date;

import org.ws.mts.database.DatabaseContext;
import org.ws.mts.database.TokensRepository;
import org.ws.mts.database.UsersRepository;
import org.ws.mts.models.Credentials;
import org.ws.mts.models.DbToken;
import org.ws.mts.models.DbUser;
import org.ws.mts.models.Response;
import org.ws.mts.models.Status;
import org.ws.mts.models.User;
import org.ws.mts.utils.Generator;

public class AuthServiceImpl implements AuthService {
	private UsersRepository usersRepo;
	private TokensRepository tokensRepo;
	
	public AuthServiceImpl(DatabaseContext database) throws SQLException {
		this.usersRepo = new UsersRepository(database);
		this.tokensRepo = new TokensRepository(database);
	}
	
	@Override
	public String userId(String token) throws Exception {
		return tokensRepo.userId(token.replace("Bearer ", ""));
	}

	@Override
	public Response singup(User user) throws Exception {
		String id = Generator.generate(8);
		String name = user.getName();
		String surname = user.getSurname();
		String phone = user.getPhone();
		String password = user.getPassword();
		
		DbUser dbUser = new DbUser();
		dbUser.setId(id);
		dbUser.setName(name);
		dbUser.setSurname(surname);
		dbUser.setPhone(phone);
		dbUser.setPassword(password);
		
		boolean invalid = false;
		if(!usersRepo.check(user.getPhone())) {
			invalid = true;
			user.setPhone("Пользователь с таким телефоном уже зарегистрирован");
		} else {
			user.setPhone(null);
		}
		if(!name.matches("[а-яА-Яa-zA-Z]+(-[а-яА-Яa-zA-Z]+)?")) {
			invalid = true;
			user.setName("Имя может состоять из букв русского или латинского алфавита. Допустимо наличие одного дефиса.");
		} else if(name.length() < 2 || name.length() > 16) {
			invalid = true;
			user.setName("Имя должно содержать от 2 до 16 символов");
		} else {
			user.setName(null);
		}
		if(!surname.matches("[а-яА-Яa-zA-Z]+(-[а-яА-Яa-zA-Z]+)?")) {
			invalid = true;
			user.setSurname("Фамилия может состоять из букв русского или латинского алфавита. Допустимо наличие одного дефиса.");
		} else if(surname.length() < 2 || surname.length() > 64) {
			invalid = true;
			user.setSurname("Фамилия должна содержать от 2 до 64 символов");
		} else {
			user.setSurname(null);
		}
		if(!phone.matches("[0-9]{11}")) {
			invalid = true;
			user.setPhone("Номер телефона должен состоять из 11 цифр. Допустимо наличие нулей в начале.");
		}
		if(!password.matches("[a-zA-Z0-9_~!@$%+\\-]+")) {
			invalid = true;
			user.setPassword("Пароль может состоять из букв латинского алфавита, цифр и может содержать следующие символы: _+-~!@$%");
		} else if(password.length() < 8 || password.length() > 32) {
			invalid = true;
			user.setPassword("Пароль должен содержать от 8 до 32 символов");
		} else {
			user.setPassword(null);
		}
		
		if(invalid) {
			return new Response(user, Status.INVALID);
		}
		
		usersRepo.save(dbUser);
				
		return new Response(id, Status.OK);
	}

	@Override
	public Response login(Credentials credentials) throws Exception {
		String phone = credentials.getPhone();
		String password = credentials.getPassword();
		
		if(usersRepo.check(phone)) {
			return new Response(null, Status.NOT_FOUND);
		}
		
		System.out.println(password.equals(usersRepo.password(phone)));
		if(password == null || !password.equals(usersRepo.password(phone))) {
			return new Response(null, Status.NOT_FOUND);
		}

		boolean invalid = false;
		if(!phone.matches("\\+?[0-9]{11}")) {
			invalid = true;
			credentials.setPhone("Номер телефона должен состоять из 11 цифр. Допустимо наличие знака + в начале.");
		}
		if(!password.matches("[a-zA-Z0-9_~!@$%+\\-]+")) {
			invalid = true;
			credentials.setPassword("Пароль может состоять из букв латинского алфавита, цифр и может содержать следующие символы: _+-~!@$%");
		}
		if(password.length() < 8 || password.length() > 32) {
			invalid = true;
			credentials.setPassword("Пароль должен содержать от 8 до 32 символов");
		}
		
		if(invalid) {
			return new Response(credentials, Status.INVALID);
		}
		
		DbToken token = new DbToken();
		token.setUser(usersRepo.userId(credentials.getPhone()));
		token.setToken(Generator.generate(64));
		token.setDieTime(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
		
		tokensRepo.save(token);
		
		return new Response(token.getToken(), Status.OK);
	}

	@Override
	public void logout(String token) throws Exception {
		tokensRepo.delete(token);
	}

	@Override
	public boolean checkToken(String token) throws Exception {
		if(!token.startsWith("Bearer")) {
			return false;
		}
		
		token = token.replace("Bearer ", "");
		
		Date dieDate = tokensRepo.dieDate(token);
		if(dieDate == null) {
			return false;
		}
		
		System.out.println(dieDate.getTime());
		System.out.println(System.currentTimeMillis());
		
		if(dieDate.getTime() < System.currentTimeMillis()) {
			tokensRepo.delete(token);
			return false;
		}
		
		return true;
	}

}
