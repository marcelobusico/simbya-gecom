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
 * Clase de entidad abstracta que contiene los datos de una persona.
 * @author Marcelo Busico.
 */
public abstract class Persona implements Normalizable {

    private long oid; //Requerido pero automático
    private String calle;
    private int numero;
    private int piso;
    private String departamento;
    private Localidad localidad;
    private String telefono;
    private String email;
    private String codigoPostal;
    private String fax;
    private boolean baja = false; //Requerido
    private Date fechaBaja = null;

    public Persona() {
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public long getOid() {
        return oid;
    }

    public String getCalle() {
        return calle;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getEmail() {
        return email;
    }

    public String getFax() {
        return fax;
    }

    public Localidad getLocalidad() {
        return localidad;
    }

    /**
     * Devuelve el número de la calle
     */
    public int getNumero() {
        return numero;
    }

    public int getPiso() {
        return piso;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public void setLocalidad(Localidad localidad) {
        this.localidad = localidad;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setPiso(int piso) {
        this.piso = piso;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isBaja() {
        return baja;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }

    public Date getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        if (localidad != null) {
            localidad.normalizarObjeto();
        }
    }

    /**
     * Devuelve una cadena con la identidad de la persona, 
     * de acuerdo al tipo de persona concreta que ha sido instanciada.
     */
    public abstract String getIdentidad();

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
        int hash = 7;
        hash = 37 * hash + (int) (this.oid ^ (this.oid >>> 32));
        return hash;
    }
}
