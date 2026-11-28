package com.connectaoficios.api.modelos;
import com.connectaoficios.api.enums.TipoMensaje;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Getter @Setter @Entity @Table(name="adjuntos_mensaje")
public class AdjuntoMensaje {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="mensaje_id",nullable=false) private Mensaje mensaje;
 @Column(name="nombre_original",nullable=false,length=255) private String nombreOriginal;
 @Column(name="mime_type",nullable=false,length=100) private String mimeType;
 @Column(name="tamano_bytes",nullable=false) private long tamanoBytes;
 @Enumerated(EnumType.STRING) @Column(name="tipo_adjunto",nullable=false,length=16) private TipoMensaje tipoAdjunto;
 @Column(name="referencia",nullable=false,length=255) private String referencia;
 @Column(name="fecha_creacion",nullable=false,updatable=false) private LocalDateTime fechaCreacion;
 @PrePersist void crear(){if(fechaCreacion==null)fechaCreacion=LocalDateTime.now();}
}
