package com.example.reservation_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomApiDto(Long id, Boolean available) {
}
