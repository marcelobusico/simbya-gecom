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
 * Clase de Entidad que representa una venta.
 * @author Marcelo Busico.
 */
public class Venta implements Normalizable {

    private long oid; //Requerido pero automático
    private String tipoFactura; //Requerido
    private Integer factura; //Requerido
    private Date fecha; //Requerido
    private Cliente cliente;
    private TipoFormaCobro formaCobro; //Requerido
    private Float importeTotal; //Requerido
    private Float importeRecargoDescuento; //Requerido
    private Set<DetalleVenta> detalles; //Requerido

    public Venta() {
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

    public void setTipoFactura(String tipoFactura) {
        this.tipoFactura = tipoFactura;
    }

    public Integer getFactura() {
        return factura;
    }

    public void setFactura(Integer factura) {
        this.factura = factura;
    }

    public TipoFormaCobro getFormaCobro() {
        return formaCobro;
    }

    public void setFormaCobro(TipoFormaCobro formaCobro) {
        this.formaCobro = formaCobro;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Float getImporteRecargoDescuento() {
        return importeRecargoDescuento;
    }

    public void setImporteRecargoDescuento(Float importeRecargoDescuento) {
        this.importeRecargoDescuento = importeRecargoDescuento;
    }

    public Float getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(Float importeTotal) {
        this.importeTotal = importeTotal;
    }

    public Set<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(Set<DetalleVenta> detalles) {
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
        if (cliente != null) {
            cliente.normalizarObjeto();
        }
        if (formaCobro != null) {
            formaCobro.normalizarObjeto();
        }
        TreeSet<DetalleVenta> arbol = new TreeSet<DetalleVenta>();
        for (DetalleVenta actual : detalles) {
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
                " - Nro: " + factura + "-" + tipoFactura + " - Importe: " + importeTotal;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!this.toString().equals(obj.toString())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (int) (this.oid ^ (this.oid >>> 32));
        return hash;
    }
}
