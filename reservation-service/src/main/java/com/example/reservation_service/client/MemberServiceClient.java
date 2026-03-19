package com.example.reservation_service.client;

import com.example.reservation_service.client.dto.MemberApiDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member-service", path = "/api/members")
public interface MemberServiceClient {

	@GetMapping("/{id}")
	MemberApiDto getMember(@PathVariable("id") Long id);

	@PatchMapping(value = "/{id}/suspended", consumes = "application/json")
	void updateSuspended(@PathVariable("id") Long id, @RequestBody SuspendedPatch body);

	record SuspendedPatch(boolean suspended) {
	}
}
