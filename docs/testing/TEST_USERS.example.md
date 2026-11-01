# Cuentas ficticias de integración

Estas cuentas NO existen ni se crean en Java o SQL. Se crearán posteriormente en la API .NET local. Nunca reutilizar cuentas ni claves personales/producción.

| Cuenta ficticia | Correo de ejemplo | Contraseña ficticia local | Rol esperado |
|---|---|---|---|
| Cliente | cliente@example.test | FicticiaLocal_Cliente_2026! | Cliente |
| Trabajador | trabajador@example.test | FicticiaLocal_Trabajador_2026! | Trabajador |
| Administrador | administrador@example.test | FicticiaLocal_Admin_2026! | Administrador |
| AdministradorPrincipal | principal@example.test | FicticiaLocal_Principal_2026! | AdministradorPrincipal |

Añadir un segundo Cliente y Trabajador para pruebas de aislamiento. Los IDs serán los asignados por .NET: no asumir coincidencia con el ID del perfil Java. Tokens ausentes de este documento y del environment versionado.

Contrato JWT compatible: NameIdentifier URI (http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier), con fallback a sub cuando no hay NameIdentifier. ID entero positivo. Si ambos existen, prevalece NameIdentifier; uno presente inválido no hace fallback. Roles: claim URI .NET o role. Validación HS256, expiración, issuer y audience. Confirmar estos claims con el emisor .NET local al integrar; no se contactó ese backend durante la preparación.

La clave/issuer/audience del emisor local deben coincidir con Docker. Nunca copiar JWT ni claves de producción.
