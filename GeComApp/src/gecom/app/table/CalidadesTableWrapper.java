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
package gecom.app.table;

import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Producto;

/**
 * Envoltorio para las calidades de un producto.
 * @author Marcelo Busico.
 */
public class CalidadesTableWrapper {

    private Producto producto;
    private CalidadProducto calidad;

    public CalidadesTableWrapper(Producto producto, CalidadProducto calidad) {
        this.producto = producto;
        this.calidad = calidad;
    }

    public CalidadProducto getCalidad() {
        return calidad;
    }

    public void setCalidad(CalidadProducto calidad) {
        this.calidad = calidad;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Producto getProducto() {
        return producto;
    }
}
