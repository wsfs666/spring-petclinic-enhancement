package org.springframework.samples.petclinic.appointment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Integer> {

}
