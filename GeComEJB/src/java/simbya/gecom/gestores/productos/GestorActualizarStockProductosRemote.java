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
import simbya.gecom.entidades.Producto;
import simbya.gecom.entidades.TipoMarca;
import simbya.gecom.entidades.TipoRubro;

/**
 * Gestor para actualizar el stock de productos cargados.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorActualizarStockProductosRemote {

    public List<Producto> getProductos(TipoRubro rubro, TipoMarca marca, String descripcion);

    /**
     * Carga los elementos disponibles desde la base de datos.
     * @param clasePersistente Clase persistente a traer de la BD.
     * @return Lista con los elementos encontrados.
     */
    public List cargarObjetosPersistentes(Class clasePersistente);

    public byte[] getImagenProducto(Producto p) throws Exception;

    /**
     * Actualiza el stock de las calidades indicadas en la BD.
     * @param calidades Calidades de los productos que serán actualizadas su
     * stock actual por el valor indicado en cada objeto.
     * @throws Exception Si sucede algún error en la actualización.
     */
    void actualizarStock(List<CalidadProducto> calidades) throws Exception;
}
