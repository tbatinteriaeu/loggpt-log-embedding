# loggpt-log-embedding

**Java-based pipeline that consumes logs from Kafka, generates AI embeddings, and stores them in a vector database for semantic search and AI-powered analysis.**

---

## Overview

`loggpt-log-embedding` is a core module of the **LogGPT** project. Its purpose is to:

1. Consume application logs from Kafka topics.
2. Normalize and preprocess logs.
3. Generate embeddings for each log entry using an AI model.
4. Store the embeddings and log metadata in a vector database (Qdrant).
5. Provide a semantic search foundation for AI-powered log analysis and chat-style queries.

This pipeline allows developers and DevOps teams to ask natural language questions about logs and retrieve semantically relevant results.

---

## Architecture

     ┌────────────┐
     │  Kafka     │
     │  logs.raw  │
     └─────┬──────┘
           │ Consumer
           ▼
    ┌────────────────┐
    │  Log Embedding │
    │  Pipeline      │
    │  (Java + AI)   │
    └───────┬────────┘
            │
            ▼
        ┌────────────┐
        │  Qdrant    │
        │  Vector DB │
        └────────────┘


---

## Features

- **Kafka Consumer:** Real-time ingestion of logs from Kafka topics.
- **Preprocessing:** Normalization and cleaning of log messages.
- **AI Embeddings:** Converts log text into vector representations for semantic similarity.
- **Vector DB Storage:** Stores embeddings and log metadata in Qdrant.
- **Scalable and Modular:** Designed to integrate easily with other LogGPT modules (API, frontend, chat).

---

## Tech Stack

- **Language:** Java 25
- **Framework:** Spring Boot
- **Streaming:** Apache Kafka
- **Vector Database:** Qdrant
- **AI:** OpenAI embeddings (or other compatible embedding provider)
- **Containerization:** Docker & Docker Compose

---

## Getting Started

### Prerequisites
- Java 25
- Docker & Docker Compose
- Kafka running (via Docker Compose)
- Qdrant running (via Docker Compose)
- API key for embedding provider (OpenAI / other)

### Setup

1. Clone the repository:
```bash
    git clone https://github.com/tbatinterieu/loggpt-log-embedding.git
    cd loggpt-log-embedding
```

## Roadmap / TODO

### Retrieval
- [ ] **Chunking strategy** — decide how log entries are split (per entry vs. time-windowed groups vs. stack-trace aware). Multi-line stack traces must not be fragmented.
- [ ] **Metadata filtering** — index and filter by `service`, `level`, `timestamp`, `traceId`, `environment`. Vector similarity alone is not enough for log search.
- [ ] **Hybrid search** — combine lexical (BM25) and vector retrieval. Exact matches (error codes, IDs, class names) must not be lost to semantic similarity.
- [ ] **Retrieval API** — expose `/search` endpoint returning top-k results with scores and metadata.

### Search Quality & Relevance
- [ ] **Golden set** — build a fixed set of 20–30 test queries with expected relevant log entries (`src/test/resources/golden-set.json`).
- [ ] **Evaluation harness** — automated test computing `recall@k` and `MRR` against the golden set. Single number, runs in CI.
- [ ] **Baseline** — record initial scores before any tuning. No tuning without a baseline.
- [ ] **Relevance tuning** — iterate on chunk size, embedding model, and hybrid weighting; measure each change against the golden set.
- [ ] **Ranking strategy** — evaluate boosting by recency, log level, and service relevance.

### Abstraction & Portability
- [ ] **Spring AI `VectorStore`** — abstract the vector database behind Spring AI so Qdrant / OpenSearch / pgvector are a configuration switch, not a rewrite.
- [ ] **Spring AI `EmbeddingModel`** — same for the embedding provider (OpenAI / Ollama / Azure OpenAI).
- [ ] **OpenSearch backend** — add as an alternative to Qdrant to enable hybrid search and enterprise-grade relevance tuning.

### Resilience & Operations
- [ ] **Retry / DLQ** — embedding provider failures must not stall the consumer. Retry topics + dead letter queue.
- [ ] **Idempotency** — re-processing the same log entry must not duplicate vectors.
- [ ] **Batching** — batch embedding calls to reduce cost and latency.
- [ ] **Rate limiting & cost control** — track tokens and spend per run.
- [ ] **Observability** — metrics for ingestion lag, embedding latency, failure rate.

### RAG Layer (next module)
- [ ] **Prompt assembly** — inject retrieved logs as context with token budget management.
- [ ] **Answer grounding** — cite the source log entries in the response; no ungrounded answers.
- [ ] **Answer evaluation** — extend the golden set to measure answer quality, not just retrieval quality.