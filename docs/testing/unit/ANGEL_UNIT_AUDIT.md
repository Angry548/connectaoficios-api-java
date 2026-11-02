# Auditoría de pruebas Java — Angel Mate

Fecha: 02/11/2026. Rama: `master`. Alcance autorizado por Angel para esta entrega.

Se revisaron POM, README, TEAM_ASSIGNMENTS, documentación de testing, interfaces,
implementaciones y fixtures/aserciones de las pruebas relacionadas. La asignación
previa no identifica a Angel por nombre; esta fase usa el alcance explícito del
solicitante, conservando completas las cinco clases asignadas a sus compañeros.
Ejecutar una prueba compartida como evidencia no transfiere su autoría.

## Casos reutilizados y brechas cubiertas

Clases de servicio bajo `src/test/java/com/connectaoficios/api/servicios/implementaciones/`.
Los nombres abreviados de esta tabla corresponden a clases terminadas en `Test`.

| Área | Evidencia existente reutilizada | Adición de Angel en esta fase |
|---|---|---|
| Categorías | `CategoriaServiceTest`: guardar, nombre duplicado, modificar, inexistente, desactivar, listar activas | Consulta positiva con identidad, descripción y estado |
| Servicios | `ServicioServiceTest`: cambiar estado, baja lógica, inexistente, activos y delegación de filtros; clase de Eduardo intacta | `ServicioConsultaActualizacionTest`: detalle, actualización parcial y normalización, título vacío sin guardar, relación perfil-servicios |
| Perfil trabajador | `PerfilTrabajadorServiceTest`: crear, duplicado, consultar por perfil/usuario, modificar propio, inexistente y no buscar otro usuario | Relación con servicios cubierta en la nueva clase de servicios |
| Disponibilidad | `DisponibilidadServicioServiceTest`: registrar, servicio inexistente, rango invertido, duplicado, solapamiento, modificar, desactivar y consulta por día | Detalle del horario y rechazo de actualización con rango invertido sin persistir |
| Zona | `ZonaCoberturaServiceTest`: crear, duplicado, modificar, inexistente, buscar departamento y desactivar | Consulta positiva con identidad geográfica |
| Solicitudes | `SolicitudServicioServiceTest`: obtener, aceptar, rechazar con motivo, iniciar, completar, cancelar; creación y transiciones inválidas asignadas a Alfredo | Ninguna: clase completa intacta; no duplicar sus comportamientos |
| Conversaciones | `ConversacionServiceTest`: crear tras aceptación, participante trabajador, acceso ajeno, pendiente, duplicado, historial completado y listado con avatares por lote | Consulta inexistente sin buscar avatares ni guardar |
| Mensajes | `MensajeServiceTest`: historial, remitente, lectura e idempotencia; envío y cronología asignados a Óscar | `MensajeConsultaNoLeidosTest`: conversación inexistente sin consultar mensajes; clase de Óscar intacta |
| Reseñas | `ResenaServiceTest`: cliente incorrecto, solicitud no completada, duplicado; creación asignada a Nuria | `ResenaConsultaTest`: sin reseñas e inexistente sin recalcular; `ResenaGuardarValidationTest`: límites 1/5 válidos y null/0/6 inválidos con Bean Validation |
| Reputación | `ReputacionTrabajadorServiceTest`: consulta, inexistente, recalcular sin reseñas, primera reseña, actualización y promedio; cálculo desde reseñas asignado a Nuria | Ninguna: conservar clase completa y no repetir su cálculo |
| Planes | `PlanPromocionServiceTest`: crear, duplicado, modificar, activar, desactivar, duración e inexistente | Consulta positiva conserva precio, duración y estado |
| Promociones | `PromocionServiceTest`: contratar, referencias y estados inválidos, activar, cancelar, finalizar, vigencia y vencimiento idempotente | Consulta positiva y filtro por propietario |
| Transacciones | `TransaccionPagoServiceTest`: registrar, moneda, propietario, monto, duplicado, aprobar/activar, rechazar/cancelar y pago simulado | Consulta positiva conserva promoción, monto y estado sin cobrar |
| Reportes | `ReporteServiceTest`: crear por usuario/servicio, consistencia, detalle, inexistente y estados de revisión/resolución/rechazo | Sin datos, periodo invertido sin consultar y filtro normalizado con tipo/estado |
| Estadísticas | `EstadisticasServiceTest`: generales, servicios no eliminados, solicitudes por estado, periodo vacío y fechas inválidas | Ninguna: reutilizar contrato existente |
| Notificaciones | `NotificacionServiceTest`: crear, consultar, leer idempotente, inexistente y ajena | Conteo de pendientes restringido al destinatario |
| Administración servicios | `ServicioAdminServiceTest`: listado, filtros, detalle, estado e inexistente | Ninguna: reutilizar |
| Perfil cliente | `PerfilClienteServiceTest` y `PerfilClienteControllerTest`: crear, duplicados, consultar, foto, conservar campos y usar subject JWT para modificar propio | Ninguna: reutilizar |
| Perfil administrador | `PerfilAdministradorServiceTest`, `PerfilAdministradorControllerTest`, `PerfilAdministradorSeguridadTest`: crear, consultar, modificar foto propia desde JWT, roles y acceso inválido | Ninguna: reutilizar |
| Imágenes y avatares | `ImagenServiceTest`, `UsuarioAvatarServiceTest`, seguridad de administrador: enrutamiento cliente/trabajador/administrador, lote por rol, foto/perfil ausente, formato/tamaño y error del proveedor simulado | Ninguna: reutilizar |
| No leídos globales | `repositorios/MensajeNoLeidosTotalTest`: vacío, varios, múltiples conversaciones, excluir leídos/propios/ajenos; `controladores/MensajeNoLeidosTotalControllerTest` | Transición real en H2: marcar un recibido como leído reduce total de 2 a 1 |

