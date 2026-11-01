#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/../.."
ENV_FILE="${1:-tests/postman/local.environment.json}"
node scripts/testing/check-local.mjs "$ENV_FILE"
mkdir -p reports/newman
npx --yes newman@6.2.1 run tests/postman/connectaoficios-java-local.collection.json -e "$ENV_FILE" --ignore-redirects --bail --reporters cli,json,junit --reporter-json-export reports/newman/results.json --reporter-junit-export reports/newman/results.xml
