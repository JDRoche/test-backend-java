# Prueba Técnica Backend – Arquitectura de Microservicios (BP)

> Documento de contexto del proyecto (Java 21 · Spring Boot 4.x · Gradle · PostgreSQL · JPA · RabbitMQ). Resume los requerimientos de la prueba, los casos de uso, los entregables y las decisiones/consideraciones recomendadas.
> Tiempo disponible según el cronómetro de la plataforma: **96 horas**.
> Después de la entrega, y según resultados, puede haber una **entrevista técnica** donde se deberá defender la solución.

---

## 1. Objetivo

Demostrar habilidades de desarrollo backend aplicando buenas prácticas: **Clean Code, Clean Architecture, patrones de diseño (ej. Repository)**, manejo de excepciones, pruebas y despliegue en contenedores. **Las buenas prácticas se toman en cuenta para la calificación.**

---

## 2. Indicaciones generales

- Aplicar buenas prácticas de programación (Clean Code, Clean Architecture) y patrones de diseño que se consideren necesarios (ej. Repository).
- El manejo de entidades se debe hacer con **JPA / Entity Framework Core**.
- Se debe aplicar **manejo de excepciones**.
- Mínimo **una prueba unitaria**.
- La solución debe **desplegarse y funcionar en Docker**.
- Se pueden incluir más detalles si se considera necesario.

## 3. Herramientas y tecnologías

### Requeridas por el enunciado

| Aspecto | Definición |
|---|---|
| Lenguaje / Framework | **Java + Spring Boot** (la posición aplicada es Java; la alternativa .NET no aplica) |
| IDE | A preferencia |
| Base de datos | **Relacional** |
| Persistencia | **JPA** |
| Validación de API | **Postman v9.13.2** / **Karate DSL** |
| Contenedores | Docker |

### Stack elegido para este proyecto

| Aspecto | Decisión |
|---|---|
| Java | **21** (records, sealed interfaces, pattern matching, virtual threads) |
| Framework | **Spring Boot 4.x** (Spring Framework 7) |
| Build tool | **Gradle** (con Gradle Wrapper `./gradlew`) |
| Base de datos | **PostgreSQL** |
| Persistencia | **Spring Data JPA / Hibernate (bloqueante)** – ver sección 11 |
| Mensajería asíncrona | **RabbitMQ** |
| Pruebas | JUnit 5, Mockito, Spring Boot Test, ArchUnit, Karate |
| Contenedores | Docker + docker-compose |

---|---|
| Lenguaje / Framework | **Java + Spring Boot** (si se aplicó a posición Java). Alternativa .NET: NetCore 5+ / ASP 4.8 o inferior |
| IDE | A preferencia |
| Base de datos | **Relacional** (ej. PostgreSQL / MySQL / SQL Server) |
| Validación de API | **Postman v9.13.2** / **Karate DSL** |
| Contenedores | Docker (y docker-compose recomendado) |

---

## 4. Requerimiento de microservicios

Separar en **2 microservicios**, con **comunicación asíncrona** entre ellos:

| Microservicio | Entidades |
|---|---|
| **MS Clientes / Personas** | `Persona`, `Cliente` |
| **MS Cuentas / Movimientos** | `Cuenta`, `Movimiento` |

Funcionalidades obligatorias: **F1, F2, F3, F4, F5**. Deseable: **F6**.
Para máxima puntuación: **F6 y F7**.

---

## 5. Modelo de entidades

### Persona
- Campos: `nombre`, `género`, `edad`, `identificación`, `dirección`, `teléfono`.
- Debe manejar su **clave primaria (PK)**.

### Cliente (hereda de Persona)
- Campos propios: `clienteId`, `contraseña`, `estado`.
- Debe tener una **clave única (PK)**.

### Cuenta
- Campos: `número de cuenta`, `tipo de cuenta`, `saldo inicial`, `estado`.
- Debe manejar su **clave única**.
- Relación: pertenece a un Cliente (referenciado por `clienteId`, ya que está en otro microservicio).

### Movimiento
- Campos: `fecha`, `tipo de movimiento`, `valor`, `saldo`.
- Debe manejar su **clave única**.
- Relación: pertenece a una Cuenta.

> Las tablas del enunciado son solo una guía; se pueden añadir más campos si es necesario.

