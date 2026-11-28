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

    @OneToMany(mappedBy = "mensaje", cascade = CascadeType.ALL)
    private java.util.List<AdjuntoMensaje> adjuntos = new java.util.ArrayList<>();

    @Column(name = "remitente_id", nullable = false)
    private Integer remitenteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 16)
    private com.connectaoficios.api.enums.TipoMensaje tipo = com.connectaoficios.api.enums.TipoMensaje.TEXT;

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

        if (tipo == null) { tipo = com.connectaoficios.api.enums.TipoMensaje.TEXT; }

        if (leido == null) {
            leido = false;
        }
    }
}