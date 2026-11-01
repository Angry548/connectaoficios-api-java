import {spawnSync} from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
const mode=process.argv[2]||'baseline';
const role=process.argv[3]||'publico';
if(!['baseline','api-safe','api-active'].includes(mode))throw new Error('Modo inválido');
const roles={publico:null,cliente:'CLIENTE_TOKEN',trabajador:'TRABAJADOR_TOKEN',administrador:'ADMIN_TOKEN'};
if(!(role in roles))throw new Error('Rol inválido');
if(mode==='api-active'&&process.env.ZAP_ALLOW_ACTIVE!=='true')throw new Error('Se requiere ZAP_ALLOW_ACTIVE=true durante la fase de security testing');
const tokenKey=roles[role];if(tokenKey&&!process.env[tokenKey])throw new Error('Falta token local del rol');
const image='ghcr.io/zaproxy/zaproxy:stable';
function docker(args,allowFailure=false,env=process.env){const r=spawnSync('docker',args,{stdio:'inherit',env});if(r.error)throw r.error;if(r.status&&!allowFailure)throw new Error('Docker falló: '+r.status);return r.status||0;}
const network='connectaoficios-zap-'+process.pid;
const output=path.resolve('reports/zap/'+role);fs.mkdirSync(output,{recursive:true});
// La red interna impide que el scanner alcance Internet, incluso por redirecciones.
// Descargar la imagen antes de conectarla a la red aislada.
docker(['pull',image]);
docker(['network','create','--internal',network]);
let connected=false;let exit=3;
try{
 docker(['network','connect','--alias','api-local',network,'connectaoficios-java-api']);connected=true;
 const command=mode==='baseline'?['zap-baseline.py','-t','http://api-local:8080/swagger-ui/index.html','-m','1']:['zap-api-scan.py','-t','http://api-local:8080/v3/api-docs','-f','openapi','-O','api-local:8080',...(mode==='api-safe'?['-S']:[])];
 const env={...process.env};const args=['run','--rm','--network',network,'-v',output+':/zap/wrk:rw'];
 if(tokenKey){env.ZAP_AUTH_HEADER_VALUE='Bearer '+process.env[tokenKey];env.ZAP_AUTH_HEADER_SITE='api-local';args.push('-e','ZAP_AUTH_HEADER_VALUE','-e','ZAP_AUTH_HEADER_SITE');}
 args.push(image,...command,'-r',mode+'.html','-J',mode+'.json','-x',mode+'.xml');exit=docker(args,true,env);
}finally{if(connected)docker(['network','disconnect',network,'connectaoficios-java-api'],true);docker(['network','rm',network],true);}
process.exitCode=exit;
