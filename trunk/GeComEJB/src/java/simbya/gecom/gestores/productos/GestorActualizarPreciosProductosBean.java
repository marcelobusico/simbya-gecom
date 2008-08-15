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
package simbya.gecom.gestores.productos;

import java.util.List;
import javax.ejb.Stateful;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import simbya.framework.persistencia.HibernateUtil;
import simbya.framework.tipos.PersistentClassLoader;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Proveedor;
import simbya.gecom.entidades.TipoCalidad;
import simbya.gecom.entidades.TipoRubro;
import simbya.gecom.interfaces.Normalizable;

/**
 * Gestor para actualizar precios de productos de un proveedor por rubro.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorActualizarPreciosProductosBean implements GestorActualizarPreciosProductosRemote {

    private Session sesion;

    /**
     * Carga los elementos disponibles desde la base de datos.
     * @param clasePersistente Clase persistente a traer de la BD.
     * @return Lista con los elementos encontrados.
     */
    public List cargarObjetosPersistentes(Class clasePersistente) {
        if (clasePersistente == null) {
            throw new IllegalArgumentException("Objeto clase nulo.");
        }
        List<Normalizable> lista = PersistentClassLoader.cargarObjetosPersistentes(clasePersistente);
        for (Normalizable normalizable : lista) {
            normalizable.normalizarObjeto();
        }
        return lista;
    }

    /**
     * Busca todos los productos en la base de datos que son del proveedor, 
     * calidad y rubro solicitado.
     * @param rubro Rubro del producto a buscar.
     * @param calidad Calidad del producto a buscar.
     * @param proveedor Proveedor del producto a buscar.
     * @return Lista con los objetos CalidadProducto encontrados.
     */
    public List<CalidadProducto> getCalidadesProductos(TipoRubro rubro, 
            TipoCalidad calidad, Proveedor proveedor) {
        
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        
        //Busca calidades de producto.
        Criteria busqueda = sesion.createCriteria(CalidadProducto.class);
        if (rubro != null) {
            Criterion rest = Restrictions.eq("pr.rubro", rubro);
            busqueda = busqueda.add(rest);
        }
        if (calidad != null) {
            Criterion rest = Restrictions.eq("calidad", calidad);
            busqueda = busqueda.add(rest);
        }
        if (proveedor != null) {
            Criterion rest = Restrictions.eq("proveedor", proveedor);
            busqueda = busqueda.add(rest);
        }
        busqueda = busqueda.createAlias("producto", "pr");
        busqueda = busqueda.createAlias("calidad", "cal");
        busqueda = busqueda.addOrder(Order.asc("pr.descripcion"));
        busqueda = busqueda.addOrder(Order.asc("cal.nombre"));
        busqueda = busqueda.setFetchSize(3);
        List<CalidadProducto> calidadesProducto = busqueda.list();
        sesion.getTransaction().commit();
        for (CalidadProducto cp : calidadesProducto) {
            cp.normalizarObjeto();
        }
        return calidadesProducto;
    }

    /**
     * Registra en la base de datos el incremento/disminución de los precios
     * de cada uno de los productos.
     * @param calidades Calidades de los productos con el precio actualizado.
     * @throws Exception Si sucede algún error en la actualización.
     */
    public void confirmarActualizacion(List<CalidadProducto> calidades) throws Exception {
        if (calidades == null || calidades.isEmpty()) {
            throw new IllegalArgumentException(
                    "El parámetro calidades es nulo o está vacío.");
        }
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        for (CalidadProducto calidad : calidades) {
            CalidadProducto calidadDB = (CalidadProducto) sesion.load(
                    CalidadProducto.class, calidad.getOid());

            //Setea los nuevos precios.
            calidadDB.setPrecioVenta(calidad.getPrecioVenta());
            calidadDB.setPrecioUltimaCompra(calidad.getPrecioUltimaCompra());

            sesion.update(calidadDB);
            calidad = calidadDB;
        }
        sesion.getTransaction().commit();
    }
}
