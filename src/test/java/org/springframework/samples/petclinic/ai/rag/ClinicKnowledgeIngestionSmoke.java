package org.springframework.samples.petclinic.ai.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@ActiveProfiles({ "postgres", "rag" })
public class ClinicKnowledgeIngestionSmoke {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(
			DockerImageName.parse("pgvector/pgvector:0.8.7-pg18-trixie").asCompatibleSubstituteFor("postgres"));

	@Autowired
	private ClinicKnowledgeIngestionService clinicKnowledgeIngestionService;

	@Autowired
	private VectorStore vStore;

	@Test
	public void shouldIngestAndRetrievePolicy() {
		// 1.digest
		clinicKnowledgeIngestionService.ingestAllPolicies();

		// similarity
		List<Document> docs = this.vStore.similaritySearch("How early should I cancel my appointment?");

		// check res is empty or not
		assertThat(docs).isNotEmpty();
		// anySatisfy for group of assertation
		String id = UUID.nameUUIDFromBytes("02-cancellation.md".getBytes(StandardCharsets.UTF_8)).toString();
		assertThat(docs).anySatisfy(doc -> {
			assertThat(doc.getId()).isEqualTo(id);
			assertThat(doc.getText()).contains("24 hours");
			assertThat(doc.getMetadata()).containsEntry("source", "clinic-knowledge/policies/02-cancellation.md");
		});

	}

}
