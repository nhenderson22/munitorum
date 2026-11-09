# 1. Record architecture decisions

Date: 2026-11-08

## Status

Accepted

## Context

Decisions about how Munitorum is built (which dependencies it uses, how code is structured, how AI tools are used) need to be written down together with the reasoning behind them. Otherwise the reasoning is lost, and the same questions get relitigated.

## Decision

Architecture decisions are recorded as Architecture Decision Records (ADRs), in the format described by Michael Nygard in [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

- Each ADR is a file in this directory named `NNNN-short-title.md`. Numbers are assigned in order and never reused.
- Each ADR has a date, a status, and three sections: **Context**, **Decision** and **Consequences**. New ADRs start from [template.md](template.md).
- The status is one of **Proposed**, **Accepted**, **Deprecated**, or **Superseded by** a later ADR.
- An accepted ADR is not rewritten when the decision changes. A new ADR records the new decision, and the old one's status becomes "Superseded by NNNN". Correcting typos, links or factual errors in the write-up is fine.
- [README.md](README.md) indexes every ADR with its status.

## Consequences

- The reasoning behind each decision lives in the repository, next to the code it shapes.
- Changing a decision takes a new ADR, so changes are deliberate and visible in the history.
- The index has to be kept in step with the ADRs.
