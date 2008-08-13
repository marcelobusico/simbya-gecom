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

import java.awt.Color;
import java.text.ParseException;
import org.apache.log4j.Logger;

/**
 * Clase para Validar entradas de texto del usuario.
 * @author Marcelo Busico.
 */
public abstract class TextValidator {

    private static final Logger log = Logger.getLogger(TextValidator.class);
    private static Color negro = new Color(0, 0, 0);
    private static Color rojo = new Color(255, 0, 0);

    /**
     * Valida que el valor ingresado en un JFormattedTextField sea un entero válido
     * y retorna su valor.
     * @return Entero (int) ingresado en el cuadro de texto, -1 si el valor ingresado
     * no es válido o la longitud de campo es 0.
     * @param padre Componente padre que invoca al método (Usualmente usar el puntero
     * this si el método es llamado desde una ventana).
     * @param campoTexto Cuadro de texto que tiene formato Integer establecido a través de
     * setValue y el cual será evaluado.
     * @param nombreCampo Descripción del campo a ser evaluado, con el fin de que si se produce
     * un error muestra un cuadro de diálogo con el nombre del campo a validar.
     */
    public static int validarInt(java.awt.Component padre, javax.swing.JFormattedTextField campoTexto, String nombreCampo) {
        campoTexto.setForeground(negro);
        if (campoTexto.getText().length() == 0) {
            return -1;
        }

        if (campoTexto.isEditValid()) {
            try {
                campoTexto.commitEdit();
            } catch (ParseException pe) {
                log.error("Error al hacer commitEdit en el JFormattedTextField", pe);
            }
            try {
                return ((Integer) campoTexto.getValue()).intValue();
            } catch (Exception e) {
                return -1;
            }
        } else {
            campoTexto.setForeground(rojo);
            campoTexto.requestFocus();
            return -1;
        }
    }

    /**
     * Valida que el valor ingresado en un JFormattedTextField sea un entero largo válido
     * y retorna su valor.
     * @return Entero Largo (long) ingresado en el cuadro de texto, -1 si el valor ingresado
     * no es válido o la longitud de campo es 0.
     * @param padre Componente padre que invoca al método (Usualmente usar el puntero
     * this si el método es llamado desde una ventana).
     * @param campoTexto Cuadro de texto que tiene formato Long establecido a través de
     * setValue y el cual será evaluado.
     * @param nombreCampo Descripción del campo a ser evaluado, con el fin de que si se produce
     * un error muestra un cuadro de diálogo con el nombre del campo a validar.
     */
    public static long validarLong(java.awt.Component padre, javax.swing.JFormattedTextField campoTexto, String nombreCampo) {
        campoTexto.setForeground(negro);
        if (campoTexto.getText().length() == 0) {
            return -1;
        }

        if (campoTexto.isEditValid()) {
            try {
                campoTexto.commitEdit();
            } catch (ParseException pe) {
                log.error("Error al hacer commitEdit en el JFormattedTextField", pe);
            }
            try {
                return ((Long) campoTexto.getValue()).longValue();
            } catch (Exception e) {
                return -1;
            }
        } else {
            campoTexto.setForeground(rojo);
            campoTexto.requestFocus();
            return -1;
        }
    }

    /**
     * Valida que el valor ingresado en un JFormattedTextField sea un float POSITIVO válido
     * y retorna su valor.
     * @return Número (float) ingresado en el cuadro de texto, -1 si el valor ingresado
     * no es válido o la longitud de campo es 0.
     * @param padre Componente padre que invoca al método (Usualmente usar el puntero
     * this si el método es llamado desde una ventana).
     * @param campoTexto Cuadro de texto que tiene formato Integer establecido a través de
     * setValue y el cual será evaluado.
     * @param nombreCampo Descripción del campo a ser evaluado, con el fin de que si se produce
     * un error muestra un cuadro de diálogo con el nombre del campo a validar.
     */
    public static float validarFloat(java.awt.Component padre, javax.swing.JFormattedTextField campoTexto, String nombreCampo) {
        campoTexto.setForeground(negro);
        if (campoTexto.getText().length() == 0) {
            return -1;
        }

        if (campoTexto.isEditValid()) {
            campoTexto.setText(transformarPuntoAComa(campoTexto.getText()));
            try {
                campoTexto.commitEdit();
            } catch (ParseException pe) {
                log.error("Error al hacer commitEdit en el JFormattedTextField", pe);
            }
            try {
                return ((Float) campoTexto.getValue()).floatValue();
            } catch (Exception e) {
                return -1;
            }
        } else {
            campoTexto.setForeground(rojo);
            campoTexto.requestFocus();
            return -1;
        }
    }

    /**
     * Toma una cadena que representa un número decimal, busca si tiene
     * algún punto y lo transforma a coma.
     * @param texto Texto donde buscar.
     * @return Texto transformado.
     */
    private static String transformarPuntoAComa(String texto) {
        String res = null;
        int lugarPunto = texto.indexOf(".");
        if (lugarPunto == -1) {
            return texto;
        }
        String entero = texto.substring(0, lugarPunto);
        String decimal = texto.substring(lugarPunto + 1);
        
        if (entero == null || entero.isEmpty()) {
            entero = "0";
        }
        if (decimal == null || decimal.isEmpty()) {
            return entero;
        }
        return entero + "," + decimal;
    }

    /**
     * Valida que el valor ingresado en un JFormattedTextField sea un float válido
     * y retorna su valor.
     * @return Número (Float) ingresado en el cuadro de texto, null si el valor ingresado
     * no es válido o la longitud de campo es 0.
     * @param campoTexto Cuadro de texto que tiene formato Integer establecido a través de
     * setValue y el cual será evaluado.
     */
    public static Float getFloatValue(javax.swing.JFormattedTextField campoTexto) {
        if (campoTexto.getText().length() == 0) {
            return null;
        }

        if (campoTexto.isEditValid()) {
            return objectToFloat(campoTexto.getValue());
        } else {
            return null;
        }
    }

