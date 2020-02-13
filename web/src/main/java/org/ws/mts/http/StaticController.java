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

@RestController
@RequestMapping("/photos/static")
public class StaticController {
	private static final String TAG = StaticController.class.getSimpleName();
	
	@Autowired
	private PhotoService photos;
	@Autowired
	private Log log;
	
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
