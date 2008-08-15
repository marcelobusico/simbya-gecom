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
package simbya.gecom.gestores.productos;

import java.util.List;
import javax.ejb.Remote;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Proveedor;
import simbya.gecom.entidades.TipoCalidad;
import simbya.gecom.entidades.TipoRubro;

/**
 * Gestor para actualizar precios de productos de un proveedor por rubro.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorActualizarPreciosProductosRemote {

    /**
     * Carga los elementos disponibles desde la base de datos.
     * @param clasePersistente Clase persistente a traer de la BD.
     * @return Lista con los elementos encontrados.
     */
    public List cargarObjetosPersistentes(Class clasePersistente);

    /**
     * Busca todos los productos en la base de datos que son del proveedor, 
     * calidad y rubro solicitado.
     * @param rubro Rubro del producto a buscar.
     * @param calidad Calidad del producto a buscar.
     * @param proveedor Proveedor del producto a buscar.
     * @return Lista con los objetos CalidadProducto encontrados.
     */
    public List<CalidadProducto> getCalidadesProductos(TipoRubro rubro,
            TipoCalidad calidad, Proveedor proveedor);

    /**
     * Registra en la base de datos el incremento/disminución de los precios
     * de cada uno de los productos.
     * @param calidades Calidades de los productos con el precio actualizado.
     * @throws Exception Si sucede algún error en la actualización.
     */
    public void confirmarActualizacion(List<CalidadProducto> calidades) throws Exception;
}
