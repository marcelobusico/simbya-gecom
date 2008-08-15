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
package simbya.gecom.gestores.conexion;

import javax.ejb.Remote;
import simbya.framework.excepciones.ServerVersionException;

/**
 * Valida el cliente con el servidor.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorConexionRemote {

    /**
     * Verifica que la versión del software del cliente sea el
     * mismo que el del servidor. Es correcto si no sucede excepción.
     * @param clientVersionMajor Versión Mayor del software del cliente.
     * @param clientVersionMinor Versión Menor del software del cliente.
     * @throws ServerVersionException Si no se logra validar el cliente debido
     * a que las versiones no son iguales.
     */
    void validarVersionCliente(int clientVersionMajor, int clientVersionMinor)
            throws ServerVersionException;
}
