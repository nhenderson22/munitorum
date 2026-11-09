# Munitorum: Executive Summary

## Overview

Munitorum is a Java 25 workflow orchestrator. It implements a workflow orchestration spec defined by this project. The implementation is in Java, but it is not an orchestrator that exists only for Java workloads. It offers a structured, opinionated way to define, deploy, and maintain distributed workflows, and it is built to scale out of the box with as little effort from the developer as possible.

The goals are **idempotency** and **correctness**, with a complete and coherent workflow story from day one rather than a toolkit that must be assembled.

> **About the names:** the domain is named after Warhammer 40,000, with a couple of Frank Zappa references, because it's fun. Every name has a plain term next to it in the tables below.

## Core Principle: Idempotency

Munitorum assumes idempotency rather than trying to prevent duplicates.

- Work may be delivered more than once (RabbitMQ delivers at least once). That's harmless by design: the orchestrator's own operations are idempotent, and every rite must be too.
- Munitorum doesn't attempt exactly-once delivery or deduplicate work.
- Because repeating a step is safe, a failure can always be recovered by retrying it.

## Core Concepts

| Term | Plain term | Meaning |
|------|------------|---------|
| **Litany** | Workflow | The unit of work the orchestrator operates on. Developers write litany definitions. Each litany produces a versioned, content-addressed output that can be read. |
| **Recitation** | Workflow run | A runtime instance the orchestrator creates from a litany definition. Work is dispatched based on these definitions. |
| **Rite** | Task | A versioned, content-addressed definition of executable logic, produced by the forge. Developers register rites with the forge. |
| **Servitor** | Worker | A separate process that hosts one or more rites, assembled by the forge. Servitors are the execution nodes of the ecosystem. |

## Orchestration Paradigm

1. Developers define litanies.
2. The orchestrator starts recitations from those definitions and dispatches work to servitors that host the matching rites.
3. Routing is similar in spirit to AWS Step Functions, but transitions aren't written in an expression DSL such as JSONata. The next step is chosen from a rite's output state, and the execution model forces a litany to account for every state its rites can produce. The details will live in the spec.
4. Every litany output is versioned and content-addressed, which makes results reproducible, verifiable, and safe to re-read.
5. Deployed servitors are monitored continuously. This data feeds build and rollout tooling.
6. Before a rollout or teardown, the tooling checks in-flight recitations against the rites they need and reports which servitors can be taken down safely and which cannot.

## Other Names

| Name | Plain term | What it is |
|------|------------|------------|
| **Forge** (`muni forge`) | Build tool | Bundles rites into servitors |
| **Augury** (`muni augury`) | Deployment safety check | Reports which servitors can be retired without breaking in-flight recitations |
| **Reliquary** | Output store | Content-addressed store of litany outputs |
| **Auspex** | Telemetry | Server and operational metrics |
| **Augmetics** | Plugins | Extension points for storage and transport implementations |
| **Central Scrutinizer** | UI | The web frontend, in its own repository |
| **Joe's Garage** (`joes-garage`) | Sample project | An example project built on Munitorum (planned) |
| **TortureNeverStops** | Unlimited retries | The retry policy that never gives up |

Central Scrutinizer and Joe's Garage are from Frank Zappa's *Joe's Garage* (1979); *The Torture Never Stops* is from *Zoot Allures* (1976). Everything else is Warhammer 40,000.

## Planned Features

### Structured Logging
All logs are emitted as flat JSON objects. The logger is a custom implementation built on the stripped-down dependency set defined in [ADR 2: Dependency selection](../architecture_decision_records/0002-dependency-selection.md).

### `muni`: CLI Tool
The CLI provides build functionality for the ecosystem. It lets developers:
- Bundle rites into new servitors (`muni forge`).
- Check whether a planned deployment of servitors is safe given the recitations currently in flight (`muni augury`).

### Auspex: Integrated Telemetry
Server and operational metrics are exposed in a lightweight, easily configurable way and displayed directly in the UI. Telemetry is kept as small as possible and reports over the existing transport.

### UI (`central-scrutinizer`)
A separate repository, `central-scrutinizer`, provides the frontend. It is an htmx server with Alpine.js that consumes the spec's public-facing metrics API. It gives a visual interface to the build tooling exposed by the CLI, alongside the telemetry views.

### Augmetics: Plugin Friendliness
Extension points let side effects and outside behavior be plugged in, so Munitorum can run on different database and transport implementations.

## Related Documents

- [ADR 2: Dependency selection](../architecture_decision_records/0002-dependency-selection.md) (governs logger dependencies)
- `central-scrutinizer` repository (UI, not yet public)
