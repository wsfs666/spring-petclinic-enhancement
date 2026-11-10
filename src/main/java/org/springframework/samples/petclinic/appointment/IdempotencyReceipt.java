package org.springframework.samples.petclinic.appointment;

import java.util.UUID;

import org.springframework.samples.petclinic.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "idempotency_receipts")
public class IdempotencyReceipt extends BaseEntity {

	@Column(name = "idempotency_key", nullable = false)
	private UUID idempotencyKey;

	@Column(name = "operation", nullable = false)
	private String operation;

	@Column(name = "request_hash", nullable = false)
	private byte[] requestHash;

	@Column(name = "principal_id", nullable = false)
	private Integer principalId;

	@Column(name = "appointment_id")
	private Integer appointmentId;

	public UUID getIdempotencyKey() {
		return idempotencyKey;
	}

	public void setIdempotencyKey(UUID idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public byte[] getRequestHash() {
		return requestHash;
	}

	public void setRequestHash(byte[] requestHash) {
		this.requestHash = requestHash;
	}

	public Integer getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(Integer principalId) {
		this.principalId = principalId;
	}

	public Integer getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(Integer appointmentId) {
		this.appointmentId = appointmentId;
	}

}
