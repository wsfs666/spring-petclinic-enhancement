package org.springframework.samples.petclinic.owner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateOwnerRequest(@NotBlank String address, @NotBlank String city,
		@NotBlank @Pattern(regexp = "\\d{10}", message = "{telephone.invalid}") String telephone,
		@NotBlank String firstName, @NotBlank String lastName) {

}
