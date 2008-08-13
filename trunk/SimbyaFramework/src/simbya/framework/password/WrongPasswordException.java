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
package simbya.framework.password;

/**
 * Excepción por contraseña inválida.
 * @author Marcelo Busico.
 */
public class WrongPasswordException extends Exception {

    private String detalle;

    /**
     * Creates a new instance of <code>WrongPasswordException</code> without detail message.
     */
    public WrongPasswordException() {
        super("La contraseña ingresada es inválida.");
    }

    /**
     * Constructs an instance of <code>WrongPasswordException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public WrongPasswordException(String msg) {
        super(msg);
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}