---

## 6. API REST

- Usar los verbos **GET, POST, PUT, PATCH, DELETE** según criterio, donde apliquen.
- Endpoints requeridos (base: `http://{servidor}:{puerto}/api/...`):

| Endpoint | Operaciones |
|---|---|
| `/clientes` | **CRUD** completo (crear, leer, actualizar, eliminar) |
| `/cuentas` | **CRU** (crear, leer, actualizar) |
| `/movimientos` | **CRU** (crear, leer, actualizar) |
| `/reportes?fecha=rango fechas&cliente` | Reporte "Estado de cuenta" (JSON) |

---

## 7. Funcionalidades

### F1 – CRUD
- CRUD en `Cliente`.
- CRU en `Cuenta` y `Movimiento`.

### F2 – Registro de movimientos
- Los movimientos pueden tener valores **positivos (depósito) o negativos (retiro)**.
- Al realizar un movimiento se debe **actualizar el saldo disponible**.
- Se debe **llevar el registro de las transacciones** realizadas.

### F3 – Validación de saldo
- Si un movimiento no cuenta con saldo, alertar con el mensaje exacto: **`"Saldo no disponible"`**.
- Definir la mejor forma de capturar y mostrar el error (ej. excepción de dominio + `@ControllerAdvice` / `@RestControllerAdvice` con respuesta JSON estandarizada y HTTP 400/409/422).

### F4 – Reportes (Estado de cuenta)
- Filtros: **rango de fechas** y **cliente**.
- Debe contener:
  - Cuentas asociadas con sus respectivos saldos.
  - Detalle de movimientos de las cuentas.
- Endpoint: `/reportes?fecha=rango fechas&cliente`
- Respuesta en **formato JSON**.
- Definir según expertise la mejor forma de solicitar y retornar la información (ej. `fecha=2022-02-08,2022-02-10&clienteId=...` o `fechaInicio`/`fechaFin`).

### F5 – Pruebas unitarias
- Implementar **1 prueba unitaria** para la **entidad de dominio Cliente**.

### F6 – Pruebas de integración (deseable)
- Implementar **1 prueba de integración**.

### F7 – Despliegue en contenedores (puntaje máximo)
- Dockerfile por microservicio + `docker-compose` con BD y broker de mensajes.

---

## 8. Casos de uso (datos de la guía)

### 8.1 Creación de usuarios

| Nombre | Dirección | Teléfono | Contraseña | Estado |
|---|---|---|---|---|
| Jose Lema | Otavalo sn y principal | 098254785 | 1234 | True |
| Marianela Montalvo | Amazonas y NNUU | 097548965 | 5678 | True |
| Juan Osorio | 13 junio y Equinoccial | 098874587 | 1245 | True |

### 8.2 Creación de cuentas de usuario

| Nº Cuenta | Tipo | Saldo inicial | Estado | Cliente |
|---|---|---|---|---|
| 478758 | Ahorros | 2000 | True | Jose Lema |
| 225487 | Corriente | 100 | True | Marianela Montalvo |
| 495878 | Ahorros | 0 | True | Juan Osorio |
| 496825 | Ahorros | 540 | True | Marianela Montalvo |

### 8.3 Nueva cuenta corriente para Jose Lema

| Nº Cuenta | Tipo | Saldo inicial | Estado | Cliente |
|---|---|---|---|---|
| 585545 | Corriente | 1000 | True | Jose Lema |

### 8.4 Movimientos a realizar

| Nº Cuenta | Tipo | Saldo inicial | Estado | Movimiento | Saldo disponible esperado |
|---|---|---|---|---|---|
| 478758 | Ahorros | 2000 | True | Retiro de 575 | 1425 |
| 225487 | Corriente | 100 | True | Depósito de 600 | 700 |
| 495878 | Ahorros | 0 | True | Depósito de 150 | 150 |
| 496825 | Ahorros | 540 | True | Retiro de 540 | 0 |

> Un retiro se registra como valor **negativo** (ej. `-540`).

### 8.5 Listado de movimientos por fechas y usuario (reporte esperado)

