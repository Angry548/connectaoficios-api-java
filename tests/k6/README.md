# k6 exclusivamente local

Los diez scripts usan la API Java; no ejecutan login .NET. Destino fijo permitido: http://localhost:8081 o http://127.0.0.1:8081. No siguen redirects.

Variables: CLIENTE_TOKEN, TRABAJADOR_TOKEN, ADMIN_TOKEN; CLIENTE_ID para solicitudes; CONVERSACION_ID para mensajería (solicitud aceptada/en proceso). Tokens obtenidos de .NET local y conservados solo en la sesión.

Por defecto se mide lectura. WRITE=true habilita creación de solicitudes (requiere SERVICIO_ID) y mensajes; genera filas que se conservan. Contratación/pago de promociones se verifica en Postman: no se repite sobre el mismo servicio bajo carga porque las reglas impiden duplicados. Preparar un servicio por contratación antes de ampliar ese escenario.

Load: 2 a 5 VUs, 100 segundos, errores <1%, p95 <1500ms. Stress: 5/20/50 VUs, 3 minutos, errores <5%, p95 <3000ms. Checks >99%. Son criterios iniciales, no una capacidad certificada ni un SLA acordado. Ajustar con el equipo y hardware documentado antes de medir capacidad.

PowerShell:

```powershell
k6 run --summary-export reports/k6/load/servicios.json tests/k6/load/servicios.js
# Solo en la fase posterior autorizada:
k6 run --summary-export reports/k6/stress/servicios.json tests/k6/stress/servicios.js
```

Los mismos comandos funcionan en Bash con k6 instalado. Para Docker multiplataforma usar el runner documentado en docs/testing/README.md. No ejecutar stress durante la preparación de master.
