# Munitorum

[![CI](https://github.com/nhenderson22/munitorum/actions/workflows/ci.yml/badge.svg)](https://github.com/nhenderson22/munitorum/actions/workflows/ci.yml)

Munitorum is a workflow orchestrator written in Java 25. It implements a workflow orchestration spec defined by this project, with idempotency and correctness as its goals and a complete workflow story from day one rather than a toolkit to assemble.

> **Status: early development.** The repository currently contains the build, test and container tooling. There is no orchestrator functionality yet.

## How it works

Developers define **litanies** (workflows). The orchestrator runs each one as a **recitation** (a workflow run), dispatching its **rites** (tasks) to **servitors**: worker processes that host them. Rites and litany outputs are versioned and content-addressed, so results are reproducible, verifiable and safe to re-read. Everything is idempotent, so any step can safely run more than once.

The names are Warhammer 40,000, plus a little Frank Zappa, because it's fun. The [executive summary](documentation/design_notes/executive_summary.md) maps each one to a plain term and covers the planned features.

## Built with

- **Java 25**, compiled ahead of time with GraalVM Native Image into a static, musl-linked binary that runs on a distroless base image.
- **PostgreSQL** for persistence and **RabbitMQ** for messaging.
- **A deliberately short dependency list**: no frameworks, no DI container, no runtime reflection. [ADR 2](documentation/architecture_decision_records/0002-dependency-selection.md) explains each choice.

## Getting started

### Prerequisites

- JDK 25
- Podman or Docker, for the integration tests and the local stack

Maven is optional: the Maven Wrapper (`./mvnw`) downloads the pinned version on first use.

Podman users: Testcontainers talks to the Podman socket, so point `DOCKER_HOST` at it (for example `unix:///run/user/$UID/podman/podman.sock`). Rootless setups may also need `TESTCONTAINERS_RYUK_DISABLED=true`.

### Build and test

```sh
./mvnw verify              # unit tests, integration tests and format check
./mvnw verify -DskipITs    # skip the container-backed integration tests
```

### Run locally

```sh
podman compose up -d           # Postgres + RabbitMQ on localhost
./mvnw -q compile exec:java    # run the app on the JVM
```

### Build the native image

```sh
podman build -t munitorum -f Containerfile .
podman run --rm munitorum
```

`docker compose` and `docker build` work the same way.

## Documentation

- [Executive summary](documentation/design_notes/executive_summary.md): what Munitorum is and where it's going
- [Architecture decision records](documentation/architecture_decision_records/README.md): dependencies, code style, AI usage
- [Command reference](documentation/development/useful_commands.md): build, test, schema, compose and Podman commands

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
