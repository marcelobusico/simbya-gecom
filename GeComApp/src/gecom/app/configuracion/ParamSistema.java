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
package gecom.app.configuracion;

import javax.naming.NamingException;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.gecom.gestores.configuracion.GestorConfiguracionGeneralRemote;

/**
 * Clase que obtiene del servidor de aplicaciones los parámetros del sistema.
 * @author Marcelo Busico.
 */
public class ParamSistema {

    private static final Logger log = Logger.getLogger(ParamSistema.class);

    public static Object getValorParametro(String clave) {
        try {
            GestorConfiguracionGeneralRemote gestor =
                    (GestorConfiguracionGeneralRemote) GestorConexion.getInstancia().getObjetoRemoto(
                    GestorConfiguracionGeneralRemote.class);
            return gestor.getValorParametro(clave);
        } catch (NamingException ex) {
            log.error(ex);
            return null;
        }
    }

    /**
     * Actualiza el valor de un parámetro del sistema en la base de datos.
     * @param clave Nombre de la clave del valor.
     * @param valor Nuevo valor a persistir.
     */
    public static void actualizarValorParametro(String clave, Object valor) throws Exception {
        GestorConfiguracionGeneralRemote gestor =
                (GestorConfiguracionGeneralRemote) GestorConexion.getInstancia().
                getObjetoRemoto(GestorConfiguracionGeneralRemote.class);

        gestor.actualizarValorParametro(clave, valor);
    }

    public static Long getValorParametroLong(String clave) {
        Object valorParametro = getValorParametro(clave);
        if (valorParametro == null) {
            return null;
        }
        return (Long) valorParametro;
    }

    public static Integer getValorParametroInteger(String clave) {
        Object valorParametro = getValorParametro(clave);
        if (valorParametro == null) {
            return null;
        }
        return (Integer) valorParametro;
    }

    public static Float getValorParametroFloat(String clave) {
        Object valorParametro = getValorParametro(clave);
        if (valorParametro == null) {
            return null;
        }
        return (Float) valorParametro;
    }

    public static String getValorParametroString(String clave) {
        Object valorParametro = getValorParametro(clave);
        if (valorParametro == null) {
            return null;
        }
        return (String) valorParametro;
    }
}
