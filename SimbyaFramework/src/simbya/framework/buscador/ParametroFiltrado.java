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

/**
 * Clase de entidad para almacenar atributos de busqueda con su descripcion y
 * tipo de dato.
 * @author Marcelo Busico.
 */
public class ParametroFiltrado {

    private String atributo;
    private String descripcion;
    private int tipo;
    public static final int cadena = 1;
    public static final int entero = 2;
    public static final int decimal = 3;
    public static final int fecha = 4;

    /**
     * Crea una nueva instancia de la clase.
     * @param atributo Nombre del atributo.
     * @param descripcion Descripción del atributo.
     * @param tipo Tipo del Atributo -> Constante de Clase.
     */
    public ParametroFiltrado(String atributo, String descripcion, int tipo) {
        this.atributo = atributo;
        this.descripcion = descripcion;
        this.tipo = tipo;
    }

    /**
     * Devuelve el nombre del atributo de clase que se usará para realizar
     * un filtrado de datos.
     * @return Cadena con el nombre del atributo de clase.
     */
    public String getAtributo() {
        return atributo;
    }

    /**
     * Establece el nombre del atributo de clase que se usará para realizar
     * un filtrado de datos.
     * @param atributo Cadena con el nombre del atributo de clase.
     */
    public void setAtributo(String atributo) {
        this.atributo = atributo;
    }

    /**
     * Devuelve la descripción del parámetro.
     * @return Cadena con la descripción.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción del parámetro.
     * @param descripcion Cadena con la descripción.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Devuelve el tipo de dato utilizado en el atributo.
     * @return Entero que representa el tipo de dato del atributo 
     * (Ver constantes de Clase).
     */
    public int getTipo() {
        return tipo;
    }

    /**
     * Establece el tipo de dato utilizado en el atributo.
     * @param tipo Entero que representa el tipo de dato del atributo 
     * (Ver constantes de Clase).
     */
    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    /**
     * Devuelve la descripción del parámetro.
     * @return Cadena con descripción.
     */
    @Override
    public String toString() {
        return this.getDescripcion();
    }
}
