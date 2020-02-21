package org.ws.mts.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/ping")
@Api(value="Ping", description="Simple ping handler for checking service availability")
public class PingController {

	@GetMapping
    public ResponseEntity<String> ping() {
		return new ResponseEntity<>("I'm MTS Rest API back end", HttpStatus.OK);
	}
	
    
}
