package dev.learningrag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    private final Auth auth = new Auth();
    private final Retrieval retrieval = new Retrieval();
    private final Chunking chunking = new Chunking();
    private final Embedding embedding = new Embedding();
    private final Output output = new Output();

    public Auth getAuth() {
        return auth;
    }

    public Retrieval getRetrieval() {
        return retrieval;
    }

    public Chunking getChunking() {
        return chunking;
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public Output getOutput() {
        return output;
    }

    public static class Auth {
        private String apiKey;
        private String principalId = "local-user";
        private String tenantId = "acme";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getPrincipalId() {
            return principalId;
        }

        public void setPrincipalId(String principalId) {
            this.principalId = principalId;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
    }

    public static class Retrieval {
        private int topK = 6;
        private int maxQuestionChars = 2000;

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public int getMaxQuestionChars() {
            return maxQuestionChars;
        }

        public void setMaxQuestionChars(int maxQuestionChars) {
            this.maxQuestionChars = maxQuestionChars;
        }
    }

    public static class Chunking {
        private int maxChars = 1200;
        private int overlapChars = 200;

        public int getMaxChars() {
            return maxChars;
        }

        public void setMaxChars(int maxChars) {
            this.maxChars = maxChars;
        }

        public int getOverlapChars() {
            return overlapChars;
        }

        public void setOverlapChars(int overlapChars) {
            this.overlapChars = overlapChars;
        }
    }

    public static class Embedding {
        private String modelPath;
        private String tokenizerPath;
        private int dimension = 384;
        private int maxSequenceLength = 256;
        private int batchSize = 8;

        public String getModelPath() {
            return modelPath;
        }

        public void setModelPath(String modelPath) {
            this.modelPath = modelPath;
        }

        public String getTokenizerPath() {
            return tokenizerPath;
        }

        public void setTokenizerPath(String tokenizerPath) {
            this.tokenizerPath = tokenizerPath;
        }

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }

        public int getMaxSequenceLength() {
            return maxSequenceLength;
        }

        public void setMaxSequenceLength(int maxSequenceLength) {
            this.maxSequenceLength = maxSequenceLength;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }

    public static class Output {
        private int maxVerbatimChars = 300;

        public int getMaxVerbatimChars() {
            return maxVerbatimChars;
        }

        public void setMaxVerbatimChars(int maxVerbatimChars) {
            this.maxVerbatimChars = maxVerbatimChars;
        }
    }
}
