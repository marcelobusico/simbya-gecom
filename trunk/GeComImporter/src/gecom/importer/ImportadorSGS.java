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
package gecom.importer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Producto;
import simbya.gecom.entidades.TipoCalidad;
import simbya.gecom.entidades.TipoMarca;
import simbya.gecom.entidades.TipoRubro;
import simbya.gecom.entidades.TipoUnidadMedida;
import simbya.gecom.gestores.productos.GestorActualizarProductoRemote;
import simbya.gecom.gestores.tipos.GestorActualizarTiposRemote;
import simbya.gecom.interfaces.InterfazTipoGenerico;

/**
 * Importa los datos cargados en el Sistema de Gestión de Stock y los
 * carga en SIMBYA-GECOM.
 * @author Marcelo Busico.
 */
public class ImportadorSGS {

    private static final Logger log = Logger.getLogger(ImportadorSGS.class);
    private String origenODBC;
    private Connection connection;
    private String server;
    private int puerto;
    GestorActualizarTiposRemote gestorTipos = null;
    GestorActualizarProductoRemote gestorProductos = null;

    private ImportadorSGS(String origenODBC, String server, int puerto) {
        this.origenODBC = origenODBC;
        this.server = server;
        this.puerto = puerto;
    }

    /**
     * Importa los datos cargados en el Sistema de Gestión de Stock y los
     * carga en SIMBYA-GECOM.
     * @param origen Origen ODBC para tomar la BD.
     * @param server Direccion ip del servidor de aplicaciones.
     * @param puerto Puerto del servidor de aplicaciones.
     * @throws Exception Si sucede algún error.
     */
    public static void importar(String origen, String server, int puerto) throws Exception {
        ImportadorSGS importadorSGS = new ImportadorSGS(origen, server, puerto);
        importadorSGS.run();
    }

    /**
     * Proceso de importación propiamente dicho.
     */
    public void run() throws Exception {
        System.out.println("**** IMPORTADOR INICIADO ****");
        System.out.print("Obteniendo Drivers...");
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        } catch (ClassNotFoundException ex) {
            log.error("Error al obtener driver odbc.", ex);
            throw new Exception("Error al obtener driver ODBC:\n" +
                    ex.getMessage());
        }
        System.out.println(" OK.");
        System.out.print("Conectando a origen...");
        connection = null;
        try {
            connection = DriverManager.getConnection(origenODBC);
        } catch (SQLException ex) {
            log.error("Error al obtener conexión.", ex);
            throw new Exception("Error al obtener conexión:\n" +
                    ex.getMessage());
        }
        System.out.println(" OK.");
        System.out.print("Conectando a destino...");
        try {
            GestorConexion.conectar(server, puerto);
            gestorTipos = (GestorActualizarTiposRemote) GestorConexion.getInstancia().
                    getObjetoRemoto(GestorActualizarTiposRemote.class);
            gestorProductos = (GestorActualizarProductoRemote) GestorConexion.getInstancia().
                    getObjetoRemoto(GestorActualizarProductoRemote.class);
        } catch (Exception ex) {
            log.error("Error al conectarse con el servidor de aplicaciones.", ex);
            throw new Exception(
                    "Error al conectarse con el servidor de aplicaciones:\n" +
                    ex.getMessage());
        }
        System.out.println(" OK.");

        System.out.println("Comenzando la importación...");

        //Importar las marcas
        System.out.println("Importando Marcas...");
        importarMarcas();

        //Importar los rubros
        System.out.println("Importando Rubros...");
        importarRubros();

        //Importar los productos
        System.out.println("Importando Productos...");
        importarProductos();