## Límites del contrato y de la evidencia

- `EstadisticasSalida` no expone estadísticas de trabajadores. No se inventa ese
  contrato ni se modifica producción para añadirlo en una fase de pruebas.
- Perfil trabajador se consulta mediante `obtenerPorId` / `obtenerPorTrabajadorId`;
  no existe un método adicional `obtenerPerfilPublico` en su interfaz.
- El DTO de reseñas se valida explícitamente con Jakarta Validator: invocar el
  servicio directamente no ejecuta automáticamente las anotaciones HTTP `@Valid`.
- La suite general incluye unitarias Mockito y pruebas Spring/HTTP/H2 preexistentes.
  Sus totales y cobertura son globales, no exclusivamente pruebas creadas por Angel.
- Mocks locales, proveedor de imágenes simulado y H2 evitan producción, MySQL,
  Cloudinary real y conexiones externas durante los tests. Maven se ejecuta con
  `-o` para reutilizar dependencias instaladas sin Internet.
- POM conserva JaCoCo 0.8.14, Surefire 3.5.6 administrado por Spring Boot y
  Surefire Report `report-only`. No se añaden librerías, exclusiones ni umbrales.

## Preservación de asignaciones

Los siguientes archivos deben tener diferencia vacía respecto del inicio de fase:

- Eduardo: `ServicioServiceTest.java`.
- Alfredo: `SolicitudServicioServiceTest.java`.
- Óscar: `MensajeServiceTest.java`.
- Nuria: `ResenaServiceTest.java` y `ReputacionTrabajadorServiceTest.java`.

No se modifica código productivo, endpoints, Docker, POM, configuración de tests
ni ignores. Los HTML, XML, cobertura y logs quedan fuera de Git. El resultado
medido de la ejecución final se encuentra en [UNIT_TEST_REPORT.md](UNIT_TEST_REPORT.md).
