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
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import org.hibernate.Hibernate;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que representa una interfaz gráfica del sistema.
 * @author Marcelo Busico.
 */
public class CasoDeUso implements Normalizable, Comparable<CasoDeUso> {

    protected static transient List<CasoDeUso> casosDeUso;
    private long oid; //Requerido pero automático
    private String nombre; //Requerido
    private String menu; //Requerido
    private String orden; //Requerido
//------------------------------------------
// INICIO DE DECLARACION DE ITEMS DE MENU.        
//------------------------------------------

    //Declaración de variables globales.
    //No olvidar de actualizar cuando se agreguen Casos de Uso al Menú Principal.

//-------------------------------------------------------------
//        ESTRUCTURA DEL MENU:
//        Archivo:
//            Configuración:
//                Actualizar Parámetros Generales
//                Actualizar Provincias y Localidades
//            Usuarios:
//                Actualizar Tipo de Usuario del Sistema
//                Actualizar Usuario del Sistema
//-------------------------------------------------------------
    //Menú Archivo:
    public static final String Archivo_Configuracion_ActualizarParametrosGenerales =
            "Actualizar Parametros Generales";
    public static final String Archivo_Configuracion_ActualizarProvinciasYLocalidades =
            "Actualizar Provincias y Localidades";
    public static final String Archivo_Usuarios_ActualizarTipoUsuarioSistema =
            "Actualizar Tipo de Usuario del Sistema";
    public static final String Archivo_Usuarios_ActualizarUsuarioSistema =
            "Actualizar Usuario del Sistema";

    //Menú Clientes:
    public static final String Clientes_ActualizarFormaCobro =
            "Actualizar Forma de Cobro";
    public static final String Clientes_ActualizarCliente =
            "Actualizar Cliente";
    public static final String Clientes_RegistrarCobroCliente =
            "Registrar Cobro a Cliente";
    public static final String Clientes_ConsultarCuentaCorrienteCliente =
            "Consultar Cuenta Corriente de Cliente";
    
    //Menú Proveedores:
    public static final String Proveedores_ActualizarFormaPago =
            "Actualizar Forma de Pago";
    public static final String Proveedores_ActualizarProveedor =
            "Actualizar Proveedor";
    public static final String Proveedores_RegistrarPagoProveedor =
            "Registrar Pago a Proveedor";
    public static final String Proveedores_ConsultarCuentaCorrienteProveedor =
            "Consultar Cuenta Corriente de Proveedor";

    //Menú Productos:
    public static final String Productos_TablasYTipos_ActualizarCalidad =
            "Actualizar Calidad";
    public static final String Productos_TablasYTipos_ActualizarCilindradaMoto =
            "Actualizar Cilindrada Moto";
    public static final String Productos_TablasYTipos_ActualizarMarca =
            "Actualizar Marca";
    public static final String Productos_TablasYTipos_ActualizarModelo =
            "Actualizar Modelo";
    public static final String Productos_TablasYTipos_ActualizarRubro =
            "Actualizar Rubro";
    public static final String Productos_TablasYTipos_ActualizarUnidadMedida =
            "Actualizar Unidad de Medida";
    public static final String Productos_ActualizarProducto =
            "Actualizar Producto";
    public static final String Productos_ActualizarPreciosProductos =
            "Actualizar Precios de Productos";
    public static final String Productos_ActualizarStockProductos =
            "Actualizar Stock de Productos";
    public static final String Productos_GenerarInformeProductosVendidos =
            "Generar Informe de Productos Vendidos";

    //Menú Compras:
    public static final String Compras_RegistrarCompra =
            "Registrar Compra";

    //Menú Ventas:
    public static final String Ventas_RegistrarVenta =
            "Registrar Ventas";
    public static final String Ventas_GenerarInformeVentas =
            "Generar Informe de Ventas";
    
