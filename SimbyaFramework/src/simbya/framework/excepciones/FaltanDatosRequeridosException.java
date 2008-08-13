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
 * Excepción para falta de datos requeridos.
 * @author Marcelo Busico.
 */
public class FaltanDatosRequeridosException extends java.lang.Exception {

    /**
     * Creates a new instance of FaltanDatosRequeridosException without detail message.
     */
    public FaltanDatosRequeridosException() {
        super("Fallo al registrar debido a que faltan datos marcados como requeridos.");
    }

    /**
     * Constructs an instance of FaltanDatosRequeridosException with the specified detail message.
     * @param msg the detail message.
     */
    public FaltanDatosRequeridosException(String msg) {
        super(msg);
    }
}
