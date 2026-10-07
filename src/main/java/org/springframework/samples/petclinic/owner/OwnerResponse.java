package org.springframework.samples.petclinic.owner;

import java.time.LocalDateTime;
import java.util.List;

public record OwnerResponse(String address, String city, String telephone, String firstName, String lastName,
		Integer id) {
	// java record, getter function will be implemented automatically and by
	// .address() not .getterAddress()
}
