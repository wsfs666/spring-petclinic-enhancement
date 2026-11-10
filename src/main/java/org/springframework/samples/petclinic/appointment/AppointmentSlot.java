package org.springframework.samples.petclinic.appointment;

import java.time.LocalDateTime;

import org.springframework.samples.petclinic.model.BaseEntity;

import jakarta.persistence.Column;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointment_slots")

public class AppointmentSlot extends BaseEntity {

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	public void setStartAt(LocalDateTime startAt) {
		this.startAt = startAt;

	}

	public LocalDateTime getStartAt() {
		return this.startAt;
	}

	public void setEndAt(LocalDateTime endAt) {
		this.endAt = endAt;
	}

	public LocalDateTime getEndAt() {
		return this.endAt;
	}

}
