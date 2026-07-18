package com.loggpt.embedding.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates common usages of Spring AI's EmbeddingModel (backed by OpenAI).
 *
 * All methods are intentionally standalone so they can be called independently
 * from tests or a CommandLineRunner.
 */
@Slf4j
@RequiredArgsConstructor
public class EmbeddingExample {

    private final EmbeddingModel embeddingModel;

    /**
     * Embed a single piece of text and return the raw float vector.
     */
    public float[] embedSingleText(String text) {
        float[] vector = embeddingModel.embed(text);
        log.info("Embedded '{}' -> {} dimensions", text, vector.length);
        return vector;
    }

    /**
     * Embed multiple texts in a single batched API call.
     * More efficient than calling embed() in a loop.
     */
    public List<float[]> embedBatch(List<String> texts) {
        EmbeddingRequest request = new EmbeddingRequest(texts, null);
        EmbeddingResponse response = embeddingModel.call(request);
        List<float[]> vectors = response.getResults().stream()
                .map(e -> e.getOutput())
                .toList();
        log.info("Batch embedded {} texts", vectors.size());
        return vectors;
    }

    /**
     * Compute cosine similarity between two texts.
     * Returns a value in [-1, 1]; closer to 1 means more similar.
     */
    public double cosineSimilarity(String textA, String textB) {
        float[] a = embeddingModel.embed(textA);
        float[] b = embeddingModel.embed(textB);
        return cosineSimilarity(a, b);
    }

    /**
     * Find the most semantically similar text to a query from a list of candidates.
     */
    public String findMostSimilar(String query, List<String> candidates) {
        float[] queryVec = embeddingModel.embed(query);
        String best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (String candidate : candidates) {
            double score = cosineSimilarity(queryVec, embeddingModel.embed(candidate));
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        log.info("Best match for '{}': '{}' (score={})", query, best, bestScore);
        return best;
    }

    // --- helpers ---

    public static double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}