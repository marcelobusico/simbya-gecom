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
package simbya.gecom.gestores.usuarios;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import simbya.framework.excepciones.RegistroFallidoException;
import simbya.gecom.entidades.seguridad.CasoDeUso;
import simbya.gecom.entidades.seguridad.TipoUsuarioSistema;

/**
 * Gestor para actualizar los privilegios de los tipos de usuario del sistema.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorActualizarTiposUsuarioSistemaRemote {

    /**
     * Carga los tipos de usuario del sistema desde la base de datos.
     * @return todos los tipos de usuario del sistema.
     */
    List<TipoUsuarioSistema> cargarTiposUsuario();

    /**
     * Indica al gestor cual fue el TipoUsuarioSistema seleccionado en la
     * ventana por el usuario.
     */
    void seleccionarTipoUsuario(TipoUsuarioSistema tus);

    /**
     * Opción para registrar un nuevo tipo de usuario del sistema.
     */
    void registrarNuevoUsuario();

    /**
     * Carga los privilegios disponibles (CasoDeUso) desde la base de datos
     * quitando de la lista los privilegios asignados del usuario seleccionado.
     * @return Lista con los privilegios disponibles.
     */
    List<CasoDeUso> cargarPrivilegiosDisponibles();

    /**
     * Guarda el nombre en el atributo nombre del objeto TipoUsuarioSistema.
     */
    void tomarNombreTipoUsuario(String nombre);

    /**
     * Guarda la descripción en el atributo descripcion del objeto 
     * TipoUsuarioSistema.
     */
    void tomarDescripcionTipoUsuario(String desc);

    /**
     * Guarda los privilegios asignados en el atributo privilegios del objeto 
     * TipoUsuarioSistema.
     */
    void tomarPrivilegiosAsignados(Set<CasoDeUso> privilegios);

    /**
     * Actualiza el tipo de usuario del sistema en la BBDD.
     * @return true si todo fue correcto, false si sucede algún error.
     */
    boolean confirmarActualizacion() throws RegistroFallidoException;

    /**
     * Borra el tipo de usuario del sistema seleccionado en el gestor.
     */
    void borrarTipo() throws RegistroFallidoException;

    boolean isBaja();

    boolean isModificacion();

    TipoUsuarioSistema getTipoUsuarioSistema();
}
