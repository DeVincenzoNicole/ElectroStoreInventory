# ElectroStore Inventory API

## Descripcion
ElectroStore Inventory API es una solucion RESTful desarrollada en Java con Spring Boot para la gestion de inventario de productos en múltiples sucursales. Permite controlar el stock por sucursal, consultar el stock centralizado, y está lista para escalar a microservicios.

## Dependencias principales
- Spring Boot
- Spring Data JPA
- Spring Security (JWT)
- Swagger/OpenAPI
- Kafka (mensajería)
- Redis (caché distribuido)
- Resilience4j (tolerancia a fallos)
- Micrometer (métricas)

## Arquitectura y diseño
- **Modelo de datos:** Clave compuesta en `Product` (`id`, `storeId`) para gestionar stock por sucursal y sumar stock centralizado.
- **Controladores REST:** Endpoints para gestion de inventario y autenticacion.
- **Servicios:** Logica de negocio, validaciones, caché y eventos.
- **Eventos:** Internos (`InventoryChangeEvent`) y externos (Kafka).
- **Seguridad:** Autenticacion JWT y roles por endpoint.
- **Caché:** Redis opcional para acelerar consultas frecuentes.
- **Tolerancia a fallos:** Resilience4j y Spring Retry.

## Endpoints principales
- `GET /inventory/{storeId}`: Consulta inventario por sucursal.
- `PATCH /inventory/{storeId}/products/{productId}/stock`: Actualiza stock de producto.
- `GET /inventory/central/{productId}`: Consulta stock total de producto.
- `POST /inventory/{storeId}/products`: Crea producto en sucursal.
- `DELETE /inventory/{storeId}/products/{productId}`: Elimina producto de sucursal.
- `POST /auth/login`: Obtiene token JWT.

## Ejecucion y configuracion
1. **Requisitos:** Java 17+, Maven
2. **Configura la base de datos:** Por defecto H2 en memoria (`application.properties`).
3. **Compila y ejecuta:**
   ```bash
   mvn spring-boot:run
   ```
4. **Swagger UI:**
   - `http://localhost:8080/swagger-ui.html` o `http://localhost:8080/swagger-ui/index.html`

## Uso de Redis (caché distribuido)
- Redis es opcional. Para activarlo:
  1. Levanta Redis localmente (ejemplo con Docker):
     ```bash
     docker run -d -p 6379:6379 redis
     ```
  2. Configura en `application.properties`:
     ```
     spring.cache.type=redis
     spring.redis.host=localhost
     spring.redis.port=6379
     spring.cache.redis.time-to-live=10000
     ```
  3. Reinicia la aplicacion.
- Beneficios: Consultas más rápidas y escalables, caché compartido entre instancias.

## Uso de Kafka (eventos de inventario)
- Para desarrollo, puedes levantar un broker Kafka local con Docker:
  ```bash
  docker run -d --name kafka -p 9092:9092 -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 wurstmeister/kafka
  ```
- La aplicacion publica eventos en el topico `inventory-events` cada vez que se actualiza el stock.
- Un listener (`InventoryKafkaListener`) procesa estos eventos para auditoría o integracion externa.

## Autenticacion y pruebas en Swagger
- Usa `/auth/login` para obtener un token JWT.
- Usuarios de ejemplo:
  - admin / adminpass (rol ADMIN)
  - user / userpass (rol USER)
- Usa el boton "Authorize" en Swagger y pega el token con el formato `Bearer <token>`.

## Pruebas y cobertura
- Ejecuta los tests con Maven:
  ```bash
  mvn test
  ```
- Hay tests unitarios y de integracion para los endpoints principales y la integracion con Kafka.
- La cobertura incluye casos de éxito y error, validaciones y seguridad.

## Migracion y escalabilidad
- El diseño favorece la migracion a microservicios, con eventos, mensajería y separacion de capas.
- Recomendaciones para escalar:
  - Separar modulos en servicios independientes.
  - Usar eventos y mensajería para sincronizacion.
  - Desplegar y escalar cada servicio según demanda.

## Notas adicionales
- Mensajes de error y éxito claros y específicos.
- Listo para agregar reportes, auditoría, integracion con sistemas externos y nuevas funcionalidades.
- Métricas personalizadas disponibles en `/actuator/metrics/inventory.stock.updates`.
- Documentacion interactiva completa en Swagger UI.

---
ElectroStore Inventory API © 2025