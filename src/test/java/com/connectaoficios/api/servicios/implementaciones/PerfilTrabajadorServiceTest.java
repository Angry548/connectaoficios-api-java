package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;
import com.connectaoficios.api.repositorios.IZonaCoberturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PerfilTrabajadorServiceTest {

    @Mock
    private IPerfilTrabajadorRepository perfilRepository;

    @Mock
    private IZonaCoberturaRepository zonaRepository;

    private PerfilTrabajadorService perfilService;

    @BeforeEach
    void setUp() {
        perfilService = new PerfilTrabajadorService(
                perfilRepository,
                zonaRepository
        );
    }
}