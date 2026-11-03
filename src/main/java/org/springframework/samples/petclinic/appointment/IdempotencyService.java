package org.springframework.samples.petclinic.appointment;

import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import io.micrometer.core.instrument.MeterRegistry; //
import org.springframework.transaction.support.TransactionSynchronization; //
import org.springframework.transaction.support.TransactionSynchronizationManager; //

@Service
public class IdempotencyService {

	private final IdempotencyReceiptRepository receipts;

	private final AppointmentService appointmentService;

	private final OwnerRepository owners;

	private final MeterRegistry meterRegistry;

	public IdempotencyService(IdempotencyReceiptRepository receipts, AppointmentService appointmentService,
			OwnerRepository owners, MeterRegistry meterRegistry) {
		this.receipts = receipts;
		this.appointmentService = appointmentService;
		this.owners = owners;
		this.meterRegistry = meterRegistry;

	}

	private void recordCreatedAfterCommit(String outcome) { // 事务真正提交后才计数
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				meterRegistry.counter("petclinic.appointment.idempotency", "outcome", outcome).increment();

			}
		});
	}

	private byte[] calculateRequestHash(int petId, AppointmentRequest request) {
		String content = petId + ":" + request.slotId();

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(content.getBytes(StandardCharsets.UTF_8));
			return md.digest();
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}

	@Transactional
	public AppointmentResponse createAppointmentIdempotently(Integer petId, AppointmentRequest request,
			UUID idempotencyKey) {
		// 1. 计算 requestHash

		byte[] requestHash = calculateRequestHash(petId, request);

		// 2. receiptRepository.claimKey(...)
		Owner owner = this.owners.findOwnerByPetId(petId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pet not found with id" + petId));

		int res = receipts.claimKey(owner.getId(), "CREATE_APPOINTMENT", idempotencyKey, requestHash);

		// 3. 如果返回 1：
		// 调用已有的 Appointment 创建逻辑
		// 更新 Receipt 的 appointmentId
		// 返回 AppointmentResponse

		if (res == 1) {
			// cretae Appointment

			AppointmentResponse appointment = appointmentService.createAppointment(petId, request);
			IdempotencyReceipt receipt = this.receipts
				.findByPrincipalIdAndOperationAndIdempotencyKey(owner.getId(), "CREATE_APPOINTMENT", idempotencyKey)
				.orElseThrow();

			receipt.setAppointmentId(appointment.id());
			recordCreatedAfterCommit("created"); // 事务真正提交后才计数
			return appointment;

		}

		// 4. 如果返回 0：
		// 查询已有 Receipt
		// 比较 requestHash
		// 相同：返回已有 Appointment
		// 不同：返回 409

		else if (res == 0) {
			IdempotencyReceipt receipt = this.receipts
				.findByPrincipalIdAndOperationAndIdempotencyKey(owner.getId(), "CREATE_APPOINTMENT", idempotencyKey)
				.orElseThrow();
			byte[] preRequestHash = receipt.getRequestHash();
			if (Arrays.equals(preRequestHash, requestHash)) {
				recordCreatedAfterCommit("replayed");
				return this.appointmentService.getAppointmentById(receipt.getAppointmentId());
			}
			else
				meterRegistry.counter("petclinic.appointment.idempotency", "outcome", "key_conflict").increment();
			// same owner, pet, UUID but different slot
			throw new ResponseStatusException(HttpStatus.CONFLICT, "conflict request");

		}
		throw new UnsupportedOperationException("Not implemented");
	}

}
