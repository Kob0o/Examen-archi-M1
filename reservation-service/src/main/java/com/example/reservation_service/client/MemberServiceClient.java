package com.example.reservation_service.client;

import com.example.reservation_service.client.dto.MemberApiDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", path = "/api/members")
public interface MemberServiceClient {

	@GetMapping("/{id}")
	MemberApiDto getMember(@PathVariable("id") Long id);
}
