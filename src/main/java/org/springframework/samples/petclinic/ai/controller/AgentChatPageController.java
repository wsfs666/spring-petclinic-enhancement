package org.springframework.samples.petclinic.ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AgentChatPageController {

	@GetMapping("/ai/chat")
	public String chatPage() {
		return "ai/chat";
	}

}
