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
package simbya.framework.appserver;

import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import simbya.framework.excepciones.ConexionFallidaException;

/**
 * Gestor que intenta establecer la conexión con el servidor de aplicaciones
 * verificando que las versiones de los programas sean las mismas.
 * @author Marcelo Busico.
 */
public class GestorConexion {

    private static final Logger log = Logger.getLogger(GestorConexion.class);
    private InitialContext ic;
    private static GestorConexion instancia = null;

    private GestorConexion(Properties p) throws NamingException {
        ic = new InitialContext(p);
    }

    /**
     * Se conecta al servidor de aplicaciones usando RMI especificado por el
     * usuario.
     * @param serverAddress Dirección de red del servidor de aplicaciones.
     * @param serverPort Puerto de escucha del protocolo.
     * @throws simbya.framework.excepciones.ConexionFallidaException Si no se
     * puede conectar al servidor indicado.
     */
    public static void conectar(String serverAddress, int serverPort)
            throws ConexionFallidaException {

        if (instancia == null) {
            try {
                log.debug("Server: " + serverAddress);
                log.debug("Puerto: " + serverPort);
                Properties p = new Properties();
                p.setProperty("java.naming.factory.initial",
                        "com.sun.enterprise.naming.SerialInitContextFactory");
                p.setProperty("java.naming.factory.url.pkgs",
                        "com.sun.enterprise.naming");
                p.setProperty("java.naming.factory.state",
                        "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
                p.setProperty("org.omg.CORBA.ORBInitialHost", serverAddress);
                p.setProperty("org.omg.CORBA.ORBInitialPort", String.valueOf(serverPort));
                instancia = new GestorConexion(p);
            } catch (NamingException ex) {
                throw new ConexionFallidaException(ex);
            }
        }
    }

    /**
     * Se conecta al servidor de aplicaciones usando RMI ubicado en
     * localhost, puerto 3700.
     * @throws simbya.framework.excepciones.ConexionFallidaException Si no se
     * puede conectar al servidor indicado.
     */
    public static void conectar()
            throws ConexionFallidaException {

        conectar("localhost", 3701);
    }

    /**
     * Devuelve la única instancia del gestor de conexión, siempre y cuando
     * haya existido una conexión previa.
     * @return Referencia al GestorConexion, null si no está previamente conectado.
     */
    public static GestorConexion getInstancia() {
        return instancia;
    }

    /**
     * Devuelve un objeto remoto del servidor de aplicaciones especificado por 
     * el usuario.
     * @param clase Clase del objeto remoto a devolver.
     * @return Referencia al objeto remoto.
     * @throws javax.naming.NamingException Si la clase especificada no existe
     * en el servidor.
     */
    public Object getObjetoRemoto(Class clase) throws NamingException {
        return ic.lookup(clase.getName());
    }
}
