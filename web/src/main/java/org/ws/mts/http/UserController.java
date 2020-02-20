package org.ws.mts.http;

import java.util.ArrayList;
import java.util.List;

import org.cuba.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ws.mts.models.SearchQuery;
import org.ws.mts.models.User;
import org.ws.mts.service.AuthService;
import org.ws.mts.service.PhotoService;
import org.ws.mts.service.UserService;
import org.ws.mts.utils.Mapper;

@RestController
@RequestMapping("/photos/api/user")
public class UserController {
	private static final String TAG = UserController.class.getSimpleName();
	
	@Autowired
	private AuthService authService;
	@Autowired
	private UserService userService;
	@Autowired
	private PhotoService photoService;
	@Autowired
	private Log log;
	
	public class MessageResponse {
		public String message;
	}
	
	@GetMapping
	public ResponseEntity<? extends Object> search(@RequestParam("search") String query, @RequestHeader(name = "Authorization", required = false) String token) {
		log.d(TAG, "Search query: " + query);
		
		try {
			if(!authService.checkToken(token)) {
				MessageResponse resp = new MessageResponse();
				resp.message = "You need authorization";
				return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
			}
						
			String[] parts = query.split(",");
			ArrayList<SearchQuery> queries = new ArrayList<>();
			
			for(String part : parts) {
				part = part.trim();
				
				if(part.isEmpty()) {
					continue;
				}
				
				String[] words = part.split(" ");
				
				if(words.length == 3) {
					queries.add(Mapper.of(words[0], words[1], words[2]));
				} else if(words.length == 2) {
					queries.add(Mapper.of(words[0], words[1], null));
					queries.add(Mapper.of(words[0], null,     words[1]));
					queries.add(Mapper.of(null,     words[0], words[1]));
					
					queries.add(Mapper.of(words[1], words[0], null));
					queries.add(Mapper.of(words[1], null,     words[0]));
					queries.add(Mapper.of(null,     words[1], words[0]));
				} else if(words.length == 1) {
					queries.add(Mapper.of(words[0], null,     null));
					queries.add(Mapper.of(null,     words[0], null));
					queries.add(Mapper.of(null,     null,     words[0]));
				}
			}
			
			List<User> users = userService.search(queries);			
			return new ResponseEntity<>(Mapper.usersFrom(users), HttpStatus.OK);
		} catch(Throwable t) {
			log.e(TAG, "search user", t);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	public static class ShareRequest {
		public List<String> photos;
	}
	
	public static class ShareResponse {
		public List<String> existing_photos;
	}
	
	@PostMapping("/{user}/share")
	public ResponseEntity<? extends Object> share(@PathVariable("user") String user, @RequestBody ShareRequest request, @RequestHeader(name = "Authorization", required = false) String token) {
		try {
			if(!authService.checkToken(token)) {
				MessageResponse resp = new MessageResponse();
				resp.message = "You need authorization";
				return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
			}
			
			if(authService.userId(token).equals(user) || !userService.exists(user)) {
				MessageResponse resp = new MessageResponse();
				resp.message = "Нельзя расшарить фото самому себе или несуществующему пользователю";
				return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
			}
			
			ShareResponse response = new ShareResponse();
			response.existing_photos = photoService.share(request.photos, user);
			
			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch(Throwable t) {
			log.e(TAG, "share", t);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
