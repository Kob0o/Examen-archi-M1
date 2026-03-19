package com.example.reservation_service.messaging;

import com.example.reservation_service.messaging.events.MemberDeletedEvent;
import com.example.reservation_service.messaging.events.RoomDeletedEvent;
import com.example.reservation_service.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationKafkaListeners {

	private static final Logger log = LoggerFactory.getLogger(ReservationKafkaListeners.class);

	private final ObjectMapper objectMapper;
	private final ReservationService reservationService;

	public ReservationKafkaListeners(ObjectMapper objectMapper, ReservationService reservationService) {
		this.objectMapper = objectMapper;
		this.reservationService = reservationService;
	}

	@KafkaListener(topics = "${app.kafka.topics.room-deleted}", groupId = "${spring.application.name}-room-deleted")
	public void onRoomDeleted(String message) {
		try {
			RoomDeletedEvent evt = objectMapper.readValue(message, RoomDeletedEvent.class);
			log.info("Événement room-deleted reçu: roomId={}", evt.roomId());
			reservationService.cancelAllConfirmedForRoom(evt.roomId());
		} catch (Exception e) {
			log.error("Traitement room-deleted impossible: {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@KafkaListener(topics = "${app.kafka.topics.member-deleted}", groupId = "${spring.application.name}-member-deleted")
	public void onMemberDeleted(String message) {
		try {
			MemberDeletedEvent evt = objectMapper.readValue(message, MemberDeletedEvent.class);
			log.info("Événement member-deleted reçu: memberId={}", evt.memberId());
			reservationService.deleteAllForMember(evt.memberId());
		} catch (Exception e) {
			log.error("Traitement member-deleted impossible: {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
