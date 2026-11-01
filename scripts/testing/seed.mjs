import fs from 'node:fs';
import {spawnSync} from 'node:child_process';
const sql=fs.readFileSync('docker/sql/02-seed-test-data.sql','utf8');
const result=spawnSync('docker',['compose','exec','-T','mysql','sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" exec mysql --user="$MYSQL_USER" "$MYSQL_DATABASE"'],{input:sql,encoding:'utf8'});
if(result.error)throw result.error;
if(result.stdout)process.stdout.write(result.stdout);if(result.stderr)process.stderr.write(result.stderr);
process.exitCode=result.status??1;
