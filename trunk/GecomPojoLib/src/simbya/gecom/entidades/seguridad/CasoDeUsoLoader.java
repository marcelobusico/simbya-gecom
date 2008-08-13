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
package simbya.gecom.entidades.seguridad;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import simbya.framework.persistencia.HibernateUtil;

/**
 * Cargador de las instancias de los Casos de Uso.
 * @author Marcelo.
 */
public class CasoDeUsoLoader extends CasoDeUso {

    private static transient final Logger log = Logger.getLogger(CasoDeUso.class);

    /**
     * Verifica que todos los CU estén en la BD cargados.
     * Si alguno no se encuentra lo carga con valores por defecto.
     */
    private static void verificarDB() {
        int cambios = 0;

//------------------------------------------
// INICIO DE DECLARACION DE ITEMS DE MENU.        
//------------------------------------------

        //Menú Archivo -> Configuración:
        String mnuArchConfig = "Archivo, Configuración";
        cambios += chequearCU(CasoDeUso.Archivo_Configuracion_ActualizarParametrosGenerales,
                mnuArchConfig, "AA001");
        cambios += chequearCU(CasoDeUso.Archivo_Configuracion_ActualizarProvinciasYLocalidades,
                mnuArchConfig, "AA002");
        
        //Menú Archivo -> Usuarios:
        String mnuArchUsu = "Archivo, Usuarios";
        cambios += chequearCU(CasoDeUso.Archivo_Usuarios_ActualizarTipoUsuarioSistema,
                mnuArchUsu, "AB003");
        cambios += chequearCU(CasoDeUso.Archivo_Usuarios_ActualizarUsuarioSistema,
                mnuArchUsu, "AB004");

        //Menú Clientes:
        String mnuClientes = "Clientes";
        cambios += chequearCU(CasoDeUso.Clientes_ActualizarFormaCobro,
                mnuClientes, "B0001");
        cambios += chequearCU(CasoDeUso.Clientes_ActualizarCliente,
                mnuClientes, "B0002");
        cambios += chequearCU(CasoDeUso.Clientes_RegistrarCobroCliente,
                mnuClientes, "B0003");
        cambios += chequearCU(CasoDeUso.Clientes_ConsultarCuentaCorrienteCliente,
                mnuClientes, "B0004");
        
        //Menú Proveedores:
        String mnuProveedores = "Proveedores";
        cambios += chequearCU(CasoDeUso.Proveedores_ActualizarFormaPago,
                mnuProveedores, "C0001");
        cambios += chequearCU(CasoDeUso.Proveedores_ActualizarProveedor,
                mnuProveedores, "C0002");
        cambios += chequearCU(CasoDeUso.Proveedores_RegistrarPagoProveedor,
                mnuProveedores, "C0003");
        cambios += chequearCU(CasoDeUso.Proveedores_ConsultarCuentaCorrienteProveedor,
                mnuProveedores, "C0004");
        
        //Menú Productos -> Tablas y Tipos:
        String mnuProdTablas = "Productos, Tablas y Tipos";
        cambios += chequearCU(CasoDeUso.Productos_TablasYTipos_ActualizarCalidad,
                mnuProdTablas, "DA001");
        cambios += chequearCU(CasoDeUso.Productos_TablasYTipos_ActualizarCilindradaMoto,
                mnuProdTablas, "DA002");
        cambios += chequearCU(CasoDeUso.Productos_TablasYTipos_ActualizarMarca,
                mnuProdTablas, "DA003");
        cambios += chequearCU(CasoDeUso.Productos_TablasYTipos_ActualizarModelo,
                mnuProdTablas, "DA004");
        cambios += chequearCU(CasoDeUso.Productos_TablasYTipos_ActualizarRubro,
                mnuProdTablas, "DA005");
        cambios += chequearCU(CasoDeUso.Productos_TablasYTipos_ActualizarUnidadMedida,
                mnuProdTablas, "DA001");

        //Menú Productos:
        String mnuProductos = "Productos";
        cambios += chequearCU(CasoDeUso.Productos_ActualizarProducto,
                mnuProductos, "DB001");
        cambios += chequearCU(CasoDeUso.Productos_ActualizarPreciosProductos,
                mnuProductos, "DB002");
        cambios += chequearCU(CasoDeUso.Productos_ActualizarStockProductos,
                mnuProductos, "DB003");
        cambios += chequearCU(CasoDeUso.Productos_GenerarInformeProductosVendidos,
                mnuProductos, "DB004");
        
        //Menú Compras:
        String mnuCompras = "Compras";
        cambios += chequearCU(CasoDeUso.Compras_RegistrarCompra,
                mnuCompras, "E0001");
        
        //Menú Ventas:
        String mnuVentas = "Ventas";
        cambios += chequearCU(CasoDeUso.Ventas_RegistrarVenta,
                mnuVentas, "F0001");
        cambios += chequearCU(CasoDeUso.Ventas_GenerarInformeVentas,
                mnuVentas, "F0002");
        
        //Privilegios Particulares:
        String particulares = "Privilegios Particulares";
        cambios += chequearCU(CasoDeUso.Particular_Venta_TodosLosPrivilegios,
                particulares, "Z0001");

//------------------------------------------
// FIN DE LA DECLARACION DE ITEMS DE MENU.        
//------------------------------------------

        //Persiste los nuevos parámetros de sistema en caso de que haya cambios.
        if (cambios > 0) {
            persistir();
            log.warn("Se han detectado Casos de Uso faltantes y se" +
                    " han creado con valores por defecto.");
        }
    }

    /**
     * Inicializa la clase con la lista de Casos de Uso desde la BD.
     */
    public static void inicializarClase() {
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        List resultados = sesion.createQuery(
                "from CasoDeUso order by orden").list();
        casosDeUso = new LinkedList<CasoDeUso>(resultados);
        sesion.getTransaction().commit();
        verificarDB();
        log.debug("Cargada Lista de Objetos CasoDeUso desde la BD.");
    }

    /**
     * Persiste todos los Casos de Uso en la base de datos.
     */
    private static void persistir() {
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        Iterator<CasoDeUso> it = casosDeUso.iterator();
        while (it.hasNext()) {
            CasoDeUso elem = it.next();
            sesion.saveOrUpdate(elem);
        }
        sesion.getTransaction().commit();
    }

    /**
     * Chequea un CU individual en la BD y si no lo encuentra lo crea.
     * @param nombre Nombre del CU a chequear.
     * @param menu Ruta del Menú Principal donde se encuentrá el caso de uso.
     * @return 1 si falta el caso de uso, o 0 si el caso de uso existe en la
     * lista. Retorna 1 si los datos del cu fueron actualizados con respecto
     * a los de la base de datos.
     * @param orden Orden del item en el menú principal.
     */
    private static int chequearCU(String nombre, String menu, String orden) {
        CasoDeUso cu = getCasoDeUso(nombre);
        if (cu == null) {
            casosDeUso.add(new CasoDeUso(
                    nombre, menu, orden));
            return 1;
        } else {
            if (!cu.getMenu().equals(menu)) {
                return 1;
            }
            if (!cu.getOrden().equals(orden)) {
                return 1;
            }
        }
        return 0;
    }

}
