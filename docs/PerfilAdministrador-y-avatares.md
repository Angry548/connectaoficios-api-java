# Perfil administrativo y fotografías del Portal

## Arquitectura y decisión

Se inspeccionaron los perfiles Java, controllers, servicios, repositories, seguridad, DTOs .NET, emisor JWT, endpoints de usuarios y cuentas administrativas, servicios Axios, sesión y pantallas React antes de modificar archivos. Se presentó el resumen e inventario durante la tarea.

.NET conserva usuario, nombre, correo, teléfono, rol, estado, contraseña y autenticación. `JwtService.GenerateToken` emite `sub = usuario.Id` y `ClaimTypes.Role = usuario.Rol.Nombre`. Java valida firma HS256, emisor, audiencia y vigencia. El convertidor lee el claim de rol .NET o `role` y produce `ROLE_CLIENTE`, `ROLE_TRABAJADOR`, `ROLE_ADMINISTRADOR` o `ROLE_ADMINISTRADORPRINCIPAL`.

Los controllers obtienen el ID propio del subject. Los DTOs de escritura no contienen IDs de usuario. Java almacena únicamente los datos complementarios de perfil. No se modificó .NET ni SecurityConfig.

**Se implementó un único PerfilAdministrador para Administrador y AdministradorPrincipal.** El ID .NET es único entre usuarios; la diferencia de permisos sigue en .NET/JWT. No existe una segunda tabla para el principal, ni columnas de nombre, correo o rol en el perfil Java.

Se encontraron dos aplicaciones React. Se preguntó cuál utilizar y, sin respuesta, se eligió explícitamente `connectaoficios-portal-web/connectaoficios-portal-web-merge`, que contiene promociones y pruebas adicionales. La escritura fuera del workspace Java se realizó mediante el script de aplicación autorizado por el entorno. `connectaoficios-web-admin` no fue modificada.

## Archivos Java creados en esta extensión

Rutas relativas a `src/main/java/com/connectaoficios/api/`:

- `modelos/PerfilAdministrador.java`
- `repositorios/IPerfilAdministradorRepository.java`
- `dtos/perfil/PerfilAdministradorGuardar.java`
- `dtos/perfil/PerfilAdministradorModificar.java`
- `dtos/perfil/PerfilAdministradorSalida.java`
- `dtos/perfil/PerfilAdministradorPublicoSalida.java`
- `servicios/interfaces/IPerfilAdministradorService.java`
- `servicios/implementaciones/PerfilAdministradorService.java`
- `controladores/PerfilAdministradorController.java`
- `dtos/perfil/UsuarioFotoConsulta.java`
- `dtos/perfil/UsuarioFotosConsulta.java`
- `dtos/perfil/UsuarioFotoSalida.java`
- `servicios/implementaciones/UsuarioAvatarService.java`
- `controladores/UsuarioAvatarController.java`

Pruebas creadas bajo `src/test/java/com/connectaoficios/api/`:

- `servicios/implementaciones/PerfilAdministradorServiceTest.java`
- `servicios/implementaciones/UsuarioAvatarServiceTest.java`
- `controladores/PerfilAdministradorControllerTest.java`
- `controladores/PerfilAdministradorSeguridadTest.java`

Archivos existentes modificados en esta extensión:

- `controladores/ImagenController.java`
- `servicios/interfaces/IImagenService.java`
- `servicios/implementaciones/ImagenService.java`
- `src/test/java/com/connectaoficios/api/servicios/implementaciones/ImagenServiceTest.java`

También se crearon `docs/sql/perfiles_administrador.sql` y este informe. Los cambios anteriores de PerfilCliente, conversaciones, Maven y Docker se conservaron sin recrearlos.

## Entidad, repository, DTOs y servicio

| Campo Java | Columna MySQL | Tipo | Condiciones |
|---|---|---|---|
| Long id | id | BIGINT | PK, NOT NULL, AUTO_INCREMENT |
| Integer administradorId | administrador_id | INT | NOT NULL, UNIQUE |
| String fotoUrl | foto_url | VARCHAR(500) | nullable |
| LocalDateTime fechaCreacion | fecha_creacion | DATETIME(6) | NOT NULL, no actualizable por JPA |
| LocalDateTime fechaActualizacion | fecha_actualizacion | DATETIME(6) | NOT NULL |

