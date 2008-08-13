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
 * Validador de Números Telefónicos.
 * @author Marcelo Busico.
 */
public class Telefono implements Validator {

    private static Validator instancia;

    private Telefono() {
    }

    /**
     * Devuelve la única instancia de la clase validadora de Teléfono.
     * @return Referencia a una implementación de Validator.
     */
    public static Validator getInstancia() {
        if (instancia == null) {
            instancia = new Telefono();
        }
        return instancia;
    }

    public boolean validar(String value) {
        //Verifica que la cadena no sea nula ni esté vacía
        if (value == null || value.isEmpty()) {
            return false;
        }

        //Verifica que la cadena tenga longitud menor igual a 18 caracteres.
        if (value.length() > 18) {
            return false;
        }

        //Verifica que la cadena tenga longitud mayor igual a 6 caracteres.
        if (value.length() < 6) {
            return false;
        }

        //Verifica que todos los caracteres sean numeros o bien un +, (, ) o -
        boolean abrioParentesis = false;
        int indiceParentesisAbre=0;
        boolean cerroParentesis = false;
        int cantGuiones = 0;
        for (int i = 0; i < value.length(); i++) {
            String letra = value.substring(i, i + 1);
            try {
                Integer.valueOf(letra);
            } catch (NumberFormatException e) {
                //Si entra aquí está mal porque se supone que no puede
                //parsear una letra a número.
                if (!letra.equals("(") &&
                        !letra.equals(")") &&
                        !letra.equals("+") &&
                        !letra.equals("-")) {
                    return false;
                } else {
                    if (letra.equals("+") && i > 0) {
                        //Sale porque hay un signo mas en u lugar que no es
                        //el principio de la cadena.
                        return false;
                    }
                    //Verifica que solo se abra un parentesis en el texto.
                    if (letra.equals("(")) {
                        if (abrioParentesis) {
                            return false;
                        } else {
                            abrioParentesis = true;
                            indiceParentesisAbre=i;
                        }
                    }
                    //Verifica que solo se cierre un parentesis en el texto.
                    if (letra.equals(")")) {
                        if (cerroParentesis || !abrioParentesis) {
                            return false;
                        } else {
                            cerroParentesis = true;
                            if((indiceParentesisAbre+1)==i) {
                                //Debe haber al menos un numero entre el
                                //parentesis que abre y el que cierra.
                                return false;
                            }
                        }
                    }
                    //Verifica la cantidad de guiones del telefono.
                    if (letra.equals("-")) {
                        if(i==0) {
                            //No puede haber un guion al comienzo del texto.
                            return false;
                        }
                        cantGuiones++;
                        if(cantGuiones>2) {
                            //No puede haber mas de 2 guiones.
                            return false;
                        }
                    }
                }
            }
        }
        if(abrioParentesis && !cerroParentesis) {
            //Abrió parentesis pero no lo cerró
            return false;
        }
        
        //Si pasa todas las validaciones llega a este punto y retorna true.
        return true;
    }
}
