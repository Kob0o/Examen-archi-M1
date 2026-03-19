package com.example.member_service.messaging;

import com.example.member_service.messaging.events.MemberDeletedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MemberDeletedKafkaProducer {

	private static final Logger log = LoggerFactory.getLogger(MemberDeletedKafkaProducer.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final String topic;

	public MemberDeletedKafkaProducer(
			KafkaTemplate<String, String> kafkaTemplate,
			ObjectMapper objectMapper,
			@Value("${app.kafka.topics.member-deleted}") String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.topic = topic;
	}

	public void publish(Long memberId) {
		try {
			String json = objectMapper.writeValueAsString(new MemberDeletedEvent(memberId));
			kafkaTemplate.send(topic, String.valueOf(memberId), json)
					.whenComplete((r, ex) -> {
						if (ex != null) {
							log.error("Échec publication member-deleted {}: {}", memberId, ex.getMessage());
						}
					});
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Sérialisation MemberDeletedEvent impossible", e);
		}
	}
}
