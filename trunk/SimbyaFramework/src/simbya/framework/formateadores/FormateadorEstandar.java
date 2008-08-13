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
package simbya.framework.formateadores;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.text.InternationalFormatter;

/**
 * Clase para formatear cadenas a partir de valores numéricos.
 * @author Marcelo Busico.
 */
public abstract class FormateadorEstandar {

    /**
     * Transforma un float en un String, con el formato de dinero
     * utilizado comunmente en Argentina -> '$ 123,40' (2 decimales).
     * @return Cadena formateada que representa el importe pasado por parámetro,
     * null si el importe no se puede formatear (error).
     * @param importe Valor float con el importe a formatear.
     */
    public static String formatearDinero(float importe) {
        InternationalFormatter format = new InternationalFormatter(new java.text.DecimalFormat("'$' 0.00"));
        try {
            return format.valueToString(new Float(importe));
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Transforma un double en un String, con el formato de dinero
     * utilizado comunmente en Argentina -> '$ 123,40' (2 decimales).
     * @return Cadena formateada que representa el importe pasado por parámetro,
     * null si el importe no se puede formatear (error).
     * @param importe Valor double con el importe a formatear.
     */
    public static String formatearDinero(double importe) {
        InternationalFormatter format = new InternationalFormatter(new java.text.DecimalFormat("'$' 0.00"));
        try {
            return format.valueToString(new Double(importe));
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Transforma un float en un String, con el formato decimal
     * utilizado comunmente en Argentina -> '123,40' (2 decimales).
     * @return Cadena formateada que representa el valor pasado por parámetro,
     * null si el valor no se puede formatear (error).
     * @param valor Valor float con el valor a formatear.
     */
    public static String formatearDecimal(float valor) {
        InternationalFormatter format = new InternationalFormatter(new java.text.DecimalFormat("0.00"));
        try {
            return format.valueToString(new Float(valor));
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Transforma un Date en un String, con el formato de fecha
     * utilizado comunmente en Argentina -> 'dd/mm/yyyy'.
     * @return Cadena formateada que representa la fecha pasada por parámetro,
     * null si la fecha no se puede formatear (error).
     * @param fecha Date (Fecha) a formatear.
     */
    public static String formatearFecha(Date fecha) {
        InternationalFormatter format = new InternationalFormatter(
                new SimpleDateFormat("dd/MM/yyyy"));
        try {
            return format.valueToString(fecha);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Transforma un String que representa un importe de dinero con el
     * formato '$ 123,40' a un objeto Float.
     * @return Float que representa el importe formateado pasado por parámetro,
     * null si el valor no se puede desformatear (error).
     * @param importeFormateado Importe a quitar el formato.
     */
    public static Float desformatearDinero(String importeFormateado) {
        InternationalFormatter format = new InternationalFormatter(new java.text.DecimalFormat("'$' 0.00"));
        try {
            try {
                float v =
                        ((Double) format.stringToValue(importeFormateado)).floatValue();
                return new Float(v);
            } catch (ClassCastException e) {
                float v =
                        ((Long) format.stringToValue(importeFormateado)).floatValue();
                return new Float(v);
            }
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Transforma un String que representa un valor decimal con el
     * formato '123,40' a un objeto Float.
     * @return Float que representa el valor formateado pasado por parámetro,
     * null si el valor no se puede desformatear (error).
     * @param valorFormateado Valor a quitar el formato.
     */
    public static Float desformatearDecimal(String valorFormateado) {
        InternationalFormatter format = new InternationalFormatter(new java.text.DecimalFormat("0.00"));
        try {
            try {
                float v =
                        ((Double) format.stringToValue(valorFormateado)).floatValue();
                return new Float(v);
            } catch (ClassCastException e) {
                float v =
                        ((Long) format.stringToValue(valorFormateado)).floatValue();
                return new Float(v);
            }
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Transforma un String que representa una fecha con el
     * formato 'dd/mm/yyyy' a un objeto Date.
     * @return Date que representa la fecha formateada pasada por parámetro,
     * null si el valor no se puede desformatear (error).
     * @param fechaFormateada Fecha a quitar el formato.
     */
    public static Date desformatearFecha(String fechaFormateada) {
        InternationalFormatter format = new InternationalFormatter(
                new SimpleDateFormat("dd/MM/yyyy"));
        try {
            return (Date) format.stringToValue(fechaFormateada);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Transforma un valor numerico que represan un mes en un String.
     * @return Cadena formateada que representa el mes pasado por parámetro,
     * null si la fecha no se puede formatear (error).
     * @param mes int del mes a formatear -> Enero=1, Diciembre=12.
     */
    public static String formatearMes(int mes) {
        if (mes < 1 || mes > 12) {
            return null;
        }

        SimpleDateFormat f = new SimpleDateFormat("MMMM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.MONTH, mes - 1);
        Date fecha = cal.getTime();
        String cadenaMes = f.format(fecha);
        return cadenaMes.substring(0, 1).toUpperCase() + cadenaMes.substring(1);
    }

    /**
     * Transforma un String que represan a un mes en un int.
     * @return int desformateado que representa el mes pasado por parámetro
     * -> Enero=1, Diciembre=12, 0 si la fecha no se puede desformatear (error).
     * @param mesFormateado String del mes a desformatear
     */
    public static int desformatearMes(String mesFormateado) {
        if (mesFormateado == null || mesFormateado.isEmpty() == true) {
            return 0;
        }

        for (int mes = 1; mes <= 12; mes++) {
            if (mesFormateado.equalsIgnoreCase(formatearMes(mes)) == true) {
                return mes;
            }
        }

        return 0;
    }
}
