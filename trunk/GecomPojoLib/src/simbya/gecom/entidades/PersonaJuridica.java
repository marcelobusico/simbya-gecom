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
 * Clase de entidad que representa a una persona jurídica.
 * @author Marcelo Busico.
 */
public class PersonaJuridica extends Persona implements Normalizable {

    private String razonSocial; //Requerido
    private String cuit;

    /**
     * Crea una nueva instancia de PersonaJuridica
     */
    public PersonaJuridica() {
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    /**
     * Muestra la Identidad de la Persona.
     * @return Razón Social de la Persona Jurídica.
     */
    @Override
    public String getIdentidad() {
        return this.razonSocial;
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
    public String toString() {
        return razonSocial;
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
        hash = 37 * hash + (this.razonSocial != null ? this.razonSocial.hashCode() : 0);
        return hash;
    }
}
