#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/../.."
# Invocación mediante sh funciona también si NTFS no conserva el bit ejecutable.
docker compose --profile testing run --rm unit-tests
