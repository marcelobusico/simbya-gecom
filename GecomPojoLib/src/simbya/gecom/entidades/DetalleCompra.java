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
package simbya.gecom.entidades;

import org.hibernate.Hibernate;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que representa un detalle de compra.
 * @author Marcelo Busico.
 */
public class DetalleCompra implements Normalizable, Comparable<DetalleCompra> {

    private long oid; //Requerido pero automático
    private Integer item; //Generado al final
    private float cantidad; //Requerido
    private Producto producto; //Requerido
    private CalidadProducto calidad; //Requerido
    private float importeUnitario; //Requerido

    public DetalleCompra() {
    }

    public float getCantidad() {
        return cantidad;
    }

    public void setCantidad(float cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getItem() {
        return item;
    }

    public void setItem(Integer item) {
        this.item = item;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public float getImporteUnitario() {
        return importeUnitario;
    }

    public void setImporteUnitario(float importeUnitario) {
        this.importeUnitario = importeUnitario;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public CalidadProducto getCalidad() {
        return calidad;
    }

    public void setCalidad(CalidadProducto calidad) {
        this.calidad = calidad;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        producto.normalizarObjeto();
        calidad.normalizarObjeto();
    }

    public int compareTo(DetalleCompra o) {
        return item.compareTo(o.item);
    }    
}
