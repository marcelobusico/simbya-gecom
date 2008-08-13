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

import java.util.*;
import org.hibernate.Hibernate;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de entidad que representa una Provincia.
 * @author Marcelo Busico.
 */
public class Provincia implements Normalizable, Comparable<Provincia> {

    private long oid; //Requerido pero automático
    private String nombre; //Requerido
    private Set<Localidad> localidades;

    public Provincia() {
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public Set<Localidad> getLocalidades() {
        return localidades;
    }

    public String getNombre() {
        return nombre;
    }

    public void setLocalidades(Set<Localidad> localidades) {
        this.localidades = localidades;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        TreeSet<Localidad> arbol = new TreeSet<Localidad>();
        for (Localidad loc : localidades) {
            loc.normalizarObjetoDesdeProvincia();
            arbol.add(loc);
        }
        localidades = arbol;
    }

    public boolean sosProvinciaDeEstaLocalidad(Localidad l) {
        Iterator<Localidad> it = localidades.iterator();
        if (l == null) {
            return false;
        }
        Localidad loc = null;
        while (it.hasNext()) {
            loc = it.next();
            if (loc.getOid() == l.getOid()) {
                return true;
            }
        }
        return false;
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
        int hash = 7;
        hash = 43 * hash + (int) (this.oid ^ (this.oid >>> 32));
        hash = 43 * hash + (this.nombre != null ? this.nombre.hashCode() : 0);
        return hash;
    }

    /**
     * Muestra el nombre de la provincia.
     */
    @Override
    public String toString() {
        return getNombre();
    }

    /**
     * Compara los nombres de las provincias para ordenarlas.
     */
    public int compareTo(Provincia o) {
        return nombre.compareTo(o.nombre);
    }
}
