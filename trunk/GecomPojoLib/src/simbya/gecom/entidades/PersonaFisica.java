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
 * Clase de entidad que representa a una persona física.
 * @author Marcelo Busico.
 */
public class PersonaFisica extends Persona implements Normalizable {

    private String nombre; //Requerido
    private String apellido; //Requerido

    /** 
     * Crea una nueva instancia de PersonaFisica.
     */
    public PersonaFisica() {
    }

    public String getApellido() {
        return apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Cadena con Tipo y Nº de Documento, Apellido y Nombre.
     */
    @Override
    public String toString() {
        if (apellido == null && nombre == null) {
            return "Sin datos cargados";
        }

        return this.getApellido() + ", " + this.getNombre();
    }

    public String getIdentidad() {
        return this.apellido + ", " + this.nombre;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    @Override
    public void normalizarObjeto() {
        super.normalizarObjeto();
        Hibernate.initialize(this);
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
        hash = 37 * hash + (this.apellido != null ? this.apellido.hashCode() : 0);
        return hash;
    }
}
