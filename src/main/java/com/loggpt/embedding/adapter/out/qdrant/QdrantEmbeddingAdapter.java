package com.loggpt.embedding.adapter.out.qdrant;

import com.loggpt.embedding.domain.model.LogEvent;
import com.loggpt.embedding.domain.port.out.LogEmbeddingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QdrantEmbeddingAdapter implements LogEmbeddingPort {

    @Override
    public void store(LogEvent logEvent, float[] embedding) {
        // TODO: persist logEvent + embedding vector into Qdrant
        log.info("[{}] {} - {}", logEvent.level(), logEvent.service(), logEvent.message());
    }
}
