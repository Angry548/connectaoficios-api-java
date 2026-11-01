param([string]$EnvironmentFile = "tests/postman/local.environment.json")
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "../..")
node scripts/testing/check-local.mjs $EnvironmentFile
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
New-Item -ItemType Directory -Force reports/newman | Out-Null
npx --yes newman@6.2.1 run tests/postman/connectaoficios-java-local.collection.json -e $EnvironmentFile --ignore-redirects --bail --reporters cli,json,junit --reporter-json-export reports/newman/results.json --reporter-junit-export reports/newman/results.xml
exit $LASTEXITCODE
