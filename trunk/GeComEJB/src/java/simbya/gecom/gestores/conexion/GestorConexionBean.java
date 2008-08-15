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

import javax.ejb.Stateless;
import simbya.framework.excepciones.ServerVersionException;
import simbya.gecom.VersionId;

/**
 * Valida el cliente con el servidor.
 * @author Marcelo Busico.
 */
@Stateless
public class GestorConexionBean implements GestorConexionRemote {

    /**
     * Verifica que la versión del software del cliente sea el
     * mismo que el del servidor. Es correcto si no sucede excepción.
     * @param clientVersionMajor Versión Mayor del software del cliente.
     * @param clientVersionMinor Versión Menor del software del cliente.
     * @throws ServerVersionException Si no se logra validar el cliente debido
     * a que las versiones no son iguales.
     */
    public void validarVersionCliente(int clientVersionMajor, int clientVersionMinor)
            throws ServerVersionException {
        int mayor = VersionId.ID_VERSION_MAYOR;
        int menor = VersionId.ID_VERSION_MENOR;

        if (clientVersionMajor != mayor || clientVersionMinor != menor) {
            throw new ServerVersionException("Las versiones de Software del cliente " +
                    "y servidor no concuerdan.\n" +
                    "Versión Cliente: " + clientVersionMajor + "." + clientVersionMinor + "\n" +
                    "Versión Servidor: " + mayor + "." + menor);
        }
    }
}
