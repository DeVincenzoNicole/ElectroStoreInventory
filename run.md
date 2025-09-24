# Como ejecutar ElectroStore Inventory API

## Requisitos previos
- Java 17 o superior
- Maven

## Pasos para ejecutar la aplicacion

1. **Clona el repositorio o descarga el proyecto.**

2. **Configura la base de datos:**
   - Por defecto, la aplicacion utiliza H2 en memoria.
   - Puedes modificar la configuracion en `src/main/resources/application.properties` para usar otra base de datos si lo deseas.
   - El esquema de la base de datos se encuentra en `src/main/resources/schema.sql`.
   - Puedes agregar datos iniciales en `src/main/resources/data.sql`.

3. **(Opcional) Configura Redis para caché distribuido:**
   - Para activar Redis, levanta un servidor local (ejemplo con Docker):
     ```bash
     docker run -d -p 6379:6379 redis
     ```
   - Configura en `application.properties`:
     ```
     spring.cache.type=redis
     spring.redis.host=localhost
     spring.redis.port=6379
     spring.cache.redis.time-to-live=10000
     ```
   - Reinicia la aplicacion.

4. **(Opcional) Configura Kafka para eventos de inventario:**
   - Para desarrollo, puedes levantar un broker Kafka local con Docker:
     ```bash
     docker run -d --name kafka -p 9092:9092 -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 wurstmeister/kafka
     ```
   - La aplicacion publicará eventos en el topico `inventory-events`.

5. **Compila y ejecuta la aplicacion:**
   - Abre una terminal en la raíz del proyecto.
   - Ejecuta:
     ```bash
     mvn spring-boot:run
     ```
   - Alternativamente, puedes compilar el JAR y ejecutarlo:
     ```bash
     mvn clean package
     java -jar target/inventory-0.0.1-SNAPSHOT.jar
     ```

6. **Accede a la API:**
   - La API estará disponible en: `http://localhost:8080/inventory`

7. **Accede a la documentacion Swagger/OpenAPI:**
   - Documentacion interactiva en:
     - `http://localhost:8080/swagger-ui.html`
     - o `http://localhost:8080/swagger-ui/index.html`

8. **Prueba la API y autenticacion JWT en Swagger:**
   - Ve al endpoint `/auth/login` en Swagger UI.
   - Usa uno de los siguientes usuarios:
     - admin / adminpass (rol ADMIN)
     - user / userpass (rol USER)
   - Realiza una peticion POST y copia el token JWT recibido.
   - Haz clic en "Authorize" y pega el token en el formato:
     ```
     Bearer <token>
     ```
   - Ahora puedes probar los endpoints protegidos.

9. **Ejecuta los tests unitarios y de integracion:**
   - Ejecuta los tests con Maven:
     ```bash
     mvn test
     ```
   - Los tests cubren los endpoints principales y la integracion con Kafka.

10. **Más informacion y ejemplos de uso:**
    - Consulta el archivo `README.md` para detalles avanzados, ejemplos de cuerpos de solicitud y recomendaciones para escalar el sistema.

---
ElectroStore Inventory API © 2025