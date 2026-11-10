package org.springframework.samples.petclinic.owner;

import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOwnerRequest(@NotBlank String address, @NotBlank String city,
		@NotBlank @Pattern(regexp = "\\d{10}", message = "{telephone.invalid}") String telephone,
		@NotBlank String firstName, @NotBlank String lastName, @Version Long version) {
}