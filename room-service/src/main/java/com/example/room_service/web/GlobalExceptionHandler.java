package com.example.room_service.web;

import com.example.room_service.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RoomService.NotFoundException.class)
	public ResponseEntity<Map<String, String>> notFound(RoomService.NotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(RoomService.BadRequestException.class)
	public ResponseEntity<Map<String, String>> badRequest(RoomService.BadRequestException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
	}
}
