package com.example.member_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class MemberServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemberServiceApplication.class, args);
	}

}
