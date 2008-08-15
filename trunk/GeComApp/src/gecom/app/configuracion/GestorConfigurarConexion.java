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

import java.io.IOException;
import javax.swing.JFrame;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.appserver.ServerConf;
import simbya.framework.excepciones.ConexionFallidaException;
import simbya.framework.excepciones.FaltanDatosRequeridosException;

/**
 * Gestor para configurar los parámetros de conexión al motor de BD.
 * @author Marcelo Busico
 */
public class GestorConfigurarConexion {

    private JFrame ventanaInvocadora;
    private VentanaConfigurarConexion ventana;

    /**
     * Crea una nueva instancia del GestorConfigurarConexion.
     * @param gestorInvocador Gestor invocador (en un gestor usar this).
     */
    public GestorConfigurarConexion(JFrame ventanaInvocadora) {
        this.ventanaInvocadora = ventanaInvocadora;
        ventana = new VentanaConfigurarConexion(this);
        ventana.setVisible(true);
    }

    /**
     * Crea una nueva instancia del GestorConfigurarConexion.
     * @param ventanaInvocadora Ventana que llama a esta funcion.
     */
    public GestorConfigurarConexion(JFrame ventanaInvocadora, ServerConf configActual) {
        this.ventanaInvocadora = ventanaInvocadora;
        ventana = new VentanaConfigurarConexion(this);
        ventana.mostrarConfiguracion(configActual);
        ventana.setVisible(true);
    }

    /**
     * Verifica los datos necesarios para la conexión.
     */
    private String verificarDatos(ServerConf sc) {
        String datosFaltantes = "";

        if (sc.getDireccion() == null || sc.getDireccion().isEmpty()) {
            datosFaltantes += "Servidor - ";
        }
        if (sc.getPuerto() <= 0) {
            //Setea el puerto por defecto.
            sc.setPuerto(3700);
        }

        if (datosFaltantes.isEmpty() == true) {
            return null;
        } else {
            return datosFaltantes;
        }
    }

    /**
     * Guarda la configuración en un archivo persistente.
     */
    public void tomarConfirmacion(ServerConf sc) throws ConexionFallidaException, IOException, FaltanDatosRequeridosException {
        //Verificar que estén todos los datos necesarios
        String datosFaltantes = verificarDatos(sc);
        if (datosFaltantes != null) {
            throw new FaltanDatosRequeridosException(datosFaltantes);
        }

        //Intenta conectar a la BD
        GestorConexion.conectar(sc.getDireccion(), sc.getPuerto());

        //Guarda el archivo
        try {
            sc.guardarEnArchivo(ServerConf.nombreArchivo);
        } catch (IOException e) {
            throw new IOException("Error al guardar configuración en archivo.\nVerifique que se tengan los permisos suficientes para guardar en el directorio de trabajo.\n" +
                    "Detalles del error:\n" + e.getMessage(), e);
        }
    }

    public void finalizar() {
        ventana.dispose();
        if (ventanaInvocadora != null) {
            ventanaInvocadora.setVisible(true);
        } else {
            System.exit(0);
        }
    }
}
