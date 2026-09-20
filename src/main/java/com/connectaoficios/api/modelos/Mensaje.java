package com.connectaoficios.api.modelos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversacion_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_mensaje_conversacion")
    )
    private Conversacion conversacion;

    @Column(name = "remitente_id", nullable = false)
    private Integer remitenteId;

    @Column(nullable = false, length = 2000)
    private String contenido;

    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    @Column(nullable = false)
    private Boolean leido = false;

    @PrePersist
    public void prePersist() {
        if (fechaEnvio == null) {
            fechaEnvio = LocalDateTime.now();
        }

        if (leido == null) {
            leido = false;
        }
    }
}