# Prueba Técnica Backend – Microservicios Clientes y Cuentas

Dos microservicios con **comunicación asíncrona** (RabbitMQ), desarrollados con **Java 21, Spring Boot 4, Gradle, PostgreSQL y JPA**, y desplegables con **Docker Compose**.

| Microservicio | Entidades | Puerto |
|---|---|---|
| **ms-clientes** | `Persona`, `Cliente` | 8081 |
| **ms-cuentas** | `Cuenta`, `Movimiento` (+ reporte *Estado de cuenta*) | 8082 |

## Contenido

1. [Despliegue rápido con Docker](#1-despliegue-rápido-con-docker)
2. [Cargar los datos de la guía y probar](#2-cargar-los-datos-de-la-guía-y-probar)
3. [API REST](#3-api-rest)
4. [Pruebas](#4-pruebas)
5. [Arquitectura y decisiones](#5-arquitectura-y-decisiones)
6. [Rendimiento, escalabilidad y resiliencia](#6-rendimiento-escalabilidad-y-resiliencia)
7. [Limitaciones conocidas](#7-limitaciones-conocidas)
8. [Estructura del repositorio](#8-estructura-del-repositorio)

---

## 1. Despliegue rápido con Docker

**Requisitos:** Docker con Docker Compose v2 (`docker compose`). Nada más: el JDK y Gradle se usan dentro de la construcción de las imágenes.

```bash
docker compose up --build
```

Levanta cuatro contenedores, en este orden (con *healthchecks* y `depends_on`):

| Servicio | URL en el host | Notas |
|---|---|---|
| PostgreSQL 16 | `localhost:5432` | BD `bancodb`, usuario/clave `postgres`/`postgres`. Crea los esquemas `clientes` y `cuentas` desde **`BaseDatos.sql`** en el primer arranque |
| RabbitMQ 3 | `localhost:5672` · consola http://localhost:15672 | usuario/clave `guest`/`guest` |
| ms-clientes | http://localhost:8081 | Swagger: http://localhost:8081/swagger-ui.html |
| ms-cuentas | http://localhost:8082 | Swagger: http://localhost:8082/swagger-ui.html |

La primera construcción tarda unos minutos (descarga de dependencias). Estará listo cuando `docker compose ps` muestre los cuatro servicios como `healthy`.

**Si falla el primer arranque por RabbitMQ.** Si `docker compose up --build` informa `dependency rabbitmq failed to start`, vuelve a ejecutar el arranque apuntando a los microservicios:

```bash
docker compose up -d ms-clientes ms-cuentas
```

Compose también iniciará o comprobará PostgreSQL y RabbitMQ según sus dependencias, y arrancará los microservicios cuando estén saludables. Este reintento no borra los datos de la base ni los volúmenes.

**Puertos ocupados.** Los puertos del host se pueden cambiar sin editar el archivo:

```bash
POSTGRES_PORT=15432 RABBIT_PORT=15673 RABBIT_UI_PORT=25672 CLIENTES_PORT=9081 CUENTAS_PORT=9082 docker compose up --build
```

**Empezar de cero** (borra los datos; el script `BaseDatos.sql` solo se ejecuta cuando la BD es nueva):

```bash
docker compose down -v
```

**Usar `BaseDatos.sql` en otra base PostgreSQL:** el script es re-ejecutable (`IF NOT EXISTS`) y crea los dos esquemas con sus tablas e índices. Los servicios se configuran con variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `RABBIT_HOST`, `RABBIT_PORT`, `RABBIT_USER`, `RABBIT_PASSWORD`, `SERVER_PORT`); los valores por defecto apuntan a `localhost`.

**Ejecutar los servicios sin contenedores** (solo la infraestructura en Docker):

```bash
docker compose up -d postgres rabbitmq
./gradlew :ms-clientes:bootRun      # en otra terminal: ./gradlew :ms-cuentas:bootRun
```

## 2. Cargar los datos de la guía y probar

### Con Postman (recomendado)

Importar [`postman/BP-Microservicios.postman_collection.json`](postman/BP-Microservicios.postman_collection.json) y ejecutar las carpetas **en orden** con el *Runner*. Variables de la colección: `baseUrlClientes` (`http://localhost:8081`) y `baseUrlCuentas` (`http://localhost:8082`).

| Carpeta | Qué hace |
|---|---|
| 1. Casos de uso – Clientes (8.1) | Crea Jose Lema, Marianela Montalvo y Juan Osorio |
| 2. Casos de uso – Cuentas (8.2 y 8.3) | Crea las 5 cuentas de la guía |
| 3. Casos de uso – Movimientos (8.4) | Retiro 575 → 1425 · depósito 600 → 700 · depósito 150 → 150 · retiro 540 → 0 |
| 4–5. CRUD / CRU | Operaciones de lectura y actualización |
| 6–7. Errores | 400, 404, 409 y **"Saldo no disponible"** |
| 8. Reporte – Estado de cuenta (F4) | El reporte 8.5 / 8.6 |

Notas: la carpeta 1 solo funciona en una BD vacía (si se repite responde 409 porque los clientes ya existen), y los clientes llegan a ms-cuentas **por eventos asíncronos**, por eso el primer request de cuentas espera 1,5 s.

Los datos de las fechas del reporte (10/2/2022 y 8/2/2022) se cargan enviando la `fecha` en el movimiento (ver abajo).

### Con curl (los casos de uso de la guía)

```bash
# 8.1 Clientes (el clienteId se elige libremente; si se omite, se genera un UUID)
curl -X POST localhost:8081/api/clientes -H 'Content-Type: application/json' -d '{
  "clienteId":"mmontalvo","nombre":"Marianela Montalvo","genero":"FEMALE","edad":30,
  "identificacion":"1700000002","direccion":"Amazonas y NNUU","telefono":"097548965","contrasena":"5678"}'

# 8.2 Cuentas (esperar ~1 s a que el cliente llegue a ms-cuentas)
curl -X POST localhost:8082/api/cuentas -H 'Content-Type: application/json' -d '{
  "numeroCuenta":"225487","tipoCuenta":"CORRIENTE","saldoInicial":100,"clienteId":"mmontalvo"}'
curl -X POST localhost:8082/api/cuentas -H 'Content-Type: application/json' -d '{
  "numeroCuenta":"496825","tipoCuenta":"AHORROS","saldoInicial":540,"clienteId":"mmontalvo"}'

# 8.4 Movimientos: positivo = depósito, negativo = retiro; la fecha es opcional (por defecto, ahora)
curl -X POST localhost:8082/api/movimientos -H 'Content-Type: application/json' -d '{
  "numeroCuenta":"225487","valor":600,"fecha":"2022-02-10T10:00:00"}'
curl -X POST localhost:8082/api/movimientos -H 'Content-Type: application/json' -d '{
  "numeroCuenta":"496825","valor":-540,"fecha":"2022-02-08T10:00:00"}'

# F3: sin saldo
curl -X POST localhost:8082/api/movimientos -H 'Content-Type: application/json' -d '{
  "numeroCuenta":"496825","valor":-1}'
# → 400 {"timestamp":"...","status":400,"error":"Bad Request","message":"Saldo no disponible","path":"/api/movimientos"}

# 8.5 / 8.6 Reporte (F4)
curl "localhost:8082/api/reportes?fecha=2022-02-08,2022-02-10&cliente=mmontalvo"
```

Respuesta del reporte (una fila por movimiento, con las llaves del ejemplo 8.6; la más reciente primero):

```json
[
  {"Fecha":"10/2/2022","Cliente":"Marianela Montalvo","Numero Cuenta":"225487","Tipo":"Corriente","Saldo Inicial":100,"Estado":true,"Movimiento":600,"Saldo Disponible":700},
  {"Fecha":"8/2/2022","Cliente":"Marianela Montalvo","Numero Cuenta":"496825","Tipo":"Ahorros","Saldo Inicial":540,"Estado":true,"Movimiento":-540,"Saldo Disponible":0}
]
```

## 3. API REST

Formato de URL: `http://{servidor}:{puerto}/api/{recurso}...`. Los errores siguen siempre el mismo formato (`timestamp`, `status`, `error`, `message`, `path`).

### ms-clientes (8081)

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/api/clientes` | Crear cliente | 201, 400, 409 |
| GET | `/api/clientes` · `/api/clientes/{clienteId}` | Listar / consultar | 200, 404 |
| PUT | `/api/clientes/{clienteId}` | Actualizar (la contraseña es opcional) | 200, 400, 404, 409 |
| DELETE | `/api/clientes/{clienteId}` | Eliminar | 204, 404 |

### ms-cuentas (8082)

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/api/cuentas` | Crear cuenta (el cliente debe existir y estar activo) | 201, 400, 404, 409 |
| GET | `/api/cuentas` (`?clienteId=`) · `/api/cuentas/{numeroCuenta}` | Listar / consultar | 200, 404 |
| PUT | `/api/cuentas/{numeroCuenta}` | Actualizar tipo y estado | 200, 400, 404 |
| POST | `/api/movimientos` | Registrar movimiento y actualizar el saldo | 201, 400 (**"Saldo no disponible"**), 404 |
| GET | `/api/movimientos` (`?numeroCuenta=`) · `/api/movimientos/{id}` | Listar (más reciente primero) / consultar | 200, 404 |
| PATCH | `/api/movimientos/{id}` | Corregir la `fecha` del movimiento | 200, 400, 404 |
| GET | `/api/reportes?fecha=<inicio>,<fin>&cliente=<clienteId>` | Estado de cuenta | 200, 400, 404 |

**Reporte:** `fecha` es un rango `yyyy-MM-dd,yyyy-MM-dd` (ambos extremos incluidos) o un solo día `yyyy-MM-dd`; `cliente` es el `clienteId`. Un cliente sin movimientos en el rango devuelve `[]`.

## 4. Pruebas

```bash
./gradlew test        # ejecuta TODAS las pruebas, incluidas las de integración y Karate
```

> **Requiere Docker en ejecución**: las pruebas de integración y Karate levantan PostgreSQL y RabbitMQ reales con Testcontainers (la primera vez descargan las imágenes). Las pruebas unitarias y de arquitectura no lo necesitan.

| Tipo | Dónde | Qué cubre |
|---|---|---|
| **Unitaria – entidad `Cliente` (F5)** | `ms-clientes/.../domain/model/ClienteTest` | Creación válida, validaciones, herencia de `Persona`, estado |
| Unitarias de dominio y casos de uso | ambos servicios | Reglas de saldo (casos 8.4 de la guía), "Saldo no disponible", idempotencia del consumidor, reporte, eventos |
| **Integración (F6)** | `ms-cuentas/.../integration/CuentaMovimientoIntegrationTest` | Crear cuenta → registrar movimientos → verificar saldo y reporte; 10 retiros concurrentes; cliente inactivo |
| Integración de clientes | `ms-clientes/.../integration/ClienteIntegrationTest` | Herencia JOINED en las tablas reales, contraseña con BCrypt, eventos publicados solo tras el commit |
| **Arquitectura** | `ArchitectureTest` en cada servicio | Regla de dependencias de la arquitectura hexagonal |
| **Karate** | `src/test/resources/karate` en cada servicio | Escenarios de `/clientes`, `/cuentas`, `/movimientos`, `/reportes` y de los eventos de RabbitMQ |

Los reportes de Karate quedan en `ms-*/build/karate-reports/karate-summary.html`.

## 5. Arquitectura y decisiones

### Comunicación entre microservicios

```
 ms-clientes ──(ClienteCreated / ClienteUpdated / ClienteDeleted)──► RabbitMQ ──► ms-cuentas
   PostgreSQL (esquema clientes)       exchange "cliente.events"      cola + DLQ    PostgreSQL (esquema cuentas)
```

- **ms-clientes** publica un evento por cada alta, cambio o baja, **solo después del commit** (`@TransactionalEventListener(AFTER_COMMIT)`): un cambio revertido nunca genera un evento.
- **ms-cuentas** guarda una **copia local mínima** del cliente (id, nombre, estado) actualizada por esos eventos. Con ella valida que el cliente exista y esté activo, y arma el reporte sin llamar al otro servicio.
- Cada servicio es dueño de su esquema; no hay claves foráneas entre ellos (la cuenta referencia al cliente por `clienteId`).

### Arquitectura hexagonal (propia y ligera)

Un solo módulo por servicio, con la regla `presentation → application → domain ← infrastructure`:

```
domain/          modelo y excepciones sin Spring, JPA ni Jackson · puertos (in/out) como interfaces
application/     casos de uso (sin anotaciones de Spring, salvo @Transactional) · commands/results como records
infrastructure/  adaptadores: JPA, RabbitMQ, BCrypt, configuración de beans
presentation/    controllers REST, requests/responses (records), @RestControllerAdvice
```

Un test de **ArchUnit** hace fallar el build si el dominio depende de Spring/JPA o si una capa depende de otra en sentido incorrecto. Único matiz: los puertos de entrada (`domain.port.in`) usan los records de `application.dto`.

### Qué pasa al eliminar un cliente

| Dónde | Efecto |
|---|---|
| ms-clientes | El cliente se **borra** (filas de `cliente` y `persona`) y se publica `cliente.deleted` |
| ms-cuentas | Su copia local pasa a **inactivo** (no se borra, para que un evento atrasado no lo reactive) |
| Sus cuentas y movimientos | **No se tocan**: conservan saldo e historial, y las cuentas siguen con `estado=true` |

A partir de ahí el cliente **no puede operar**: registrar movimientos o abrir cuentas responde `400 "El cliente X no está activo"`. Sus cuentas, movimientos y el reporte de estado de cuenta siguen consultables. Está cubierto por `CuentaMovimientoIntegrationTest`.

### Logs

Cada servicio escribe en la salida estándar (`docker compose logs -f ms-cuentas`), una línea por hecho relevante:

| Nivel | Qué se registra | Ejemplo |
|---|---|---|
| INFO | Cada petición: método, ruta, estado y duración | `POST /api/movimientos -> 201 (56 ms)` |
| INFO | Cada escritura de negocio | `Movimiento registered: id=1 numeroCuenta=478758 tipo=RETIRO valor=-575.00 saldo=1425.00` |
| INFO | Eventos entre servicios (publicados, recibidos y su efecto) | `Published cliente.created for clienteId=jlema` · `Cliente copy deactivated (cliente deleted): clienteId=jlema` |
| WARN | Peticiones rechazadas por regla de negocio o validación, con el motivo | `Request rejected: POST /api/movimientos -> 400 Saldo no disponible` |
| ERROR | Fallos inesperados (con traza) | |

No se registran cuerpos de petición, contraseñas ni *hashes*, y los *healthchecks* de Docker (`/actuator`) se omiten para no llenar el log. Para más detalle: `LOG_LEVEL=DEBUG docker compose up` (o la variable de entorno `LOG_LEVEL` al ejecutar el servicio).

### Decisiones principales

- **JPA bloqueante + virtual threads** (`spring.threads.virtual.enabled=true`): el enunciado exige JPA, y los hilos virtuales dan alta concurrencia con código imperativo. No se usa WebFlux ni R2DBC.
- **Herencia `Persona` ← `Cliente`** con estrategia `JOINED` (tablas `persona` y `cliente`). Son clases, no *records*, porque los records no pueden heredar.
- **Dinero con `BigDecimal`**, 2 decimales; más decimales se rechazan en lugar de redondear silenciosamente.
- **Registrar un movimiento** actualiza el saldo y guarda el movimiento en **una sola transacción**, bloqueando la fila de la cuenta (`PESSIMISTIC_WRITE`): dos movimientos simultáneos se aplican uno tras otro y el saldo nunca queda negativo. Está verificado con una prueba de 10 retiros concurrentes sobre PostgreSQL.
- **Un movimiento es inmutable** (`valor` y `saldo`): solo se puede corregir su `fecha`. Un error de valor se corrige con un movimiento compensatorio. El tipo (depósito/retiro) se deriva del signo del valor.
- **Contraseñas con BCrypt**; nunca se devuelven en las respuestas.
- **Esquema por script SQL** (`BaseDatos.sql`), con `ddl-auto: none`.
- **Errores:** excepciones de dominio traducidas por un `@RestControllerAdvice`. "Saldo no disponible" responde **400** con el mensaje literal.
- Código en inglés salvo las entidades del enunciado y sus atributos (`Persona`, `Cliente`, `Cuenta`, `Movimiento`, `nombre`, `clienteId`, ...); los mensajes de error de la API están en español.

## 6. Rendimiento, escalabilidad y resiliencia

**Implementado**

| Aspecto | Qué hay |
|---|---|
| Rendimiento | Índices en `cliente_id`, `numero_cuenta` (único) y `fecha` (`(cuenta_id, fecha)` para el reporte); el reporte y los listados usan una sola consulta con `join fetch` (sin N+1); virtual threads |
| Escalabilidad | Servicios *stateless*, independientes y escalables horizontalmente; el acoplamiento es por mensajes, no por llamadas; el bloqueo de la cuenta es por fila |
| Resiliencia | Consumidor **idempotente** (tabla `processed_event` por `eventId`) y tolerante al desorden (se descarta un evento más antiguo que el último aplicado); **reintentos** con *backoff* y **Dead Letter Queue** para mensajes que fallan o están mal formados; publicación **solo tras el commit**; *healthchecks* (`/actuator/health`) y `depends_on` en Compose |

**Contemplado, no implementado**

- **Patrón Outbox:** hoy, si RabbitMQ cae justo después del commit, el evento se pierde (se registra en el log). Outbox lo resuelve guardando el evento en la misma transacción y publicándolo después; el cambio queda acotado a dos adaptadores de infraestructura.
- Paginación en los listados, caché de lecturas, *circuit breaker* (Resilience4j) y reintentos selectivos (hoy un mensaje mal formado también se reintenta antes de ir a la DLQ).

## 7. Limitaciones conocidas

- La fecha de un movimiento puede enviarse (la guía lo necesita para el reporte de 2022). Se aceptan fechas pasadas y futuras, y el `saldo` es el acumulado en **orden de registro**, no de fecha.
- El reporte devuelve una fila por movimiento: una cuenta sin movimientos en el rango no aparece.
- Los datos de acceso de `docker-compose.yml` (`postgres`/`postgres`, `guest`/`guest`) son de desarrollo; en producción irían en un gestor de secretos.
- Los clientes de prueba no traen `identificacion`, `edad` ni `genero` en la guía; la colección de Postman usa valores ficticios.

## 8. Estructura del repositorio

```
├── settings.gradle.kts · build.gradle.kts · gradlew · gradle/     monorepo Gradle (wrapper incluido, Java 21)
├── ms-clientes/   build.gradle.kts · Dockerfile · src/
├── ms-cuentas/    build.gradle.kts · Dockerfile · src/
├── docker-compose.yml          PostgreSQL + RabbitMQ + los dos servicios
├── BaseDatos.sql               esquema de ambos microservicios
├── postman/                    colección Postman
└── README.md
```
