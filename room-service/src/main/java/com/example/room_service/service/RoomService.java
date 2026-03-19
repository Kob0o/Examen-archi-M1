package com.example.room_service.service;

import com.example.room_service.client.ReservationConflictClient;
import com.example.room_service.messaging.RoomDeletedKafkaProducer;
import com.example.room_service.model.Room;
import com.example.room_service.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomService {

	private final RoomRepository roomRepository;
	private final ReservationConflictClient reservationConflictClient;
	private final RoomDeletedKafkaProducer roomDeletedKafkaProducer;

	public RoomService(
			RoomRepository roomRepository,
			ReservationConflictClient reservationConflictClient,
			RoomDeletedKafkaProducer roomDeletedKafkaProducer) {
		this.roomRepository = roomRepository;
		this.reservationConflictClient = reservationConflictClient;
		this.roomDeletedKafkaProducer = roomDeletedKafkaProducer;
	}

	public List<Room> findAll() {
		return roomRepository.findAll();
	}

	public Room getById(Long id) {
		return roomRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Salle introuvable: " + id));
	}

	@Transactional
	public Room create(Room room) {
		return roomRepository.save(room);
	}

	@Transactional
	public Room update(Long id, Room input) {
		Room existing = getById(id);
		existing.setName(input.getName());
		existing.setCity(input.getCity());
		existing.setCapacity(input.getCapacity());
		existing.setType(input.getType());
		existing.setHourlyRate(input.getHourlyRate());
		existing.setAvailable(input.isAvailable());
		return roomRepository.save(existing);
	}

	@Transactional
	public void delete(Long id) {
		if (!roomRepository.existsById(id)) {
			throw new NotFoundException("Salle introuvable: " + id);
		}
		roomRepository.deleteById(id);
		roomDeletedKafkaProducer.publish(id);
	}

	/**
	 * Disponibilité sur un créneau : salle marquée disponible + pas de réservation CONFIRMED qui chevauche.
	 */
	public boolean isSlotAvailable(Long roomId, LocalDateTime start, LocalDateTime end) {
		validateSlot(start, end);
		Room room = getById(roomId);
		if (!room.isAvailable()) {
			return false;
		}
		return !reservationConflictClient.hasConflict(roomId, start, end);
	}

	private static void validateSlot(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null || !end.isAfter(start)) {
			throw new BadRequestException("Créneau invalide : dates manquantes ou fin non postérieure au début.");
		}
	}

	public static class NotFoundException extends RuntimeException {
		public NotFoundException(String message) {
			super(message);
		}
	}

	public static class BadRequestException extends RuntimeException {
		public BadRequestException(String message) {
			super(message);
		}
	}
}
