package org.ws.mts.http;

import org.cuba.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.ws.mts.models.Response;
import org.ws.mts.models.User;
import org.ws.mts.models.WebCredentials;
import org.ws.mts.models.WebUser;
import org.ws.mts.service.AuthService;
import org.ws.mts.utils.Mapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/photos/api")
@Api(value="Authorization", description="User authorization: login, signup and logout")
public class AuthController {
	private static final String TAG = AuthController.class.getSimpleName();
	
	@Autowired
	private AuthService service;
	@Autowired
	private Log log;
	
	public class SignupResponse {
		public String id;
	}
	
	@ApiOperation(value = "Register in the service", response = SignupResponse.class)
	@ApiResponses(value = {
        @ApiResponse(code = 201, message = "User created. You need to login", response = SignupResponse.class),
        @ApiResponse(code = 422, message = "Some fields has incorrect values", response = WebUser.class),
        @ApiResponse(code = 500, message = "Unknown internal server error"),
    })
	@ResponseStatus(HttpStatus.CREATED)
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

	@ApiOperation(value = "Login with your credentials to take access token")
	@ApiResponses(value = {
        @ApiResponse(code = 201, message = "Access token generated", response = LoginTokenResponse.class),
        @ApiResponse(code = 422, message = "Some fields has incorrect values", response = WebUser.class),
        @ApiResponse(code = 404, message = "User with specified credentials not found", response = LoginResponse.class),
        @ApiResponse(code = 500, message = "Unknown internal server error"),
    })
	@ResponseStatus(HttpStatus.CREATED)
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

	@ApiOperation(value = "Kill access token")
	@ApiResponses(value = {
        @ApiResponse(code = 200, message = "Access token removed from server storage"),
        @ApiResponse(code = 403, message = "You need Authorization header", response = MessageResponse.class),
        @ApiResponse(code = 500, message = "Unknown internal server error"),
    })
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
