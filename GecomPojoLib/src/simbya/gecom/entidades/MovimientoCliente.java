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
import org.hibernate.Hibernate;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que representa un movimiento de un cliente.
 * @author Marcelo Busico.
 */
public class MovimientoCliente implements Normalizable {

    private long oid; //Requerido pero automático
    private Date fecha; //Requerido
    private Float importe; //Requerido
    private Cobro cobro;
    private Venta venta;

    public MovimientoCliente() {
        cobro = null;
        venta = null;
    }

    public MovimientoCliente(Venta venta) {
        this.venta = venta;
        this.fecha = venta.getFecha();
        this.importe = venta.getImporteTotal();
        cobro = null;
    }

    public MovimientoCliente(Cobro cobro) {
        this.cobro = cobro;
        this.fecha = cobro.getFecha();
        this.importe = cobro.getImporte();
        venta = null;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Float getImporte() {
        return importe;
    }

    public void setImporte(Float importe) {
        this.importe = importe;
    }

    public Cobro getCobro() {
        return cobro;
    }

    public void setCobro(Cobro cobro) {
        this.cobro = cobro;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        Hibernate.initialize(cobro);
        Hibernate.initialize(venta);
    }
}
