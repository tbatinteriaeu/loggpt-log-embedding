package com.loggpt.embedding.domain.model;

import java.time.Instant;

public record LogEvent(
        Instant timestamp,
        String level,
        String service,
        String message,
        String traceId,
        String spanId
) {
}
