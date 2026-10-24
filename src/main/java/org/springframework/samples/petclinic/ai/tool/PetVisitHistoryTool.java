package org.springframework.samples.petclinic.ai.tool;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetVisitHistoryResult;
import org.springframework.samples.petclinic.owner.VisitResponse;
import org.springframework.samples.petclinic.owner.Service.VisitService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PetVisitHistoryTool {

	private final VisitService visitService;

	public PetVisitHistoryTool(VisitService visitService) {
		this.visitService = visitService;
	}

	@Tool(description = "Get the visit history for a specific pet using its pet ID")
	public PetVisitHistoryResult getPetVisitHistory(@ToolParam(description = "The unique ID of the pet") int petId) {

		PetVisitHistoryResult result = this.visitService.getVisitsByPetId(petId);

		return result;

	}

}
