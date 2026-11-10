package org.springframework.samples.petclinic.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

	long countBySlot_IdAndStatus(Integer slotId, AppointmentStatus status);

}