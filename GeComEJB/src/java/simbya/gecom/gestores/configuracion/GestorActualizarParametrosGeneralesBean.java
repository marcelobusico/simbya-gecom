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
package simbya.gecom.gestores.configuracion;

import java.util.List;
import javax.ejb.Stateful;
import simbya.gecom.entidades.parametros.ConfiguracionGeneral;
import simbya.gecom.entidades.parametros.ParametroSistema;

/**
 * Gestor para actualizar los parámetros del sistema.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorActualizarParametrosGeneralesBean
        implements GestorActualizarParametrosGeneralesRemote {

    /**
     * Obtiene una lista con todos los parámetros del sistema.
     * @return Lista con parámetros del sistema.
     */
    public List<ParametroSistema> getParametros() {
        ConfiguracionGeneral.renovarInstancia();
        return ConfiguracionGeneral.getInstancia().getParametros();
    }

    /**
     * Actualiza el valor de los parámetros del sistema en la base de datos.
     * @param parametros Lista con los parámetros a persistir.
     * @throws Exception Si sucede algún error en la actualización.
     */
    public void actualizarParametros(List<ParametroSistema> parametros)
            throws Exception {

        for (ParametroSistema param : parametros) {
            ConfiguracionGeneral.getInstancia().setValorParametro(
                    param.getClave(), param.getValor(), param.getTipoValor());
        }
        ConfiguracionGeneral.getInstancia().persistir();
        ConfiguracionGeneral.renovarInstancia();
    }
}
