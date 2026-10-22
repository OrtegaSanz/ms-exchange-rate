# ms-exchange-rate

Microservicio construido con [Quarkus](https://quarkus.io/) que expone el tipo de cambio del día
(consumido desde la API pública `https://free.e-api.net.pe`), gestiona los clientes y les permite
**cambiar montos de soles a dólares** al tipo de cambio venta del día.

- La **consulta** del tipo de cambio es pública: no requiere cliente, no tiene límite y no se registra.
- Las **operaciones de cambio** (cuando se ingresa un monto) sí se registran en PostgreSQL junto con
  el tipo de cambio aplicado. Hay dos sentidos, **soles → dólares** y **dólares → soles**, y ambos
  comparten el mismo contador: en total **10 operaciones por día** por cliente (día calendario en la
  zona horaria `America/Lima`).

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

- `Cliente` → `id`, `nombre`, `apellido`, `dni` (único)
- `consultas_cambio` → `id`, `cliente_id`, `fecha_acceso`, `direccion`, `monto_soles`, `monto_dolares`, `tipo_cambio_aplicado`

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
| Consulta del tipo de cambio | <http://localhost:8082/api/exchange-rate> |
| Cambio soles → dólares (POST) | <http://localhost:8082/api/exchange-rate/{dni}/soles-a-dolares> |
| Cambio dólares → soles (POST) | <http://localhost:8082/api/exchange-rate/{dni}/dolares-a-soles> |
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

#### `GET /api/exchange-rate`

Devuelve el tipo de cambio del día tal como lo publica el proveedor externo. **Es público: no
requiere un cliente registrado, no tiene límite diario y no registra ninguna operación.** Se ejecuta
sobre *virtual threads* (`@RunOnVirtualThread`).

**Respuesta `200 OK`**

```json
{
  "fecha": "2026-10-21",
  "sunat": 3.512,
  "compra": 3.505,
  "venta": 3.520
}
```

| Código | Descripción |
|---|---|
| `200` | Tipo de cambio del día obtenido correctamente |
| `503` | El proveedor externo no está disponible o no devolvió un tipo de cambio venta válido |

---

#### `POST /api/exchange-rate/{dni}/soles-a-dolares`

Cambia un monto en **soles a dólares** aplicando el **tipo de cambio venta** del día (el cliente
compra dólares) y **registra la operación** con el monto y la tasa aplicada.

**Parámetro de ruta:** `dni` (string) — debe corresponder a un cliente registrado.

**Request body**

```json
{
  "monto": 350.00
}
```

**Respuesta `200 OK`** — `montoDestino = monto / venta`, redondeado a 2 decimales (`HALF_UP`)

```json
{
  "dni": "12345678",
  "direccion": "SOLES_A_DOLARES",
  "montoOrigen": 350.00,
  "monedaOrigen": "PEN",
  "montoDestino": 99.43,
  "monedaDestino": "USD",
  "tipoCambioAplicado": 3.520,
  "fechaOperacion": "2026-10-21T16:42:10.512Z",
  "operacionesRealizadasHoy": 3,
  "limiteDiario": 10
}
```

| Código | Descripción |
|---|---|
| `200` | Cambio realizado y registrado correctamente |
| `400` | El monto a cambiar debe ser mayor a cero (nulo, cero o negativo) |
| `404` | Cliente no encontrado — `Cliente not found with dni: {dni}` |
| `429` | Límite diario de operaciones de cambio alcanzado para el DNI |
| `503` | El proveedor externo no está disponible o no devolvió un tipo de cambio válido |

---

#### `POST /api/exchange-rate/{dni}/dolares-a-soles`

Cambia un monto en **dólares a soles** aplicando el **tipo de cambio compra** del día (el cliente
vende dólares) y **registra la operación**. Comparte el mismo contador diario que el sentido
anterior: las dos direcciones suman hacia el mismo límite de 10 operaciones.

**Parámetro de ruta:** `dni` (string) — debe corresponder a un cliente registrado.

**Request body**

```json
{
  "monto": 100.00
}
```

**Respuesta `200 OK`** — `montoDestino = monto * compra`, redondeado a 2 decimales (`HALF_UP`)

```json
{
  "dni": "12345678",
  "direccion": "DOLARES_A_SOLES",
  "montoOrigen": 100.00,
  "monedaOrigen": "USD",
  "montoDestino": 350.50,
  "monedaDestino": "PEN",
  "tipoCambioAplicado": 3.505,
  "fechaOperacion": "2026-10-21T16:45:02.118Z",
  "operacionesRealizadasHoy": 4,
  "limiteDiario": 10
}
```

| Código | Descripción |
|---|---|
| `200` | Cambio realizado y registrado correctamente |
| `400` | El monto a cambiar debe ser mayor a cero (nulo, cero o negativo) |
| `404` | Cliente no encontrado — `Cliente not found with dni: {dni}` |
| `429` | Límite diario de operaciones de cambio alcanzado para el DNI |
| `503` | El proveedor externo no está disponible o no devolvió un tipo de cambio válido |

**Respuesta `429 Too Many Requests`** (igual en ambos sentidos)

```json
{
  "message": "Limite diario de 10 operaciones de cambio alcanzado para el DNI 12345678"
}
```

> `operacionesRealizadasHoy` cuenta **todas** las operaciones del cliente ese día, en los dos
> sentidos, incluyendo la que acaba de realizarse.

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

# Consultar el tipo de cambio del día (público, sin límite)
curl http://localhost:8082/api/exchange-rate

# Cambiar 350 soles a dólares (cuenta para el límite diario)
curl -i -X POST http://localhost:8082/api/exchange-rate/12345678/soles-a-dolares \
  -H "Content-Type: application/json" \
  -d '{"monto": 350.00}'

# Cambiar 100 dólares a soles (cuenta para el mismo límite diario)
curl -i -X POST http://localhost:8082/api/exchange-rate/12345678/dolares-a-soles \
  -H "Content-Type: application/json" \
  -d '{"monto": 100.00}'
```

---

## Estructura del proyecto

```
src/main/java/com/nttdata/ms/exchange/rate/
├── api/
│   ├── CambioApi.java            # Cliente REST hacia free.e-api.net.pe
│   ├── CambioController.java     # GET /api/exchange-rate y los dos POST de cambio
│   ├── ClienteController.java    # CRUD /api/cliente
│   ├── dto/                      # ClienteRequest/Response, CambioRequest, CambioConsultaResponse, ApiError
│   └── mapper/                   # ClienteMapper, CambioMapper (dominio → DTO)
├── domain/
│   ├── Cliente.java              # Entidad JPA
│   ├── ConsultaCambio.java       # Entidad JPA (consultas_cambio): operación de cambio registrada
│   ├── DireccionCambio.java      # Enum: SOLES_A_DOLARES / DOLARES_A_SOLES
│   ├── ResultadoCambio.java      # Operación + estado del límite diario
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

# limite de operaciones de cambio por cliente y por dia
app.consultas.limite-diario=10

quarkus.rest-client.exchange-rate.url=https://free.e-api.net.pe
quarkus.rest-client.exchange-rate.connect-timeout=5000
quarkus.rest-client.exchange-rate.read-timeout=5000
```

---

## Reglas de negocio

1. El tipo de cambio del día se consulta libremente en `GET /api/exchange-rate`: sin cliente y sin límite.
2. Para **cambiar montos** el cliente debe estar registrado (`POST /api/cliente`).
3. El tipo de cambio aplicado depende del sentido de la operación:

   | Sentido | Tasa aplicada | Cálculo |
   |---|---|---|
   | `SOLES_A_DOLARES` (el cliente compra dólares) | `venta` | `montoDolares = montoSoles / venta` |
   | `DOLARES_A_SOLES` (el cliente vende dólares) | `compra` | `montoSoles = montoDolares * compra` |

   En los dos casos el resultado se redondea a 2 decimales con `HALF_UP`.
4. Cada operación se registra en `consultas_cambio` con la dirección, los dos montos y la tasa
   aplicada — y es lo que cuenta para el límite diario.
5. El límite es de **10 operaciones por cliente y por día** (`app.consultas.limite-diario`), contadas
   sobre el día calendario en `America/Lima`. **Las dos direcciones comparten el mismo contador.**
   Al superarlo, la API responde `429`.

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
