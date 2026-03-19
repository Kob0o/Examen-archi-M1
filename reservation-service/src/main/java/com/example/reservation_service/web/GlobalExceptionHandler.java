package com.example.reservation_service.web;

import com.example.reservation_service.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ReservationService.NotFoundException.class)
	public ResponseEntity<Map<String, String>> notFound(ReservationService.NotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(ReservationService.BadRequestException.class)
	public ResponseEntity<Map<String, String>> badRequest(ReservationService.BadRequestException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
	}
}
