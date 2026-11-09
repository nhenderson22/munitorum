# Dev seed data

SQL files here run against the local compose database right after the schema, every time Postgres starts. The database is in memory, so each start is a fresh database with the schema and this seed data, and nothing else.

- Files run in name order: `01_litanies.sql`, `02_recitations.sql`, ...
- An error stops Postgres from starting; `podman compose logs postgres` shows which file and line.
- To reseed after editing: `podman compose down && podman compose up -d`.

This is local dev data only. Integration tests don't load it: `TestDatabase.withSchema()` applies the schema alone, and each test sets up the data it needs.
