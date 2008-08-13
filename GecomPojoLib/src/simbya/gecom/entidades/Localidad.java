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
 * Clase de Entidad que representa una Localidad de una Provincia.
 * @author Marcelo Busico.
 */
public class Localidad implements Normalizable, Comparable<Localidad> {

    private long oid; //Requerido pero automático
    private String nombre; //Requerido
    private Provincia provincia; //Requerido

    public Localidad() {
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public long getOid() {
        return oid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        provincia.normalizarObjeto();
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjetoDesdeProvincia() {
        Hibernate.initialize(this);
        Hibernate.initialize(provincia);
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
        int hash = 3;
        hash = 83 * hash + (int) (this.oid ^ (this.oid >>> 32));
        hash = 83 * hash + (this.nombre != null ? this.nombre.hashCode() : 0);
        return hash;
    }

    /**
     * Muestra el nombre de la localidad.
     */
    @Override
    public String toString() {
        return getNombre();
    }

    /**
     * Compara los nombres de las localidades para ordenarlas.
     */
    public int compareTo(Localidad o) {
        return nombre.compareTo(o.nombre);
    }
}
