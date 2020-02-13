package org.ws.mts.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PingController {

	@GetMapping("/ping")
    public ResponseEntity<String> ping() {
		return new ResponseEntity<>("I'm MTS Rest API back end", HttpStatus.OK);
	}
	
    
}