    //Privilegios Particulares:
    public static final String Particular_Venta_TodosLosPrivilegios =
            "Venta: Todos los Privilegios";
//------------------------------------------
// FIN DE LA DECLARACION DE ITEMS DE MENU.        
//------------------------------------------
    /**
     * Crea una nueva instancia de CasoDeUso.
     */
    protected CasoDeUso() {
    }

    /**
     * Crea una nueva instancia de CasoDeUso.
     */
    protected CasoDeUso(String nombre, String menu, String orden) {
        this.nombre = nombre;
        this.menu = menu;
        this.orden = orden;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public String getOrden() {
        return orden;
    }

    public void setOrden(String orden) {
        this.orden = orden;
    }

    /**
     * Devuelve todos los Casos de Uso del Sistema cargados desde la BD.
     * @return Lista con objetos CasoDeUso.
     */
    public static List<CasoDeUso> getCasosDeUso() {
        if (casosDeUso == null) {
            CasoDeUsoLoader.inicializarClase();
        }
        return casosDeUso;
    }

    /**
     * Setea los CU traidos desde el gestor.
     * @param casosDeUso Casos de uso a cargar.
     */
    public static void setCasosDeUso(List<CasoDeUso> casosDeUso) {
        CasoDeUso.casosDeUso = casosDeUso;
    }
    
    /**
     * Busca un CasoDeUso en funcion del nombre pasado.
     * @return CasoDeUso correspondiente al nombre, null
     * si no se encuentra.
     * @param nombreCU Nombre o clave del CU a buscar.
     * El parámetro String es un atributo estático de la clase que puede
     * accederse de forma pública.
     */
    public static CasoDeUso getCasoDeUso(String nombreCU) {
        if (casosDeUso == null) {
            CasoDeUsoLoader.inicializarClase();
        }

        CasoDeUso cu;

        Iterator<CasoDeUso> li = casosDeUso.iterator();
        while (li.hasNext()) {
            cu = li.next();
            if (cu.getNombre().equalsIgnoreCase(nombreCU)) {
                return cu;
            }
        }
        return null;
    }

    /**
     * Activa o desactiva un item del menú principal de acuerdo a que el
     * usuario tenga o no privilegios de acceso al mismo.
     * @param usuario Usuario del sistema a verificar los permisos.
     * @param nombreCU Nombre del Caso de Uso que corresponde al item de
     * menú.
     * @param componente Nombre del item de menú que se está validando.
     */
    public static void validarMenu(UsuarioSistema usuario,
            String nombreCU, JComponent componente) {
        Set<CasoDeUso> cus = usuario.getTipo().getPrivilegiosCU();
        CasoDeUso cu = getCasoDeUso(nombreCU);
        if (cus.contains(cu)) {
            componente.setEnabled(true);
        } else {
            componente.setEnabled(false);
        }
    }

    /**
     * Muestra el nombre del Caso de Uso con su ruta de acceso en el 
     * Menú Principal.
     */
    @Override
    public String toString() {
        return menu + " -> " + nombre;
    }

    /**
     * Compara si el objeto es igual a otro.
     * @param obj Objeto a comparar.
     * @return true si tienen el mismo oid, false de lo contrario.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!this.toString().equals(obj.toString())) {
            return false;
        }
        return true;
    }

    /**
     * Genera el valor de dispersión del objeto en base al nombre
     * del Caso de Uso.
     * @return Hashcode del objeto.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.nombre != null ? this.nombre.hashCode() : 0);
        return hash;
    }

    /**
     * Compara dos objetos casos de uso y devuelve el orden en base al atributo
     * orden de los objetos.
     * @param casoDeUso CasoDeUso a comparar con el actual.
     * @return Número que indica quien es mayor a quién (ver compareTo de String).
     */
    @Override
    public int compareTo(CasoDeUso casoDeUso) {
        if (casoDeUso == null) {
            return -1;
        }
        return orden.compareTo(casoDeUso.getOrden());
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
    }
}
