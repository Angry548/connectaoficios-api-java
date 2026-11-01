import http from 'k6/http';
import { check, sleep } from 'k6';
const base = __ENV.BASE_URL || 'http://localhost:8081';
if (!( __ENV.K6_DOCKER==='true' && base==='http://127.0.0.1:8080') && !/^http:\/\/(localhost|127\.0\.0\.1):8081$/.test(base)) throw new Error('Solo localhost:8081');
function token(role) { const value=__ENV[role+'_TOKEN']; if(!value) throw new Error('Falta '+role+'_TOKEN'); return value; }
function id(key) {const value=__ENV[key];if(!/^[1-9][0-9]*$/.test(value||''))throw new Error('Falta ID positivo '+key);return value;}
function params(role) {return {headers:{Authorization:'Bearer '+token(role),'Content-Type':'application/json'},redirects:0};}
function get(url,role) {const r=http.get(base+url,params(role));check(r,{'HTTP 200':r=>r.status===200});return r;}
export function configuration(stress=false){return {stages:stress?[{duration:'30s',target:5},{duration:'1m',target:20},{duration:'1m',target:50},{duration:'30s',target:0}]:[{duration:'20s',target:2},{duration:'1m',target:5},{duration:'20s',target:0}],thresholds:{http_req_failed:[stress?'rate<0.05':'rate<0.01'],http_req_duration:[stress?'p(95)<3000':'p(95)<1500'],checks:['rate>0.99']},maxRedirects:0};}
export function validate(module){
 if(module==='servicios')token('CLIENTE');
 if(module==='solicitudes'){token('CLIENTE');id('CLIENTE_ID');if(__ENV.WRITE==='true')id('SERVICIO_ID');}
 if(module==='mensajeria'){token('CLIENTE');id('CONVERSACION_ID');}
 if(module==='promociones')token('TRABAJADOR');
 if(module==='reportes-estadisticas')token('ADMIN');
}
export function run(module){
 if(module==='servicios'){get('/api/servicios?page=0&size=20','CLIENTE');get('/api/servicios?texto=TEST_LOCAL&page=0&size=20','CLIENTE');}
 if(module==='solicitudes'){
   get('/api/solicitudes/cliente/'+id('CLIENTE_ID')+'/paginadas?page=0&size=10','CLIENTE');
   if(__ENV.WRITE==='true'){const body={servicioId:Number(id('SERVICIO_ID')),fechaPropuesta:new Date(Date.now()+86400000).toISOString().slice(0,10),horaAproximada:'09:00:00',direccionServicio:'Dirección ficticia local',descripcionTrabajo:'Fixture k6 local'};const r=http.post(base+'/api/solicitudes',JSON.stringify(body),params('CLIENTE'));check(r,{'Solicitud creada':r=>r.status===201});}
 }
 if(module==='mensajeria'){get('/api/mensajes/conversacion/'+id('CONVERSACION_ID')+'/paginados?page=0&size=20','CLIENTE');get('/api/mensajes/no-leidos/total','CLIENTE');if(__ENV.WRITE==='true'){const r=http.post(base+'/api/mensajes',JSON.stringify({conversacionId:Number(id('CONVERSACION_ID')),contenido:'Mensaje k6 local'}),params('CLIENTE'));check(r,{'Mensaje creado':r=>r.status===201});}}
 if(module==='promociones'){get('/api/promociones/mis-promociones','TRABAJADOR');get('/api/planes-promocion/activos','TRABAJADOR');}
 if(module==='reportes-estadisticas'){get('/api/reportes/admin?page=0&size=20','ADMIN');get('/api/admin/estadisticas','ADMIN');}
 sleep(1);
}
