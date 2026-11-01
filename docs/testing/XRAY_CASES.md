# Casos recomendados para Xray

Conservar todos los casos existentes. No se modifico Jira. Propuestas para planificar despues de preparar develop:

| Grupo | Casos nuevos o ampliados |
|---|---|
| Identidad JWT | NameIdentifier, fallback sub, precedencia, invalido/negativo; firma, expiracion, issuer, audience; alias legacy Administrador_Principal |
| Servicios | Crear con perfil ajeno; modificar/estado/eliminar ajeno denegados; dueno permitido |
| Solicitudes | IDs derivados; suplantacion rechazada; consulta y transiciones de tercero denegadas; listado ajeno; motivo de rechazo visible |
| PerfilTrabajador | Crear, duplicado, consultar, modificar propio, inexistente y rol invalido |
| PerfilCliente | Propio/publico, modificacion desde identidad JWT, rol ajeno denegado |
| PerfilAdministrador | Ambos roles administrativos, modificacion propia, aislamiento y roles invalidos |
| Avatar | Cuatro roles, ID compartido entre roles, inexistente, lote, duplicados, limite invalido |
| Imagen | Simulacion local sin red; vacio, 5 MB, exceso, MIME invalido, error del proveedor sin detalles internos |
| Notificaciones | Crear, listado del usuario, inexistente 404, ajena 403, lectura idempotente y AdministradorPrincipal |
| Mensajes | Lectura idempotente, tercero ajeno, total de varias conversaciones y exclusion de enviados/ya leidos |
| Pago simulado | Dueno/pendiente, aprobacion, referencia SIM, activacion; ajeno/estado invalido rechazados |
| Promociones | Vencimiento automatico, segunda sincronizacion idempotente y conservacion de vigentes |
| ServicioAdmin | Listado, filtros, detalle, estado, inexistente y roles administrativos/denegados |
| Estadisticas | Servicios no eliminados, estados de solicitudes, periodo vacio y fechas invalidas |
| WebSocket | CONNECT sin JWT/invalido, origen no permitido, topic propio/ajeno, wildcard y SEND denegados; aislamiento |
| Integracion | Flujo HTTP servicio-solicitud-chat-resena/reputacion-promocion/pago; persistencia MySQL al reiniciar API |
| Infraestructura | Copia de plantilla, health SELECT 1, arranque sin Cloudinary, volumen conservado, reportes nuevos |

Las unitarias y HTTP/H2 automatizadas no sustituyen evidencias Docker/MySQL, mobile STOMP, Newman, ZAP ni rendimiento. Esos resultados se adjuntaran posteriormente a sus casos.
