# HU-04: borrador de integracion - no definitivo

Este documento describe unicamente los contratos implementados hasta ahora. No es una aceptacion de HU-04: falta validacion completa de los flujos y pruebas de proveedor real.

## HTTP

Usar `Authorization: Bearer <JWT>`. El identificador del remitente se extrae del JWT.

- `POST /api/mensajes`: conserva el contrato existente de texto `{ "conversacionId": 1, "contenido": "Hola" }`.
- `POST /api/mensajes/adjunto`: `multipart/form-data` con `conversacionId` (numero), `tipo` (`IMAGE` o `DOCUMENT`) y `archivo` (binario). Respuesta 201 con campos existentes mas `tipo` y `adjuntos: [{id,tipo,nombre,mimeType,tamanoBytes,url}]`. El limite es 5 MiB por defecto. No se admiten URL de origen suministradas por el cliente.
- `GET /api/adjuntos/{id}/contenido`: autenticado; tras verificar participacion devuelve los bytes con su MIME. El cliente debe proporcionar JWT.
- `GET /api/conversaciones/paginadas?page=0&size=20`: anade `ultimaActividad`, `ultimoMensaje` y `noLeidos`. `ultimoMensaje` puede ser null. Orden por ultima actividad y, en empate, id descendente.
- `GET /api/stickers`: catalogo controlado; `POST /api/mensajes/sticker`: `{ "conversacionId": 1, "codigo": "feliz" }`.
- `GET /api/conversaciones/{id}/presencia`: `{ "usuarioId": 20, "online": true }`, para el otro participante.
- `POST /api/dispositivos-push`: `{ "token": "ExpoPushToken[...]", "plataforma": "ANDROID" }` (tambien `IOS`); devuelve `{ "id": 1 }` sin incluir token.
- `DELETE /api/dispositivos-push/{id}`: desactiva el dispositivo propio y responde 204.
- `PUT /api/mensajes/{id}/leido`: marca recibido como leido; los no leidos se calculan en el servidor, sin incremento por evento STOMP.

## STOMP

Conectar a `/ws` y enviar `Authorization: Bearer <JWT>` en el frame CONNECT. Suscribirse a `/topic/conversaciones/{id}` para mensajes y `/topic/conversaciones/{id}/presencia` para eventos `{ "tipo": "PRESENCIA", "usuarioId": 20, "online": true }`. Ambos destinos comprueban participacion. El mensaje contiene su id persistido y puede deduplicarse con el. Envios se realizan por HTTP.

La presencia en memoria funciona con una sola instancia. Para varias replicas se necesita Redis, Pub/Sub u otro almacenamiento compartido. El cliente debe consultar el estado inicial y volver a sincronizar tras reconectar.

## Push

Una notificacion Expo contiene `data: { type: "CHAT_MESSAGE", conversacionId, mensajeId }`. Se envia solo a dispositivos activos del receptor. Su entrega es best-effort. El token no debe enviarse a logs ni exponerse en pantallas.

## Errores y restricciones

La API existente devuelve 401 sin JWT, 403 para acceso denegado y puede devolver 400 para reglas de negocio o multipart invalido; no depender de mensajes literales. Los adjuntos usan Cloudinary autenticado con acceso mediado por la API, pendiente de prueba con la cuenta de despliegue. Consultar `docs/hu04-migracion.md` antes de cualquier cambio de esquema.

## Ejemplos de respuesta

Un mensaje puede tener esta forma (las fechas representan hora local del servidor):

```json
{"id":42,"conversacionId":3,"remitenteId":10,"contenido":"","tipo":"IMAGE","fechaEnvio":"2026-11-27T12:30:00","leido":false,"adjuntos":[{"id":7,"tipo":"IMAGE","nombre":"foto.png","mimeType":"image/png","tamanoBytes":1024,"url":"/api/adjuntos/7/contenido"}]}
```

Para texto, `tipo` es `TEXT` y `adjuntos` es una lista vacia; `DOCUMENT` corresponde a PDF y `STICKER` a una imagen del catalogo. El remitente tambien recibira el evento STOMP, por lo que debe deduplicarse con `id` frente a la respuesta REST.

La pagina de conversaciones mantiene el formato de paginacion existente; cada elemento incorpora `ultimaActividad`, `ultimoMensaje` y `noLeidos`. El ultimo mensaje tiene id, tipo, contenido, remitente, fecha y estado de lectura. Sin mensajes, `ultimoMensaje` es null y la actividad es la fecha de creacion.

Registro de dispositivo devuelve 200 con `{"id": 7}`. Se puede registrar el mismo token del mismo usuario repetidamente; el id identifica el dispositivo para su desregistro. No se debe enviar el id del usuario en la solicitud.

## Orden dinamico de conversaciones

El backend entrega el listado ordenado globalmente por `ultimaActividad DESC` antes de paginar; en empate usa `id DESC`. Para una conversacion sin mensajes, `ultimaActividad` es su fecha de creacion. La cantidad de no leidos y el estado de presencia no cambian ese orden.

Al recibir un mensaje STOMP, mobile debe usar su `id` para deduplicar, actualizar el ultimo mensaje y la fecha de actividad de su conversacion, ajustar no leidos si corresponde y moverla a la primera posicion sin esperar a recargar toda la lista. Al confirmar un envio propio por REST debe hacer lo mismo; si llega tambien por STOMP, procesarlo una sola vez. Una sincronizacion posterior con `/api/conversaciones/paginadas` devuelve el orden autoritativo. Ante eventos fuera de orden o fechas iguales, comparar actividad e ID y sincronizar las paginas afectadas.

Ejemplo: `A, B, C`; llega un mensaje a C: `C, A, B`; luego llega uno a B: `B, C, A`.
