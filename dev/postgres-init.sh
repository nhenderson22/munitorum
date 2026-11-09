#!/bin/sh
# Runs inside the compose postgres container each time it creates the database, which is
# every start: the data lives in memory. Applies the schema, then the dev seed data.
# Any error stops startup, and the container exits with the psql error in its logs.
set -eu

for file in /munitorum/schema/*.sql /munitorum/seed/*.sql; do
  [ -e "$file" ] || continue # an empty directory leaves the glob unexpanded
  echo "postgres-init: applying $file"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --file "$file"
done
