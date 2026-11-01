$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "../..")
docker compose --profile testing run --rm unit-tests
exit $LASTEXITCODE
