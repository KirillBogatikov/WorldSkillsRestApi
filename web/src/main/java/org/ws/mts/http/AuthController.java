package org.ws.mts.http;

import org.cuba.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ws.mts.models.Response;
import org.ws.mts.models.User;
import org.ws.mts.models.WebCredentials;
import org.ws.mts.models.WebUser;
import org.ws.mts.service.AuthService;
import org.ws.mts.utils.Mapper;

@RestController
@RequestMapping("/photos/api")
public class AuthController {
	private static final String TAG = AuthController.class.getSimpleName();
	
	@Autowired
	private AuthService service;
	@Autowired
	private Log log;
	
	public class SignupResponse {
		public String id;
	}
	
	@PostMapping("/signup")
	public ResponseEntity<? extends Object> signup(@RequestBody WebUser user) {
		try {
			Response response = service.singup(Mapper.from(user));
			
			switch(response.getStatus()) {
				case OK: 
					SignupResponse resp = new SignupResponse();
					resp.id = response.getContent();
					return new ResponseEntity<>(resp, HttpStatus.CREATED);
				case INVALID:
					User _user = response.getContent();
					return new ResponseEntity<>(Mapper.from(_user), HttpStatus.UNPROCESSABLE_ENTITY);
				default:
					break;
			}
		} catch(Throwable t) {
			log.e(TAG, "signup", t);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	public class LoginTokenResponse {
		public String token;
	}
	
	public class LoginResponse {
		public String login;
	}

	@PostMapping("/login")
	public ResponseEntity<? extends Object> login(@RequestBody WebCredentials credentials) {
		try {
			Response response = service.login(Mapper.from(credentials));
			
			switch(response.getStatus()) {
				case OK: {
					LoginTokenResponse resp = new LoginTokenResponse();
					resp.token = response.getContent();
					return new ResponseEntity<>(resp, HttpStatus.CREATED);
				}
				case INVALID:
					User _user = response.getContent();
					return new ResponseEntity<>(Mapper.from(_user), HttpStatus.UNPROCESSABLE_ENTITY);
				case NOT_FOUND: {
					LoginResponse resp = new LoginResponse();
					resp.login = "Incorrect login or password";
					return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
				}
				default:
					break;
			}
		} catch(Throwable t) {
			log.e(TAG, "login", t);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	public class MessageResponse {
		public String message;
	}

	@PostMapping("/logout")
	public ResponseEntity<? extends Object> logout(@RequestHeader(name = "Authorization", required = false) String token) {
		try {
			if(!service.checkToken(token)) {
				MessageResponse resp = new MessageResponse();
				resp.message = "You need authorization";
				return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
			}
			
			service.logout(token.replace("Bearer ", ""));
			return new ResponseEntity<>(HttpStatus.OK);
		} catch(Throwable t) {
			log.e(TAG, "logout", t);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
