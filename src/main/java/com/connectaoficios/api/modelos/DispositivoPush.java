package com.connectaoficios.api.modelos;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Getter @Setter @Entity
@Table(name="dispositivos_push", uniqueConstraints=@UniqueConstraint(name="uk_push_token", columnNames="token"))
public class DispositivoPush {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="usuario_id",nullable=false) private Integer usuarioId;
 @Column(nullable=false,length=255) private String token;
 @Column(nullable=false,length=16) private String plataforma;
 @Column(nullable=false) private boolean activo=true;
 @Column(name="fecha_registro",nullable=false,updatable=false) private LocalDateTime fechaRegistro;
 @Column(name="fecha_actualizacion",nullable=false) private LocalDateTime fechaActualizacion;
 @PrePersist void crear(){fechaRegistro=LocalDateTime.now();fechaActualizacion=fechaRegistro;}
 @PreUpdate void actualizar(){fechaActualizacion=LocalDateTime.now();}
}