Tabla: `perfiles_administrador`. Restricción: `uk_perfil_administrador`. Índices: PRIMARY y el índice UNIQUE de administrador_id; no se agrega un índice duplicado. Sin FK a usuarios porque pertenecen a .NET.

Sigue el patrón de Cliente y Trabajador: Lombok Getter/Setter, GenerationType.IDENTITY, PrePersist inicializando ambas fechas con el mismo instante y PreUpdate actualizando la segunda. Sin campos profesionales.

`IPerfilAdministradorRepository extends JpaRepository<PerfilAdministrador, Long>` incluye `findByAdministradorId`, `existsByAdministradorId`, `findByAdministradorIdIn(Collection<Integer>)`.

Guardar/Modificar contienen solo fotoUrl con Size máximo 500. Salida privada contiene los cinco campos; salida pública contiene id, administradorId y fotoUrl.

`IPerfilAdministradorService` y `PerfilAdministradorService` implementan guardar, obtenerPorId, obtenerPorAdministradorId, obtenerPerfilPublicoPorAdministradorId y modificar. Utilizan mapeo manual y transacciones. No existe DELETE. Null conserva la fotografía al modificar. Un perfil inexistente lanza RecursoNoEncontradoException.

La creación comprueba duplicados y utiliza saveAndFlush. Una violación Hibernate identificada como uk_perfil_administrador se traduce a ReglaNegocioException, igual que Cliente; otras violaciones de integridad se propagan sin etiquetarlas como duplicado.

## Endpoints y roles

| Método | Ruta | Acceso |
|---|---|---|
| POST | /api/perfiles-administrador | Ambos administrativos; ID del JWT, respuesta 201 |
| GET | /api/perfiles-administrador/{id} | Autenticado |
| GET | /api/perfiles-administrador/administrador/{administradorId} | Autenticado |
| GET | /api/perfiles-administrador/administrador/{administradorId}/publico | Autenticado |
| PUT | /api/perfiles-administrador/mi-perfil | Ambos administrativos; ID del JWT |
| POST | /api/imagenes/perfil | Los cuatro roles; ID del JWT |
| POST | /api/perfiles/fotos | Ambos administrativos; solo lectura en lote |

POST y PUT propios utilizan `hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')`. Se valida subject ausente, no numérico, no positivo o fuera de rango. La vista pública es para otros usuarios autenticados, consistente con SecurityConfig; no se habilitó acceso anónimo.

## Imágenes y resolución de avatares

No se creó otro sistema de imágenes. `IImagenService` incorpora `subirFotoPerfilAdministrador`; conserva las firmas de Trabajador y Cliente. Los tres métodos reutilizan el uploader y las validaciones existentes. Cloudinary utiliza la misma carpeta `connectaoficios/perfiles`, con public_id separado:

| Rol | Perfil para lectura | public_id para subida |
|---|---|---|
| Cliente | PerfilCliente | cliente_{id} |
| Trabajador | PerfilTrabajador | trabajador_{id} |
| Administrador | PerfilAdministrador | administrador_{id} |
| AdministradorPrincipal | PerfilAdministrador | administrador_{id} |

Se mantienen JPEG/PNG/WEBP, máximo actual de 5 MB, overwrite=true, invalidate=true, resource_type=image y lectura de secure_url. Subir no persiste el perfil: la URL se guarda después mediante POST/PUT. No se agregó eliminación física.

`UsuarioAvatarService` resuelve usuarioId y rol individualmente o en lote. La resolución individual reutiliza el lote. Se agrupan IDs en sets para tres categorías de perfil; ambos roles administrativos comparten el mismo set y consulta. Hay como máximo tres consultas de perfiles por lote, ninguna por fila. Los mapas mantienen separados Cliente/Trabajador/Administrador incluso si reciben el mismo ID. No se guardan nombres ni roles. Perfil o fotografía ausente produce null. No se modificó la lógica existente de conversaciones en esta extensión.

El endpoint batch usa un objeto contenedor para validar los elementos de la lista y limitar a 200 usuarios. Acepta roles sin distinguir mayúsculas/minúsculas y devuelve roles en mayúsculas. Conserva orden y entradas repetidas, consultando IDs únicos. Lista vacía devuelve [] sin consultar perfiles. IDs y roles inválidos se rechazan.

