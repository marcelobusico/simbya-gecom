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
package simbya.framework.buscador;

import java.util.Date;
import simbya.framework.formateadores.FormateadorEstandar;

/**
 * Clase de entidad que representa un filtro de búsqueda.
 * @author Marcelo Busico
 */
public class Filtro {

    private ParametroFiltrado parametro;
    private Object valor;
    private int tipo;
    
    //CONSTANTES DE CLASE
    public static final int TIPO_CADENA_EXACTO = 10;
    public static final int TIPO_CADENA_CONTIENE = 11;
    public static final int TIPO_CADENA_COMIENZO = 12;
    public static final int TIPO_CADENA_FINAL = 13;
    
    public static final int TIPO_ENTERO_IGUAL = 20;
    public static final int TIPO_ENTERO_DISTINTO = 21;
    public static final int TIPO_ENTERO_MENOR = 22;
    public static final int TIPO_ENTERO_MAYOR = 23;
    
    public static final int TIPO_DECIMAL_IGUAL = 30;
    public static final int TIPO_DECIMAL_DISTINTO = 31;
    public static final int TIPO_DECIMAL_MENOR = 32;
    public static final int TIPO_DECIMAL_MAYOR = 33;

    public static final int TIPO_FECHA_IGUAL = 40;
    public static final int TIPO_FECHA_DISTINTA = 41;
    public static final int TIPO_FECHA_ANTES = 42;
    public static final int TIPO_FECHA_DESPUES = 43;
    
    /**
     * Crea una nueva instancia de Filtro.
     */
    public Filtro() {
        this.parametro = null;
        this.valor = null;
        this.tipo = 0;
    }

    /**
     * Crea una nueva instancia de Filtro.
     * @param parametro Parámetro por el cual filtrar.
     * @param valor Valor del parámetro a filtrar.
     * @param tipo Opciones de filtrado (Ver constantes de clase).
     */
    public Filtro(ParametroFiltrado parametro, Object valor, int tipo) {
        this.parametro = parametro;
        this.valor = valor;
        this.tipo = tipo;
    }

    /**
     * Devuelve el parámetro por el cual filtrar.
     */
    public ParametroFiltrado getParametro() {
        return parametro;
    }
    /**
     * Establece el parámetro por el cual filtrar.
     */
    public void setParametro(ParametroFiltrado parametro) {
        this.parametro = parametro;
    }

    /**
     * Devuelve un objeto que puede ser un Date, Float o String.
     */
    public Object getValor() {
        return valor;
    }

    /**
     * Establece un objeto Date, Float o String.
     */
    public void setValor(Object valor) {
        this.valor = valor;
    }

    /**
     * Devuelve la opción de filtrado (Ver constantes de clase).
     * @return Valor correspondiente a la opción.
     */
    public int getTipo() {
        return tipo;
    }

    /**
     * Establece la la opción de filtrado (Ver constantes de clase).
     * @param tipo Valor correspondiente a la opción.
     */
    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    /**
     * Muestra el filtro en una cadena de texto.
     * @return Cadena con los datos del filtro.
     */
    @Override
    public String toString() {
        if(parametro==null || valor==null) {
            return "Filtro erroneo.";
        }
        String res = parametro.getDescripcion();
        
        switch(tipo) {
            case TIPO_CADENA_EXACTO:
                res+=" es exactamente '" + valor.toString() + "'";                
                break;
            case TIPO_CADENA_CONTIENE:
                res+=" contiene a '" + valor.toString() + "'";                
                break;
            case TIPO_CADENA_COMIENZO:
                res+=" comienza con '" + valor.toString() + "'";                
                break;
            case TIPO_CADENA_FINAL:
                res+=" termina en '" + valor.toString() + "'";                
                break;
                
            case TIPO_ENTERO_IGUAL:
                res+=" es igual a " + valor.toString();
                break;
            case TIPO_ENTERO_DISTINTO:
                res+=" es distinto a " + valor.toString();
                break;
            case TIPO_ENTERO_MENOR:
                res+=" es menor que " + valor.toString();
                break;
            case TIPO_ENTERO_MAYOR:
                res+=" es mayor que " + valor.toString();
                break;
                
            case TIPO_DECIMAL_IGUAL:
                res+=" es igual a " + 
                        FormateadorEstandar.formatearDecimal(
                        ((Float) valor).floatValue());
                break;
            case TIPO_DECIMAL_DISTINTO:
                res+=" es distinto a " +
                        FormateadorEstandar.formatearDecimal(
                        ((Float) valor).floatValue());
                break;
            case TIPO_DECIMAL_MENOR:
                res+=" es menor que " +
                        FormateadorEstandar.formatearDecimal(
                        ((Float) valor).floatValue());
                break;
            case TIPO_DECIMAL_MAYOR:
                res+=" es mayor que " +
                        FormateadorEstandar.formatearDecimal(
                        ((Float) valor).floatValue());
                break;
                
            case TIPO_FECHA_IGUAL:
                res+=" es igual a " + 
                        FormateadorEstandar.formatearFecha(
                        (Date) valor);
                break;
            case TIPO_FECHA_DISTINTA:
                res+=" es distinta a " +
                        FormateadorEstandar.formatearFecha(
                        (Date) valor);
                break;
            case TIPO_FECHA_ANTES:
                res+=" es anterior a " +
                        FormateadorEstandar.formatearFecha(
                        (Date) valor);
                break;
            case TIPO_FECHA_DESPUES:
                res+=" es posterior a " +
                        FormateadorEstandar.formatearFecha(
                        (Date) valor);
                break;
        }
        return res;
    }
    
}
