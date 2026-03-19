package com.example.reservation_service.repository;

import com.example.reservation_service.model.Reservation;
import com.example.reservation_service.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	long countByMemberIdAndStatus(Long memberId, ReservationStatus status);

	@Query("""
			SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
			FROM Reservation r
			WHERE r.roomId = :roomId
			  AND r.status = :status
			  AND r.startDateTime < :end
			  AND r.endDateTime > :start
			""")
	boolean existsOverlapForRoom(
			@Param("roomId") Long roomId,
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end,
			@Param("status") ReservationStatus status
	);
}
