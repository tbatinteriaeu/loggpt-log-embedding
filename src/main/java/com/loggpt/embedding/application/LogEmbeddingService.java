package com.loggpt.embedding.application;

import com.loggpt.embedding.domain.model.LogEvent;
import com.loggpt.embedding.domain.port.in.ProcessLogUseCase;
import com.loggpt.embedding.domain.port.out.EmbeddingGenerationPort;
import com.loggpt.embedding.domain.port.out.LogEmbeddingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogEmbeddingService implements ProcessLogUseCase {

    private final EmbeddingGenerationPort embeddingGenerationPort;
    private final LogEmbeddingPort logEmbeddingPort;

    @Override
    public void process(LogEvent logEvent) {
        log.debug("Processing log event: service={} level={}", logEvent.service(), logEvent.level());

        String normalized = normalize(logEvent.message());
        float[] embedding = embeddingGenerationPort.generate(normalized);
        logEmbeddingPort.store(logEvent, embedding);
    }

    private String normalize(String message) {
        if (message == null) return "";
        // Strip ANSI escape codes (common in raw log output)
        return message.replaceAll("\\u001B\\[[;\\d]*m", "").trim();
    }
}
