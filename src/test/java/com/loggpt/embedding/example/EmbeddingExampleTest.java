package com.loggpt.embedding.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmbeddingExample — all OpenAI calls are mocked so no API key is needed.
 */
class EmbeddingExampleTest {

    private EmbeddingModel embeddingModel;
    private EmbeddingExample example;

    @BeforeEach
    void setUp() {
        embeddingModel = mock(EmbeddingModel.class);
        example = new EmbeddingExample(embeddingModel);
    }

    // --- embedSingleText ---

    @Test
    void embedSingleText_returnsVectorFromModel() {
        float[] vector = {0.1f, 0.2f, 0.3f};
        when(embeddingModel.embed(anyString())).thenReturn(vector);

        float[] result = example.embedSingleText("NullPointerException in UserService");

        assertThat(result).isEqualTo(vector);
        verify(embeddingModel).embed("NullPointerException in UserService");
    }

    @Test
    void embedSingleText_propagatesModelDimensions() {
        when(embeddingModel.embed(anyString())).thenReturn(new float[1536]);

        float[] result = example.embedSingleText("any log line");

        assertThat(result).hasSize(1536);
    }

    // --- embedBatch ---

    @Test
    void embedBatch_returnsOneVectorPerInput() {
        float[] v1 = {0.1f, 0.2f};
        float[] v2 = {0.3f, 0.4f};
        EmbeddingResponse response = mockEmbeddingResponse(v1, v2);
        when(embeddingModel.call(any())).thenReturn(response);

        List<float[]> results = example.embedBatch(List.of("log line A", "log line B"));

        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo(v1);
        assertThat(results.get(1)).isEqualTo(v2);
    }

    @Test
    void embedBatch_singleItemBatch() {
        float[] v = {0.5f, 0.6f};
        EmbeddingResponse response = mockEmbeddingResponse(v);
        when(embeddingModel.call(any())).thenReturn(response);

        List<float[]> results = example.embedBatch(List.of("only one log"));

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(v);
    }

    // --- cosineSimilarity (via EmbeddingModel) ---

    @Test
    void cosineSimilarity_identicalTextsReturnNearOne() {
        float[] v = {1f, 0f, 0f};
        when(embeddingModel.embed(anyString())).thenReturn(v);

        double score = example.cosineSimilarity("same text", "same text");

        assertThat(score).isCloseTo(1.0, within(1e-6));
    }

    @Test
    void cosineSimilarity_orthogonalVectorsReturnZero() {
        when(embeddingModel.embed("textA")).thenReturn(new float[]{1f, 0f});
        when(embeddingModel.embed("textB")).thenReturn(new float[]{0f, 1f});

        double score = example.cosineSimilarity("textA", "textB");

        assertThat(score).isCloseTo(0.0, within(1e-6));
    }

    @Test
    void cosineSimilarity_oppositeVectorsReturnNearMinusOne() {
        when(embeddingModel.embed("pos")).thenReturn(new float[]{1f, 0f});
        when(embeddingModel.embed("neg")).thenReturn(new float[]{-1f, 0f});

        double score = example.cosineSimilarity("pos", "neg");

        assertThat(score).isCloseTo(-1.0, within(1e-6));
    }

    // --- static helper ---

    @Test
    void staticCosineSimilarity_zeroVectorReturnsZero() {
        float[] zero = {0f, 0f};
        float[] other = {1f, 1f};

        double score = EmbeddingExample.cosineSimilarity(zero, other);

        assertThat(score).isEqualTo(0.0);
    }

    // --- findMostSimilar ---

    @Test
    void findMostSimilar_returnsClosestCandidate() {
        // query is close to candidate B
        when(embeddingModel.embed("query")).thenReturn(new float[]{1f, 0f});
        when(embeddingModel.embed("candidate A")).thenReturn(new float[]{0f, 1f}); // orthogonal
        when(embeddingModel.embed("candidate B")).thenReturn(new float[]{1f, 0f}); // identical direction

        String best = example.findMostSimilar("query", List.of("candidate A", "candidate B"));

        assertThat(best).isEqualTo("candidate B");
    }

    @Test
    void findMostSimilar_singleCandidateAlwaysWins() {
        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.5f, 0.5f});

        String best = example.findMostSimilar("any query", List.of("only option"));

        assertThat(best).isEqualTo("only option");
    }

    // --- helpers ---

    private EmbeddingResponse mockEmbeddingResponse(float[]... vectors) {
        List<Embedding> embeddings = java.util.stream.IntStream.range(0, vectors.length)
                .mapToObj(i -> new Embedding(vectors[i], i))
                .toList();
        EmbeddingResponse response = mock(EmbeddingResponse.class);
        when(response.getResults()).thenReturn(embeddings);
        return response;
    }
}
