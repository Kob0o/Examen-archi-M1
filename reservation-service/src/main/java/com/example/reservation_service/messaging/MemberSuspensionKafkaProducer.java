package com.example.reservation_service.messaging;

import com.example.reservation_service.messaging.events.MemberSuspensionEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MemberSuspensionKafkaProducer {

	private static final Logger log = LoggerFactory.getLogger(MemberSuspensionKafkaProducer.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final String topic;

	public MemberSuspensionKafkaProducer(
			KafkaTemplate<String, String> kafkaTemplate,
			ObjectMapper objectMapper,
			@Value("${app.kafka.topics.member-suspension}") String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.topic = topic;
	}

	public void publish(MemberSuspensionEvent event) {
		try {
			String json = objectMapper.writeValueAsString(event);
			kafkaTemplate.send(topic, String.valueOf(event.memberId()), json)
					.whenComplete((r, ex) -> {
						if (ex != null) {
							log.error("Échec publication suspension membre {}: {}", event.memberId(), ex.getMessage());
						}
					});
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Sérialisation MemberSuspensionEvent impossible", e);
		}
	}
}