| Fecha | Cliente | Nº Cuenta | Tipo | Saldo inicial | Estado | Movimiento | Saldo disponible |
|---|---|---|---|---|---|---|---|
| 10/2/2022 | Marianela Montalvo | 225487 | Corriente | 100 | True | 600 | 700 |
| 8/2/2022 | Marianela Montalvo | 496825 | Ahorros | 540 | True | -540 | 0 |

### 8.6 Ejemplo de JSON de respuesta del reporte

```json
{
  "Fecha": "10/2/2022",
  "Cliente": "Marianela Montalvo",
  "Numero Cuenta": "225487",
  "Tipo": "Corriente",
  "Saldo Inicial": 100,
  "Estado": true,
  "Movimiento": 600,
  "Saldo Disponible": 700
}
```

> **Observación:** las fechas del reporte (10/2/2022 y 8/2/2022) muestran que el endpoint de movimientos debe **permitir enviar la fecha** (o que los datos de prueba se carguen con esas fechas), para poder reproducir el reporte y probar el filtro por rango de fechas.

---

## 9. Entregables

1. **`BaseDatos.sql`**: script de base de datos con entidades y esquema de datos (con ese nombre exacto).
2. **Colección Postman (JSON)** para validar los endpoints. Formato de URL: `http://{servidor}:{puerto}/api/{metodo}...{Parámetros}`.
3. **Karate** (si se aplica con Java): incluir el set de pruebas **dentro del proyecto Java**, garantizando que se ejecuten junto con todos los tests (`mvn test`).
4. **Repositorio público en GitHub** con todos los archivos generados; colocar la URL en los comentarios del ejercicio.
5. **Adicional:** subir un `.zip` o `.rar` con todos los archivos generados.

### Observaciones
- La solución se debe **desplegar y funcionar en Docker** (verificar que funcione).
- Cualquier instrucción necesaria para el despliegue debe ir en el **README**.

---

## 10. Consideraciones adicionales (máxima puntuación)

- Implementar **F6 y F7**.
- Contemplar (no necesariamente implementar) **rendimiento, escalabilidad y resiliencia**:
  - Rendimiento: índices en BD, paginación, consultas optimizadas (evitar N+1), caché.
  - Escalabilidad: servicios stateless, broker de mensajes, microservicios independientes escalables horizontalmente.
  - Resiliencia: reintentos, Dead Letter Queue, idempotencia en consumidores, circuit breaker (Resilience4j), health checks.

---

## 11. Decisiones técnicas y de arquitectura (establecidas)

> Estas decisiones están **cerradas** para el proyecto. Deben respetarse al generar código y poder defenderse en la entrevista técnica.

### 11.1 Arquitectura: hexagonal personalizada y ligera

- **Decisión:** arquitectura hexagonal / Clean Architecture **propia**, NO el scaffold de Bancolombia.
- **Motivos:**
  - El scaffold de Bancolombia genera muchos módulos Gradle y boilerplate (multiplicado por 2 microservicios) y está orientado a WebFlux/reactivo, lo que choca con JPA.
  - Una estructura propia es más fácil de entender, mantener y defender en la entrevista.
- **Qué se toma de esa filosofía:** dominio sin dependencias de frameworks, casos de uso explícitos, puertos y adaptadores.
- **Estructura de paquetes por microservicio (un solo módulo por servicio):**

```
src/main/java/com/<empresa>/<servicio>/
├── domain/
│   ├── model/          # Entidades/value objects de dominio (sin Spring ni JPA)
│   ├── exception/      # Excepciones de dominio
│   └── port/
│       ├── in/         # Puertos de entrada (interfaces de casos de uso)
│       └── out/        # Puertos de salida (repositorios, publicador de eventos)
├── application/
│   ├── usecase/        # Implementación de casos de uso
│   └── dto/            # Commands / Results (records)
├── infrastructure/
│   ├── persistence/    # Entidades JPA, Spring Data repos, adapters, mappers
│   ├── messaging/      # Publishers/consumers RabbitMQ
│   └── config/         # Configuración de beans, Rabbit, etc.
└── presentation/
    ├── controller/     # Controllers REST
    ├── request|response/ # Records de entrada/salida
    └── advice/         # @RestControllerAdvice
```

