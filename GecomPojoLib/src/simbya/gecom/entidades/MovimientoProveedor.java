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
 * Clase de Entidad que representa un movimiento de un proveedor.
 * @author Marcelo Busico.
 */
public class MovimientoProveedor implements Normalizable {

    private long oid; //Requerido pero automático
    private Date fecha; //Requerido
    private Float importe; //Requerido
    private Pago pago;
    private Compra compra;

    public MovimientoProveedor() {
        compra = null;
        pago = null;
    }

    public MovimientoProveedor(Compra compra) {
        this.compra = compra;
        this.fecha = compra.getFecha();
        this.importe = compra.getImporte();
        pago = null;
    }

    public MovimientoProveedor(Pago pago) {
        this.pago = pago;
        this.fecha = pago.getFecha();
        this.importe = pago.getImporte();
        compra = null;
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

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);        
        Hibernate.initialize(pago);
        Hibernate.initialize(compra);
    }
}
