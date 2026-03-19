package com.example.room_service.web;

import com.example.room_service.model.Room;
import com.example.room_service.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

	private final RoomService roomService;

	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}

	@GetMapping
	public List<Room> list() {
		return roomService.findAll();
	}

	@GetMapping("/{id}")
	public Room get(@PathVariable Long id) {
		return roomService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Room create(@RequestBody Room room) {
		return roomService.create(room);
	}

	@PutMapping("/{id}")
	public Room update(@PathVariable Long id, @RequestBody Room room) {
		return roomService.update(id, room);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		roomService.delete(id);
	}

	@GetMapping("/{id}/slot-available")
	public boolean slotAvailable(
			@PathVariable Long id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
		return roomService.isSlotAvailable(id, start, end);
	}
}
