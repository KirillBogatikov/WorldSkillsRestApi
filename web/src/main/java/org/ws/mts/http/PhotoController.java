package org.ws.mts.http;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.codec.binary.Base64;
import org.cuba.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.ws.mts.models.Config;
import org.ws.mts.models.Config.Server;
import org.ws.mts.models.Photo;
import org.ws.mts.models.Response;
import org.ws.mts.models.WebPhoto;
import org.ws.mts.models.WebUpdatePhoto;
import org.ws.mts.service.AuthService;
import org.ws.mts.service.PhotoService;
import org.ws.mts.utils.Mapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/photos/api/photo")
@Api(value="Photos", description="Photo service: get by id, upload, update, list and delete photos. Only authorized users can use this API")
public class PhotoController {
	private static final String TAG = PhotoController.class.getSimpleName();
	
	@Autowired
	private AuthService authService;
	@Autowired
	private PhotoService photoService;
	@Autowired
	private Log log;
	@Autowired
	private Config config;
	
	public class MessageResponse {
		public String message;
	}
	
	@ApiOperation(value = "Update early uploaded photo")
	@ApiResponses(value = {
        @ApiResponse(code = 200, message = "Information updated", response = WebPhoto.class),
        @ApiResponse(code = 403, message = "You need Authorization header", response = MessageResponse.class),
        @ApiResponse(code = 403, message = "You haven't access to specified photo"),
        @ApiResponse(code = 422, message = "Some fields have incorrect values", response = WebUpdatePhoto.class),
        @ApiResponse(code = 500, message = "Unknown internal server error"),
    })
	@PostMapping("/{id}")
	public ResponseEntity<? extends Object> update(@PathVariable("id") String id, @RequestBody WebUpdatePhoto photo, @RequestHeader(name = "Authorization", required = false) String token) {
		try {
			if(!authService.checkToken(token)) {
				MessageResponse resp = new MessageResponse();
				resp.message = "You need authorization";
				return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
			}
			
			if(!photoService.checkAccess(authService.userId(token), id)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			
			if(!photo.getMethod().equals("patch")) {
				return new ResponseEntity<>(Mapper.from(null, photo.getMethod()), HttpStatus.UNPROCESSABLE_ENTITY);
			}
			
			String base64 = photo.getImageBase64().replace("data:image/jpeg;base64,", "").replace("data:image/png;base64,", "");
			Response response = photoService.update(id, base64 != null ? Base64.decodeBase64(base64) : null, photo.getName());
			Object content = response.getContent();
			
			switch(response.getStatus()) {
				case OK: 
					Photo p = (Photo)content;
					p.setLink(fixedLink(p.getLink()));
					return new ResponseEntity<>(content, HttpStatus.OK);
				case INVALID:
					return new ResponseEntity<>(content, HttpStatus.UNPROCESSABLE_ENTITY);
				default:
					break;
			}
		} catch(Throwable t) {
			log.e(TAG, "update", t);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ApiOperation(value = "Upload new photo")
	@ApiResponses(value = {
        @ApiResponse(code = 201, message = "Photo uploaded, static link generated", response = WebPhoto.class),
        @ApiResponse(code = 403, message = "You need Authorization header", response = MessageResponse.class),
        @ApiResponse(code = 403, message = "You haven't access to specified photo"),
        @ApiResponse(code = 422, message = "Some fields have incorrect values", response = WebUpdatePhoto.class),
        @ApiResponse(code = 500, message = "Unknown internal server error"),
    })
	@PostMapping
	public ResponseEntity<? extends Object> upload(@RequestPart("photo") MultipartFile imageFile, 
												   @RequestHeader(name = "Authorization", required = false) String token, HttpServletRequest request) {
		try {
			if(!authService.checkToken(token)) {
				MessageResponse resp = new MessageResponse();
				resp.message = "You need authorization";
				return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
			}
			
			Response response = photoService.upload(imageFile.getBytes(), authService.userId(token));

			Photo photo = response.getContent();
			
			switch(response.getStatus()) {
				case OK:
					photo.setLink(fixedLink(photo.getLink()));
					return new ResponseEntity<>(Mapper.from(photo), HttpStatus.CREATED);
				case INVALID:
					return new ResponseEntity<>(Mapper.from(photo), HttpStatus.UNPROCESSABLE_ENTITY);
				default:
					break;
			}
		} catch(Throwable t) {
			log.e(TAG, "upload", t);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	private String fixedLink(String link) {
		String url;
		
		Server server = config.getServer();
		if(server.useSsl()) {
			url = "https://";
		} else {
			url = "http://";
		}
		
		url += server.getHost() + "/photos/static/" + link;
		return url;
	}

	@ApiOperation(value = "List all available photos: own and shared")
	@ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of photos returned", response = WebPhoto.class),
        @ApiResponse(code = 403, message = "You need Authorization header", response = MessageResponse.class),
        @ApiResponse(code = 500, message = "Unknown internal server error"),
    })
	@GetMapping
	public ResponseEntity<? extends Object> list(@RequestHeader(name = "Authorization", required = false) String token) {
		try {
			if(!authService.checkToken(token)) {
				MessageResponse resp = new MessageResponse();
				resp.message = "You need authorization";
				return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
			}
						
			List<Photo> photos = photoService.list(authService.userId(token));
			
			for(Photo photo : photos) {
				photo.setLink(fixedLink(photo.getLink()));
			}
			
			return new ResponseEntity<>(Mapper.photosFrom(photos), HttpStatus.OK);
		} catch(Throwable t) {
			log.e(TAG, "update", t);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ApiOperation(value = "Get information by photo id")
	@ApiResponses(value = {
        @ApiResponse(code = 200, message = "Photo found", response = Photo.class),
        @ApiResponse(code = 403, message = "You need Authorization header", response = MessageResponse.class),
        @ApiResponse(code = 500, message = "Unknown internal server error"),
    })
	@GetMapping("/{id}")
	public ResponseEntity<? extends Object> getById(@PathVariable("id") String id, @RequestHeader(name = "Authorization", required = false) String token) {
		try {
			if(!authService.checkToken(token)) {
				MessageResponse resp = new MessageResponse();
				resp.message = "You need authorization";
				return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
			}
			
			Photo photo = photoService.get(id);
			photo.setLink(fixedLink(photo.getLink()));
			return new ResponseEntity<>(Mapper.from(photo), HttpStatus.OK);
		} catch(Throwable t) {
			log.e(TAG, "update", t);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ApiOperation(value = "Delete photo")
	@ApiResponses(value = {
        @ApiResponse(code = 204, message = "Photo deleted"),
        @ApiResponse(code = 403, message = "You need Authorization header", response = MessageResponse.class),
        @ApiResponse(code = 403, message = "You haven't access to specified photo"),
        @ApiResponse(code = 500, message = "Unknown internal server error"),
    })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}")
	public ResponseEntity<? extends Object> delete(@PathVariable("id") String id, @RequestHeader(name = "Authorization", required = false) String token) {
		try {
			if(!authService.checkToken(token)) {
				MessageResponse resp = new MessageResponse();
				resp.message = "You need authorization";
				return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
			}
			
			if(!photoService.checkAccess(authService.userId(token), id)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
						
			photoService.delete(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch(Throwable t) {
			log.e(TAG, "update", t);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
