package com.example.reservation_service.client;

import com.example.reservation_service.client.dto.RoomApiDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "room-service", path = "/api/rooms")
public interface RoomServiceClient {

	@GetMapping("/{id}")
	RoomApiDto getRoom(@PathVariable("id") Long id);

	@GetMapping("/{id}/slot-available")
	boolean isSlotAvailable(
			@PathVariable("id") Long id,
			@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
	);
}
