package com.example.reservation_service.web;

import com.example.reservation_service.service.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/reservations/internal")
public class ReservationInternalController {

	private final ReservationService reservationService;

	public ReservationInternalController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@GetMapping("/rooms/{roomId}/has-conflict")
	public boolean hasConflict(
			@PathVariable Long roomId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
		return reservationService.hasConflictForRoom(roomId, start, end);
	}
}
