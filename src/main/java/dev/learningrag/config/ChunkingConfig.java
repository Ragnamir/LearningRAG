package dev.learningrag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.learningrag.chunk.DocumentChunker;
import dev.learningrag.chunk.ParagraphChunker;

@Configuration
public class ChunkingConfig {

    @Bean
    public DocumentChunker documentChunker(RagProperties properties) {
        RagProperties.Chunking chunking = properties.getChunking();
        return new ParagraphChunker(chunking.getMaxChars(), chunking.getOverlapChars());
    }
}
