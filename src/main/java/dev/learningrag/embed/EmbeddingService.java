package dev.learningrag.embed;

import java.util.List;

public interface EmbeddingService {

    float[] embed(String text);

    default List<float[]> embedAll(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }

    int dimension();
}