        System.out.println("**** IMPORTADOR FINALIZADO ****");
    }

    private void importarMarcas() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(
                "SELECT Id, Nombre " +
                "FROM Marca");

        List<InterfazTipoGenerico> tipos = new LinkedList<InterfazTipoGenerico>();
        while (rs.next()) {
            long oid = rs.getInt(1);
            if (oid > 0) {
                TipoMarca marca = new TipoMarca();
                marca.setNombre(rs.getString(2));
                tipos.add(marca);
                System.out.println("Marca importada: " + marca.getNombre());
            }
        }
        rs.close();
        System.out.print("Guardando marcas en BD...");
        gestorTipos.actualizarTipos(tipos);
        System.out.println(" OK.");
    }

    private void importarRubros() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(
                "SELECT Id, Nombre " +
                "FROM Rubro");

        List<InterfazTipoGenerico> tipos = new LinkedList<InterfazTipoGenerico>();
        while (rs.next()) {
            long oid = rs.getInt(1);
            if (oid > 0) {
                TipoRubro rubro = new TipoRubro();
                rubro.setNombre(rs.getString(2));
                tipos.add(rubro);
                System.out.println("Rubro importado: " + rubro.getNombre());
            }
        }
        rs.close();
        System.out.print("Guardando rubros en BD...");
        gestorTipos.actualizarTipos(tipos);
        System.out.println(" OK.");
    }

    /**
     * Obtiene la unidad de medida que representa unidades.
     */
    private TipoUnidadMedida getUMUnidades() {
        List<TipoUnidadMedida> ums = gestorProductos.cargarObjetosPersistentes(TipoUnidadMedida.class);
        TipoUnidadMedida unidades = null;
        for (TipoUnidadMedida tipoUnidadMedida : ums) {
            if (tipoUnidadMedida.getNombre().equals("Unidades")) {
                unidades = tipoUnidadMedida;
                break;
            }
        }
        if (unidades == null) {
            unidades = new TipoUnidadMedida("Unidades");
            long oid = gestorTipos.actualizarTipo(unidades);
            unidades.setOid(oid);
        }
        System.out.println("ID de Objeto Unidades:" + unidades.getOid());
        return unidades;
    }

    /**
     * Obtiene la calidad que representa la calidad por defecto (La primera
     * encontrada).
     */
    private TipoCalidad getCalidadDefecto() {
        //Obtener la unidad de medida que representa unidades.
        List<TipoCalidad> calidades = gestorProductos.cargarObjetosPersistentes(TipoCalidad.class);
        TipoCalidad defecto = null;
        for (TipoCalidad tipoCalidad : calidades) {
            defecto = tipoCalidad;
            break;
        }
        if (defecto == null) {
            defecto = new TipoCalidad("Estandar");
            long oid = gestorTipos.actualizarTipo(defecto);
            defecto.setOid(oid);
        }
        System.out.println("ID de Objeto Calidad:" + defecto.getOid());
        return defecto;
    }

    private void importarProductos() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(
                "SELECT p.Codigo, p.Nombre, p.Modelo, m.Nombre, r.Nombre, " +
                "p.StockDisp, p.StockMin, p.StockMax, p.PrecioUltCompra, " +
                "p.PrecioFijo, p.PorcentajeGanancia " +
                "FROM Producto p, Marca m, Rubro r " +
                "WHERE p.IdMarca = m.Id and " +
                "p.IdRubro = r.Id");

        System.out.println("Consulta SQL Ejecutada en Origen.");

        List<Producto> productos = new LinkedList<Producto>();

        //Obtener referencias de los objetos relacionados.
        List<TipoMarca> marcas = gestorProductos.cargarObjetosPersistentes(TipoMarca.class);
        List<TipoRubro> rubros = gestorProductos.cargarObjetosPersistentes(TipoRubro.class);

        TipoUnidadMedida unidades = getUMUnidades();
        TipoCalidad calidad = getCalidadDefecto();

        int cantProductos = 0;
        while (rs.next()) {
            System.out.println("Producto encontrado.");
            Producto producto = new Producto();
            producto.setCalidades(new TreeSet<CalidadProducto>());
            CalidadProducto cp = new CalidadProducto();
            producto.getCalidades().add(cp);

            //Nombre (Requerido) (2)
            String desc = rs.getString(2);

            //Modelo (3)
            String modelo = rs.getString(3);

            //Descripcion del producto
            producto.setDescripcion(desc + (modelo != null ? " | " + modelo : ""));

            //Marca (Requerido) (4)
            String strMarca = rs.getString(4);
            TipoMarca tipoMarca = new TipoMarca(strMarca);
            for (TipoMarca marca : marcas) {
                if (marca.equals(tipoMarca)) {
                    producto.setMarca(marca);
                    break;
                }
            }

            //Rubro (Requerido) (5)
            String strRubro = rs.getString(5);
            TipoRubro tipoRubro = new TipoRubro(strRubro);
            for (TipoRubro rubro : rubros) {
                if (rubro.equals(tipoRubro)) {
                    producto.setRubro(rubro);
                    break;
                }
            }

            //Stock Disponible (6)
            cp.setStockActual((float) rs.getDouble(6));

            //Stock Minimo (7)
            cp.setStockMinimo((float) rs.getDouble(7));

            //Stock Maximo (8)
            cp.setStockMaximo((float) rs.getDouble(8));

            //Precio Ultima Compra (9)
            double ultCompra = rs.getDouble(9);
            if (ultCompra != 0) {
                cp.setPrecioUltimaCompra((float) ultCompra);
            }

            //Precio Fijo (10)
            double precioFijo = rs.getDouble(10);
            if (precioFijo != 0) {
                cp.setPrecioVenta((float) precioFijo);
                cp.setPrecioVentaFijo(true);
            } else {
                cp.setPrecioVenta(0f);
            }

            //Porcentaje Ganancia (11)
            double porcGanancia = rs.getDouble(11);
            if (porcGanancia != 0) {
                cp.setPorcentajeGanancia((float) porcGanancia);
                cp.setPrecioVentaFijo(false);
            } else {
                cp.setPorcentajeGanancia(0f);
            }

            //Unidad de Medida
            producto.setProductoUM(unidades);

            //Calidad
            cp.setCalidad(calidad);

            productos.add(producto);
            cantProductos++;
            System.out.println("Producto importado: " + producto.getDescripcion());
        }
        rs.close();

        //Informar al gestor.
        System.out.println("Guardando productos en BD...");
        for (Producto producto : productos) {
            System.out.print("Guardando " + producto.getDescripcion() + "...");
            gestorProductos.confirmarActualizacion(true, producto);
            System.out.println(" OK.");
        }
        System.out.println(cantProductos + " productos importados correctamente.");
    }
}
