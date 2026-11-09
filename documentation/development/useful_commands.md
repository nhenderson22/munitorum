# Command Reference

All Maven commands go through the Maven Wrapper (`./mvnw`), which downloads and uses the pinned Maven version. A system `mvn` 3.9+ works too.

## Build & Test

```sh
./mvnw compile                                  # compile only (runs RecordBuilder + avaje-jsonb processors)
./mvnw test                                     # unit tests, no containers
./mvnw verify                                   # unit + integration tests + format check
./mvnw verify -DskipITs                         # verify without the container tests
./mvnw clean verify                             # from scratch (use after annotation-processor weirdness)
```

The build fails on any compiler warning (`-Xlint:all` with `failOnWarning`), and on the wrong JDK or Maven version (maven-enforcer-plugin).

### Running specific tests

```sh
./mvnw test -Dtest=AppTest                      # one unit test class
./mvnw test -Dtest='AppTest#should*'            # matching methods in one class
./mvnw verify -Dit.test=ContainerSmokeIT -Dtest=skip -Dsurefire.failIfNoSpecifiedTests=false   # one integration test only
```

## Formatting

```sh
./mvnw spotless:apply                           # format everything
./mvnw spotless:check                           # check without changing anything
```

## Database Schema

The schema lives in `src/main/sql` (see the [README there](../../src/main/sql/README.md)) and local seed data in [`dev/seed`](../../dev/seed/README.md). The local database is throwaway: it lives in memory, and every start creates it fresh with the schema and then the seed data. There are no migrations yet: change the schema in place and restart.

```sh
podman compose down && podman compose up -d     # fresh database with the current schema + seed data
podman compose logs postgres                     # which schema or seed file failed, and where
./mvnw verify -Dit.test=SchemaIT -Dtest=skip -Dsurefire.failIfNoSpecifiedTests=false   # check the schema applies
```

In integration tests, use `TestDatabase.withSchema()` for a Postgres container with the schema applied (no seed data).

## Running Locally

```sh
podman compose up -d                            # start Postgres (fresh, schema + seed) + RabbitMQ
./mvnw -q compile exec:java                     # run on the JVM against them
```

Or run `App` from the IDE. That gives the fastest restarts and a debugger.

## Native Image

```sh
podman build -t munitorum -f Containerfile .    # static native binary in a distroless image
podman run --rm munitorum                       # run it standalone
podman images munitorum                         # check image size
podman compose --profile app up -d --build      # build + run it against the compose stack
```

## Compose Stack

```sh
podman compose up -d                            # start infra
podman compose ps                               # status + health
podman compose logs -f rabbitmq                 # follow one service's logs
podman compose down                             # stop; nothing is kept
```

Ports are published on `127.0.0.1` only. Nothing in the stack persists: Postgres keeps its data in memory and RabbitMQ has no volume, so every `up` after a `down` starts clean.

### Poking at the services

```sh
podman compose exec postgres psql -U munitorum          # SQL shell
podman compose exec rabbitmq rabbitmqctl list_queues name messages consumers
podman compose exec rabbitmq rabbitmqctl list_exchanges
```

RabbitMQ management UI: http://localhost:15672 (`munitorum` / `munitorum`)

## Dependencies

```sh
./mvnw dependency:tree                          # full tree
./mvnw dependency:tree -Dincludes=org.slf4j     # who pulls in a specific artifact
./mvnw versions:display-dependency-updates      # newer dependency versions
./mvnw versions:display-plugin-updates          # newer plugin versions
```

Dependabot opens weekly PRs for Maven dependencies and plugins, the Maven Wrapper, GitHub Actions and the Containerfile base images (see `.github/dependabot.yml`). The compose and Testcontainers images are bumped by hand, together.

## Podman Housekeeping

```sh
systemctl --user status podman.socket           # is the API socket up (Testcontainers needs it)
podman ps -a                                    # all containers, including exited
podman ps -a --filter label=org.testcontainers=true -q | xargs -r podman rm -f   # clean up leftover test containers (Ryuk is off)
podman volume ls                                # volumes
podman image prune                              # remove dangling images (old native builds pile up)
podman system df                                # disk usage
```
