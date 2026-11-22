package dev.learningrag.store;

import java.util.Arrays;

/**
 * Converts embeddings to the pgvector text literal and provides the cosine helpers used by tests.
 */
public final class VectorCodec {

    private VectorCodec() {
    }

    public static String toPgVector(float[] vector) {
        StringBuilder sb = new StringBuilder(vector.length * 12 + 2);
        sb.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        return sb.append(']').toString();
    }

    public static float[] normalize(float[] vector) {
        double sum = 0.0;
        for (float v : vector) {
            sum += (double) v * v;
        }
        double norm = Math.sqrt(sum);
        if (norm == 0.0 || !Double.isFinite(norm)) {
            return Arrays.copyOf(vector, vector.length);
        }
        float[] result = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = (float) (vector[i] / norm);
        }
        return result;
    }

    public static double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vector dimensions differ: " + a.length + " vs " + b.length);
        }
        double dot = 0.0;
        double na = 0.0;
        double nb = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            na += (double) a[i] * a[i];
            nb += (double) b[i] * b[i];
        }
        if (na == 0.0 || nb == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
