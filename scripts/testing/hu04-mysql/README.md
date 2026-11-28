# Ensayo HU-04 aislado (pendiente MySQL 8.4)

No conectar a defaultdb ni a una instancia existente. Usar un contenedor MySQL 8.4 nuevo sin publicar puertos ni montar volumenes existentes. Ejemplo, cuando Docker este disponible: `docker run --name hu04-mysql84 --network none -e MYSQL_ROOT_PASSWORD=<CLAVE_LOCAL_TEMPORAL> -d mysql:8.4`. No poner claves reales ni imprimirlas. Esperar disponibilidad mediante mysqladmin dentro del contenedor.

El archivo 01 es un fixture **reducido**, no una reproduccion completa del esquema HU-03. Crea solo las tablas y relaciones necesarias para el ensayo anterior; antes de declarar HU-04 listo hay que sustituirlo o complementarlo por una restauracion aislada y anonimizada del esquema completo posterior a HU-03. No instalar ni descargar imagenes automaticamente.

Dentro del contenedor de ensayo, ejecutar en orden: `01-escenario-reducido.sql`, seleccionar `hu04_ensayo`, `02-prevalidacion.sql`, `scripts/V004__hu04_chat_archivos_push.sql`, y `03-verificacion-reducida.sql`. El ultimo inserta un mensaje adicional solo despues de comprobar 4/51/15 y cambia deliberadamente el total a 52 para la prueba de orden. Nunca reutilizar el nombre de base o contenedor sin verificar su aislamiento. No reenviar entradas a un host remoto. El procedimiento detallado de inspeccion y recuperacion esta en docs/hu04-migracion.md.

En PowerShell, en un entorno ya preparado con Docker y con la imagen `mysql:8.4` disponible localmente, un operador puede usar estos comandos tras definir `HU04_MYSQL_PASS` como clave temporal local. No usar un contenedor existente ni sustituir el nombre por uno de produccion:

```powershell
docker run --name hu04-mysql84 --network none -e MYSQL_ROOT_PASSWORD=$env:HU04_MYSQL_PASS -d mysql:8.4
docker exec -e MYSQL_PWD=$env:HU04_MYSQL_PASS hu04-mysql84 mysqladmin -u root ping
Get-Content -Raw scripts/testing/hu04-mysql/01-escenario-reducido.sql | docker exec -i -e MYSQL_PWD=$env:HU04_MYSQL_PASS hu04-mysql84 mysql -u root
Get-Content -Raw scripts/testing/hu04-mysql/02-prevalidacion.sql | docker exec -i -e MYSQL_PWD=$env:HU04_MYSQL_PASS hu04-mysql84 mysql -u root hu04_ensayo
Get-Content -Raw scripts/V004__hu04_chat_archivos_push.sql | docker exec -i -e MYSQL_PWD=$env:HU04_MYSQL_PASS hu04-mysql84 mysql -u root hu04_ensayo
Get-Content -Raw scripts/testing/hu04-mysql/03-verificacion-reducida.sql | docker exec -i -e MYSQL_PWD=$env:HU04_MYSQL_PASS hu04-mysql84 mysql -u root
```

Revisar `$LASTEXITCODE` tras cada comando y detenerse ante cualquier error. El fixture no cumple aun el requisito de esquema completo y no autoriza produccion. Al terminar, detener el contenedor aislado y seguir la politica local de limpieza; no afectar otros contenedores.
