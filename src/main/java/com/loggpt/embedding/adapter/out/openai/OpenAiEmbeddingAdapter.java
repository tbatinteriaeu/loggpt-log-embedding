package com.loggpt.embedding.adapter.out.openai;

import com.loggpt.embedding.domain.port.out.EmbeddingGenerationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiEmbeddingAdapter implements EmbeddingGenerationPort {

    private final EmbeddingModel embeddingModel;

    @Override
    public float[] generate(String text) {
        log.debug("Generating embedding for text of length={}", text.length());
        float[] embedding = embeddingModel.embed(text);
        log.debug("Generated embedding with dimensions={}", embedding.length);
        return embedding;
    }
}
