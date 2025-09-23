package com.jooany.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class RAGConfig {

    @Bean
    ChatModel chatModel() {
        return GoogleAiGeminiChatModel.builder()
            .apiKey(System.getenv("AI_API_KEY"))
            .modelName("gemini-1.5-flash")
            .build();
    }

    @Bean
    EmbeddingModel embeddingModel() {
        return GoogleAiEmbeddingModel.builder()
            .apiKey(System.getenv("AI_API_KEY"))
            .modelName("embedding-001")
            .build();
    }

    @Bean
    ChatService chatService(ChatModel chatModel, RetrievalAugmentor retrievalAugmentor) {
        return AiServices.builder(ChatService.class)
            .chatModel(chatModel)
            .retrievalAugmentor(retrievalAugmentor)
            .build();
    }

    @Bean
    RetrievalAugmentor retrievalAugmentor(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {

        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(1)
            .build();

        return DefaultRetrievalAugmentor.builder()
            .contentRetriever(contentRetriever)
            .build();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel, ResourceLoader resourceLoader) throws IOException, IOException {

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        Resource resource = resourceLoader.getResource("classpath:정주연_이력서.pdf");
        DocumentParser parser = new ApachePdfBoxDocumentParser();
        Document document = parser.parse(resource.getInputStream());
        TextSegment wholeResumeSegment = document.toTextSegment();
        Embedding embedding = embeddingModel.embed(wholeResumeSegment).content();

        embeddingStore.add(embedding, wholeResumeSegment);

        return embeddingStore;
    }
}
