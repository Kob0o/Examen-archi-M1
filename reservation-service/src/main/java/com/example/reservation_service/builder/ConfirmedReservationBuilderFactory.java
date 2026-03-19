package com.example.reservation_service.builder;

import com.example.reservation_service.client.MemberServiceClient;
import com.example.reservation_service.client.RoomServiceClient;
import com.example.reservation_service.client.dto.MemberApiDto;
import com.example.reservation_service.client.dto.RoomApiDto;
import com.example.reservation_service.model.Reservation;
import com.example.reservation_service.model.ReservationStatus;
import com.example.reservation_service.repository.ReservationRepository;
import com.example.reservation_service.service.ReservationService;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ConfirmedReservationBuilderFactory {

	private final RoomServiceClient roomServiceClient;
	private final MemberServiceClient memberServiceClient;
	private final ReservationRepository reservationRepository;

	public ConfirmedReservationBuilderFactory(
			RoomServiceClient roomServiceClient,
			MemberServiceClient memberServiceClient,
			ReservationRepository reservationRepository) {
		this.roomServiceClient = roomServiceClient;
		this.memberServiceClient = memberServiceClient;
		this.reservationRepository = reservationRepository;
	}

	public Builder newBuilder() {
		return new Builder(roomServiceClient, memberServiceClient, reservationRepository);
	}

	public static final class Builder {

		private final RoomServiceClient roomServiceClient;
		private final MemberServiceClient memberServiceClient;
		private final ReservationRepository reservationRepository;

		private Long roomId;
		private Long memberId;
		private LocalDateTime startDateTime;
		private LocalDateTime endDateTime;

		private Builder(
				RoomServiceClient roomServiceClient,
				MemberServiceClient memberServiceClient,
				ReservationRepository reservationRepository) {
			this.roomServiceClient = roomServiceClient;
			this.memberServiceClient = memberServiceClient;
			this.reservationRepository = reservationRepository;
		}

		public Builder roomId(Long roomId) {
			this.roomId = roomId;
			return this;
		}

		public Builder memberId(Long memberId) {
			this.memberId = memberId;
			return this;
		}

		public Builder startDateTime(LocalDateTime startDateTime) {
			this.startDateTime = startDateTime;
			return this;
		}

		public Builder endDateTime(LocalDateTime endDateTime) {
			this.endDateTime = endDateTime;
			return this;
		}

		public Reservation build() {
			if (roomId == null || memberId == null) {
				throw new ReservationService.BadRequestException("roomId et memberId sont obligatoires.");
			}
			validateSlot(startDateTime, endDateTime);

			RoomApiDto room = fetchRoom(roomId);
			if (!Boolean.TRUE.equals(room.available())) {
				throw new ReservationService.BadRequestException("La salle n'est pas marquée comme disponible.");
			}
			if (!roomServiceClient.isSlotAvailable(roomId, startDateTime, endDateTime)) {
				throw new ReservationService.BadRequestException(
						"La salle n'est pas libre sur ce créneau (chevauchement ou indisponibilité).");
			}

			MemberApiDto member = fetchMember(memberId);
			if (member.suspended()) {
				throw new ReservationService.BadRequestException(
						"Le membre est suspendu : libérez une réservation avant de réserver à nouveau.");
			}
			Integer max = member.maxConcurrentBookings();
			if (max == null || max <= 0) {
				throw new ReservationService.BadRequestException("Quota du membre invalide.");
			}
			long active = reservationRepository.countByMemberIdAndStatus(memberId, ReservationStatus.CONFIRMED);
			if (active >= max) {
				throw new ReservationService.BadRequestException("Quota de réservations actives atteint pour ce membre.");
			}
			if (reservationRepository.existsOverlapForRoom(roomId, startDateTime, endDateTime, ReservationStatus.CONFIRMED)) {
				throw new ReservationService.BadRequestException(
						"Une réservation CONFIRMED existe déjà sur ce créneau pour cette salle.");
			}

			Reservation r = new Reservation();
			r.setRoomId(roomId);
			r.setMemberId(memberId);
			r.setStartDateTime(startDateTime);
			r.setEndDateTime(endDateTime);
			r.setStatus(ReservationStatus.CONFIRMED);
			return r;
		}

		private static void validateSlot(LocalDateTime start, LocalDateTime end) {
			if (start == null || end == null || !end.isAfter(start)) {
				throw new ReservationService.BadRequestException(
						"Créneau invalide : dates manquantes ou fin non postérieure au début.");
			}
		}

		private RoomApiDto fetchRoom(Long roomId) {
			try {
				return roomServiceClient.getRoom(roomId);
			} catch (FeignException e) {
				if (e.status() == 404) {
					throw new ReservationService.NotFoundException("Salle introuvable: " + roomId);
				}
				throw new ReservationService.BadRequestException("Erreur d'appel au room-service: " + e.getMessage());
			}
		}

		private MemberApiDto fetchMember(Long memberId) {
			try {
				return memberServiceClient.getMember(memberId);
			} catch (FeignException e) {
				if (e.status() == 404) {
					throw new ReservationService.NotFoundException("Membre introuvable: " + memberId);
				}
				throw new ReservationService.BadRequestException("Erreur d'appel au member-service: " + e.getMessage());
			}
		}
	}
}
