# 3. Code style

Date: 2026-11-08

## Status

Accepted

## Context

Formatting and naming need a single standard that a tool can enforce, so style isn't argued about in review. Beyond formatting, the codebase needs one consistent approach to modelling the domain, handling state and side effects, and concurrency.

## Decision

### Baseline

Formatting and naming follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

### Philosophy

This codebase does not take a traditional object-oriented approach. It is **data-oriented**: data is modelled as plain, immutable values, and behaviour lives in functions that operate on that data rather than in objects that hide it.

### Domain Modelling

- **Sealed interfaces implemented by records.** These are the primary modelling tool. Expect heavy use of `switch` with pattern matching over sealed hierarchies, so the compiler checks that every case is handled.
- **Make illegal states unrepresentable.** Domain models expose entry points (constructors and factory methods) that validate their inputs, so an invalid value cannot be built in the first place.
- **Classes only where encapsulation is needed.** A class is used when it genuinely has to own and protect state, not by default.

### Functions and State

- **Pure functions by default.** Data is passed into and returned from pure functions. Side effects are kept out of the core logic.
- **Ports and adapters at the edges.** Mutations against external services (databases, queues, HTTP APIs) happen in imperative code at the edges, never in the core logic.
  - An **adapter** is anything that talks to external storage or an out-of-process dependency. It maps data into the domain, and that's it: no business logic.
  - Ports and adapters are added where multiple implementations are needed.
  - Ports are defined in terms of the **domain**, not the operation. A port says what the domain needs, not how a particular service does it.

### Errors and Absence

- **Sealed result types for expected failures.** An operation that can fail in an expected way returns a sealed result, with a record for success and one for each failure, and callers handle every case with an exhaustive `switch`.
- **Exceptions are avoided as much as possible.**
- **No nulls.** Absent values are modelled explicitly, and constructors reject null.

### Concurrency

- **Asynchronous where it makes sense**, not everywhere.
- **Virtual threads** are the default way to get concurrency. Blocking-style code on virtual threads is preferred over callback- or reactive-style code.

### Wiring

All wiring is explicit in `main`. There is no dependency-injection container (see [ADR 2: Dependency selection](0002-dependency-selection.md)).

## Consequences

- Formatting is enforced by Spotless running google-java-format: `./mvnw verify` fails on a violation, and `./mvnw spotless:apply` fixes it.
- Because sealed hierarchies are matched exhaustively, adding a case to a sealed interface is a compile error at every `switch` that doesn't handle it yet. The same goes for failures: a new failure case in a result type can't be silently ignored.
