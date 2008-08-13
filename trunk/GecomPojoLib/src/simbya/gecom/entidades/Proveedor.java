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

import java.util.Set;
import java.util.TreeSet;
import org.hibernate.Hibernate;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que representa un Proveedor.
 * @author Marcelo Busico.
 */
public class Proveedor extends PersonaJuridica implements Normalizable {

    private Set<MovimientoProveedor> movimientos;

    public Proveedor() {
    }

    public Set<MovimientoProveedor> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(Set<MovimientoProveedor> movimientos) {
        this.movimientos = movimientos;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    @Override
    public void normalizarObjeto() {
        super.normalizarObjeto();
        Hibernate.initialize(this);
        TreeSet<MovimientoProveedor> arbol = new TreeSet<MovimientoProveedor>();
        for (MovimientoProveedor actual : movimientos) {
            actual.normalizarObjeto();
            arbol.add(actual);
        }
        movimientos = arbol;        
    }

}
