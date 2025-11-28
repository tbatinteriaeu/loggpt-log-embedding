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