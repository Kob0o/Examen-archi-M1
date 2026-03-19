package com.example.member_service.messaging;

import com.example.member_service.messaging.events.MemberSuspensionEvent;
import com.example.member_service.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Applique la mise à jour {@code suspended} demandée par le reservation-service via Kafka (étape 3).
 */
@Component
public class MemberSuspensionKafkaListener {

	private static final Logger log = LoggerFactory.getLogger(MemberSuspensionKafkaListener.class);

	private final ObjectMapper objectMapper;
	private final MemberService memberService;

	public MemberSuspensionKafkaListener(ObjectMapper objectMapper, MemberService memberService) {
		this.objectMapper = objectMapper;
		this.memberService = memberService;
	}

	@KafkaListener(topics = "${app.kafka.topics.member-suspension}", groupId = "${spring.application.name}-suspension")
	public void onMemberSuspension(String message) {
		try {
			MemberSuspensionEvent evt = objectMapper.readValue(message, MemberSuspensionEvent.class);
			memberService.setSuspended(evt.memberId(), evt.suspended());
		} catch (MemberService.NotFoundException e) {
			log.warn("Suspension Kafka ignorée (membre absent) : {}", e.getMessage());
		} catch (Exception e) {
			log.error("Traitement member-suspension impossible: {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
