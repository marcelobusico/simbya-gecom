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
package gecom.app;

import gecom.app.tipos.VentanaActualizarTipos;
import javax.naming.NamingException;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.interfaces.VentanaInterna;
import simbya.gecom.entidades.seguridad.UsuarioSistema;
import simbya.gecom.interfaces.InterfazTipoGenerico;

/**
 * Gestor del menú principal de la aplicación.
 * @author Marcelo Busico.
 */
public class GestorMenuPrincipal {

    private static final Logger log = Logger.getLogger(GestorMenuPrincipal.class);
    private int ultimaPosicionXVentana = -20;
    private int ultimaPosicionYVentana = -20;
    private UsuarioSistema usuarioSistema;
    // Parámetros de ubicación de ventanas. Pueden modificarse.
    private int cantVentanasVerticales = 3;
    private int cantVentanasHorizontales = 3;
    private int separacionEntreVentanas = 30;

    public GestorMenuPrincipal() {
    }

    /**
     * Genera las coordenadas X e Y de la próxima posición 
     * en donde debería aparecer la próxima ventana.<br>
     * Utilizar este método en ventanas llamadas del menú.<br>
     * NO se debería utilizar en ventanas hijas llamadas por otras
     * ventanas internas.
     */
    public void generarPosicionVentana() {
        // Lógica del método de acá en adelante.
        int total = (cantVentanasVerticales - 1) * separacionEntreVentanas;
        int totalHorizontal = (cantVentanasHorizontales - 1) * total;
        if (ultimaPosicionYVentana >= total) {
            ultimaPosicionYVentana = -(separacionEntreVentanas - 10);
            if (ultimaPosicionXVentana >= totalHorizontal) {
                ultimaPosicionXVentana = -(separacionEntreVentanas - 10);
            } else {
                ultimaPosicionXVentana -= total;
            }
        }
        ultimaPosicionXVentana += separacionEntreVentanas;
        ultimaPosicionYVentana += separacionEntreVentanas;
    }

    /**
     * Devuelve la posición donde deberían colocarse las 
     * ventanas internas en el Menú Principal.
     * @return Posición en el eje Horizonal del panel de fondo.
     */
    public int getPosicionXVentana() {
        return ultimaPosicionXVentana;
    }

    /**
     * Devuelve la posición donde deberían colocarse las 
     * ventanas internas en el Menú Principal.
     * @return Posición en el eje Vertical del panel de fondo.
     */
    public int getPosicionYVentana() {
        return ultimaPosicionYVentana;
    }

    /**
     * Devuelve el Usuario del Sistema que ha iniciado la sesión actual.
     * @return Referencia a un Usuario del Sistema.
     */
    public UsuarioSistema getUsuarioSistema() {
        return usuarioSistema;
    }

    /**
     * Establece el Usuario del Sistema que ha iniciado la sesión actual.
     * @param usuarioSistema Referencia a un Usuario del Sistema.
     */
    public void setUsuarioSistema(UsuarioSistema usuarioSistema) {
        this.usuarioSistema = usuarioSistema;
    }

    /**
     * Abre una ventana en el menú principal.
     * @param ventana Ventana a abir.
     * @return Referencia a la ventana creada.
     */
    public VentanaInterna iniciarCU(Class claseVentana) {
        VentanaInterna ventana = null;
        try {
            ventana = (VentanaInterna) claseVentana.newInstance();
            VentanaMenuPrincipal.getInstancia().getPanelPrincipal().add(ventana);
            //Enlaza la ventana con el gestor.
            try {
                ventana.enlazarGestorRemoto(GestorConexion.getInstancia());
            } catch (NamingException ex) {
                String mensaje = "No se pudo conectar con el gestor remoto.";
                log.error(mensaje, ex);
                JOptionPane.showMessageDialog(null, mensaje + "\n" + ex.getMessage());
                return null;
            }
            //Establece la posicion de la ventana.
            generarPosicionVentana();
            ventana.setLocation(getPosicionXVentana(), getPosicionYVentana());
            //Inicializar la ventana.
            ventana.inicializarVentana();
            //Muestra la ventana.
            ventana.setVisible(true);
            //Intenta establecer a la ventana como seleccionada,
            //Si no puede, lanza imprime la excepcion en la salida estandar.
            try {
                ventana.setSelected(true);
            } catch (java.beans.PropertyVetoException e) {
                log.warn("Error al establecer ventana como seleccionada.", e);
            }
            log.debug("Iniciado CU: " + claseVentana.getName());
        } catch (InstantiationException ex) {
            log.error("No se pudo instanciar la clase", ex);
        } catch (IllegalAccessException ex) {
            log.error("No se pudo instanciar la clase", ex);
        }
        return ventana;
    }

    /**
     * Abre el Caso de Uso e inicia su ejecución para las ventanas
     * VentanaActualizarTiposGenericoContable y VentanaActualizarTiposGenerico
     * @param nombreClaseVentana Nombre de la Clase de la Ventana que se 
     * va a mostrar.
     * @param nombreClaseGenerica Nombre de la Clase que se va a actualizar.
     * @param titulo Nombre de la parte del título que indica actividad (no poner
     * todo completo, solo lo que se está actualizando). La ventana mostrará:
     * "Actualizar " + titulo.
     * @return Referencia a la ventana creada.
     */
    public JInternalFrame iniciarCUGenerico(Class claseVentana,
            Class claseGenerica, String titulo) {
        JInternalFrame ventana = null;
        try {
            //Instanciar el CU.
            ventana = (JInternalFrame) claseVentana.newInstance();
            VentanaMenuPrincipal.getInstancia().getPanelPrincipal().add(ventana);
            //Establece la posicion de la ventana.
            generarPosicionVentana();
            ventana.setLocation(getPosicionXVentana(), getPosicionYVentana());
            //Muestra la ventana.
            ventana.setVisible(true);
            //Intenta establecer a la ventana como seleccionada,
            //Si no puede, lanza imprime la excepcion en la salida estandar.
            try {
                ventana.setSelected(true);
            } catch (java.beans.PropertyVetoException e) {
                log.warn("Error al establecer ventana como seleccionada.", e);
            }
            InterfazTipoGenerico tg = (InterfazTipoGenerico) claseGenerica.newInstance();
            ((VentanaActualizarTipos) ventana).opcionActualizarTipos(titulo, tg);
            log.debug("Iniciado CU Genérico: " + claseVentana.getName() +
                    " - Para la Clase: " + claseGenerica.getName());
        } catch (InstantiationException ex) {
            log.error("No se pudo instanciar la clase", ex);
        } catch (IllegalAccessException ex) {
            log.error("No se pudo instanciar la clase", ex);
        }
        return ventana;
    }
}
