package com.example.reservation_service.web;

import com.example.reservation_service.model.Reservation;
import com.example.reservation_service.service.ReservationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Réservations", description = "Création, annulation et complétion des réservations")
public class ReservationController {

	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@GetMapping
	public List<Reservation> list() {
		return reservationService.findAll();
	}

	@GetMapping("/{id}")
	public Reservation get(@PathVariable Long id) {
		return reservationService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Reservation create(@RequestBody CreateReservationRequest body) {
		return reservationService.create(body.roomId(), body.memberId(), body.startDateTime(), body.endDateTime());
	}

	@PostMapping("/{id}/cancel")
	public Reservation cancel(@PathVariable Long id) {
		return reservationService.cancel(id);
	}

	@PostMapping("/{id}/complete")
	public Reservation complete(@PathVariable Long id) {
		return reservationService.complete(id);
	}

	public record CreateReservationRequest(
			Long roomId,
			Long memberId,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime
	) {
	}
}
