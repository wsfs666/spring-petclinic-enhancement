package org.springframework.samples.petclinic.appointment;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.owner.OwnerRepository;

import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppointmentService {

	private AppointmentRepository appointments;

	private AppointmentSlotRepository appointmentSlots;

	private OwnerRepository owners;

	public AppointmentService(AppointmentRepository appointments, AppointmentSlotRepository appointmentSlots,
			OwnerRepository owners) {
		this.appointments = appointments;
		this.appointmentSlots = appointmentSlots;
		this.owners = owners;
	}

	// create appointment by petId
	@Transactional
	public AppointmentResponse createAppointment(int petId, AppointmentRequest aRequest) {

		// get pet
		Pet pet = this.owners.findPetById(petId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pet not found with id" + petId));

		AppointmentSlot aSlot = this.appointmentSlots.findById(aRequest.slotId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
					"slot not found with id" + aRequest.slotId()));

		// create appointment
		Appointment ap = new Appointment();
		ap.setSlot(aSlot);
		ap.setPet(pet);

		Appointment appointment = this.appointments.saveAndFlush(ap);

		AppointmentResponse aResponse = new AppointmentResponse(appointment.getId(), petId,
				appointment.getSlot().getId(), appointment.getSlot().getStartAt(), appointment.getSlot().getEndAt(),
				appointment.getStatus(), appointment.getCreatedAt());

		return aResponse;

	}

	// GET /api/appointments/{appointmentId}
	@Transactional(readOnly = true)
	public AppointmentResponse getAppointmentById(int appointmentId) {
		Appointment appointment = this.appointments.findById(appointmentId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
					"appointment not found with id" + appointmentId));

		AppointmentResponse aResponse = new AppointmentResponse(appointment.getId(), appointment.getPet().getId(),
				appointment.getSlot().getId(), appointment.getSlot().getStartAt(), appointment.getSlot().getEndAt(),
				appointment.getStatus(), appointment.getCreatedAt());

		return aResponse;
	}

}
