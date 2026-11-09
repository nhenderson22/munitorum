# Database schema

The schema lives in `schema.sql`. Until Munitorum has deployments holding data that must be kept, there are no migrations: every database is created fresh from these files, and the schema is edited in place.

## How it's applied

The official Postgres image runs init scripts from `/docker-entrypoint-initdb.d/` when it creates a new database. Both environments use that, and both always start from an empty database:

| Where | How |
|---|---|
| Tests | `TestDatabase.withSchema()` copies this directory into a new test container. `SchemaIT` fails if any file has an error |
| Local | The compose `postgres` service keeps its data in memory. On every start, `dev/postgres-init.sh` applies this directory and then the seed data in [`dev/seed`](../../../dev/seed/README.md) |

After changing the schema, restart the local database to pick it up:

```sh
podman compose down && podman compose up -d
```

If `schema.sql` gets large, split it into numbered files (`01_litanies.sql`, `02_recitations.sql`, ...); they run in name order.

## Later

Once there's data to keep, schema changes need migrations instead. That's a decision for the first release.
