# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`loggpt-log-embedding` is a Spring Boot module of the **LogGPT** project. It consumes log events from a Kafka
topic, generates vector embeddings for the log message via an AI embedding model (OpenAI, through Spring AI),
and stores the log + embedding in a vector database (Qdrant) for semantic search. It is one module in a larger
multi-repo project (API, frontend, chat modules live elsewhere).

Pipeline: `Kafka (logs.raw)` → consumer → normalize/embed → `Qdrant`.

## Commands

Build (Maven wrapper is not present; use system `mvn`):
```bash
mvn compile
```

Run all tests:
```bash
mvn test
```

Run a single test class:
```bash
mvn test -Dtest=EmbeddingExampleTest
```

Run a single test method:
```bash
mvn test -Dtest=EmbeddingExampleTest#embedSingleText_returnsVectorFromModel
```

Run the app locally:
```bash
mvn spring-boot:run
```

Requires Java 25 and a running Kafka broker (`KAFKA_BOOTSTRAP_SERVERS`, default `localhost:9092`) plus an
`OPENAI_API_KEY` env var — the app fails to start without an OpenAI API key since the `EmbeddingModel` bean
is created eagerly.

`RawLogConsumerIntegrationTest` uses Testcontainers to spin up a real Kafka broker (`confluentinc/cp-kafka:7.6.0`),
so Docker must be running for `mvn test` to pass.

## Architecture

The code follows **hexagonal / ports-and-adapters architecture**, under `com.loggpt.embedding`:

- `domain/model` — plain domain types, no framework dependencies. `LogEvent` is the core record
  (timestamp, level, service, message, traceId, spanId).
- `domain/port/in` — inbound use-case interfaces the application exposes (`ProcessLogUseCase`).
- `domain/port/out` — outbound interfaces the application depends on, implemented by adapters
  (`EmbeddingGenerationPort` for generating embeddings, `LogEmbeddingPort` for persisting them).
- `application` — use-case implementations that orchestrate ports. `LogEmbeddingService` implements
  `ProcessLogUseCase`: normalizes the log message (currently just strips ANSI escape codes), calls
  `EmbeddingGenerationPort.generate`, then `LogEmbeddingPort.store`.
- `adapter/in/kafka` — inbound adapter. `RawLogConsumer` is the `@KafkaListener` that reads `LogEvent`
  messages off the `kafka.topics.logs-raw` topic and calls into `ProcessLogUseCase`. `KafkaConsumerConfig`
  wires a `JsonDeserializer<LogEvent>`-based consumer factory (trusts all packages for deserialization).
- `adapter/out/openai` — `OpenAiEmbeddingAdapter` implements `EmbeddingGenerationPort` using Spring AI's
  `EmbeddingModel`.
- `adapter/out/qdrant` — `QdrantEmbeddingAdapter` implements `LogEmbeddingPort`. **Currently a stub** — it
  only logs the log event instead of writing to Qdrant; actual Qdrant persistence is not yet implemented.
- `example` — `EmbeddingExample` is a standalone component demonstrating Spring AI `EmbeddingModel` usage
  patterns (single embed, batch embed, cosine similarity, nearest-match search). Not part of the production
  pipeline; kept as a reference/demo.

When adding new outbound integrations (e.g. a different vector DB or embedding provider), add a new adapter
implementing the relevant `domain/port/out` interface rather than changing `application` or `domain` code.

## Configuration

`src/main/resources/application.yml` — Kafka bootstrap servers, consumer group, OpenAI model
(`text-embedding-3-small`), and the `kafka.topics.logs-raw` topic name. `src/test/resources/application-test.yml`
overrides Kafka settings for tests (embedded broker, `*-test` consumer group).

## Project status

This is an early-stage pipeline. Per the README roadmap, the following are explicitly **not yet implemented**:
chunking strategy for multi-line/stack-trace logs, metadata filtering, hybrid (lexical + vector) search, a
`/search` retrieval API, a relevance evaluation harness (golden set / recall@k / MRR), retry/DLQ handling,
idempotency, batching, rate limiting, and observability metrics. The actual Qdrant write (`QdrantEmbeddingAdapter`)
is a TODO stub. Keep this in mind when asked to build on or evaluate retrieval/search quality — the underlying
storage and search capability doesn't exist yet.
