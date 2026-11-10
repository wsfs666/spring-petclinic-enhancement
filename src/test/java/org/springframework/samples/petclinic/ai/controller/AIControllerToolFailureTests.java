package org.springframework.samples.petclinic.ai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.samples.petclinic.ai.agent.AgentService;
import org.springframework.samples.petclinic.owner.Service.VisitService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AIControllerToolFailureTests {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	VisitService visitService;

	@MockitoBean
	private ChatModel chatModel;

	@Test
	/*
	 * failure case: Tool 内部调用真实业务 service → service 出错 → Spring AI 的 tool execution
	 * return isInternalServerError
	 *
	 *
	 */
	void shouldReturnWhenToolException() throws Exception {

		// mock
		given(visitService.getVisitsByPetId(anyInt())).willThrow(new RuntimeException("database failure"));

		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall("call-1", "function", "getPetVisitHistory",
				"{\"petId\":7}");

		List<AssistantMessage.ToolCall> toolcalls = new ArrayList<>();

		toolcalls.add(toolCall);

		AssistantMessage message = AssistantMessage.builder().content("").toolCalls(toolcalls).build();
		Generation generation = new Generation(message);
		ChatResponse toolCallresponse = new ChatResponse(List.of(generation));

		given(chatModel.getOptions()).willReturn(ToolCallingChatOptions.builder().build());

		given(chatModel.call(any(Prompt.class))).willReturn(toolCallresponse);

		mockMvc.perform(post("/api/ai/query").contentType(MediaType.TEXT_PLAIN).content("What visits does pet 7 have?"))
			.andExpect(status().isInternalServerError())
			.andExpect(content().string("AI service requests failed"));

		verify(this.visitService, times(1)).getVisitsByPetId(7);

	}

}
