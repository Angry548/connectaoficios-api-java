package com.connectaoficios.api.dtos.comun;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class PaginaSalida<T> {

    private List<T> contenido;

    private int pagina;

    private int tamanio;

    private long totalElementos;

    private int totalPaginas;

    private boolean primera;

    private boolean ultima;

    private boolean tieneAnterior;

    private boolean tieneSiguiente;

    public static <T> PaginaSalida<T> desde(Page<T> page) {

        PaginaSalida<T> salida = new PaginaSalida<>();

        salida.setContenido(page.getContent());
        salida.setPagina(page.getNumber());
        salida.setTamanio(page.getSize());
        salida.setTotalElementos(page.getTotalElements());
        salida.setTotalPaginas(page.getTotalPages());
        salida.setPrimera(page.isFirst());
        salida.setUltima(page.isLast());
        salida.setTieneAnterior(page.hasPrevious());
        salida.setTieneSiguiente(page.hasNext());

        return salida;
    }
}