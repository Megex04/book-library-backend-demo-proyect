# 📚 Biblioteca Backend API

Sistema de gestión de biblioteca robusto y escalable construido con **Java 21** y **Spring Boot 3**. Este backend proporciona una API RESTful segura para gestionar usuarios, libros, categorías y préstamos, incluyendo validación por correo electrónico y control de stock.

## 🚀 Tecnologías

*   **Lenguaje:** Java 21
*   **Framework:** Spring Boot 3.x
*   **Base de Datos:** PostgreSQL
*   **Seguridad:** Spring Security 6 + JWT (JSON Web Tokens)
*   **Documentación:** Swagger / OpenAPI
*   **Contenedores:** Docker & Docker Compose
*   **Herramientas:** Maven, Lombok

## ✨ Funcionalidades Principales

### 🔐 Autenticación y Seguridad
*   **Registro de Usuarios:** Con validación de email mediante código OTP (6 dígitos).
*   **Login:** Autenticación vía JWT.
*   **Roles:** Sistema RBAC con roles `ADMIN`, `LIBRARIAN` y `MEMBER`.
*   **Protección:** Endpoints protegidos según el rol del usuario.

### 📖 Gestión de Libros (Inventario)
*   CRUD completo de Libros y Categorías.
*   Control automático de **Stock** y disponibilidad.
*   Búsqueda y filtrado.

### 🔄 Préstamos (Core Business)
*   Solicitud de préstamos con validaciones de negocio (Stock, límite de libros, deudas).
*   Devolución de libros con cálculo de fechas.
*   Historial de préstamos por usuario.

## 🛠️ Requisitos Previos

*   Java JDK 21
*   Docker y Docker Compose
*   Maven (o usar el wrapper incluido `./mvnw`)

## ⚙️ Instalación y Ejecución

### 1. Clonar el repositorio
```bash
git clone https://github.com/TU_USUARIO/biblioteca-backend.git
cd biblioteca-backend
```
### 2. Levantar la Base de Datos
Usamos Docker Compose para levantar PostgreSQL y pgAdmin rápidamente.

```bash
docker-compose up -d
```
### 3. Configurar Variables de Entorno
   El archivo de configuración original application.yml está ignorado por seguridad. Debes crear uno nuevo basado en el ejemplo.

Renombra o copia src/main/resources/application.yml.example a src/main/resources/application.yml.
Edita el archivo con tus credenciales reales:
```
spring:
  application:
    name: book-library

  # Configuración de Base de Datos
  datasource:
    url: jdbc:postgresql://localhost:5432/biblioteca_db
    username: postgres
    password: TU_PASSWORD_AQUI
    driver-class-name: org.postgresql.Driver

  # Configuración de JPA / Hibernate
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true # Muestra las consultas SQL en la consola (útil para debug)
    properties:
      hibernate:
        format_sql: true # Formatea el SQL para que sea legible
        dialect: org.hibernate.dialect.PostgreSQLDialect
  mail:
    host: smtp.gmail.com # O sandbox.smtp.mailtrap.io
    port: 587
    username: TU_USUARIO_EMAIL
    password: TU_PASSWORD_EMAIL
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
  # Configuración básica de JWT
  security:
    jwt:
      secret-key: PON_AQUI_TU_CLAVE_BASE64_DE_256_BITS # Clave larga y segura (Hex o Base64)
      expiration-time: 3600000 # 1 hora en milisegundos

# Configuración de Logs
logging:
  level:
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE # Para ver los valores de los ? en las queries
```
### 4. Ejecutar la aplicación
   ```
   ./mvnw spring-boot:run
   ```
   La aplicación iniciará en http://localhost:8080.

### 📄 Documentación de la API (Swagger)
Una vez iniciada la aplicación, puedes ver y probar todos los endpoints en la interfaz interactiva de Swagger UI:

👉 http://localhost:8080/swagger-ui/index.html

### 📂 Estructura del Proyecto
- src/main/java/com/biblioteca/api
```
├── config/          # Configuraciones (Security, AppConfig)
├── controller/      # Controladores REST (Endpoints)
├── dto/             # Data Transfer Objects (Records)
├── exception/       # Manejo global de errores
├── model/           # Entidades JPA (User, Book, Loan)
├── repository/      # Interfaces de acceso a datos
└── service/         # Lógica de negocio
```
### 🤝 Contribución
Haz un Fork del proyecto
Crea una rama para tu feature (git checkout -b feature/AmazingFeature)
Haz Commit de tus cambios (git commit -m 'Add some AmazingFeature')
Haz Push a la rama (git push origin feature/AmazingFeature)
Abre un Pull Request
### 📝 Licencia
Distribuido bajo la licencia MIT. Ver LICENSE para más información.