- **Regla de dependencias:** `presentation` → `application` → `domain` ← `infrastructure`. El `domain` **no** importa Spring, JPA ni Jackson.
- **Verificación automática:** test con **ArchUnit** que falle si `domain` depende de Spring/JPA o si hay dependencias en sentido incorrecto.
- **Patrones:** Repository (puerto + adapter), Use Case, Mapper, Strategy/sealed types donde aporte (ej. tipo de movimiento).

### 11.2 Persistencia: JPA bloqueante + virtual threads

- **Decisión:** **Spring Data JPA (Hibernate) bloqueante**. **No** se usa WebFlux ni R2DBC.
- **Motivos:** el enunciado exige JPA ("manejo de entidades con JPA") y R2DBC no es JPA; usarlo incumpliría un requisito.
- **Escalabilidad:** activar **virtual threads** de Java 21 con `spring.threads.virtual.enabled=true`. Permite alta concurrencia con código imperativo y simple, y sirve como argumento para el punto de rendimiento/escalabilidad.
- Montos con **`BigDecimal`**. Operaciones de movimiento con `@Transactional`.
- Esquema controlado por script SQL (`BaseDatos.sql`) y/o Flyway; `ddl-auto` en `validate` o `none`.

### 11.3 Uso de características modernas de Java 21

| Característica | Dónde usarla |
|---|---|
| **Records** | DTOs, requests/responses, comandos de casos de uso, eventos de mensajería, objetos del reporte |
| **Sealed interfaces + pattern matching en `switch`** | Tipos de movimiento (depósito/retiro), resultados de operaciones |
| **Virtual threads** | Configuración del servidor (ver 11.2) |
| `var`, text blocks, `Optional`, Streams | Donde mejore la legibilidad |

**Restricciones importantes:**
- Las **entidades JPA NO pueden ser records** → son clases normales.
- **`Persona` y `Cliente` NO pueden ser records**, porque el enunciado exige que `Cliente` herede de `Persona` y los records no pueden extender clases → `Persona` es clase abstracta y `Cliente extends Persona`. Esta clase es la que se prueba en F5.

### 11.4 Spring Boot 4.x

- Usar Spring Boot 4.x (Spring Framework 7) con Java 21.
- Esta versión modularizó starters y cambió algunos nombres/paquetes: **generar el proyecto desde start.spring.io** y consultar la guía de migración ante dudas.
- Verificar que la versión de **Karate** (y otras librerías externas) sea compatible con Boot 4.

### 11.5 Build: Gradle

- **Decisión:** **Gradle** con **Gradle Wrapper** (`gradlew` / `gradlew.bat` versionados en el repo). Preferencia: **Kotlin DSL** (`build.gradle.kts`).
- **Estructura del repositorio (monorepo):**

```
/
├── settings.gradle.kts        # include("ms-clientes", "ms-cuentas")
├── build.gradle.kts           # configuración común (opcional)
├── gradle/                    # wrapper (+ libs.versions.toml opcional)
├── gradlew / gradlew.bat
├── ms-clientes/
│   ├── build.gradle.kts
│   ├── Dockerfile
│   └── src/
├── ms-cuentas/
│   ├── build.gradle.kts
│   ├── Dockerfile
│   └── src/
├── docker-compose.yml
├── BaseDatos.sql
├── postman/                   # colección JSON
└── README.md
```

- Cada microservicio es un **subproyecto Gradle independiente** (un solo módulo por servicio, sin multi-módulo interno).
- **Java toolchain 21** configurado en Gradle (`java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }`).
- **Tests:** `./gradlew test` debe ejecutar **todas** las pruebas (unitarias, integración, ArchUnit y **Karate**), ya que el enunciado exige que Karate corra junto con el resto. Los features de Karate van en `src/test/resources` y se lanzan con un runner JUnit 5.
- **Docker:** Dockerfile **multi-stage** (etapa de build con Gradle/JDK 21 → imagen final con JRE 21) usando `./gradlew :ms-xxx:bootJar`.

### 11.6 Mensajería y comunicación asíncrona

- **Decisión:** **RabbitMQ** (más simple de levantar en docker-compose que Kafka para este alcance).
- **MS Clientes** publica eventos (`ClienteCreado`, `ClienteActualizado`, `ClienteEliminado`) modelados como **records**.
- **MS Cuentas** los consume para validar/guardar la referencia al cliente (ej. al crear una cuenta verificar que el `clienteId` existe y está activo).
- Para el reporte por cliente, MS Cuentas mantiene una copia local mínima del cliente (id, nombre, estado) actualizada vía eventos.
- **Resiliencia (mencionar aunque no se implemente todo):** consumidores idempotentes, reintentos, Dead Letter Queue, patrón Outbox, health checks.

