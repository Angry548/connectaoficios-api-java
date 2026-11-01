import fs from 'node:fs';
const filename=process.argv[2];
if(!filename)throw new Error('Indicar environment local con tokens');
const env=JSON.parse(fs.readFileSync(filename,'utf8'));
const get=key=>env.values.find(v=>v.key===key && v.enabled!==false)?.value;
if(!/^http:\/\/(localhost|127\.0\.0\.1):8081$/.test(get('baseUrl')||''))throw new Error('Destino no local');
for(const key of ['clienteToken','trabajadorToken','adminToken','clienteId','trabajadorId'])if(!get(key))throw new Error('Falta variable: '+key);
console.log('Environment local válido; tokens no mostrados.');
