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
package simbya.gecom.gestores.configuracion;

import java.util.List;
import javax.ejb.Remote;
import simbya.gecom.interfaces.Normalizable;

/**
 * Gestor para actualizar provincias y localidades.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorActualizarProvinciasYLocalidadesRemote {

    /**
     * Carga los elementos disponibles desde la base de datos.
     * @param clasePersistente Clase persistente a traer de la BD.
     * @return Lista con los elementos encontrados.
     */
    public List cargarObjetosPersistentes(Class clasePersistente);

    /**
     * Actualizar el objeto en la base de datos.
     * @param obj Objeto a actualizar.
     * @return Oid del objeto actualizado.
     */
    long confirmarActualizacion(Normalizable obj);

    /**
     * Elimina el objeto de la base de datos.
     * @param obj Objeto a eliminar.
     * @throws java.lang.Exception Si sucede algún error, como ser objeto en
     * uso.
     */
    void confirmarEliminar(Normalizable obj) throws Exception;
}
