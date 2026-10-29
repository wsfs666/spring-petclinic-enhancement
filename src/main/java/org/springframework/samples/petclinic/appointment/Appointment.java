package org.springframework.samples.petclinic.appointment;

import java.time.LocalDateTime;

import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.owner.Pet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointments")

public class Appointment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pet_id", nullable = false)
	private Pet pet;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "slot_id", nullable = false)
	private AppointmentSlot slot;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AppointmentStatus status = AppointmentStatus.CONFIRMED;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	public Pet getPet() {
		return this.pet;
	}

	public void setSlot(AppointmentSlot slot) {
		this.slot = slot;
	}

	public AppointmentSlot getSlot() {
		return this.slot;
	}

	public AppointmentStatus getStatus() {
		return this.status;
	}

	public LocalDateTime getCreatedAt() {
		return this.createdAt;
	}

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

}
