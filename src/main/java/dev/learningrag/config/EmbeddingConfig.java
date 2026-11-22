package dev.learningrag.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.learningrag.embed.EmbeddingService;
import dev.learningrag.embed.OnnxEmbeddingService;

@Configuration
public class EmbeddingConfig {

    @Bean(destroyMethod = "close")
    public EmbeddingService embeddingService(RagProperties properties) {
        RagProperties.Embedding embedding = properties.getEmbedding();
        if (embedding.getModelPath() == null || embedding.getTokenizerPath() == null) {
            throw new IllegalStateException(
                    "rag.embedding.model-path and rag.embedding.tokenizer-path must be configured");
        }
        return new OnnxEmbeddingService(Path.of(embedding.getModelPath()), Path.of(embedding.getTokenizerPath()),
                embedding.getDimension(), embedding.getMaxSequenceLength(), embedding.getBatchSize());
    }
}
