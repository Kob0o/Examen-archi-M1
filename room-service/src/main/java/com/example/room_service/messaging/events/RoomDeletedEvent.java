package com.example.room_service.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomDeletedEvent(Long roomId) {
}
