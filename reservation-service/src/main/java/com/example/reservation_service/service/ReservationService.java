package com.example.reservation_service.service;

import com.example.reservation_service.client.MemberServiceClient;
import com.example.reservation_service.client.RoomServiceClient;
import com.example.reservation_service.client.dto.MemberApiDto;
import com.example.reservation_service.client.dto.RoomApiDto;
import com.example.reservation_service.model.Reservation;
import com.example.reservation_service.model.ReservationStatus;
import com.example.reservation_service.repository.ReservationRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

	private final ReservationRepository reservationRepository;
	private final RoomServiceClient roomServiceClient;
	private final MemberServiceClient memberServiceClient;

	public ReservationService(
			ReservationRepository reservationRepository,
			RoomServiceClient roomServiceClient,
			MemberServiceClient memberServiceClient) {
		this.reservationRepository = reservationRepository;
		this.roomServiceClient = roomServiceClient;
		this.memberServiceClient = memberServiceClient;
	}

	public List<Reservation> findAll() {
		return reservationRepository.findAll();
	}

	public Reservation getById(Long id) {
		return reservationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Réservation introuvable: " + id));
	}

	public boolean hasConflictForRoom(Long roomId, LocalDateTime start, LocalDateTime end) {
		validateSlot(start, end);
		return reservationRepository.existsOverlapForRoom(roomId, start, end, ReservationStatus.CONFIRMED);
	}

	@Transactional
	public Reservation create(Long roomId, Long memberId, LocalDateTime start, LocalDateTime end) {
		validateSlot(start, end);

		RoomApiDto room = fetchRoom(roomId);
		if (!Boolean.TRUE.equals(room.available())) {
			throw new BadRequestException("La salle n'est pas marquée comme disponible.");
		}
		if (!roomServiceClient.isSlotAvailable(roomId, start, end)) {
			throw new BadRequestException("La salle n'est pas libre sur ce créneau (chevauchement ou indisponibilité).");
		}

		MemberApiDto member = fetchMember(memberId);
		if (member.suspended()) {
			throw new BadRequestException("Le membre est suspendu : libérez une réservation avant de réserver à nouveau.");
		}
		Integer max = member.maxConcurrentBookings();
		if (max == null || max <= 0) {
			throw new BadRequestException("Quota du membre invalide.");
		}
		long active = reservationRepository.countByMemberIdAndStatus(memberId, ReservationStatus.CONFIRMED);
		if (active >= max) {
			throw new BadRequestException("Quota de réservations actives atteint pour ce membre.");
		}
		if (reservationRepository.existsOverlapForRoom(roomId, start, end, ReservationStatus.CONFIRMED)) {
			throw new BadRequestException("Une réservation CONFIRMED existe déjà sur ce créneau pour cette salle.");
		}

		Reservation r = new Reservation();
		r.setRoomId(roomId);
		r.setMemberId(memberId);
		r.setStartDateTime(start);
		r.setEndDateTime(end);
		r.setStatus(ReservationStatus.CONFIRMED);
		r = reservationRepository.save(r);

		syncMemberSuspension(memberId);
		return r;
	}

	@Transactional
	public Reservation cancel(Long id) {
		Reservation r = getById(id);
		if (r.getStatus() != ReservationStatus.CONFIRMED) {
			throw new BadRequestException("Seules les réservations CONFIRMED peuvent être annulées.");
		}
		r.setStatus(ReservationStatus.CANCELLED);
		reservationRepository.save(r);
		syncMemberSuspension(r.getMemberId());
		return r;
	}

	@Transactional
	public Reservation complete(Long id) {
		Reservation r = getById(id);
		if (r.getStatus() != ReservationStatus.CONFIRMED) {
			throw new BadRequestException("Seules les réservations CONFIRMED peuvent être complétées.");
		}
		r.setStatus(ReservationStatus.COMPLETED);
		reservationRepository.save(r);
		syncMemberSuspension(r.getMemberId());
		return r;
	}

	private void syncMemberSuspension(Long memberId) {
		MemberApiDto m = fetchMember(memberId);
		int max = m.maxConcurrentBookings() != null ? m.maxConcurrentBookings() : 0;
		long active = reservationRepository.countByMemberIdAndStatus(memberId, ReservationStatus.CONFIRMED);
		boolean shouldSuspend = max > 0 && active >= max;
		if (m.suspended() != shouldSuspend) {
			memberServiceClient.updateSuspended(memberId, new MemberServiceClient.SuspendedPatch(shouldSuspend));
		}
	}

	private RoomApiDto fetchRoom(Long roomId) {
		try {
			return roomServiceClient.getRoom(roomId);
		} catch (FeignException e) {
			if (e.status() == 404) {
				throw new NotFoundException("Salle introuvable: " + roomId);
			}
			throw new BadRequestException("Erreur d'appel au room-service: " + e.getMessage());
		}
	}

	private MemberApiDto fetchMember(Long memberId) {
		try {
			return memberServiceClient.getMember(memberId);
		} catch (FeignException e) {
			if (e.status() == 404) {
				throw new NotFoundException("Membre introuvable: " + memberId);
			}
			throw new BadRequestException("Erreur d'appel au member-service: " + e.getMessage());
		}
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
