/*
 * Copyright (C) 2008  Marcelo Busico <marcelobusico@simbya.com.ar>
 * 
 * This file is part of a SIMBYA project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package simbya.gecom.gestores.tipos;

import java.util.List;
import javax.ejb.Remote;
import simbya.gecom.interfaces.InterfazTipoGenerico;

/**
 * Gestor para actualizar tipos genéricos.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorActualizarTiposRemote {

    List opcionActualizarTipos(InterfazTipoGenerico tg);

    List cargarTipos();

    boolean verificarDatos(String nombre, String descripcion);

    void registrarTipo();

    void modificarTipo(InterfazTipoGenerico tg);

    void eliminarTipo(InterfazTipoGenerico tg) throws java.lang.Exception;

    InterfazTipoGenerico getTipoGenerico();

    void setTipoGenerico(InterfazTipoGenerico tipoGenerico);

    /**
     * Registra o actualiza el tipo genérico especificado.
     * @param tg Objeto a registrar.
     * @return oid del objeto actualizado.
     */
    long actualizarTipo(InterfazTipoGenerico tg);

    /**
     * Registra o actualiza los objetos tipo genérico especificado.
     * @param lista Objetos a registrar.
     */
    void actualizarTipos(List<InterfazTipoGenerico> lista);
}
