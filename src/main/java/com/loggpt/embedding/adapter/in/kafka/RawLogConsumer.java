package com.loggpt.embedding.adapter.in.kafka;

import com.loggpt.embedding.domain.model.LogEvent;
import com.loggpt.embedding.domain.port.in.ProcessLogUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RawLogConsumer {

    private final ProcessLogUseCase processLogUseCase;

    @KafkaListener(
            topics = "${kafka.topics.logs-raw}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            ConsumerRecord<String, LogEvent> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        LogEvent logEvent = record.value();

        log.debug("Received log event from topic={} partition={} offset={}: service={} level={}",
                topic, partition, offset, logEvent.service(), logEvent.level());

        processLogUseCase.process(logEvent);
    }
}
