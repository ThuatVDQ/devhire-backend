package com.hcmute.devhire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevhireApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevhireApplication.class, args);
	}

}
