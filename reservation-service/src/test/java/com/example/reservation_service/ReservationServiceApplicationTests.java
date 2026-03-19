package com.example.reservation_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(
		partitions = 1,
		topics = {"coworking.room.deleted", "coworking.member.deleted", "coworking.member.suspension"})
class ReservationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
