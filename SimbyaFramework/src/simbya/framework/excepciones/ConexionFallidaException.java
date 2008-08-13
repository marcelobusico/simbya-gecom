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
 * Excepción lanzada en fallos de conexión con el motor de BD.
 * @author Marcelo Busico.
 */
public class ConexionFallidaException extends java.lang.Exception {

    /**
     * Creates a new instance of ConexionFallidaException without detail message.
     */
    public ConexionFallidaException() {
        super("Fallo al conectar a la BD. Verifique los datos de conexión.");
    }

    /**
     * Creates a new instance of ConexionFallidaException without detail message.
     */
    public ConexionFallidaException(Exception ex) {
        super("Fallo al conectar a la BD. Verifique los datos de conexión.", ex);
    }

    /**
     * Constructs an instance of ConexionFallidaException with the specified detail message.
     * @param msg the detail message.
     */
    public ConexionFallidaException(String msg) {
        super(msg);
    }
}
