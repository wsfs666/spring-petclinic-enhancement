package org.springframework.samples.petclinic.ai.tool;

import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOException;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.samples.petclinic.ai.rag.ClinicKnowledgeIngestionService;
import org.springframework.samples.petclinic.ai.rag.ClinicKnowledgeReply;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Profile("rag")
public class RetrieveClinicKnowledgeTool implements AgentTool {

	private final VectorStore vectorstore;

	private static final Logger log = LoggerFactory.getLogger(RetrieveClinicKnowledgeTool.class);

	public RetrieveClinicKnowledgeTool(VectorStore vectorstore) {
		this.vectorstore = vectorstore;
	}

	@Tool(description = "retrieve clinic top3 related policies from pgVector database based on current question. If the knowledge retrieval returns no relevant policy, acknowledge that the information is unavailable. Do not invent clinic policies.")
	// get related knowledge, return sourceId and content
	public List<ClinicKnowledgeReply> getClinicKnowledge(String question) {
		log.info("invoke RetrieveClinicKnowledgeTool");
		List<Document> docs = this.vectorstore
			.similaritySearch(SearchRequest.builder().query(question).topK(3).build());

		if (docs.size() == 0) {
			return List.of();
		}

		List<ClinicKnowledgeReply> res = new ArrayList<>();

		for (Document doc : docs) {
			String content = doc.getText();
			String sourceId = (String) doc.getMetadata().get("souceId");
			res.add(new ClinicKnowledgeReply(content, sourceId));
		}

		return res;

	}

}
