package com.loggpt.embedding.domain.port.out;

public interface EmbeddingGenerationPort {

    float[] generate(String text);
}
