# ms-exchange-rate

Microservicio construido con [Quarkus](https://quarkus.io/) que expone el tipo de cambio del día
(consumido desde la API pública `https://free.e-api.net.pe`) y gestiona los clientes autorizados a
consultarlo. Cada cliente está limitado a **10 consultas por día** (hora de `America/Lima`); las
consultas se registran en PostgreSQL.

---

## Requisitos previos

| Elemento | Versión / detalle | Notas |
|---|---|---|
| **Java JDK** | **21** | El proyecto compila con `maven.compiler.release=21`. Verificar con `java -version`. |
| **Maven** | 3.9+ | No es obligatorio instalarlo: el repositorio incluye el wrapper `./mvnw` (`mvnw.cmd` en Windows). |
| **PostgreSQL** | 17.4 | Puede levantarse con el `docker-compose.yaml` incluido o usar una instalación local en el puerto `5432`. |
| **IntelliJ IDEA** | 2023.3+ (Community o Ultimate) | IDE recomendado. Se incluye configuración en `.idea/`. |
| **Conexión a internet** | — | Necesaria para consumir la API externa de tipo de cambio. |

### Plugins recomendados en IntelliJ IDEA

- **Quarkus** (soporte de `application.properties`, Dev Mode y run configurations)
- **Lombok** (el proyecto usa `@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`)
- Habilitar **Enable annotation processing**: `Settings → Build, Execution, Deployment → Compiler → Annotation Processors`
- Configurar el **SDK del proyecto** en Java 21: `File → Project Structure → Project SDK`

---

## Base de datos

El servicio se conecta con los siguientes valores (definidos en `src/main/resources/application.properties`):

| Propiedad | Valor |
|---|---|
| Motor | PostgreSQL |
| Host / Puerto | `localhost:5432` |
| Base de datos | `postgres` |
| Usuario | `admin` |
| Contraseña | `postgresMaster123` |
| Generación de esquema | `drop-and-create` (las tablas se recrean en cada arranque) |

### Levantar PostgreSQL con Docker

```shell
docker compose up -d
```

> **Importante:** el `docker-compose.yaml` crea la base de datos `mydatabase`, mientras que la
> aplicación apunta a `postgres` (que siempre existe en la imagen oficial). Si se desea usar
> `mydatabase`, hay que ajustar `quarkus.datasource.jdbc.url` en `application.properties`.

Las tablas se crean automáticamente al iniciar la aplicación:

- `Cliente` → `id`, `nombre`, `apellido`, `dni`
- `consultas_cambio` → `id`, `cliente_id`, `fecha_acceso`

---

## Cómo ejecutar el proyecto

### 1. Modo desarrollo (recomendado)

Con live reload y Dev UI activos:

```shell
./mvnw quarkus:dev
```

En Windows:

```shell
mvnw.cmd quarkus:dev
```

### 2. Desde IntelliJ IDEA

1. `File → Open…` y seleccionar la carpeta del proyecto (IntelliJ detecta el `pom.xml`).
2. Esperar a que Maven descargue las dependencias.
3. Ejecutar el goal `quarkus:dev` desde la pestaña **Maven → Plugins → quarkus**, o crear una
   **Run Configuration** de tipo *Quarkus* apuntando a este módulo.

### 3. Empaquetar y ejecutar como JAR

```shell
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Para generar un *über-jar*:

```shell
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

### 4. Ejecutable nativo (opcional)

```shell
./mvnw package -Dnative
```

Sin GraalVM instalado, compilando en un contenedor:

```shell
./mvnw package -Dnative -Dquarkus.native.container-build=true
./target/ms-exchange-rate-1.0.0-SNAPSHOT-runner
```

### 5. Ejecutar las pruebas

```shell
./mvnw test
```

---

## Puertos y enlaces de acceso

La aplicación corre en el puerto **8082** (`quarkus.http.port=8082`).

| Recurso | URL |
|---|---|
| **Base de la API (localhost)** | <http://localhost:8082> |
| **Swagger UI** | <http://localhost:8082/q/swagger-ui> |
| **Especificación OpenAPI (JSON/YAML)** | <http://localhost:8082/q/openapi> |
| **Dev UI de Quarkus** (solo en modo dev) | <http://localhost:8082/q/dev/> |
| Endpoints de clientes | <http://localhost:8082/api/cliente> |
| Endpoint de tipo de cambio | <http://localhost:8082/api/exchange-rate/{dni}> |
| PostgreSQL | `localhost:5432` |
| API externa de tipo de cambio | <https://free.e-api.net.pe/tipo-cambio/today.json> |

> Swagger UI está habilitado por defecto en modo dev y test. Para exponerlo también en producción,
> agregar en `application.properties`:
>
> ```properties
> quarkus.swagger-ui.always-include=true
> ```

---

## Endpoints

### Clientes — `/api/cliente`

#### `GET /api/cliente`

Lista todos los clientes registrados.

**Respuesta `200 OK`**

```json
[
  {
    "id": 1,
    "nombre": "Carlos",
    "apellido": "Rojas",
    "dni": "12345678"
  },
  {
    "id": 2,
    "nombre": "Ana",
    "apellido": "Pérez",
    "dni": "87654321"
  }
]
```

| Código | Descripción |
|---|---|
| `200` | Lista de clientes obtenida correctamente |
| `500` | Error interno del servidor |

---

#### `POST /api/cliente`

Registra un nuevo cliente.

**Request body**

```json
{
  "nombre": "Carlos",
  "apellido": "Rojas",
  "dni": "12345678"
}
```

**Respuesta `201 Created`** — incluye la cabecera `Location: http://localhost:8082/api/cliente/12345678`

```json
{
  "id": 1,
  "nombre": "Carlos",
  "apellido": "Rojas",
  "dni": "12345678"
}
```

| Código | Descripción |
|---|---|
| `201` | Cliente creado correctamente |
| `400` | Solicitud incorrecta — `nombre`, `apellido` y `dni` son obligatorios y no pueden estar vacíos o el DNI ya existe |
| `500` | Error interno del servidor |

---

#### `GET /api/cliente/{dni}`

Obtiene un cliente por su número de documento.

**Parámetro de ruta:** `dni` (string)

**Respuesta `200 OK`**

```json
{
  "id": 1,
  "nombre": "Carlos",
  "apellido": "Rojas",
  "dni": "12345678"
}
```

| Código | Descripción |
|---|---|
| `200` | Cliente encontrado correctamente |
| `400` | El DNI no puede estar vacío |
| `404` | Cliente no encontrado — `Cliente not found with dni: {dni}` |
| `500` | Error interno del servidor |

---

#### `PUT /api/cliente/{id}`

Actualiza los datos de un cliente existente.

**Parámetro de ruta:** `id` (long)

**Request body**

```json
{
  "nombre": "Carlos Eduardo",
  "apellido": "Rojas",
  "dni": "12345678"
}
```

**Respuesta `200 OK`**

```json
{
  "id": 1,
  "nombre": "Carlos Eduardo",
  "apellido": "Rojas",
  "dni": "12345678"
}
```

| Código | Descripción |
|---|---|
| `200` | Cliente actualizado correctamente |
| `400` | Solicitud incorrecta — `nombre`, `apellido` y `dni` son obligatorios o el DNI ingresado ya existe |
| `404` | Cliente no encontrado — `Cliente not found with id: {id}` |
| `500` | Error interno del servidor |

---

#### `DELETE /api/cliente/{id}`

Elimina un cliente.

**Parámetro de ruta:** `id` (long)

**Respuesta `204 No Content`** — sin cuerpo.

| Código | Descripción |
|---|---|
| `204` | Cliente eliminado correctamente |
| `404` | Cliente no encontrado — `Cliente not found with id: {id}` |
| `500` | Error interno del servidor |

---

### Tipo de cambio — `/api/exchange-rate`

#### `GET /api/exchange-rate/{dni}`

Devuelve el tipo de cambio del día para un cliente registrado y **registra la consulta**. Cada
cliente puede realizar hasta **10 consultas diarias** (`CambioService.LIMITE_DIARIO`), contadas
sobre el día calendario en la zona horaria `America/Lima`. Se ejecuta sobre *virtual threads*
(`@RunOnVirtualThread`).

**Parámetro de ruta:** `dni` (string) — debe corresponder a un cliente existente.

**Respuesta `200 OK`**

```json
{
  "fecha": "2026-10-21",
  "sunat": 3.512,
  "compra": 3.505,
  "venta": 3.520
}
```

**Respuesta `429 Too Many Requests`**

```json
{
  "mensaje": "Límite diario de consultas alcanzado para el DNI 12345678"
}
```

| Código | Descripción |
|---|---|
| `200` | Tipo de cambio obtenido correctamente (la consulta queda registrada) |
| `404` | Cliente no encontrado — `Cliente not found with dni: {dni}` |
| `429` | Límite diario de 10 consultas alcanzado para el DNI |
| `500` | Error interno del servidor o fallo al consumir la API externa |

---

## Ejemplos con cURL

```shell
# Crear un cliente
curl -i -X POST http://localhost:8082/api/cliente \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Carlos","apellido":"Rojas","dni":"12345678"}'

# Listar clientes
curl http://localhost:8082/api/cliente

# Buscar por DNI
curl http://localhost:8082/api/cliente/12345678

# Actualizar
curl -X PUT http://localhost:8082/api/cliente/1 \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Carlos Eduardo","apellido":"Rojas","dni":"12345678"}'

# Eliminar
curl -i -X DELETE http://localhost:8082/api/cliente/1

# Consultar el tipo de cambio
curl -i http://localhost:8082/api/exchange-rate/12345678
```

---

## Estructura del proyecto

```
src/main/java/com/nttdata/ms/exchange/rate/
├── api/
│   ├── CambioApi.java            # Cliente REST hacia free.e-api.net.pe
│   ├── CambioController.java     # GET /api/exchange-rate/{dni}
│   ├── ClienteController.java    # CRUD /api/cliente
│   ├── dto/                      # ClienteRequest, ClienteResponse, CambioConsultaResponse, ApiError
│   └── mapper/ClienteMapper.java # Entidad → DTO de respuesta
├── domain/
│   ├── Cliente.java              # Entidad JPA
│   ├── ConsultaCambio.java       # Entidad JPA (consultas_cambio)
│   └── CambioProxy.java          # Modelo de la respuesta externa
├── repository/                   # Repositorios Panache
└── service/                      # CambioService, ClienteService (reglas de negocio)
```

---

## Configuración (`application.properties`)

```properties
quarkus.http.port=8082

quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=admin
quarkus.datasource.password=postgresMaster123
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/postgres

quarkus.hibernate-orm.database.generation=drop-and-create

app.consultas.limite-diario=10
quarkus.rest-client.exchange-rate.url=https://free.e-api.net.pe
quarkus.rest-client.exchange-rate.connect-timeout=5000
quarkus.rest-client.exchange-rate.read-timeout=5000
```

---

## Stack técnico

- Quarkus **3.40.1.1** (BOM `io.quarkus.platform:quarkus-bom`)
- Quarkus REST + JSON-B (`quarkus-rest-jsonb`)
- Quarkus REST Client + JSON-B (`quarkus-rest-client-jsonb`)
- Hibernate ORM con Panache
- Driver JDBC PostgreSQL
- SmallRye OpenAPI / Swagger UI
- Lombok 1.18.36
- JUnit 5 + REST Assured

## Guías relacionadas

- [Quarkus REST — JSON serialisation](https://quarkus.io/guides/rest#json-serialisation)
- [Hibernate ORM con Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [Datasources / JDBC PostgreSQL](https://quarkus.io/guides/datasource)
- [REST Client](https://quarkus.io/guides/rest-client)
- [OpenAPI y Swagger UI](https://quarkus.io/guides/openapi-swaggerui)
- [Maven tooling y ejecutables nativos](https://quarkus.io/guides/maven-tooling)
