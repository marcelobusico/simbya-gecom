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
package simbya.gecom.entidades.parametros;

import java.io.Serializable;

/**
 * Clase de entidad para los Parámetros del Sistema.
 * @author Marcelo Busico
 */
public class ParametroSistema implements Serializable {

    private long oid;
    private String clave;
    private String valor;
    private Class tipoValor;

    /** 
     * Crea una nueva instancia de ParametroSistema.
     */
    public ParametroSistema() {
    }

    /** 
     * Crea una nueva instancia de ParametroSistema.
     * @param clave Clave del parámetro a crear.
     * @param valor Valor del parámetro actual.
     * @param valor Tipo del parámetro actual.
     */
    public ParametroSistema(String clave, String valor, Class tipoValor) {
        this.clave = clave;
        this.valor = valor;
        this.tipoValor = tipoValor;
    }

    public Class getTipoValor() {
        return tipoValor;
    }

    public void setTipoValor(Class tipoValor) {
        this.tipoValor = tipoValor;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getClave() {
        return clave;
    }

    public long getOid() {
        return oid;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    /**
     * Muestra el nombre de la clave del parámetro del sistema.
     */
    @Override
    public String toString() {
        return this.getClave();
    }
}
