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
package gecom.app.table;

import simbya.gecom.entidades.parametros.ParametroSistema;

/**
 * Envoltorio para los parámetros de sistema.
 * @author Marcelo Busico.
 */
public class ParametroSistemaTableWrapper {

    private ParametroSistema parametro;

    public ParametroSistemaTableWrapper(ParametroSistema parametro) {
        this.parametro = parametro;
    }

    public ParametroSistema getParametro() {
        return parametro;
    }

    public void setParametro(ParametroSistema parametro) {
        this.parametro = parametro;
    }

    /**
     * Muestra el valor del parámetro.
     */
    @Override
    public String toString() {
        return parametro.getValor();
    }
}