    /**
     * Valida que el valor ingresado en un JFormattedTextField sea una fecha válida
     * y retorna su valor.
     * @return Date ingresado en el cuadro de texto, null si el valor ingresado
     * no es válido o la longitud de campo es 0.
     * @param padre Componente padre que invoca al método (Usualmente usar el puntero
     * this si el método es llamado desde una ventana).
     * @param campoTexto Cuadro de texto que tiene formato Date establecido a través de
     * setValue y el cual será evaluado.
     * @param nombreCampo Descripción del campo a ser evaluado, con el fin de que si se produce
     * un error muestra un cuadro de diálogo con el nombre del campo a validar.
     */
    public static java.util.Date validarDate(java.awt.Component padre, javax.swing.JFormattedTextField campoTexto, String nombreCampo) {
        campoTexto.setForeground(negro);
        if (campoTexto.getText().length() == 0) {
            return null;
        }

        if (campoTexto.isEditValid()) {
            try {
                campoTexto.commitEdit();
            } catch (ParseException pe) {
                log.error("Error al hacer commitEdit en el JFormattedTextField", pe);
            }
            try {
                return ((java.util.Date) campoTexto.getValue());
            } catch (Exception e) {
                return null;
            }
        } else {
            campoTexto.setForeground(rojo);
            campoTexto.requestFocus();
            return null;
        }
    }

    /**
     * Valida que el valor ingresado en un JTextField sea un número de CUIT válido
     * y retorna su valor.
     * @return String con CUIT ingresado en el cuadro de texto, null si el valor ingresado
     * no es válido o la longitud de campo es 0.
     * @param padre Componente padre que invoca al método (Usualmente usar el puntero
     * this si el método es llamado desde una ventana).
     * @param campoTexto Cuadro de texto con String el cual será evaluado.
     */
    public static String validarCuit(java.awt.Component padre, javax.swing.JTextField campoTexto) {
        campoTexto.setForeground(negro);
        if (campoTexto.getText().length() == 0) {
            return null;
        }

        if (Cuit.validar(campoTexto.getText())) {
            return campoTexto.getText();
        } else {
            campoTexto.setForeground(rojo);
            campoTexto.requestFocus();
            return null;
        }
    }

    /**
     * Valida que el valor ingresado en un JTextField sea un número de Telefono coherente
     * y retorna su valor.
     * @return String con Número de Teléfono ingresado en el cuadro de texto, 
     * null si el valor ingresado no es válido o la longitud de campo es 0.
     * @param padre Componente padre que invoca al método (Usualmente usar el puntero
     * this si el método es llamado desde una ventana).
     * @param campoTexto Cuadro de texto con String el cual será evaluado.
     */
    public static String validarTelefono(java.awt.Component padre, javax.swing.JTextField campoTexto) {
        campoTexto.setForeground(negro);
        if (campoTexto.getText().length() == 0) {
            return null;
        }

        if (Telefono.getInstancia().validar(campoTexto.getText())) {
            return campoTexto.getText();
        } else {
            campoTexto.setForeground(rojo);
            campoTexto.requestFocus();
            return null;
        }
    }

    /**
     * Valida que el valor ingresado en un JTextField sea un E-Mail coherente
     * y retorna su valor.
     * @return String con Dirección de mail ingresada en el cuadro de texto, 
     * null si el mail ingresado no es válido o la longitud de campo es 0.
     * @param padre Componente padre que invoca al método (Usualmente usar el puntero
     * this si el método es llamado desde una ventana).
     * @param campoTexto Cuadro de texto con String el cual será evaluado.
     */
    public static String validarEMail(java.awt.Component padre, javax.swing.JTextField campoTexto) {
        campoTexto.setForeground(negro);
        if (campoTexto.getText().length() == 0) {
            return null;
        }

        if (EMail.getInstancia().validar(campoTexto.getText())) {
            return campoTexto.getText();
        } else {
            campoTexto.setForeground(rojo);
            campoTexto.requestFocus();
            return null;
        }
    }

    /**
     * Valida que el valor ingresado en un JTextField sea un Texto de la longitud deseada
     * y retorna su valor.
     * @return String ingresado en el cuadro de texto, 
     * null si el texto ingresado no es válido o la longitud de campo es 0.
     * @param padre Componente padre que invoca al método (Usualmente usar el puntero
     * this si el método es llamado desde una ventana).
     * @param campoTexto Cuadro de texto con String el cual será evaluado.
     * @param longitudMinima Longitud de texto mínima para que sea válido (Comienza en 1).
     * @param longitudMaxima Longitud de texto máxima para que sea válido (Incluye al valor máximo).
     */
    public static String validarTexto(java.awt.Component padre, javax.swing.text.JTextComponent campoTexto, int longitudMinima, int longitudMaxima) {
        campoTexto.setForeground(negro);
        if (campoTexto.getText().length() == 0) {
            return null;
        }
        int longitud = campoTexto.getText().length();
        if (longitud < (longitudMaxima + 1) && longitud >= longitudMinima) {
            return campoTexto.getText();
        } else {
            campoTexto.setForeground(rojo);
            campoTexto.requestFocus();
            return null;
        }
    }

    public static Float objectToFloat(Object obj) {
        Float res = null;
        if (obj instanceof Float) {
            res = (Float) obj;
        }
        if (obj instanceof Integer) {
            res = ((Integer) obj).floatValue();
        }
        return res;
    }
}
