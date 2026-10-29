package com.dev.mscuentas.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "cliente_snapshot")
@Getter
@Setter
@NoArgsConstructor
public class ClienteSnapshotJpaEntity {

    @Id
    @Column(name = "cliente_id")
    private String clienteId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private boolean estado;

    @Column(name = "last_event_at", nullable = false)
    private Instant lastEventAt;
}
