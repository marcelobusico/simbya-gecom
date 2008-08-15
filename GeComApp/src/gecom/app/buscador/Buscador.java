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
package gecom.app.buscador;

import gecom.app.VentanaMenuPrincipal;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.buscador.DialogoFiltroGenerico;
import simbya.framework.buscador.ParametroFiltrado;
import simbya.gecom.entidades.Cliente;
import simbya.gecom.entidades.Producto;
import simbya.gecom.entidades.Proveedor;
import simbya.gecom.gestores.buscador.GestorBuscadorRemote;

/**
 * Clase que contiene métodos útiles a la hora de buscar entidades en la BD.
 * @author Marcelo Busico.
 */
public final class Buscador {

    private static final Logger log = Logger.getLogger(Buscador.class);

    private Buscador() {
    }

    /**
     * Busca un producto en la base de datos a partir de su codigo.
     * @param codigo Código del producto a buscar.
     * @return Referencia al objeto seleccionado por el usuario, null si no se
     * encontró.
     */
    public static Producto encontrarProducto(long codigo) {
        try {
            GestorBuscadorRemote gestor = (GestorBuscadorRemote) GestorConexion.getInstancia().
                    getObjetoRemoto(GestorBuscadorRemote.class);
            Object res = gestor.buscarUnicoResultado("from Producto where " +
                    "baja = false and codigo = " + codigo);
            if (res != null) {
                return (Producto) res;
            }
        } catch (Exception ex) {
            log.error("Error al buscar producto.", ex);
        }
        return null;
    }

    /**
     * Busca y selecciona un Producto a partir del diálogo de selección genérico.
     * @param mostrarBajas Informa si desea mostrar los productos dados de baja (true)
     * o no (false).
     * @return Referencia al objeto seleccionado por el usuario, null si canceló
     * la operación.
     */
    public static Producto buscarProducto(boolean mostrarBajas) {
        String condicion = null;
        if (!mostrarBajas) {
            condicion = "baja = false";
        }
        Object seleccion = null;
        try {
            GestorBuscadorRemote gestor = (GestorBuscadorRemote) GestorConexion.getInstancia().
                    getObjetoRemoto(GestorBuscadorRemote.class);
            LinkedList<ParametroFiltrado> parametros = new LinkedList<ParametroFiltrado>();
            parametros.add(new ParametroFiltrado("descripcion", "Descripción", ParametroFiltrado.cadena));
            parametros.add(new ParametroFiltrado("marca.nombre", "Marca", ParametroFiltrado.cadena));
            parametros.add(new ParametroFiltrado("rubro.nombre", "Rubro", ParametroFiltrado.cadena));
            parametros.add(new ParametroFiltrado("cilindradaMoto.nombre", "Cilindrada Moto", ParametroFiltrado.cadena));
            parametros.add(new ParametroFiltrado("modelo.nombre", "Modelo", ParametroFiltrado.cadena));
            parametros.add(new ParametroFiltrado("anio", "Año", ParametroFiltrado.entero));
            parametros.add(new ParametroFiltrado("ubicacionEstanteria", "Ubicación Estantería", ParametroFiltrado.cadena));
            parametros.add(new ParametroFiltrado("medidaInterior", "Medida Interior", ParametroFiltrado.decimal));
            parametros.add(new ParametroFiltrado("medidaExterior", "Medida Exterior", ParametroFiltrado.decimal));
            parametros.add(new ParametroFiltrado("medidaEspesor", "Medida Espesor", ParametroFiltrado.decimal));
            seleccion = DialogoFiltroGenerico.mostrarDialogo(
                    VentanaMenuPrincipal.getInstancia().getFrame(),
                    Producto.class.getName(),
                    parametros,
                    gestor, condicion);
        } catch (Exception ex) {
            log.error("Error al abrir cuadro de diálogo.", ex);
        }
        Producto res = null;
        if (seleccion != null) {
            res = (Producto) seleccion;
        }
        return res;
    }
    
    /**
     * Busca y selecciona un Proveedor a partir del diálogo de selección genérico.
     * @param mostrarBajas Informa si desea mostrar los proveedores dados de baja (true)
     * o no (false).
     * @return Referencia al objeto seleccionado por el usuario, null si canceló
     * la operación.
     */
    public static Proveedor buscarProveedor(boolean mostrarBajas) {
        String condicion = null;
        if (!mostrarBajas) {
            condicion = "fechaBaja is null";
        }
        Object seleccion = null;
        try {
            GestorBuscadorRemote gestor = (GestorBuscadorRemote) GestorConexion.getInstancia().
                    getObjetoRemoto(GestorBuscadorRemote.class);
            LinkedList<ParametroFiltrado> parametros = new LinkedList<ParametroFiltrado>();
            parametros.add(new ParametroFiltrado("razonSocial", "Razón Social", ParametroFiltrado.cadena));
            parametros.add(new ParametroFiltrado("cuit", "Número de CUIT", ParametroFiltrado.cadena));
            parametros.add(new ParametroFiltrado("localidad.nombre", "Localidad", ParametroFiltrado.cadena));
            seleccion = DialogoFiltroGenerico.mostrarDialogo(
                    VentanaMenuPrincipal.getInstancia().getFrame(),
                    Proveedor.class.getName(),
                    parametros,
                    gestor, condicion);
        } catch (Exception ex) {
            log.error("Error al abrir cuadro de diálogo.", ex);
        }
        Proveedor res = null;
        if (seleccion != null) {
            res = (Proveedor) seleccion;
        }
        return res;
    }

    /**
     * Busca y selecciona un Cliente a partir del diálogo de selección genérico.
     * @param mostrarBajas Informa si desea mostrar los clientes dados de baja (true)
     * o no (false).
     * @return Referencia al objeto seleccionado por el usuario, null si canceló
     * la operación.
     */
    public static Cliente buscarCliente(boolean mostrarBajas) {
        String condicion = null;
        if (!mostrarBajas) {
            condicion = "fechaBaja is null";
        }
        Object seleccion = null;
        try {
            GestorBuscadorRemote gestor = (GestorBuscadorRemote) GestorConexion.getInstancia().
                    getObjetoRemoto(GestorBuscadorRemote.class);
            LinkedList<ParametroFiltrado> parametros = new LinkedList<ParametroFiltrado>();
            parametros.add(new ParametroFiltrado("apellido", "Apellido", ParametroFiltrado.cadena));
            parametros.add(new ParametroFiltrado("nombre", "Nombre", ParametroFiltrado.cadena));
            parametros.add(new ParametroFiltrado("localidad.nombre", "Localidad", ParametroFiltrado.cadena));
            seleccion = DialogoFiltroGenerico.mostrarDialogo(
                    VentanaMenuPrincipal.getInstancia().getFrame(),
                    Cliente.class.getName(),
                    parametros,
                    gestor, condicion);
        } catch (Exception ex) {
            log.error("Error al abrir cuadro de diálogo.", ex);
        }
        Cliente res = null;
        if (seleccion != null) {
            res = (Cliente) seleccion;
        }
        return res;
    }
}