### 11.7 Base de datos

- **PostgreSQL**, una BD o esquema por microservicio (cada servicio dueño de sus datos).
- Entidades de referencia:
  - `persona` (id PK, nombre, genero, edad, identificacion UNIQUE, direccion, telefono)
  - `cliente` (hereda de persona: `cliente_id` UNIQUE, contrasena, estado) – estrategia `JOINED` o `@MappedSuperclass`
  - `cuenta` (id PK, numero_cuenta UNIQUE, tipo_cuenta, saldo_inicial, estado, cliente_id)
  - `movimiento` (id PK, fecha, tipo_movimiento, valor, saldo, cuenta_id FK)
- Índices en `numero_cuenta`, `cliente_id` y `fecha` (para el reporte).

### 11.8 Manejo de errores

- Excepciones de dominio: `SaldoNoDisponibleException`, `RecursoNoEncontradoException`, `ClienteDuplicadoException`, etc.
- `@RestControllerAdvice` con respuesta estándar (puede modelarse con un record):

```json
{
  "timestamp": "2022-02-10T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Saldo no disponible",
  "path": "/api/movimientos"
}
```

### 11.9 Reglas de negocio clave

- Saldo disponible = saldo anterior + valor del movimiento (positivo o negativo).
- Si el resultado es `< 0` → lanzar `"Saldo no disponible"` y **no** registrar el movimiento.
- Registro del movimiento y actualización del saldo en **una sola transacción**.
- Validar que la cuenta y el cliente estén activos (`estado = true`).

### 11.10 Estrategia de pruebas

- **F5 (unitaria):** entidad de dominio `Cliente` (creación válida, validaciones, herencia de Persona).
- **F6 (integración):** flujo crear cuenta → registrar movimiento → verificar saldo, con `@SpringBootTest` + PostgreSQL de prueba (Testcontainers opcional).
- **Arquitectura:** ArchUnit para validar la regla de dependencias (11.1).
- **Karate:** escenarios para `/clientes`, `/cuentas`, `/movimientos` y `/reportes`.
- **Postman:** colección JSON con los casos de uso de la sección 8.

### 11.11 Docker

- `Dockerfile` multi-stage por microservicio (ver 11.5).
- `docker-compose.yml` con: PostgreSQL, RabbitMQ, `ms-clientes` y `ms-cuentas` (con `depends_on` y healthchecks).
- README con requisitos, comando `docker-compose up --build`, puertos y ejemplos de uso.

---

## 12. Checklist de entrega

- [ ] 2 microservicios funcionando con comunicación asíncrona
- [ ] Entidades Persona, Cliente, Cuenta, Movimiento con JPA
- [ ] CRUD `/clientes`, CRU `/cuentas` y `/movimientos`
- [ ] Registro de movimientos con actualización de saldo (F2)
- [ ] Mensaje `"Saldo no disponible"` (F3) con manejo global de excepciones
- [ ] Reporte `/reportes` por rango de fechas y cliente en JSON (F4)
- [ ] Prueba unitaria de la entidad Cliente (F5)
- [ ] Prueba de integración (F6)
- [ ] Docker / docker-compose funcionando (F7)
- [ ] Proyecto con Gradle Wrapper y Java 21 toolchain; `./gradlew test` ejecuta todos los tests (incluido Karate)
- [ ] Arquitectura hexagonal respetada (test ArchUnit en verde)
- [ ] JPA bloqueante con virtual threads habilitados (sin WebFlux/R2DBC)
- [ ] `BaseDatos.sql`
- [ ] Colección Postman (JSON)
- [ ] Karate dentro del proyecto Java (si aplica)
- [ ] README con instrucciones de despliegue
- [ ] Repositorio público en GitHub + URL en comentarios
- [ ] `.zip` / `.rar` con todos los archivos
- [ ] Datos de los casos de uso (8.1–8.5) cargados/probados y coincidiendo con los resultados esperados