```json
{
  "usuarios": [
    {"usuarioId": 1, "rol": "Cliente"},
    {"usuarioId": 2, "rol": "Trabajador"},
    {"usuarioId": 3, "rol": "AdministradorPrincipal"}
  ]
}
```

Respuesta (las URLs son valores ilustrativos del contrato):

```json
[
  {"usuarioId": 1, "rol": "CLIENTE", "fotoUrl": "https://..."},
  {"usuarioId": 2, "rol": "TRABAJADOR", "fotoUrl": null},
  {"usuarioId": 3, "rol": "ADMINISTRADORPRINCIPAL", "fotoUrl": null}
]
```

## Portal: pantallas, servicios y componentes

El Portal mantiene `adminUsersService` y `adminAccountsService` para obtener usuarios de .NET, con páginas `items/page/totalItems` y búsquedas/detalles. Los DTOs .NET no se modificaron. apiDotNet y apiJava conservan sus interceptores Bearer y tratamiento de expiración de sesión.

| Pantalla/componente | Persona | Antes | Cambio |
|---|---|---|---|
| Usuarios, tabla y detalle | Cliente/Trabajador | Texto sin foto | UserIdentity y batch |
| Administradores, tabla y detalle | Ambos administrativos | Círculo con icono/texto | UserAvatar/UserIdentity y batch |
| AdminLayout, cabecera y menú | Administrador actual | Iniciales | Foto con iniciales de fallback y enlace Mi perfil |
| Mi perfil, nueva ruta /mi-perfil | Administrador actual | No existía | Información .NET de solo lectura, selección, preview, subida y guardado de foto |
| Servicios, tabla y detalle | Trabajador propietario | Texto | UserIdentity y batch |
| Promociones contratadas y detalle | Trabajador | Texto | UserIdentity y batch |
| Reportes, tabla y detalle | Reportante/reportado/trabajador relacionado | Texto | UserIdentity y batch |
| Dashboard | Saludo de administrador | Texto | Cubierto por avatar de cabecera; saludo sin cambios |
| Estadísticas/planes | Sin identidad de una persona concreta | Sin foto | Sin cambios |
| Conversaciones | No hay pantalla ni servicio en este Portal | — | Sin cambios |

Servicios nuevos: `src/services/avatarService.js`, `src/services/adminProfileService.js`. No se modificaron servicios de identidad .NET. Componentes nuevos: `UserAvatar.jsx`, `UserIdentity.jsx`, `AvatarNotice.jsx`; se modificó AdminLayout y el diálogo compartido de promociones. También se agregaron `src/hooks/useUserAvatars.js`, `src/utils/avatars.js`, `src/pages/AdminProfilePage.jsx`, `tests/avatars.test.js`, ruta en App y script npm test.

El hook de cada pantalla recibe las personas de la página/detalle, deduplica por rol/ID y hace un batch (200 por solicitud si se excede el límite). Una tabla de 50 usuarios genera una llamada de fotografías, más la consulta independiente de la cabecera para el administrador actual. No hay peticiones desde cada UserAvatar. Los cambios de página cancelan la consulta anterior; la clave de consulta evita mostrar fotos de una página anterior. Un fallo muestra iniciales y aviso con reintento. Un evento tras guardar la fotografía refresca los avatares de las vistas montadas. Se mantienen tamaños explícitos y fallback de iniciales también cuando falla la carga de una imagen, sin imágenes inventadas.

Mi perfil lee los datos desde .NET y su perfil desde Java; la información de identidad es de solo lectura. Para foto nueva: seleccionar archivo, previsualizar, subir por multipart con el token y guardar exclusivamente fotoUrl. Si el perfil no existía se crea, si existía se modifica. No envía administradorId en la escritura. Si falla guardar después de subir, conserva la URL pendiente para reintentar sin volver a subir. La contraseña conserva su pantalla y flujo .NET.

Hay consultas individuales preexistentes de nombres/relaciones .NET en servicios, reportes y promociones. No se amplió esa estrategia para fotografías ni se refactorizó la identidad .NET. Los filtros/autocompletados conservan su presentación actual; los cambios visuales se aplican a las tablas, detalles, cabecera y perfil propio.

## Base de datos y producción

