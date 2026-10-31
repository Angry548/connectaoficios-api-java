# ConnectaOficios API Java

Backend Spring Boot 4.0.8 con Java 21.

## Pruebas y cobertura

Surefire muestra el resultado de las pruebas (casos, errores, fallos, omitidas
y tiempos). JaCoCo muestra cuanto codigo fue ejecutado por las pruebas,
incluyendo instrucciones, ramas, lineas, metodos y clases. No hay umbral minimo
de cobertura ni exclusiones de clases.

Desde PowerShell, ejecutar en este orden para conservar ambos reportes:

```powershell
# Pruebas
.\mvnw.cmd clean test
# Pruebas y cobertura JaCoCo
.\mvnw.cmd clean verify
# Reporte HTML Surefire a partir de los XML existentes, sin repetir pruebas
.\mvnw.cmd site
# Abrir reportes
Start-Process .\target\site\surefire-report.html
Start-Process .\target\site\jacoco\index.html
```

`clean` elimina `target`, incluido cualquier reporte anterior; por eso se
ejecuta `site` despues del ultimo `clean verify`. Los XML originales quedan
en `target/surefire-reports` y los HTML en `target/site`. Son artefactos
locales ignorados por Git. Las pruebas de contexto usan H2 en memoria,
segun `src/test/resources/application.properties`.

Si hay pruebas fallidas, Maven conserva los XML y los datos `target/jacoco.exec`,
pero detiene el ciclo antes de `verify`. Para inspeccionar la cobertura de esa
ejecucion sin ocultar fallos ni repetir pruebas:

```powershell
.\mvnw.cmd jacoco:report
.\mvnw.cmd site
```

Esto genera evidencia del codigo ejecutado; no convierte el build fallido en
un build aprobado.

## Docker local

El Dockerfile existente compila con `maven:3.9-eclipse-temurin-21` y ejecuta
el JAR con `eclipse-temurin:21-jre`. El build usa `-DskipTests`: no ejecuta
pruebas ni genera reportes de calidad. Los reportes locales no se sirven en Docker.

Requisitos: Docker Desktop iniciado con contenedores Linux y Docker Compose v2.
Desde PowerShell en la raiz del proyecto:

```powershell
Copy-Item .env.example .env
```

Completar `.env` con contrasenas locales y una clave JWT aleatoria de al menos
32 bytes para HS256. `.env` esta ignorado por Git y por Docker.
No copiar credenciales de Aiven, Render ni produccion.

`JWT_ISSUER` y `JWT_AUDIENCE` deben coincidir exactamente con el emisor JWT
local que genera los tokens. La configuracion principal los recibe mediante
variables y no define valores predeterminados. Los unicos valores literales
del repositorio (`connectaoficios-test` para ambos) pertenecen a las pruebas;
no se han asumido como valores del emisor de desarrollo.

`CLOUDINARY_URL` es obligatoria incluso si no se usan endpoints de imagen:
`CloudinaryConfig` crea el bean al iniciar y exige el prefijo `cloudinary://`.
El formato de la biblioteca es `cloudinary://API_KEY:API_SECRET@CLOUD_NAME`.
Para usar imagenes, proporcionar una cuenta de desarrollo. Una URL vacia o
`CAMBIAR_SI_ES_NECESARIO` no cumple la configuracion actual. La construccion
del bean no hace una subida; no se han probado credenciales ficticias ni se
ha validado el arranque del contenedor. No se desactiva ni modifica ese bean.

Validar y construir antes de iniciar:

```powershell
docker compose config --quiet
docker compose build
```

Solo si ambos comandos terminan correctamente:

```powershell
docker compose up -d
docker compose ps
docker compose logs --tail=150
docker images
```

La imagen propia se llama `connectaoficios-java-api:local`. MySQL utiliza
`mysql:8.4`, descargada al iniciar si todavia no existe localmente; el build
de la API no descarga automaticamente la imagen de MySQL.

### Puertos y red

- API: `http://localhost:8081`, enlazada solo a `127.0.0.1`; puerto interno 8080.
- MySQL: `127.0.0.1:3307`, porque 3306 estaba ocupado al preparar el proyecto.
  Puede cambiarse mediante `MYSQL_HOST_PORT` si el puerto elegido esta ocupado.
- JDBC interno: `jdbc:mysql://mysql:3306/ConnectaOficios?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`.
- Red bridge de Compose: `connectaoficios-local-network`.
- Contenedores: `connectaoficios-mysql` y `connectaoficios-java-api`.

La API siempre usa `mysql:3306`, independientemente del puerto publicado en
Windows. Compose no recibe un `DB_URL` externo: construye la URL con el servicio
local `mysql`. Los servicios se comunican por la red de Compose.

### Esquema y persistencia

En el primer inicio, la imagen oficial MySQL crea `MYSQL_DATABASE` y el usuario
configurado sobre el volumen vacio `connectaoficios-mysql-data`, montado en
`/var/lib/mysql`. El healthcheck usa `mysqladmin ping`; la API depende de MySQL
healthy. No se agrega Actuator ni un endpoint de health a la API.

Exclusivamente Compose proporciona `SPRING_JPA_HIBERNATE_DDL_AUTO=update`:
Hibernate usa las entidades existentes para crear las tablas faltantes.
`src/main/resources/application.properties` conserva
`spring.jpa.hibernate.ddl-auto=none`. No hay migraciones ni scripts nuevos.
La creacion de tablas no incluye datos iniciales ni usuarios/tokens externos.

Compose habilita DEBUG para `org.hibernate.tool.schema` a fin de inspeccionar
el proceso de esquema. Revisar los logs del primer arranque y comprobar que
no existan errores DDL; el resultado aun requiere validacion en Docker.
Para comprobar tablas sin exponer la contrasena en la linea de comandos:

```powershell
docker compose exec mysql mysql -u connectaoficios -p ConnectaOficios -e "SHOW TABLES;"
```

Introducir la contrasena local cuando se solicite. Adaptar usuario/base si se
cambiaron en `.env`. En un volumen ya inicializado, cambiar las variables de
MySQL no recrea la base ni cambia automaticamente sus credenciales.

```powershell
docker compose logs -f connectaoficios-java-api
docker compose logs -f mysql
docker compose stop
docker compose start
docker compose down
```

Tanto `stop`/`start` como `down` seguido de `up -d` conservan los datos.
**`docker compose down -v` elimina el volumen y los datos MySQL locales.**
No ejecutarlo para detener o reiniciar el entorno.

### Swagger

Springdoc no tiene rutas personalizadas y SecurityConfig permite
`/swagger-ui/**`, `/swagger-ui.html` y `/v3/api-docs/**`.
Tras iniciar correctamente, comprobar:

```powershell
Invoke-WebRequest http://localhost:8081/swagger-ui/index.html
Invoke-RestMethod http://localhost:8081/v3/api-docs
```

Rutas esperadas: `http://localhost:8081/swagger-ui/index.html` y
`http://localhost:8081/v3/api-docs`. Pendientes de comprobacion HTTP con los
contenedores en marcha.
