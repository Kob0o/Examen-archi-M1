package com.example.member_service.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberSuspensionEvent(Long memberId, boolean suspended) {
}
