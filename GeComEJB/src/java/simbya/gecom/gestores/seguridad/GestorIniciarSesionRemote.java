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
package simbya.gecom.gestores.seguridad;

import javax.ejb.Remote;
import simbya.gecom.entidades.seguridad.UsuarioSistema;

/**
 * Gestor para iniciar sesión en el sistema.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorIniciarSesionRemote {

    /**
     * Valida al usuario y contraseña ingresados.
     * @param user Nombre de usuario a validar.
     * @param password Contraseña del usuario.
     * @return Objeto UsuarioSistema validado, null si no fue válido.
     */
    UsuarioSistema iniciarSesion(String user, char[] password);

    void salir();
}
