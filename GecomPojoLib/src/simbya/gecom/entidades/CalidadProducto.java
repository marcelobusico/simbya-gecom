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
 * Clase de Entidad que representa una Calidad de un Producto con su precio.
 * @author Marcelo Busico.
 */
public class CalidadProducto implements Normalizable, Comparable<CalidadProducto> {

    private long oid; //Requerido pero automático
    private Producto producto; //Requerido
    private TipoCalidad calidad; //Requerido
    private String codigoProveedor;
    private Float precioUltimaCompra;
    private Proveedor proveedor;
    private boolean precioVentaFijo; //Requerido
    private Float precioVenta; //Requerido
    private Float porcentajeGanancia; //Requerido
    private Float stockActual = 0f; //Requerido
    private Float stockMinimo; //Requerido
    private Float stockMaximo; //Requerido

    public CalidadProducto() {
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public String getCodigoProveedor() {
        return codigoProveedor;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public void setCodigoProveedor(String codigoProveedor) {
        this.codigoProveedor = codigoProveedor;
    }

    public TipoCalidad getCalidad() {
        return calidad;
    }

    public void setCalidad(TipoCalidad calidad) {
        this.calidad = calidad;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Float getPrecioUltimaCompra() {
        return precioUltimaCompra;
    }

    public void setPrecioUltimaCompra(Float precioUltimaCompra) {
        this.precioUltimaCompra = precioUltimaCompra;
    }

    public Float getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Float precioVenta) {
        this.precioVenta = precioVenta;
    }

    public boolean isPrecioVentaFijo() {
        return precioVentaFijo;
    }

    public void setPrecioVentaFijo(boolean precioVentaFijo) {
        this.precioVentaFijo = precioVentaFijo;
    }

    public Float getPorcentajeGanancia() {
        return porcentajeGanancia;
    }

    public void setPorcentajeGanancia(Float porcentajeGanancia) {
        this.porcentajeGanancia = porcentajeGanancia;
    }

    public Float getStockActual() {
        return stockActual;
    }

    public void setStockActual(Float stockActual) {
        this.stockActual = stockActual;
    }

    public Float getStockMaximo() {
        return stockMaximo;
    }

    public void setStockMaximo(Float stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public Float getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Float stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int compareTo(CalidadProducto o) {
        return calidad.compareTo(o.getCalidad());
    }

    /**
     * Calcula el precio de venta del producto.
     * @return Precio de venta, null si faltan datos.
     */
    public Float calcularPrecioVenta() {
        if (isPrecioVentaFijo()) {
            return getPrecioVenta();
        } else {
            if (getPrecioUltimaCompra() != null && getPorcentajeGanancia() != null) {
                return getPrecioUltimaCompra() * (1 + getPorcentajeGanancia() / 100);
            } else {
                return null;
            }
        }
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        if (producto != null) {
            producto.normalizarObjeto();
        }
        if (calidad != null) {
            calidad.normalizarObjeto();
        }
        if (proveedor != null) {
            proveedor.normalizarObjeto();
        }
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjetoDesdeProducto() {
        Hibernate.initialize(this);
        if (calidad != null) {
            calidad.normalizarObjeto();
        }
        if (proveedor != null) {
            proveedor.normalizarObjeto();
        }
    }

    /**
     * Muestra el nombre de la calidad.
     */
    @Override
    public String toString() {
        return getCalidad().getNombre();
    }
}
