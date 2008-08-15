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

/**
 * Envoltorio para las calidades de un producto.
 * @author Marcelo Busico.
 */
public class CantidadStockTableWrapper {

    private CalidadProducto calidad;
    private Float cantidadSolicitada;
    private boolean actualizarStock;
    private Float cantidadOriginal;

    public CantidadStockTableWrapper(CalidadProducto calidad) {
        this.calidad = calidad;
        cantidadSolicitada = null;
        cantidadOriginal = null;
        actualizarStock = false;
    }

    public CantidadStockTableWrapper(CalidadProducto calidad, Float cantidadSolicitada) {
        this.calidad = calidad;
        this.cantidadSolicitada = cantidadSolicitada;
        cantidadOriginal = cantidadSolicitada;
        actualizarStock = false;
    }

    public CantidadStockTableWrapper(CalidadProducto calidad, Float cantidadSolicitada, boolean actualizarStock) {
        this.calidad = calidad;
        this.cantidadSolicitada = cantidadSolicitada;
        cantidadOriginal = cantidadSolicitada;
        this.actualizarStock = actualizarStock;
    }

    public boolean isActualizarStock() {
        return actualizarStock;
    }

    public void setActualizarStock(boolean actualizarStock) {
        this.actualizarStock = actualizarStock;
    }

    public CalidadProducto getCalidad() {
        return calidad;
    }

    public void setCalidad(CalidadProducto calidad) {
        this.calidad = calidad;
    }

    public Float getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(Float cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public Float getCantidadOriginal() {
        return cantidadOriginal;
    }

    public void setCantidadOriginal(Float cantidadOriginal) {
        this.cantidadOriginal = cantidadOriginal;
    }
}
