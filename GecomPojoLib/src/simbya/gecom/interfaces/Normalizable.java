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
package simbya.gecom.interfaces;

import java.io.Serializable;

/**
 * Interfaz que implementan los objetos de entidad persistentes a través de 
 * hibernate que deben ser serializados a un cliente desde un servidor de
 * aplicaciones.
 * @author Marcelo Busico.
 */
public interface Normalizable extends Serializable {

    /**
     * Devuelve el identificador del objeto.
     * @return Valor del oid del objeto.
     */
    long getOid();

    /**
     * Establece el identificador del objeto
     * @param oid Valor del ID del objeto.
     */
    void setOid(long oid);

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    void normalizarObjeto();
}
