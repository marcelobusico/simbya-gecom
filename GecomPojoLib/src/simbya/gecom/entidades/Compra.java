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

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import org.hibernate.Hibernate;
import simbya.framework.formateadores.FormateadorEstandar;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que representa una compra.
 * @author Marcelo Busico.
 */
public class Compra implements Normalizable {

    private long oid; //Requerido pero automático
    private String tipoFactura; //Requerido
    private Integer sucursal;
    private Integer factura;
    private Date fecha; //Requerido
    private Proveedor proveedor;
    private TipoFormaPago formaPago; //Requerido
    private Float importe; //Requerido
    private Set<DetalleCompra> detalles; //Requerido

    public Compra() {
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public String getTipoFactura() {
        return tipoFactura;
    }

    public Integer getSucursal() {
        return sucursal;
    }

    public void setSucursal(Integer sucursal) {
        this.sucursal = sucursal;
    }

    public void setTipoFactura(String tipoFactura) {
        this.tipoFactura = tipoFactura;
    }

    public Integer getFactura() {
        return factura;
    }

    public void setFactura(Integer factura) {
        this.factura = factura;
    }

    public TipoFormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(TipoFormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Float getImporte() {
        return importe;
    }

    public void setImporte(Float importe) {
        this.importe = importe;
    }

    public Set<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(Set<DetalleCompra> detalles) {
        this.detalles = detalles;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        if (proveedor != null) {
            proveedor.normalizarObjeto();
        }
        if (formaPago != null) {
            formaPago.normalizarObjeto();
        }
        TreeSet<DetalleCompra> arbol = new TreeSet<DetalleCompra>();
        for (DetalleCompra actual : detalles) {
            actual.normalizarObjeto();
            arbol.add(actual);
        }
        detalles = arbol;
    }

    /**
     * Muestra la fecha, el numero de factura y el importe.
     */
    @Override
    public String toString() {
        return FormateadorEstandar.formatearFecha(fecha) +
                " - Nro: " + factura + "-" + tipoFactura + " - Importe: " + importe;
    }
}
