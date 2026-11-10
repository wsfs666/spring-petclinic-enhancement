package org.springframework.samples.petclinic.ai.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.retry.Retryable;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class AiRetryPolicyTests {

	/*
	 *
	 * Arrange → 准备一个有调用次数的 operation
	 *
	 * Act → 通过 retry mechanism 执行
	 *
	 * Assert → result == "success" → callCount == 2
	 *
	 *
	 */

	@Autowired
	RetryTemplate template;

	@Test

	void shouldReturnWhenRetry() throws Exception {

		// mock

		AtomicInteger attemps = new AtomicInteger(0);

		Retryable<String> op = () -> {
			if (attemps.incrementAndGet() == 1)
				throw new TransientAiException("temprory failure.");

			return "success.";

		};
		// action

		String result = this.template.execute(op);

		// assert

		assertThat(attemps.get()).isEqualTo(2);

		assertThat(result).isEqualTo("success.");

	}

	/*
	 * operation 每次都抛 TransientAiException → RetryTemplate 重试 → 达到 max attempts → 最终仍抛异常 →
	 * attempts == 你配置的最大次数
	 *
	 */

	@Test
	void shouldReturnWhenRetryMax() throws Exception {
		// mock
		AtomicInteger attemps = new AtomicInteger(0);

		Retryable<String> op = () -> {
			attemps.incrementAndGet();

			throw new TransientAiException("temprory failure.");

		};

		assertThatThrownBy(() -> this.template.execute(op)).isInstanceOf(RetryException.class);

		assertThat(attemps.get()).isEqualTo(4);

	}

	/*
	 * Case 3: non-transient error → 不 retry → attempts == 1
	 *
	 */
	@Test
	void shouldNotRetryWhenNonTransien() throws Exception {
		AtomicInteger attemps = new AtomicInteger(0);
		Retryable<String> op = () -> {
			attemps.incrementAndGet();
			throw new NonTransientAiException("request failed");

		};

		assertThatThrownBy(() -> this.template.execute(op)).isInstanceOf(RetryException.class)
			.hasCauseInstanceOf(NonTransientAiException.class);

		assertThat(attemps.get()).isEqualTo(1);
		// assertThat(res).isEqualTo("AI service requests failed");

	}

}
