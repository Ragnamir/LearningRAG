package dev.learningrag.embed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import dev.learningrag.core.error.ErrorCode;
import dev.learningrag.core.error.RagException;
import dev.learningrag.store.VectorCodec;

/**
 * Sentence embeddings from a local ONNX encoder (multilingual MiniLM) with mean pooling and L2 normalization.
 *
 * <p>The model is loaded during construction: a missing or unreadable model fails application startup instead of
 * silently degrading retrieval to meaningless vectors.
 */
public class OnnxEmbeddingService implements EmbeddingService, AutoCloseable {

    private final HuggingFaceTokenizer tokenizer;
    private final ZooModel<NDList, NDList> model;
    private final Predictor<NDList, NDList> predictor;
    private final int dimension;
    private final int batchSize;

    public OnnxEmbeddingService(Path modelPath, Path tokenizerPath, int dimension, int maxSequenceLength,
                                int batchSize) {
        if (!Files.isRegularFile(modelPath)) {
            throw new IllegalStateException("Embedding model not found: " + modelPath);
        }
        if (!Files.isRegularFile(tokenizerPath)) {
            throw new IllegalStateException("Embedding tokenizer not found: " + tokenizerPath);
        }
        this.dimension = dimension;
        this.batchSize = Math.max(1, batchSize);
        try {
            this.tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath, Map.of(
                    "padding", "true",
                    "truncation", "true",
                    "maxLength", Integer.toString(maxSequenceLength)));
            this.model = Criteria.builder()
                    .setTypes(NDList.class, NDList.class)
                    .optModelPath(modelPath)
                    .optEngine("OnnxRuntime")
                    .build()
                    .loadModel();
            this.predictor = model.newPredictor();
        } catch (IOException | ai.djl.repository.zoo.ModelNotFoundException | ai.djl.MalformedModelException e) {
            throw new IllegalStateException("Cannot load embedding model from " + modelPath, e);
        }
    }

    @Override
    public float[] embed(String text) {
        return embedAll(List.of(text == null ? "" : text)).get(0);
    }

    @Override
    public List<float[]> embedAll(List<String> texts) {
        List<float[]> result = new ArrayList<>(texts.size());
        for (int from = 0; from < texts.size(); from += batchSize) {
            List<String> batch = texts.subList(from, Math.min(texts.size(), from + batchSize));
            result.addAll(embedBatch(batch));
        }
        return result;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    private List<float[]> embedBatch(List<String> batch) {
        Encoding[] encodings = tokenizer.batchEncode(batch);
        try (NDManager manager = NDManager.newBaseManager()) {
            NDList inputs = Encoding.toNDList(encodings, manager, true, false);
            NDList outputs = predictor.predict(inputs);
            float[] flat = outputs.get(0).toFloatArray();
            int tokens = (int) outputs.get(0).getShape().get(1);
            int width = (int) outputs.get(0).getShape().get(2);
            if (width != dimension) {
                throw new IllegalStateException(
                        "Embedding model produced " + width + " dimensions, configured " + dimension);
            }
            List<float[]> vectors = new ArrayList<>(batch.size());
            for (int row = 0; row < batch.size(); row++) {
                vectors.add(meanPool(flat, row, tokens, width, encodings[row].getAttentionMask()));
            }
            return vectors;
        } catch (TranslateException e) {
            throw new RagException(ErrorCode.INTERNAL, "Embedding inference failed", e);
        }
    }

    private static float[] meanPool(float[] flat, int row, int tokens, int width, long[] attentionMask) {
        float[] pooled = new float[width];
        int counted = 0;
        int rowOffset = row * tokens * width;
        for (int t = 0; t < tokens; t++) {
            if (t < attentionMask.length && attentionMask[t] == 0L) {
                continue;
            }
            int base = rowOffset + t * width;
            for (int d = 0; d < width; d++) {
                pooled[d] += flat[base + d];
            }
            counted++;
        }
        if (counted > 1) {
            for (int d = 0; d < width; d++) {
                pooled[d] /= counted;
            }
        }
        return VectorCodec.normalize(pooled);
    }

    @Override
    public void close() {
        predictor.close();
        model.close();
        tokenizer.close();
    }
}
