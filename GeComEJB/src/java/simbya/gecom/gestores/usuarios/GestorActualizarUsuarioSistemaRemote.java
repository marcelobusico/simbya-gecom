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

import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.ejb.Remote;
import simbya.framework.excepciones.RegistroFallidoException;
import simbya.framework.password.WrongPasswordException;
import simbya.gecom.entidades.seguridad.TipoUsuarioSistema;
import simbya.gecom.entidades.seguridad.UsuarioSistema;

/**
 * Gestor para actualizar usuarios del sistema.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorActualizarUsuarioSistemaRemote {

    /**
     * Carga la lista de tipos de usuario del sistema de la BD.
     * @return Devuelve una Lista con TipoUsuarioSistema correspondiente a cada
     * tipo cargado.
     */
    List<TipoUsuarioSistema> cargarTiposUsuario();

    /**
     * Selecciona un tipo de usuario y muestra en la ventana los usuarios
     * que se encuentran cargados que corresponden al tipo seleccionado.
     * @param idTipoUsuario Identificador del tipo de usuario.
     * @return Lista con usuarios del sistema, null si el tipo de usuario es null.
     */
    List<UsuarioSistema> seleccionarTipoUsuario(long idTipoUsuario);

    void seleccionarUsuario(UsuarioSistema usuario);

    void bajaDeUsuario() throws RegistroFallidoException;

    TipoUsuarioSistema getTipoUsuarioSistema();

    UsuarioSistema getUsuario();

    boolean isBaja();

    boolean isModificacion();

    /**
     * Actualiza los datos de la empresa en la BBDD.
     */
    void confirmarActualizacion() throws RegistroFallidoException;

    /**
     * Verifica los datos requeridos ingresados.
     * @return true si los datos son correctos, false de lo contrario.
     */
    boolean verificarDatos();

    void registrarNuevoUsuario();

    void tomarNombreUsuario(String nombre);

    /**
     * Toma la contraseña del usuario ingresado.
     * @param password1 Contraseña Ingresada por primera vez.
     * @param password2 Contraseña Repetida que debería ser igual a la primera.
     * @throws soporte.WrongPasswordException En el caso de que password1 sea
     * distinta a password2.
     * @throws java.security.NoSuchAlgorithmException Si no se encuentra el
     * algoritmo de encriptación de claves MD5 en el sistema.
     */
    void tomarContrasenia(char[] password1, char[] password2)
            throws WrongPasswordException, NoSuchAlgorithmException;
}
