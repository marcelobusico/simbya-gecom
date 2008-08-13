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
package simbya.framework.decoradores;

import java.util.*;

/**
 * Clase con métodos útiles para tratar fechas.
 * @author Marcelo Busico.
 */
public class Fecha {

    private Date fecha;

    /**
     * Crea una nueva instancia de Fecha.
     * Asigna la fecha actual del sistema.
     */
    public Fecha() {
        fecha = new Date();
    }

    public Fecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    /**
     * Establece la fecha a través de los parámetros pasados. 
     * @param anio Año.
     * @param mes Mes (01-12).
     * @param dia Dia.
     */
    public void setFecha(int anio, int mes, int dia) {
        Calendar ca = Calendar.getInstance();
        ca.set(anio, mes - 1, dia);
        fecha = ca.getTime();
    }

    /**
     * Establece la fecha a través de una cadena pasada.
     * @param f String con fecha en formato yyyy-mm-dd.
     */
    public void setFechaSQL(String f) {
        Calendar ca = Calendar.getInstance();
        int anio = Integer.valueOf(f.substring(0, 4));
        int mes = Integer.valueOf(f.substring(5, 7));
        int dia = Integer.valueOf(f.substring(8, 10));
        ca.set(anio, mes - 1, dia);
        fecha = ca.getTime();
    }

    /**
     * Establece la fecha a través de una cadena pasada.
     * @param f String con fecha en formato dd/mm/yyyy.
     */
    public void setFechaArgentina(String f) {
        if (f == null || f.length() != 10) {
            return;
        }
        Calendar ca = Calendar.getInstance();
        int dia = Integer.valueOf(f.substring(0, 2));
        int mes = Integer.valueOf(f.substring(3, 5));
        int anio = Integer.valueOf(f.substring(6, 10));
        ca.set(anio, mes - 1, dia);
        fecha = ca.getTime();
    }

    /**
     * Devuelve un String con la fecha con formato yyyy-mm-dd. 
     */
    @Override
    public String toString() {
        Calendar ca = Calendar.getInstance();
        ca.setTime(fecha);

        String res = String.valueOf(ca.get(Calendar.YEAR)) + "-" +
                String.valueOf(ca.get(Calendar.MONTH) + 1) + "-" +
                String.valueOf(ca.get(Calendar.DAY_OF_MONTH));

        return res;
    }

    /**
     * Devuelve un String con la fecha con formato dd/mm/yyyy.
     */
    public String getFechaArgentina() {
        Calendar ca = Calendar.getInstance();
        if (fecha == null) {
            return "";
        }
        ca.setTime(fecha);

        String dia = String.valueOf(ca.get(Calendar.DAY_OF_MONTH));
        if (dia.length() == 1) {
            dia = "0" + dia;
        }

        String mes = String.valueOf(ca.get(Calendar.MONTH) + 1);
        if (mes.length() == 1) {
            mes = "0" + mes;
        }

        String res = dia + "/" + mes + "/" + String.valueOf(ca.get(Calendar.YEAR));

        return res;
    }

    /**
     * Devuelve un String con la fecha con formato yyyy-mm-dd.
     */
    public String getFechaSQL() {
        Calendar ca = Calendar.getInstance();
        if (fecha == null) {
            return "";
        }
        ca.setTime(fecha);

        String res = String.valueOf(ca.get(Calendar.YEAR)) + "-" +
                String.valueOf(ca.get(Calendar.MONTH) + 1) + "-" +
                String.valueOf(ca.get(Calendar.DAY_OF_MONTH));

        return res;
    }

    /**
     * Devuelve la fecha Minima del Sistema.
     * Actualmente es 1 de Enero de 1900.
     */
    public static java.util.Date fechaMinimaSistema() {
        Calendar cal = Calendar.getInstance();
        cal.set(1900, 0, 1);
        return cal.getTime();
    }

    /**
     * Devuelve la fecha actual del Sistema.
     */
    public static java.util.Date getFechaActualSistema() {
        return (new java.util.Date());
    }

    /**
     * Devuelve el año actual del Sistema en un int
     */
    public static int getAnioActualSistema() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date());
        return cal.get(Calendar.YEAR);
    }

    public static int getMesActualSistema() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date());
        return cal.get(Calendar.MONTH);
    }

    public static int getDiaActualSistema() {
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date());
        return c.get(Calendar.DATE);
    }

    /**
     * Devuelve la hora actual, en formato HH:MM.
     * @return Hora actual del sistema.
     */
    public static String getHoraActual() {
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date());
        String hora = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        if (hora.length() == 1) {
            hora = "0" + hora;
        }
        String minutos = String.valueOf(c.get(Calendar.MINUTE));
        if (minutos.length() == 1) {
            minutos = "0" + minutos;
        }
        return hora + ":" + minutos;
    }

    /**
     * Devuelve la fecha actual, en formato dd/mm/yyyy.
     * @return Fecha actual del sistema.
     */
    public static String getFechaActual() {
        return new Fecha().getFechaArgentina();
    }
} //Fin Clase Fecha
