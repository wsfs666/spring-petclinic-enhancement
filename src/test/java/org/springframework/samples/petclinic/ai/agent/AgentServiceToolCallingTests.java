package org.springframework.samples.petclinic.ai.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.samples.petclinic.ai.tool.PetVisitHistoryTool;
import org.springframework.samples.petclinic.owner.PetVisitHistoryResult;
import org.springframework.samples.petclinic.owner.Service.VisitService;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.ai.chat.model.ChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

public class AgentServiceToolCallingTests {

	@Test
	void shouldCallPetVisitHistoryTool() {

		// fake LLM output
		// 1. 构造 ToolCall
		// name = getPetVisitHistory
		// arguments = {"petId":7}

		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall("call-1", "function", "getPetVisitHistory",
				"{\"petId\":7}");

		List<AssistantMessage.ToolCall> toolcalls = new ArrayList<>();
		toolcalls.add(toolCall);

		// 2. 构造 toolCallResponse
		// 表示：模型第一次说“我要调用 Tool”
		AssistantMessage message = AssistantMessage.builder().content("").toolCalls(toolcalls).build();
		Generation generation = new Generation(message);
		ChatResponse toolCallresponse = new ChatResponse(List.of(generation));

		// 3. 构造 finalResponse
		// 表示：模型拿到 Tool result 后给出的最终回答
		AssistantMessage finalMessage = new AssistantMessage("Pet 7 is Samantha and has two visits.");
		Generation finalGeneration = new Generation(finalMessage);
		ChatResponse finalResponse = new ChatResponse(List.of(finalGeneration));

		// set fake LLM output to fake chatmodel
		AtomicInteger callCount = new AtomicInteger(0);

		ChatModel fakeChatModel = new ChatModel() {

			@Override
			public ChatResponse call(Prompt prompt) {

				if (callCount.getAndIncrement() == 0) {
					return toolCallresponse;
				}

				return finalResponse;
			}

			@Override
			public ToolCallingChatOptions getOptions() {
				return ToolCallingChatOptions.builder().build();
			}
		};

		VisitService visitService = mock(VisitService.class);

		when(visitService.getVisitsByPetId(7)).thenReturn(new PetVisitHistoryResult(7, "Cola", List.of()));

		PetVisitHistoryTool petVisitHistoryTool = new PetVisitHistoryTool(visitService);

		ChatClient.Builder chatClientBuilder = ChatClient.builder(fakeChatModel);
		AgentService agentService = new AgentService(chatClientBuilder, petVisitHistoryTool);

		//
		assertThat(toolCallresponse.hasToolCalls()).isTrue();

		assertThat(finalResponse.hasToolCalls()).isFalse();

		assertThat(finalResponse.getResult().getOutput().getText()).isEqualTo("Pet 7 is Samantha and has two visits.");

		// action

		String answer = agentService.ask("What visits does pet 7 have?");

		//
		assertThat(callCount.get()).isEqualTo(2);
		verify(visitService).getVisitsByPetId(7);
		// assertThat(callCount.get()).isEqualTo(2);

		assertThat(answer).isEqualTo("Pet 7 is Samantha and has two visits.");

	}

}
