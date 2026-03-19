package com.example.member_service.service;

import com.example.member_service.messaging.MemberDeletedKafkaProducer;
import com.example.member_service.model.Member;
import com.example.member_service.model.SubscriptionType;
import com.example.member_service.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

	private final MemberRepository memberRepository;
	private final MemberDeletedKafkaProducer memberDeletedKafkaProducer;

	public MemberService(MemberRepository memberRepository, MemberDeletedKafkaProducer memberDeletedKafkaProducer) {
		this.memberRepository = memberRepository;
		this.memberDeletedKafkaProducer = memberDeletedKafkaProducer;
	}

	public List<Member> findAll() {
		return memberRepository.findAll();
	}

	public Member getById(Long id) {
		return memberRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Membre introuvable: " + id));
	}

	@Transactional
	public Member create(Member member) {
		applyQuotaFromSubscription(member);
		return memberRepository.save(member);
	}

	@Transactional
	public Member update(Long id, Member input) {
		Member existing = getById(id);
		existing.setFullName(input.getFullName());
		existing.setEmail(input.getEmail());
		if (input.getSubscriptionType() != null) {
			existing.setSubscriptionType(input.getSubscriptionType());
		}
		if (input.getMaxConcurrentBookings() != null) {
			existing.setMaxConcurrentBookings(input.getMaxConcurrentBookings());
		} else if (input.getSubscriptionType() != null) {
			existing.setMaxConcurrentBookings(quotaFor(input.getSubscriptionType()));
		}
		return memberRepository.save(existing);
	}

	@Transactional
	public void delete(Long id) {
		if (!memberRepository.existsById(id)) {
			throw new NotFoundException("Membre introuvable: " + id);
		}
		memberRepository.deleteById(id);
		memberDeletedKafkaProducer.publish(id);
	}

	@Transactional
	public Member setSuspended(Long id, boolean suspended) {
		Member m = getById(id);
		m.setSuspended(suspended);
		return memberRepository.save(m);
	}

	/**
	 * Quotas imposés par l'énoncé : BASIC 2, PRO 5, ENTERPRISE 10.
	 */
	private static void applyQuotaFromSubscription(Member member) {
		if (member.getSubscriptionType() == null) {
			throw new BadRequestException("subscriptionType est obligatoire à la création.");
		}
		if (member.getMaxConcurrentBookings() == null) {
			member.setMaxConcurrentBookings(quotaFor(member.getSubscriptionType()));
		}
	}

	private static int quotaFor(SubscriptionType type) {
		return switch (type) {
			case BASIC -> 2;
			case PRO -> 5;
			case ENTERPRISE -> 10;
		};
	}

	public static class NotFoundException extends RuntimeException {
		public NotFoundException(String message) {
			super(message);
		}
	}

	public static class BadRequestException extends RuntimeException {
		public BadRequestException(String message) {
			super(message);
		}
	}
}
