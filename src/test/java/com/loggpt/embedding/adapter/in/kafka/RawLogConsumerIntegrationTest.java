package com.loggpt.embedding.adapter.in.kafka;

import com.loggpt.embedding.domain.model.LogEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
class RawLogConsumerIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JsonSerializer");
        registry.add("spring.kafka.producer.properties.spring.json.add.type.headers", () -> "false");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private RawLogConsumer rawLogConsumer;

    @Value("${kafka.topics.logs-raw}")
    private String logsRawTopic;

    @Test
    void shouldConsumeLogEventFromLogsRawTopic() {
        LogEvent event = new LogEvent(
                Instant.now(), "INFO", "order-service", "Order placed successfully", "trace-001", "span-001");

        kafkaTemplate.send(logsRawTopic, event);

        verify(rawLogConsumer, timeout(10_000).times(1))
                .consume(any(ConsumerRecord.class), anyString(), anyInt(), anyLong());
    }

    @Test
    void shouldConsumeMultipleLogEvents() {
        int messageCount = 3;
        for (int i = 0; i < messageCount; i++) {
            LogEvent event = new LogEvent(
                    Instant.now(), "ERROR", "payment-service", "Payment failed, attempt " + i,
                    "trace-" + i, null);
            kafkaTemplate.send(logsRawTopic, event);
        }

        verify(rawLogConsumer, timeout(15_000).times(messageCount))
                .consume(any(ConsumerRecord.class), anyString(), anyInt(), anyLong());
    }

    @Test
    void shouldConsumeLogEventsOfDifferentLevels() {
        kafkaTemplate.send(logsRawTopic,
                new LogEvent(Instant.now(), "DEBUG", "auth-service", "Token validated", "t1", "s1"));
        kafkaTemplate.send(logsRawTopic,
                new LogEvent(Instant.now(), "WARN",  "auth-service", "Token expiring soon", "t2", "s2"));
        kafkaTemplate.send(logsRawTopic,
                new LogEvent(Instant.now(), "ERROR", "auth-service", "Token invalid", "t3", "s3"));

        verify(rawLogConsumer, timeout(15_000).times(3))
                .consume(any(ConsumerRecord.class), anyString(), anyInt(), anyLong());
    }
}
