package org.springframework.samples.petclinic.ai.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.samples.petclinic.ai.tool.GetAvailableSlotsTool;
import org.springframework.samples.petclinic.ai.tool.PetVisitHistoryTool;
import org.springframework.samples.petclinic.appointment.AppointmentSlotService;
import org.springframework.samples.petclinic.appointment.AvailableSlotResponse;
import org.springframework.samples.petclinic.owner.PetVisitHistoryResult;
import org.springframework.samples.petclinic.owner.Service.VisitService;

import com.openai.models.beta.assistants.AssistantTool;

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

		AssistantMessage.ToolCall toolCall1 = new AssistantMessage.ToolCall("call-1", "function", "getPetVisitHistory",
				"{\"petId\":7}");

		List<AssistantMessage.ToolCall> toolcalls = new ArrayList<>();
		toolcalls.add(toolCall1);

		// 2. 构造 toolCallResponse
		// 表示：模型第一次说“我要调用 Tool”
		AssistantMessage message = AssistantMessage.builder().content("").toolCalls(toolcalls).build();
		Generation generation = new Generation(message);
		ChatResponse toolCallresponse = new ChatResponse(List.of(generation));

		// 3. 构造 finalResponse
		// 表示：模型拿到 Tool result 后给出的最终回答
		AssistantMessage finalMessage = new AssistantMessage("Pet 7 is Samantha and has two visits.");

		Generation finalGeneration = new Generation(finalMessage);
		List<Generation> list = new ArrayList<>();
		list.add(finalGeneration);

		ChatResponse finalResponse = new ChatResponse(list);

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
		AppointmentSlotService appointmentSlotService = mock(AppointmentSlotService.class);

		when(visitService.getVisitsByPetId(7)).thenReturn(new PetVisitHistoryResult(7, "Cola", List.of()));

		PetVisitHistoryTool petVisitHistoryTool = new PetVisitHistoryTool(visitService);
		GetAvailableSlotsTool getAvailableSlotsTool = new GetAvailableSlotsTool(appointmentSlotService);

		ChatClient.Builder chatClientBuilder = ChatClient.builder(fakeChatModel);
		AgentService agentService = new AgentService(chatClientBuilder, petVisitHistoryTool, getAvailableSlotsTool);

		//
		assertThat(toolCallresponse.hasToolCalls()).isTrue();

		assertThat(finalResponse.hasToolCalls()).isFalse();

		assertThat(finalResponse.getResult().getOutput().getText()).isEqualTo("Pet 7 is Samantha and has two visits.");

		// action

		String answer1 = agentService.ask("What visits does pet 7 have?");

		//
		assertThat(callCount.get()).isEqualTo(2);
		verify(visitService).getVisitsByPetId(7);
		// assertThat(callCount.get()).isEqualTo(2);

		assertThat(answer1).isEqualTo("Pet 7 is Samantha and has two visits.");

	}

	@Test
	public void shouldCallAvailableSlotsTool() throws Exception {

		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall("call_2", "function", "getAvailableSlots",
				"{}");
		List<AssistantMessage.ToolCall> calls = new ArrayList<>();
		calls.add(toolCall);

		AssistantMessage message = AssistantMessage.builder().content("").toolCalls(calls).build();
		Generation generation = new Generation(message);
		ChatResponse toolCallResponse = new ChatResponse(List.of(generation));

		AssistantMessage finalMessage = new AssistantMessage("no future available slots for appointment.");
		Generation fianlGeneration = new Generation(finalMessage);
		ChatResponse finalResponse = new ChatResponse(List.of(fianlGeneration));

		AtomicInteger callCount = new AtomicInteger(0);

		ChatModel fakChatModel = new ChatModel() {

			@Override
			public ChatResponse call(Prompt prompt) {
				if (callCount.incrementAndGet() == 1) {
					return toolCallResponse;
				}

				return finalResponse;
			}

			@Override
			public ToolCallingChatOptions getOptions() {
				return ToolCallingChatOptions.builder().build();
			}
		};

		VisitService visitService = mock(VisitService.class);
		AppointmentSlotService appointmentSlotService = mock(AppointmentSlotService.class);

		when(appointmentSlotService.getAvailableSlots()).thenReturn(List.of());

		PetVisitHistoryTool petVisitHistoryTool = new PetVisitHistoryTool(visitService);
		GetAvailableSlotsTool getAvailableSlotsTool = new GetAvailableSlotsTool(appointmentSlotService);

		ChatClient.Builder chatClientBuilder = ChatClient.builder(fakChatModel);
		AgentService agentService = new AgentService(chatClientBuilder, petVisitHistoryTool, getAvailableSlotsTool);

		assertThat(toolCallResponse.hasToolCalls()).isTrue();

		assertThat(finalResponse.hasToolCalls()).isFalse();

		assertThat(finalResponse.getResult().getOutput().getText())
			.isEqualTo("no future available slots for appointment.");

		// action

		String answer1 = agentService.ask("What are available future time slots for appointment?");

		//
		assertThat(callCount.get()).isEqualTo(2);
		verify(appointmentSlotService).getAvailableSlots();
		// assertThat(callCount.get()).isEqualTo(2);

		assertThat(answer1).isEqualTo("no future available slots for appointment.");

	}

}
