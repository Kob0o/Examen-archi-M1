package com.example.reservation_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberApiDto(Long id, boolean suspended, Integer maxConcurrentBookings) {
}
