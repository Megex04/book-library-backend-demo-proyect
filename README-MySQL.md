# Configuración de MySQL para Biblio-System

Este documento proporciona instrucciones para configurar y ejecutar Biblio-System con una base de datos MySQL.

## Requisitos previos

- MySQL Server 8.0 o superior instalado y en ejecución
- Java 17 o superior
- Maven 3.6 o superior

## Configuración de la base de datos

### 1. Crear la base de datos

Puedes crear la base de datos manualmente ejecutando el siguiente comando en MySQL:

```sql
CREATE DATABASE bibliodb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

O puedes utilizar el script `schema.sql` que se encuentra en `src/main/resources/db/mysql/`.

### 2. Configurar usuario y contraseña

Asegúrate de tener un usuario con permisos suficientes para acceder a la base de datos. Por defecto, la aplicación está configurada para usar:

- Usuario: `root`
- Contraseña: `password`

Si deseas utilizar credenciales diferentes, debes modificar el archivo `application-prod.properties`.

### 3. Inicializar la base de datos

Puedes inicializar la estructura de la base de datos y cargar datos iniciales ejecutando los scripts SQL proporcionados:

**Para el entorno de producción:**
```bash
mysql -u root -p < src/main/resources/db/mysql/schema.sql
mysql -u root -p < src/main/resources/db/mysql/data.sql
```

**Para el entorno de desarrollo:**
```bash
mysql -u root -p < src/main/resources/db/mysql/schema-dev.sql
mysql -u root -p < src/main/resources/db/mysql/data-dev.sql
```

**Nota:** El script de desarrollo incluye más datos de ejemplo para facilitar las pruebas.

## Configuración de la aplicación

### Perfiles de configuración

La aplicación tiene tres perfiles de configuración:

1. **dev**: Utiliza MySQL como base de datos (configuración para desarrollo)
2. **prod**: Utiliza MySQL como base de datos (configuración optimizada para producción)
3. **h2**: Utiliza H2 como base de datos en memoria (útil para pruebas rápidas sin necesidad de MySQL)

### Diferencias entre perfiles MySQL

**Perfil dev (MySQL para desarrollo)**:
- Base de datos: `bibliodb_dev`
- Modo DDL: `update` (actualiza automáticamente el esquema)
- SQL visible en logs: `true`
- Pool de conexiones: más pequeño (5 conexiones máximo)

**Perfil prod (MySQL para producción)**:
- Base de datos: `bibliodb`
- Modo DDL: `validate` (verifica que el esquema coincida con las entidades)
- SQL visible en logs: `false`
- Pool de conexiones: más grande (10 conexiones máximo)
- Configuración optimizada para rendimiento

### Cambiar el perfil activo

Puedes cambiar el perfil activo de varias formas:

1. **En application.properties**:
   ```properties
   spring.profiles.active=prod
   ```

2. **Al ejecutar la aplicación**:
   ```bash
   java -jar biblio-system.jar --spring.profiles.active=prod
   ```

3. **Con Maven**:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

4. **Para usar H2 en lugar de MySQL**:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=h2
   ```

## Configuración de producción

Para un entorno de producción, se recomienda:

1. Cambiar las credenciales de la base de datos
2. Configurar un secreto JWT seguro
3. Configurar correctamente el correo electrónico

Puedes hacerlo mediante variables de entorno:

```bash
export SPRING_DATASOURCE_USERNAME=tu_usuario
export SPRING_DATASOURCE_PASSWORD=tu_contraseña
export JWT_SECRET=tu_secreto_jwt_seguro
export MAIL_USERNAME=tu_email@dominio.com
export MAIL_PASSWORD=tu_contraseña_de_aplicacion
export ALLOWED_ORIGINS=https://tu-dominio.com
```

## Solución de problemas

### Error de conexión a la base de datos

Si encuentras errores de conexión, verifica:

1. Que MySQL esté en ejecución
2. Que las credenciales sean correctas
3. Que la base de datos exista
4. Que el usuario tenga permisos suficientes

### Error de esquema

Si encuentras errores relacionados con el esquema de la base de datos:

1. Asegúrate de haber ejecutado correctamente los scripts SQL
2. Si estás utilizando `spring.jpa.hibernate.ddl-auto=validate` en producción, asegúrate de que el esquema coincida con las entidades

## Migración desde H2

Si estás migrando datos desde H2 a MySQL, considera utilizar herramientas como Flyway o Liquibase para gestionar la migración de forma controlada.