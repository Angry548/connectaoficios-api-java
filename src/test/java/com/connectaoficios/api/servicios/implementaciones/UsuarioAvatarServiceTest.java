package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.perfil.*;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.*;
import com.connectaoficios.api.repositorios.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioAvatarServiceTest {
    private final IPerfilClienteRepository clientes = mock(IPerfilClienteRepository.class);
    private final IPerfilTrabajadorRepository trabajadores = mock(IPerfilTrabajadorRepository.class);
    private final IPerfilAdministradorRepository administradores = mock(IPerfilAdministradorRepository.class);
    private final UsuarioAvatarService service = new UsuarioAvatarService(clientes, trabajadores, administradores);

    @Test void resuelveTodosLosRolesEnTresConsultasSinCruzarFotografias() {
        var cliente = new PerfilCliente();
        cliente.setClienteId(1);
        cliente.setFotoUrl("cliente.png");
        var trabajador = new PerfilTrabajador();
        trabajador.setTrabajadorId(1);
        trabajador.setFotoUrl("trabajador.png");
        var admin = new PerfilAdministrador();
        admin.setAdministradorId(2);
        admin.setFotoUrl("admin.png");
        var principal = new PerfilAdministrador();
        principal.setAdministradorId(3);
        principal.setFotoUrl("principal.png");
        when(clientes.findByClienteIdIn(Set.of(1))).thenReturn(List.of(cliente));
        when(trabajadores.findByTrabajadorIdIn(Set.of(1))).thenReturn(List.of(trabajador));
        when(administradores.findByAdministradorIdIn(Set.of(2, 3))).thenReturn(List.of(admin, principal));
        var salida = service.resolverFotos(List.of(consulta(1, "Cliente"), consulta(1, "Trabajador"),
                consulta(2, "Administrador"), consulta(3, "AdministradorPrincipal"), consulta(1, "CLIENTE")));
        assertEquals(List.of("cliente.png", "trabajador.png", "admin.png", "principal.png", "cliente.png"),
                salida.stream().map(UsuarioFotoSalida::getFotoUrl).toList());
        assertEquals("ADMINISTRADORPRINCIPAL", salida.get(3).getRol());
        verify(clientes).findByClienteIdIn(Set.of(1));
        verify(trabajadores).findByTrabajadorIdIn(Set.of(1));
        verify(administradores).findByAdministradorIdIn(Set.of(2, 3));
        verifyNoMoreInteractions(clientes, trabajadores, administradores);
    }

    @Test void perfilAusenteYFotoAusenteSonNull() {
        var perfil = new PerfilCliente();
        perfil.setClienteId(1);
        when(clientes.findByClienteIdIn(Set.of(1, 2))).thenReturn(List.of(perfil));
        var salida = service.resolverFotos(List.of(consulta(1, "Cliente"), consulta(2, "Cliente")));
        assertNull(salida.get(0).getFotoUrl());
        assertNull(salida.get(1).getFotoUrl());
        verifyNoInteractions(trabajadores, administradores);
    }

    @Test void resolucionIndividualReutilizaLote() {
        assertNull(service.resolverFoto(4, "AdministradorPrincipal"));
        verify(administradores).findByAdministradorIdIn(Set.of(4));
        verifyNoInteractions(clientes, trabajadores);
    }

    @Test void loteVacioNoConsulta() {
        assertTrue(service.resolverFotos(List.of()).isEmpty());
        verifyNoInteractions(clientes, trabajadores, administradores);
    }

    @Test void rechazaLoteExcesivoIdsYRolesInvalidosAntesDeConsultar() {
        assertThrows(ReglaNegocioException.class,
                () -> service.resolverFotos(Collections.nCopies(201, consulta(1, "Cliente"))));
        assertThrows(ReglaNegocioException.class, () -> service.resolverFoto(0, "Cliente"));
        assertThrows(ReglaNegocioException.class, () -> service.resolverFoto(1, "Otro"));
        assertThrows(ReglaNegocioException.class, () -> service.resolverFoto(1, null));
        verifyNoInteractions(clientes, trabajadores, administradores);
    }

    private UsuarioFotoConsulta consulta(int id, String rol) {
        var consulta = new UsuarioFotoConsulta();
        consulta.setUsuarioId(id);
        consulta.setRol(rol);
        return consulta;
    }
}
