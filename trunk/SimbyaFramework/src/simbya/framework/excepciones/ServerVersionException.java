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
package simbya.framework.excepciones;

/**
 * Excepción producida cuando la versión del programa del cliente y del
 * servidor no concuerdan.
 * @author Marcelo Busico.
 */
public class ServerVersionException extends Exception {

    /**
     * Creates a new instance of <code>ServerVersionException</code> without detail message.
     */
    public ServerVersionException() {
    }

    /**
     * Constructs an instance of <code>ServerVersionException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ServerVersionException(String msg) {
        super(msg);
    }
}
