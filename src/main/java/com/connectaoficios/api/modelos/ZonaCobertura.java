package com.connectaoficios.api.modelos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "zonas_cobertura")
public class ZonaCobertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String departamento;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Column(length = 150)
    private String localidad;

    @Column(nullable = false)
    private Boolean activo = true;
}