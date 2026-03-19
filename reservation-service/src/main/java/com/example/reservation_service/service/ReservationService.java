package com.example.reservation_service.service;

import com.example.reservation_service.client.MemberServiceClient;
import com.example.reservation_service.client.dto.MemberApiDto;
import com.example.reservation_service.builder.ConfirmedReservationBuilderFactory;
import com.example.reservation_service.messaging.MemberSuspensionKafkaProducer;
import com.example.reservation_service.messaging.events.MemberSuspensionEvent;
import com.example.reservation_service.model.Reservation;
import com.example.reservation_service.model.ReservationStatus;
import com.example.reservation_service.repository.ReservationRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReservationService {

	private final ReservationRepository reservationRepository;
	private final MemberServiceClient memberServiceClient;
	private final MemberSuspensionKafkaProducer memberSuspensionKafkaProducer;
	private final ConfirmedReservationBuilderFactory confirmedReservationBuilderFactory;

	public ReservationService(
			ReservationRepository reservationRepository,
			MemberServiceClient memberServiceClient,
			MemberSuspensionKafkaProducer memberSuspensionKafkaProducer,
			ConfirmedReservationBuilderFactory confirmedReservationBuilderFactory) {
		this.reservationRepository = reservationRepository;
		this.memberServiceClient = memberServiceClient;
		this.memberSuspensionKafkaProducer = memberSuspensionKafkaProducer;
		this.confirmedReservationBuilderFactory = confirmedReservationBuilderFactory;
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
	public void cancelAllConfirmedForRoom(Long roomId) {
		List<Reservation> list = reservationRepository.findByRoomIdAndStatus(roomId, ReservationStatus.CONFIRMED);
		Set<Long> affectedMembers = new HashSet<>();
		for (Reservation r : list) {
			r.setStatus(ReservationStatus.CANCELLED);
			reservationRepository.save(r);
			affectedMembers.add(r.getMemberId());
		}
		for (Long memberId : affectedMembers) {
			publishSuspensionRecalc(memberId);
		}
	}

	@Transactional
	public void deleteAllForMember(Long memberId) {
		reservationRepository.deleteByMemberId(memberId);
	}

	@Transactional
	public Reservation create(Long roomId, Long memberId, LocalDateTime start, LocalDateTime end) {
		Reservation r = confirmedReservationBuilderFactory
				.newBuilder()
				.roomId(roomId)
				.memberId(memberId)
				.startDateTime(start)
				.endDateTime(end)
				.build();
		r = reservationRepository.save(r);
		publishSuspensionRecalc(memberId);
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
		publishSuspensionRecalc(r.getMemberId());
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
		publishSuspensionRecalc(r.getMemberId());
		return r;
	}

	private void publishSuspensionRecalc(Long memberId) {
		MemberApiDto m = fetchMember(memberId);
		int max = m.maxConcurrentBookings() != null ? m.maxConcurrentBookings() : 0;
		long active = reservationRepository.countByMemberIdAndStatus(memberId, ReservationStatus.CONFIRMED);
		boolean shouldSuspend = max > 0 && active >= max;
		if (m.suspended() != shouldSuspend) {
			memberSuspensionKafkaProducer.publish(new MemberSuspensionEvent(memberId, shouldSuspend));
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
