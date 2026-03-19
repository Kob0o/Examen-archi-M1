package com.example.room_service.messaging;

import com.example.room_service.messaging.events.RoomDeletedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RoomDeletedKafkaProducer {

	private static final Logger log = LoggerFactory.getLogger(RoomDeletedKafkaProducer.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final String topic;

	public RoomDeletedKafkaProducer(
			KafkaTemplate<String, String> kafkaTemplate,
			ObjectMapper objectMapper,
			@Value("${app.kafka.topics.room-deleted}") String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.topic = topic;
	}

	public void publish(Long roomId) {
		try {
			String json = objectMapper.writeValueAsString(new RoomDeletedEvent(roomId));
			kafkaTemplate.send(topic, String.valueOf(roomId), json)
					.whenComplete((r, ex) -> {
						if (ex != null) {
							log.error("Échec publication room-deleted {}: {}", roomId, ex.getMessage());
						}
					});
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Sérialisation RoomDeletedEvent impossible", e);
		}
	}
}
