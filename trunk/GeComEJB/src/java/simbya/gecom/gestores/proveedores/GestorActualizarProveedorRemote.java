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
package simbya.gecom.gestores.proveedores;

import java.util.List;
import javax.ejb.Remote;
import simbya.gecom.entidades.Proveedor;
import simbya.gecom.entidades.Provincia;

/**
 * Gestor para actualizar proveedores.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorActualizarProveedorRemote {

    List<Provincia> cargarProvincias();

    /**
     * Actualizar el proveedor en la base de datos.
     * @param proveedor Proveedor a actualizar.
     * @return Oid del proveedor.
     */
    long confirmarActualizacion(Proveedor proveedor);

    void confirmarBaja(Proveedor proveedor) throws Exception;
}
