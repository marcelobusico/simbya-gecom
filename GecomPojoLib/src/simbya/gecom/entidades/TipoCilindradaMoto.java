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
import simbya.gecom.interfaces.InterfazTipoGenerico;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que representa la medida de Cilindrada de una Moto.
 * @author Marcelo Busico.
 */
public class TipoCilindradaMoto implements InterfazTipoGenerico<TipoCilindradaMoto>, Normalizable {

    private long oid; //Requerido pero automático
    private String nombre; //Requerido
    private String descripcion;

    public TipoCilindradaMoto() {
    }

    public TipoCilindradaMoto(String nombre) {
        this.nombre = nombre;
    }

    public TipoCilindradaMoto(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    @Override
    public void setOid(long oid) {
        this.oid = oid;
    }

    @Override
    public long getOid() {
        return oid;
    }

    @Override
    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
    }

    /**
     * Muestra el nombre de la cilindrada.
     */
    @Override
    public String toString() {
        return this.getNombre();
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
        hash = 37 * hash + (this.nombre != null ? this.nombre.hashCode() : 0);
        return hash;
    }

    /**
     * Compara el objeto actual con otro para ordenarlo de acuerdo a su nombre.
     * @param o Objeto a comparar con el actual.
     * @return Valor que está dado por el compareTo de la clase String sobre
     * el atributo nombre de la clase.
     */
    public int compareTo(TipoCilindradaMoto o) {
        return nombre.compareTo(o.getNombre());
    }
}
