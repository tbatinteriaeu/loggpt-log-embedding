package com.loggpt.embedding.domain.port.in;

import com.loggpt.embedding.domain.model.LogEvent;

public interface ProcessLogUseCase {

    void process(LogEvent logEvent);
}
