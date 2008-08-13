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
import simbya.framework.formateadores.FormateadorEstandar;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que representa un pago.
 * @author Marcelo Busico.
 */
public class Pago implements Normalizable {

    private long oid; //Requerido pero automático
    private Date fecha; //Requerido
    private float importe; //Requerido
    private TipoFormaPago formaPago; //Requerido

    public Pago() {
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public TipoFormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(TipoFormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public float getImporte() {
        return importe;
    }

    public void setImporte(float importe) {
        this.importe = importe;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        Hibernate.initialize(formaPago);        
    }

    /**
     * Muestra la fecha y el importe.
     */
    @Override
    public String toString() {
        return "Fecha Pago: " + FormateadorEstandar.formatearFecha(getFecha()) +
                " - " + String.valueOf(getImporte());
    }
}