SQL de referencia: [perfiles_administrador.sql](sql/perfiles_administrador.sql). Debe revisarse y aplicarse mediante el proceso habitual de migración antes de habilitar el módulo en producción. Si la tabla de Cliente aún no existe allí, también se necesita el SQL anterior [perfiles_cliente.sql](sql/perfiles_cliente.sql). No se ejecutó ninguno remotamente.

Producción conserva `spring.jpa.hibernate.ddl-auto=none`. Docker local conserva `SPRING_JPA_HIBERNATE_DDL_AUTO=update`. No se modificaron Docker, contenedores, volúmenes, Aiven ni Render. No hubo commit, push ni despliegue.

## Validación Java

`.\mvnw.cmd test`: **173 ejecutadas, 156 aprobadas, 8 fallos, 9 errores, 0 omitidas**. El código principal y las pruebas compilaron. Las 22 pruebas nuevas pasan: servicio administrativo 10, controller 3, seguridad de método 4 y avatares 5. Se amplió la prueba existente de ImagenService para comprobar administrador_{id}, manteniendo Cliente/Trabajador.

La seguridad se prueba con el convertidor real y proxies de method security: ambos administrativos pueden escribir y consultar batch; Cliente/Trabajador no pueden hacerlo; la subida sigue enrutando los roles previos. Se cubren JWT inválido, creación, duplicado anticipado y por UNIQUE simulado, otras violaciones de integridad, consultas, inexistentes, foto, conservación con null y perfil público. El lote cubre los cuatro roles, fotos ausentes, IDs repetidos, misma identidad en distintas categorías, consultas limitadas a tres, lote vacío y entradas inválidas.

No hay fallos nuevos detectados. Persisten los anteriores: PromocionServiceTest (7 fallos, 7 errores por mocks que devuelven null en findByEstado); SolicitudServicioServiceTest (1 fallo por motivo de rechazo y 1 error por perfilTrabajador null); contextLoads (1 error por cloudinary.url ausente). El acceso al repositorio Maven fuera del sandbox fue autorizado. No se ejecutó clean ni se ocultaron/omitieron pruebas.

Log: `target/perfil-administrador-maven.log`; reportes: `target/surefire-reports`.

## Validación del Portal

Resultado final en la aplicación elegida: `npm test` **16/16 aprobadas, 0 omitidas**; `npm run build` **exit 0**; `npm run lint` **exit 0**. Las pruebas incluyen las 11 existentes y 5 nuevas: una llamada para 50 usuarios, normalización y separación de roles, deduplicación, límite de lote, errores/reintento e iniciales. Tras ajustar el fallback de reportes sin servicio relacionado se repitieron los tres comandos y todos terminaron correctamente.

La primera ejecución del build fue interrumpida por PowerShell al tratar como error el stderr de una advertencia de Vite. Se repitió capturando el código real de npm: build 0 y lint 0. No se modificaron límites para silenciar advertencias. Vite informa de un chunk de aproximadamente 1.18 MB (355 kB gzip), por encima del aviso de 500 kB. El build genera dist correctamente.

Las comprobaciones estáticas y de build no sustituyen una prueba visual con sesión real. Está pendiente probar en navegador el flujo completo contra Java/.NET locales, ambos roles administrativos y Cloudinary. No se hicieron solicitudes a Aiven ni Render para validar el Portal.

Logs en el workspace Java: `target/portal-avatar-npm-test.log`, `target/portal-avatar-build.log` y `target/portal-avatar-lint.log`. Las copias de trabajo y scripts temporales están bajo target y no forman parte del código fuente del Portal.

## Pendientes y límites

- Validar el flujo real con JWT .NET, Cloudinary y MySQL local después de reconstruir la API. Las pruebas Java no contactan servicios remotos.
- Confirmar la traducción de la restricción UNIQUE con el driver MySQL local; el test unitario simula la excepción Hibernate identificada por nombre.
- Sigue pendiente el problema previo de clean sobre target. No se considera resuelto.
- Se conserva max-request-size=5MB; al incluir multipart, un archivo de exactamente 5 MB podría superar el total. No se cambiaron propiedades de producción.
- No hay borrado de foto mediante null ni eliminación física. Una subida y posterior fallo de persistencia puede dejar una imagen sin referencia.
- Las consultas de nombres ya existentes en .NET siguen siendo independientes del batch de fotografías.
- Si el perfil administrativo se crea en otra pestaña entre la lectura y el POST, Java rechaza el duplicado correctamente; el usuario puede recargar Mi perfil y guardar de nuevo.
- La variante `connectaoficios-web-admin` queda sin cambios; no se desplegó ni se validó contra Aiven/Render.

