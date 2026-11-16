# Ambientes de desarrollo, pruebas y ejecución

## 1. Ambiente de desarrollo

| Elemento | Versión / valor |
|---|---|
| Sistema operativo | Windows 11 (también funciona en Linux y macOS) |
| JDK | Eclipse Temurin 21 (LTS) |
| Construcción | Apache Maven mediante Maven Wrapper (`mvnw`, `mvnw.cmd`); no requiere instalar Maven |
| IDE | Visual Studio Code con *Extension Pack for Java* (Red Hat Java, Debugger, Test Runner, Maven) |
| Control de versiones | Git 2.x + GitHub |
| Pruebas de API | Postman (colección en `postman/`) |
| Base de datos | MySQL 8 (producción) · H2 2 en modo MySQL (perfil `demo` y pruebas) |

Abrir el proyecto en VS Code: **Archivo → Abrir carpeta** → `gestion-pedidos-spring`. La extensión de Java
reconoce el `pom.xml`, descarga las dependencias y muestra las pruebas en la vista *Testing*.

## 2. Perfiles y configuración

| Archivo | Perfil | Uso |
|---|---|---|
| `src/main/resources/application.properties` | por defecto | MySQL `sena_gestion_pedidos` en `localhost:3306` |
| `src/main/resources/application-demo.properties` | `demo` | H2 en memoria, sin instalar nada |
| `src/main/resources/db/schema.sql` | todos | Tablas `usuario`, `producto`, `pedido`, `detalle_pedido` |
| `src/main/resources/db/data.sql` | todos | Usuarios, productos y pedidos de prueba |

Variables de entorno opcionales para MySQL: `DB_URL`, `DB_USUARIO` (por defecto `root`), `DB_CLAVE`.

## 3. Servidor de aplicaciones

- Tomcat 11 embebido en el JAR (Spring Boot), puerto **8080** (`server.port`).
- Codificación UTF-8 forzada, sesión de 30 minutos solo por cookie.
- Zona horaria de negocio `America/Bogota` (`RelojConfig`).

## 4. Ambiente de pruebas

| Tipo | Herramienta | Comando |
|---|---|---|
| Unitarias (sin Spring ni BD) | JUnit 5 + doble de prueba en memoria | `mvnw test` |
| Integración (servicios, vistas, API) | JUnit 5 + aplicación completa con H2 | `mvnw test` |
| Cobertura | JaCoCo 0.8.13 | `mvnw verify` → `target/site/jacoco/index.html` |
| API manual / automática | Postman | *Run collection* |
| Estándar de codificación | `herramientas/VerificadorEstandar.java` | `java herramientas/VerificadorEstandar.java` |

Resultado de la versión 1.3.0: **85 pruebas, 0 fallos, 0 errores**; cobertura de instrucciones 86 %,
de líneas 86,5 % y de ramas 77 %.

## 5. Compilar y ejecutar

```bash
# Windows: use mvnw.cmd en lugar de ./mvnw
./mvnw -B verify                    # compila, ejecuta las 85 pruebas, genera cobertura y el JAR
java -jar target/gestion-pedidos-spring-1.3.0.jar --spring.profiles.active=demo   # sin MySQL
java -jar target/gestion-pedidos-spring-1.3.0.jar                                 # con MySQL
```

Abrir <http://localhost:8080> e ingresar con los usuarios de prueba del `README.md`.
