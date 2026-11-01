import fs from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import {spawnSync} from 'node:child_process';
const file='tests/postman/connectaoficios-java-local.collection.json';
const c=JSON.parse(fs.readFileSync(file,'utf8'));
const env=JSON.parse(fs.readFileSync('tests/postman/connectaoficios-java-local.environment.example.json','utf8'));
assert.equal(env.values.find(v=>v.key==='baseUrl').value,'http://localhost:8081');
assert.ok(env.values.filter(v=>v.key.endsWith('Token')).every(v=>v.value===''));
const pre=c.event.find(e=>e.listen==='prerequest').script.exec.join('\n');
for(const base of ['https://example.invalid','http://localhost:8081.evil.invalid','http://localhost:8081@evil.invalid']){
 assert.throws(()=>vm.runInNewContext(pre,{pm:{environment:{get:()=>base}}}));
}
vm.runInNewContext(pre,{pm:{environment:{get:()=> 'http://localhost:8081'},variables:{replaceIn:()=> 'http://localhost:8081/api/servicios',set:()=>{}},request:{url:{toString:()=> '{{baseUrl}}/api/servicios'}}}});
for(const item of c.item){assert.equal(item.protocolProfileBehavior.followRedirects,false);assert.ok(item.request.url.startsWith('{{baseUrl}}/'));for(const event of item.event||[])new vm.Script(event.script.exec.join('\n'));}
const files=['scripts/testing/zap.mjs','scripts/testing/k6.mjs','scripts/testing/check-local.mjs','scripts/testing/seed.mjs','scripts/testing/report-summary.mjs','scripts/testing/validate.mjs','tests/k6/common/scenarios.js'];
for(const mode of ['load','stress'])for(const name of ['servicios','solicitudes','mensajeria','promociones','reportes-estadisticas'])files.push('tests/k6/'+mode+'/'+name+'.js');
for(const f of files){const r=spawnSync(process.execPath,['--input-type=module','--check'],{input:fs.readFileSync(f,'utf8'),encoding:'utf8'});assert.equal(r.status,0,r.stderr);}
console.log('OK: '+c.item.length+' requests Postman, tokens vacíos, guardas locales y sintaxis de '+files.length+' scripts. Sin requests HTTP.');
