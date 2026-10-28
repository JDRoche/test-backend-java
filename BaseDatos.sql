-- BaseDatos.sql
-- PostgreSQL. Una sola base de datos (bancodb) con un esquema por microservicio:
-- cada servicio es dueño de su esquema y no consulta el del otro.
-- El script es re-ejecutable (IF NOT EXISTS).

-- =====================================================================
-- Esquema: clientes (MS Clientes)
-- =====================================================================
CREATE SCHEMA IF NOT EXISTS clientes;

-- Persona: tabla padre de la herencia JOINED
CREATE TABLE IF NOT EXISTS clientes.persona (
    id             BIGSERIAL PRIMARY KEY,
    nombre         VARCHAR(150) NOT NULL,
    genero         VARCHAR(20),
    edad           INTEGER CHECK (edad BETWEEN 0 AND 150),
    identificacion VARCHAR(30)  NOT NULL UNIQUE,
    direccion      VARCHAR(255),
    telefono       VARCHAR(30)
);

-- Cliente: hereda de persona (misma PK en persona_id)
CREATE TABLE IF NOT EXISTS clientes.cliente (
    persona_id BIGINT PRIMARY KEY REFERENCES clientes.persona (id) ON DELETE CASCADE,
    cliente_id VARCHAR(64)  NOT NULL UNIQUE,
    contrasena VARCHAR(100) NOT NULL,
    estado     BOOLEAN      NOT NULL DEFAULT TRUE
);

-- =====================================================================
-- Esquema: cuentas (MS Cuentas)
-- =====================================================================
CREATE SCHEMA IF NOT EXISTS cuentas;

-- Cuenta: cliente_id referencia al cliente de MS Clientes sin FK (otro microservicio)
CREATE TABLE IF NOT EXISTS cuentas.cuenta (
    id            BIGSERIAL PRIMARY KEY,
    numero_cuenta VARCHAR(20)   NOT NULL UNIQUE,
    tipo_cuenta   VARCHAR(20)   NOT NULL CHECK (tipo_cuenta IN ('AHORROS', 'CORRIENTE')),
    saldo_inicial NUMERIC(19, 2) NOT NULL CHECK (saldo_inicial >= 0),
    saldo_actual  NUMERIC(19, 2) NOT NULL CHECK (saldo_actual >= 0),
    estado        BOOLEAN       NOT NULL DEFAULT TRUE,
    cliente_id    VARCHAR(64)   NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_cuenta_cliente_id ON cuentas.cuenta (cliente_id);

-- Movimiento: valor positivo = deposito, negativo = retiro; saldo = saldo de la cuenta tras el movimiento
CREATE TABLE IF NOT EXISTS cuentas.movimiento (
    id              BIGSERIAL PRIMARY KEY,
    fecha           TIMESTAMP     NOT NULL,
    tipo_movimiento VARCHAR(20)   NOT NULL CHECK (tipo_movimiento IN ('DEPOSITO', 'RETIRO')),
    valor           NUMERIC(19, 2) NOT NULL CHECK (valor <> 0),
    saldo           NUMERIC(19, 2) NOT NULL CHECK (saldo >= 0),
    cuenta_id       BIGINT        NOT NULL REFERENCES cuentas.cuenta (id)
);
CREATE INDEX IF NOT EXISTS idx_movimiento_cuenta_fecha ON cuentas.movimiento (cuenta_id, fecha);
CREATE INDEX IF NOT EXISTS idx_movimiento_fecha ON cuentas.movimiento (fecha);

-- Copia local minima del cliente, actualizada por los eventos de MS Clientes
CREATE TABLE IF NOT EXISTS cuentas.cliente_snapshot (
    cliente_id    VARCHAR(64)  PRIMARY KEY,
    nombre        VARCHAR(150) NOT NULL,
    estado        BOOLEAN      NOT NULL,
    last_event_at TIMESTAMPTZ  NOT NULL
);

-- Eventos ya procesados: hace idempotente al consumidor
CREATE TABLE IF NOT EXISTS cuentas.processed_event (
    event_id     UUID        PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
