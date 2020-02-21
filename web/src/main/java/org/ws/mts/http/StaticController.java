package org.ws.mts.http;

import org.cuba.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ws.mts.service.PhotoService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/photos/static")
@Api(value="Static photos data", description="Static service: returns photo files by id")
public class StaticController {
	private static final String TAG = StaticController.class.getSimpleName();
	
	@Autowired
	private PhotoService photos;
	@Autowired
	private Log log;

	@ApiOperation(value = "Get photo file by id")
	@ApiResponses(value = {
        @ApiResponse(code = 200, message = "File returned", response = byte[].class),
        @ApiResponse(code = 404, message = "Photo not found"),
        @ApiResponse(code = 500, message = "Unknown internal server error"),
    })
	@GetMapping(path = {"{id}.png", "{id}.jpg"}, produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
	public ResponseEntity<byte[]> get(@PathVariable("id") String id) {
		try {
			byte[] bytes = photos.content(id);
			if(bytes == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(bytes, HttpStatus.OK);
		} catch(Throwable t) {
			log.e(TAG, "upload", t);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
