package com.example.member_service.web;

import com.example.member_service.model.Member;
import com.example.member_service.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Membres", description = "Gestion des membres et abonnements")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping
	public List<Member> list() {
		return memberService.findAll();
	}

	@GetMapping("/{id}")
	public Member get(@PathVariable Long id) {
		return memberService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Member create(@RequestBody Member member) {
		return memberService.create(member);
	}

	@PutMapping("/{id}")
	public Member update(@PathVariable Long id, @RequestBody Member member) {
		return memberService.update(id, member);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		memberService.delete(id);
	}

	/**
	 * Mise à jour manuelle (tests / admin). En production métier, la suspension est pilotée par Kafka.
	 */
	@PatchMapping("/{id}/suspended")
	public Member patchSuspended(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
		Boolean suspended = body.get("suspended");
		if (suspended == null) {
			throw new MemberService.BadRequestException("Champ 'suspended' (boolean) requis.");
		}
		return memberService.setSuspended(id, suspended);
	}
}
