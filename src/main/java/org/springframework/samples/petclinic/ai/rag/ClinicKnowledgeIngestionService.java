package org.springframework.samples.petclinic.ai.rag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Profile("rag")
public class ClinicKnowledgeIngestionService {

	private final VectorStore vStore;

	public ClinicKnowledgeIngestionService(VectorStore vStore) {
		this.vStore = vStore;
	}

	public void ingestAllPolicies() {
		// PgVectorStore default id type id UUID
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		List<Document> documents = new ArrayList<>();
		try {
			Resource[] resources = resolver.getResources("classpath*:clinic-knowledge/policies/*.md");
			if (resources.length == 0) {
				throw new IllegalStateException("no clinic policies found.");
			}
			for (Resource resource : resources) {
				String filename = resource.getFilename();
				String content;
				try (InputStream input = resource.getInputStream()) {
					content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
				}
				String uuid = UUID.nameUUIDFromBytes(filename.getBytes(StandardCharsets.UTF_8)).toString();
				String sourceId = filename.replaceFirst("\\.md$", "");
				Document document = Document.builder()
					.id(uuid)
					.text(content)
					.metadata("source", "clinic-knowledge/policies/" + filename)
					.metadata("sourceId", sourceId)
					.metadata("version", "v1")
					.build();
				documents.add(document);

			}

		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to load clinic policies", ex);
		}

		this.vStore.add(documents);

	}

}