## Estado Git final

Workspace Java (incluye cambios previos):

```text
 M .dockerignore
 M mvnw.cmd
 M pom.xml
 M src/main/java/com/connectaoficios/api/controladores/ImagenController.java
 M src/main/java/com/connectaoficios/api/dtos/conversacion/ConversacionSalida.java
 M src/main/java/com/connectaoficios/api/repositorios/IConversacionRepository.java
 M src/main/java/com/connectaoficios/api/repositorios/IPerfilTrabajadorRepository.java
 M src/main/java/com/connectaoficios/api/servicios/implementaciones/ConversacionService.java
 M src/main/java/com/connectaoficios/api/servicios/implementaciones/ImagenService.java
 M src/main/java/com/connectaoficios/api/servicios/interfaces/IImagenService.java
 M src/test/java/com/connectaoficios/api/servicios/implementaciones/ConversacionServiceTest.java
?? .env.example
?? README.md
?? compose.yaml
?? docs/PerfilAdministrador-y-avatares.md
?? docs/PerfilCliente.md
?? docs/sql/
?? src/main/java/com/connectaoficios/api/controladores/PerfilAdministradorController.java
?? src/main/java/com/connectaoficios/api/controladores/PerfilClienteController.java
?? src/main/java/com/connectaoficios/api/controladores/UsuarioAvatarController.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilAdministradorGuardar.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilAdministradorModificar.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilAdministradorPublicoSalida.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilAdministradorSalida.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilClienteGuardar.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilClienteModificar.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilClientePublicoSalida.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilClienteSalida.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/UsuarioFotoConsulta.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/UsuarioFotoSalida.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/UsuarioFotosConsulta.java
?? src/main/java/com/connectaoficios/api/modelos/PerfilAdministrador.java
?? src/main/java/com/connectaoficios/api/modelos/PerfilCliente.java
?? src/main/java/com/connectaoficios/api/repositorios/IPerfilAdministradorRepository.java
?? src/main/java/com/connectaoficios/api/repositorios/IPerfilClienteRepository.java
?? src/main/java/com/connectaoficios/api/servicios/implementaciones/PerfilAdministradorService.java
?? src/main/java/com/connectaoficios/api/servicios/implementaciones/PerfilClienteService.java
?? src/main/java/com/connectaoficios/api/servicios/implementaciones/UsuarioAvatarService.java
?? src/main/java/com/connectaoficios/api/servicios/interfaces/IPerfilAdministradorService.java
?? src/main/java/com/connectaoficios/api/servicios/interfaces/IPerfilClienteService.java
?? src/test/java/com/connectaoficios/api/controladores/
?? src/test/java/com/connectaoficios/api/servicios/implementaciones/ImagenServiceTest.java
?? src/test/java/com/connectaoficios/api/servicios/implementaciones/PerfilAdministradorServiceTest.java
?? src/test/java/com/connectaoficios/api/servicios/implementaciones/PerfilClienteServiceTest.java
?? src/test/java/com/connectaoficios/api/servicios/implementaciones/UsuarioAvatarServiceTest.java
```

Portal merge:

```text
 M package.json
 M src/App.jsx
 M src/components/AdminLayout.jsx
 M src/pages/administradores/AdministratorsPage.jsx
 M src/pages/promociones/ContractedPromotionsPage.jsx
 M src/pages/promociones/PromotionComponents.jsx
 M src/pages/reportes/ReportsPage.jsx
 M src/pages/servicios/ServicesPage.jsx
 M src/pages/usuarios/UsersPage.jsx
?? src/components/AvatarNotice.jsx
?? src/components/UserAvatar.jsx
?? src/components/UserIdentity.jsx
?? src/hooks/
?? src/pages/AdminProfilePage.jsx
?? src/services/adminProfileService.js
?? src/services/avatarService.js
?? src/utils/avatars.js
?? tests/avatars.test.js
```
