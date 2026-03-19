package com.example.room_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "reservation-service", path = "/api/reservations")
public interface ReservationConflictClient {

	@GetMapping("/internal/rooms/{roomId}/has-conflict")
	boolean hasConflict(
			@PathVariable("roomId") Long roomId,
			@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
	);
}
