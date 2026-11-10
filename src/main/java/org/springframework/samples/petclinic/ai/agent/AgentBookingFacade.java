package org.springframework.samples.petclinic.ai.agent;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.appointment.AppointmentRequest;
import org.springframework.samples.petclinic.appointment.AppointmentResponse;
import org.springframework.samples.petclinic.appointment.AppointmentSlot;
import org.springframework.samples.petclinic.appointment.AppointmentSlotRepository;
import org.springframework.samples.petclinic.appointment.AppointmentSlotService;
import org.springframework.samples.petclinic.appointment.AvailableSlotResponse;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.samples.petclinic.appointment.IdempotencyService;

@Service
public class AgentBookingFacade {

	private OwnerRepository owners;

	private AppointmentSlotService appointmentSlotService;

	private AppointmentSlotRepository slots;

	private IdempotencyService idemService;

	private final Integer currentOwnerId = 3;

	private final Map<UUID, PendingBooking> pendingBookings = new ConcurrentHashMap<>();

	public AgentBookingFacade(OwnerRepository owners, AppointmentSlotService appointmentSlotService,
			AppointmentSlotRepository slots, IdempotencyService idemService) {
		this.owners = owners;
		this.appointmentSlotService = appointmentSlotService;
		this.slots = slots;
		this.idemService = idemService;
	}

	/*
	 * simulate user login and appointment confirmation
	 *
	 *
	 */
	public void verifyPetOwnership(Integer currentOwnerId, Integer petId) {

		Owner owner = this.owners.findOwnerByPetId(petId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
		if (!owner.getId().equals(currentOwnerId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to book for this pet");
		}

	}

	public BookingPreview prepareBooking(int petId, int slotId) {

		// verifyPetOwnership
		verifyPetOwnership(currentOwnerId, petId);

		// check if slotId is available or not

		List<AvailableSlotResponse> AvaSlots = this.appointmentSlotService.getAvailableSlots();

		boolean flag = AvaSlots.stream().anyMatch(slot -> slot.slotId() == slotId);

		if (!flag)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "slotId is not available");

		UUID confirmationId = UUID.randomUUID();
		UUID idempotencyKey = UUID.randomUUID();

		LocalDateTime expiredTime = LocalDateTime.now().plusMinutes(10);

		PendingBooking pBooking = new PendingBooking(currentOwnerId, petId, slotId, idempotencyKey, expiredTime);
		pendingBookings.put(confirmationId, pBooking);
		AppointmentSlot slot = this.slots.findById(slotId).orElseThrow();
		return new BookingPreview(confirmationId, petId, slotId, slot.getStartAt(), slot.getEndAt(), expiredTime);

	}

	public BookingPreview getBookingPreview(UUID confirmationId) {
		PendingBooking pBooking = pendingBookings.get(confirmationId);
		if (pBooking == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "confirmation not found");
		}

		// 1. 从 pendingBookings 查找 PendingBooking
		// 2. 检查当前 Owner 是否有权限查看
		if (!pBooking.ownerId().equals(currentOwnerId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not authorized.");
		}
		// 3. 检查 Confirmation 是否过期 current>=expired
		if (!LocalDateTime.now().isBefore(pBooking.expiresAt())) {
			pendingBookings.remove(confirmationId, pBooking);
			throw new ResponseStatusException(HttpStatus.GONE, "expired confirmation.");
		}
		// 4. 根据 slotId 查询 AppointmentSlot
		AppointmentSlot slot = this.slots.findById(pBooking.slotId()).orElseThrow();
		// 5. 返回 BookingPreview
		return new BookingPreview(confirmationId, pBooking.petId(), pBooking.slotId(), slot.getStartAt(),
				slot.getEndAt(), pBooking.expiresAt());

	}

	public AppointmentResponse confirmationBooking(UUID confirmationId) {

		PendingBooking pBooking = this.pendingBookings.get(confirmationId);

		if (pBooking == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "confirmation not found");
		}

		// check if confirmation expired or not >= expried time, need to remove
		if (!LocalDateTime.now().isBefore(pBooking.expiresAt())) {
			pendingBookings.remove(confirmationId, pBooking);
			throw new ResponseStatusException(HttpStatus.GONE, "expired confimation");
		}

		// check if confirmation belongs to currentOwner
		if (!pBooking.ownerId().equals(currentOwnerId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
		}

		// check pet ownership
		verifyPetOwnership(currentOwnerId, pBooking.petId());

		AppointmentRequest request = new AppointmentRequest(pBooking.slotId());
		AppointmentResponse response = this.idemService.createAppointmentIdempotently(pBooking.petId(), request,
				pBooking.idempotencyKey());

		return response;

	}

}
