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
package simbya.gecom.decoradores;

import java.io.Serializable;
import simbya.gecom.entidades.Producto;

/**
 * Clase que representa un producto vendido en un periodo determinado.
 * @author Marcelo Busico.
 */
public class ProductoVendido implements Serializable, Comparable<ProductoVendido> {

    private Producto producto = null;
    private float cantidadVendida = 0;
    private float sumaImportesVenta = 0;
    private int cantidadVentas = 0;
    private float importePromedioVenta = 0;
    private String claseProducto = null;
    public static final String CLASE_A = "A";
    public static final String CLASE_B = "B";
    public static final String CLASE_C = "C";

    public ProductoVendido() {
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getClaseProducto() {
        return claseProducto;
    }

    public void setClaseProducto(String claseProducto) {
        this.claseProducto = claseProducto;
    }

    public float getCantidadVendida() {
        return cantidadVendida;
    }

    public void sumarACantidadVendida(float cantidadASumar) {
        cantidadVendida += cantidadASumar;
    }

    public float getImportePromedioVenta() {
        return importePromedioVenta;
    }

    public void agregarYPromediarVenta(float importeVenta) {
        cantidadVentas++;
        sumaImportesVenta += importeVenta;
        importePromedioVenta = sumaImportesVenta / (float) cantidadVentas;
    }

    public float getSubtotalVenta() {
        return importePromedioVenta * cantidadVendida;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProductoVendido other = (ProductoVendido) obj;
        if (this.producto != other.producto && (this.producto == null || !this.producto.equals(other.producto))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.producto != null ? this.producto.hashCode() : 0);
        return hash;
    }

    /**
     * Muestra el toString de producto.
     */
    @Override
    public String toString() {
        return producto.toString();
    }

    /**
     * Compara los productos a través de su precio promedio de venta y los
     * ordena primero el más caro y luego el más barato.
     * @param o Producto a comparar con el actual.
     * @return Diferencia entera entre el importe del producto pasado
     * por parámetro y el actual, o -1 si los productos tienen el mismo importe
     * pero son realmente 2 productos distintos.
     */
    public int compareTo(ProductoVendido o) {
        float dif = o.getImportePromedioVenta() - this.getImportePromedioVenta();
        if (dif == 0 && o.getProducto() != getProducto()) {
            return -1;
        }
        return (int) dif;
    }
}
