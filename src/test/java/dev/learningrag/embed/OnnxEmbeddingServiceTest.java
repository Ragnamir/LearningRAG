package dev.learningrag.embed;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import dev.learningrag.store.VectorCodec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Sanity checks against the real ONNX encoder. Skipped when the model files are absent so a fresh clone can still
 * build; the model itself is not part of the repository.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OnnxEmbeddingServiceTest {

    private static final Path MODEL_DIR = Path.of("models/multilingual-minilm");

    private OnnxEmbeddingService service;

    @BeforeAll
    void loadModel() {
        assumeTrue(Files.isRegularFile(MODEL_DIR.resolve("model.onnx"))
                        && Files.isRegularFile(MODEL_DIR.resolve("tokenizer.json")),
                "embedding model is not present");
        service = new OnnxEmbeddingService(MODEL_DIR.resolve("model.onnx"), MODEL_DIR.resolve("tokenizer.json"),
                384, 256, 4);
    }

    @AfterAll
    void closeModel() {
        if (service != null) {
            service.close();
        }
    }

    @Test
    void producesNormalizedVectorsOfConfiguredDimension() {
        float[] vector = service.embed("Политика паролей и многофакторной аутентификации.");

        assertThat(vector).hasSize(384);
        double sum = 0.0;
        for (float value : vector) {
            sum += (double) value * value;
        }
        assertThat(Math.sqrt(sum)).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-4));
    }

    @Test
    void ranksRelatedRussianTextAboveUnrelatedText() {
        float[] query = service.embed("Как часто нужно менять пароль учётной записи?");
        float[] related = service.embed("Сотрудники обязаны менять пароль каждые 90 дней. Повторное использование "
                + "последних пяти паролей запрещено.");
        float[] unrelated = service.embed("Регламент утилизации резервных копий и порядок вывоза носителей.");

        double relatedScore = VectorCodec.cosineSimilarity(query, related);
        double unrelatedScore = VectorCodec.cosineSimilarity(query, unrelated);

        assertThat(relatedScore).isGreaterThan(unrelatedScore);
        assertThat(relatedScore).isGreaterThan(0.4);
    }

    @Test
    void batchesGiveSameVectorsAsSingleCalls() {
        List<String> texts = List.of("Первое предложение.", "Второе предложение.", "Третье предложение.");
        List<float[]> batched = service.embedAll(texts);

        assertThat(batched).hasSize(3);
        for (int i = 0; i < texts.size(); i++) {
            assertThat(VectorCodec.cosineSimilarity(batched.get(i), service.embed(texts.get(i))))
                    .isGreaterThan(0.999);
        }
    }

    @Test
    void emptyTextStillProducesAFiniteVector() {
        float[] vector = service.embed("");
        assertThat(vector).hasSize(384);
        for (float value : vector) {
            assertThat(Float.isFinite(value)).isTrue();
        }
    }
}
