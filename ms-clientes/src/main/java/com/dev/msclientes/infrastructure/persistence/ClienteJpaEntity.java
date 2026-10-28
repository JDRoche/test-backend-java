package com.dev.msclientes.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "persona_id")
@Getter
@Setter
@NoArgsConstructor
public class ClienteJpaEntity extends PersonaJpaEntity {

    @Column(name = "cliente_id", nullable = false, unique = true)
    private String clienteId;

    @Column(nullable = false)
    private String contrasena;

    @Column(nullable = false)
    private boolean estado;
}
