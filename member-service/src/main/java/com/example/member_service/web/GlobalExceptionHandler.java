package com.example.member_service.web;

import com.example.member_service.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MemberService.NotFoundException.class)
	public ResponseEntity<Map<String, String>> notFound(MemberService.NotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(MemberService.BadRequestException.class)
	public ResponseEntity<Map<String, String>> badRequest(MemberService.BadRequestException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
	}
}
