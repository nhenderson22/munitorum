# 2. Dependency selection

Date: 2026-11-08

## Status

Accepted

## Context

The goal is to keep the dependency list as short as possible. The more self-contained this codebase is, the better it is for:

- **Reliability** — fewer moving parts I don't control, fewer surprise breakages on upgrade.
- **Security** — a smaller supply-chain surface to audit and patch.
- **Maintainability** — anyone reading the repo can understand the whole of it.

I also want as little magic as possible. I value readability, locality of state and control flow, and being able to hold a complete understanding of the repo in my head.

## Decision

A dependency has to earn its place. It should provide something that is both hard to implement correctly **and** too much work to maintain myself. If it fails either test, I write it.

In practice that means:

- No runtime reflection, classpath scanning, or proxy generation.
- No dependency-injection container. Everything is wired explicitly in `main`.
- Configuration is plain Java, compiled into the binary, not YAML/properties files interpreted at runtime (see [Configuration](#configuration)).
- Anything that generates code must do so at compile time, as readable source I can inspect.

### Language & Runtime

- **Java 25.** Modern Java (records, pattern matching, sealed types, virtual threads) covers most of what used to require a library.
- **The JDK first.** Before adding a dependency, check whether the standard library already does it. For example, HTTP is handled with the JDK's built-in `java.net.http.HttpClient`.

### Build Tooling

- **Maven** owns the build lifecycle. The Maven Wrapper (`./mvnw`) pins the Maven version, so every build uses the same one.
- **GraalVM Native Image** is the release target. The engine ships as an ahead-of-time compiled native binary, not a JVM application.
- **Statically linked against musl** (`--static --libc=musl`), so the binary has no runtime dependency on the host's libc.
- **Distroless base container.** Because the binary is fully static, the runtime image needs nothing beyond the binary itself (e.g. `gcr.io/distroless/static`). No shell, no package manager, minimal attack surface.

### Testing

A standard, boring stack:

- **JUnit 6** — test runner.
- **AssertJ** — fluent, readable assertions.
- **Testcontainers** — integration tests against real Postgres and RabbitMQ instances rather than mocks.
- **slf4j-simple** — a logging backend for Testcontainers, so its output (image pulls, container start-up) shows up in test runs.

I run Podman locally. Testcontainers only needs an OCI-compatible, Docker-API-compatible runtime, so Docker works just as well for other contributors. (Podman users will need the Podman socket exposed via `DOCKER_HOST`, and may need to disable Ryuk with `TESTCONTAINERS_RYUK_DISABLED=true` if running rootless.)

These are test-scope only and never ship in the binary.

### Libraries

#### RecordBuilder

[RecordBuilder](https://github.com/Randgalt/record-builder) generates builders (and related helpers like withers) for Java records via an annotation processor.

Why it passes the bar:

- **It generates real source code** at compile time. Unlike Lombok, it does not hook into the compiler or modify the AST. The output is ordinary Java I can open and read.
- It's compile-time only, so it adds nothing at runtime and has no impact on native image.
- Writing and maintaining builders for every record by hand is tedious, error-prone boilerplate.

#### avaje-jsonb

[avaje-jsonb](https://avaje.io/jsonb/) handles JSON serialization and deserialization. Annotating a record with `@Json` generates a plain Java adapter for it at compile time.

Why it passes the bar:

- **No runtime reflection.** Adapters are generated source, so serialization logic is readable and debuggable like any other code in the repo.
- **Native-image friendly by construction.** There is no reflection configuration to maintain, unlike Jackson or Jakarta JSON-B (Yasson).
- A correct, fast JSON parser and writer is real work to build and maintain, and JSON sits on the hot path (workflow templates, HTTP payloads).

### Database Schema

No migration tool. Until Munitorum has deployments holding data that must be kept, every database (tests, CI, local) is created fresh from the SQL files in `src/main/sql`, and the schema is edited in place.

- The official Postgres image applies those files when it creates a database (`/docker-entrypoint-initdb.d`). Compose mounts the directory; tests copy it into the Testcontainers database.
- Migrations only matter once there's data to preserve, so choosing how to do them is deferred to the first release.

### Configuration

Configuration is plain Java, compiled into the binary. There are no config files to parse and no config library.

- **Changing configuration means a rebuild and a redeploy.** A config change is a deployment like any other, so it goes through the same augury check before it rolls out.
- **Secrets are the exception.** Database passwords and other credentials are never compiled in. They are supplied from outside at runtime, so they stay out of the image and can be rotated without a rebuild.

### HTTP

HTTP needs no dependency.

- **Outbound:** the JDK's `java.net.http.HttpClient`.
- **Inbound:** the JDK's `com.sun.net.httpserver.HttpServer` (`jdk.httpserver` module), run on a virtual-thread executor. That covers the engine's API and the UI's backend.

HTTP is used only where a request/response API makes sense, such as the UI, CLI and admin operations. Engine and worker communication goes through RabbitMQ.

**gRPC is not used.** It would bring protobuf code generation and a large transitive dependency tree. That cost buys nothing here, because the asynchronous, high-volume traffic already runs over RabbitMQ and the remaining HTTP traffic is low-volume JSON.

### External Integration Dependencies

These exist because the system talks to external services, and reimplementing their wire protocols would be unreasonable.

- **RabbitMQ Java client** — messaging backbone for the fully asynchronous engine.
- **PostgreSQL JDBC driver** — persistence. Storage sits behind an interface, with both a plain Postgres implementation and a distributed (Citus) implementation; both use the same driver.

### Deliberately Not Used

Written down so the reasoning doesn't get relitigated:

| Not using | Instead | Why |
|---|---|---|
| Spring / DI frameworks | Explicit wiring in `main` | Proxies, classpath scanning, runtime magic, memory overhead, poor native-image fit |
| Lombok | RecordBuilder + records | Lombok manipulates the compiler AST; RecordBuilder emits readable source |
| Apache/OkHttp clients | JDK `HttpClient` | The standard library is sufficient |
| Web frameworks (Javalin, Helidon, etc.) | JDK `HttpServer` + virtual threads | The standard library is sufficient for a small API |
| gRPC | RabbitMQ + JSON over HTTP | Codegen and a heavy dependency tree, for traffic that doesn't need it |
| Jackson / Jakarta JSON-B | avaje-jsonb | Reflection-based; needs native-image reflection config |
| Logging frameworks (Logback, Log4j) | In-house structured JSON logger | Small, fully understood, no config-file magic |
| YAML/properties config libraries | Config as plain Java | Type-checked, debuggable, no runtime interpretation |

### Adding a New Dependency

Before adding anything, answer in the PR:

1. What does it do that the JDK doesn't?
2. Why is it too hard or too costly to implement and maintain ourselves?
3. Does it use runtime reflection or proxies, and does it work under GraalVM native image?
4. What does it pull in transitively?

## Consequences

- The native-image target reinforces the no-magic rule: reflection- and proxy-heavy libraries are painful under GraalVM, so anything added here must work cleanly with closed-world AOT compilation.
- Wiring, configuration and logging are written and maintained in this repository rather than taken from a framework.
- Every new dependency carries the cost of answering the four questions above, which is the point: adding one is a deliberate act.
