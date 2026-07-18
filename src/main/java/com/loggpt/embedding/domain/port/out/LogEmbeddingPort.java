package com.loggpt.embedding.domain.port.out;

import com.loggpt.embedding.domain.model.LogEvent;

public interface LogEmbeddingPort {

    void store(LogEvent logEvent, float[] embedding);
}
