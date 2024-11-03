package com.inghubs.brokerage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BrokerageApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrokerageApplication.class, args);
	}

}
