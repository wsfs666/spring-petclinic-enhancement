package org.springframework.samples.petclinic.appointment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Integer> {

	@Query(value = """
			SELECT s.*
			FROM appointment_slots s
			WHERE s.start_at > :now
			AND NOT EXISTS (
			      SELECT 1
			      FROM appointments a
			      WHERE a.slot_id = s.id
			  )
			ORDER BY s.start_at;
			""", nativeQuery = true)
	public List<AppointmentSlot> findAvailableSlots(@Param("now") LocalDateTime now);

}
