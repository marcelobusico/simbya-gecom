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
package simbya.framework.validadores;

/**
 * Validador de direcciones de Correo Electrónico.
 * @author Marcelo Busico.
 */
public class EMail implements Validator {

    private static Validator instancia;

    private EMail() {
    }

    /**
     * Devuelve la única instancia de la clase validadora.
     * @return Referencia a una implementación de Validator.
     */
    public static Validator getInstancia() {
        if (instancia == null) {
            instancia = new EMail();
        }
        return instancia;
    }

    public boolean validar(String value) {
        //Verifica que la cadena no sea nula ni esté vacía
        if (value == null || value.isEmpty()) {
            return false;
        }

        //Verifica que la cadena tenga longitud menor a 64 caracteres.
        if (value.length() > 64) {
            return false;
        }

        //Verifica que la cadena tenga longitud mayor igual a 7 caracteres.
        if (value.length() < 6) {
            return false;
        }

        //Verificar que en el texto haya una sola @.
        int indiceArroba = value.indexOf("@");
        if (indiceArroba == -1) {
            return false;
        }

        //Cadena del usuario no vacia
        String usuario = value.substring(0, indiceArroba);
        if (usuario.length() == 0) {
            return false;
        }

        //Ver si el dominio termina en 2 letras (Ejemplo .ar)
        int fin1 = value.indexOf(".", value.length() - 3);
        if (fin1 == -1) {
            //El dominio termina en 3 letras (Ej: .com)
            int fin2 = value.indexOf(".", value.length() - 4);
            if (fin2 == -1) {
                //El dominio está mal formado y no termina en (.xyz)
                return false;
            }
            //Entre el @ y el . final hay al menos una letra.
            if ((value.length() - 4) - (indiceArroba + 1) == 0) {
                return false;
            }
        } else {
            //El dominio termina en 2 letras (Ej: .ar)

            //Entre el @ y el . final hay al menos una letra.
            if ((value.length() - 3) - (indiceArroba + 1) == 0) {
                return false;
            }

            //Ver si hay otro dominio mas antes del final (Ej: .com.ar)
            int fin2 = value.indexOf(".", value.length() - 7);
            if (fin2 != fin1) {
                //El dominio termina en '.xyz.ab'
                //Entre el @ y el . anterior al final hay al menos una letra.
                if ((value.length() - 7) - (indiceArroba + 1) == 0) {
                    return false;
                }
            }
        }

        //Si pasa todas las validaciones llega a este punto y retorna true.
        return true;
    }
}
