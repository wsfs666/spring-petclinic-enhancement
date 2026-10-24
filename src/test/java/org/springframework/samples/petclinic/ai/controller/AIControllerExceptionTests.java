package org.springframework.samples.petclinic.ai.controller;

import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.samples.petclinic.ai.agent.AgentService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.SocketTimeoutException;

import org.springframework.ai.retry.TransientAiException;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.MediaType;

@WebMvcTest(AIController.class)
public class AIControllerExceptionTests {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	AgentService agentService;

	@Test
	// NonTransientAiException return 500
	void shouldReturnWhenNonTransientAiException() throws Exception {

		// mock

		given(this.agentService.ask(anyString())).willThrow(new NonTransientAiException("provider request failed"));

		// act

		mockMvc.perform(post("/api/ai/query").contentType(MediaType.TEXT_PLAIN).content("What visits does pet 7 have?"))
			.andExpect(status().isInternalServerError());

	}

	@Test
	void shouldReturnWhenTransientAiException() throws Exception {

		// mock
		given(this.agentService.ask(anyString()))
			.willThrow(new TransientAiException("provider temporarily unavailable"));

		// act
		mockMvc.perform(post("/api/ai/query").contentType(MediaType.TEXT_PLAIN).content("What visits does pet 7 have?"))
			.andExpect(status().isServiceUnavailable())
			.andExpect(content().string("AI service temporarily unavailable"));
	}

	@Test
	// socketTImeout 504
	void shouldReturnWhenTimeOutExcption() throws Exception {
		given(this.agentService.ask(anyString())).willThrow(
				new TransientAiException("outer layer ", new SocketTimeoutException("provider service time out")));

		mockMvc.perform(post("/api/ai/query").contentType(MediaType.TEXT_PLAIN).content("What visits does pet 7 have?"))
			.andExpect(status().isGatewayTimeout())
			.andExpect(content().string("AI provider request timeout"));

	}

}
