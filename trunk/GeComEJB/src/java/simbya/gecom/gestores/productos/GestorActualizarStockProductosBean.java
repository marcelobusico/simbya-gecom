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

import java.sql.SQLException;
import java.util.List;
import javax.ejb.Stateful;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import simbya.framework.persistencia.HibernateUtil;
import simbya.framework.tipos.PersistentClassLoader;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Producto;
import simbya.gecom.entidades.TipoMarca;
import simbya.gecom.entidades.TipoRubro;

/**
 * Gestor para actualizar el stock de productos cargados.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorActualizarStockProductosBean implements GestorActualizarStockProductosRemote {

    private static final Logger log = Logger.getLogger(
            GestorActualizarStockProductosBean.class);
    private Session sesion;

    /**
     * Carga los elementos disponibles desde la base de datos.
     * @param clasePersistente Clase persistente a traer de la BD.
     * @return Lista con los elementos encontrados.
     */
    public List cargarObjetosPersistentes(Class clasePersistente) {
        return PersistentClassLoader.cargarObjetosPersistentes(clasePersistente);
    }

    public List<Producto> getProductos(TipoRubro rubro, TipoMarca marca,
            String descripcion) {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        Criteria busqueda = sesion.createCriteria(Producto.class).
                add(Restrictions.eq("baja", false));
        if (rubro != null) {
            Criterion rest = Restrictions.eq("rubro", rubro);
            busqueda = busqueda.add(rest);
        }
        if (marca != null) {
            Criterion rest = Restrictions.eq("marca", marca);
            busqueda = busqueda.add(rest);
        }
        if (descripcion != null) {
            Criterion rest = Restrictions.ilike("descripcion", descripcion, MatchMode.ANYWHERE);
            busqueda = busqueda.add(rest);
        }
        busqueda = busqueda.addOrder(Order.asc("descripcion"));
        List<Producto> productos = busqueda.list();
        sesion.getTransaction().commit();
        for (Producto producto : productos) {
            producto.normalizarObjeto();
        }
        return productos;
    }

    public byte[] getImagenProducto(Producto p) throws Exception {
        if (p == null) {
            throw new IllegalArgumentException("El producto es nulo.");
        }
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        Object obj = sesion.load(Producto.class, p.getOid());
        Producto prod = (Producto) obj;
        try {
            return prod.getImageData();
        } catch (SQLException ex) {
            log.error("Error al obtener imagen de producto " + prod.toString(), ex);
            throw new SQLException("Error al obtener imagen de producto " + prod.toString(), ex);
        } finally {
            sesion.getTransaction().commit();
        }
    }

    /**
     * Actualiza el stock de las calidades indicadas en la BD.
     * @param calidades Calidades de los productos que serán actualizadas su
     * stock actual por el valor indicado en cada objeto.
     * @throws Exception Si sucede algún error en la actualización.
     */
    public void actualizarStock(List<CalidadProducto> calidades) throws Exception {
        if (calidades == null || calidades.isEmpty()) {
            throw new IllegalArgumentException(
                    "El parámetro calidades es nulo o está vacío.");
        }
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        for (CalidadProducto calidad : calidades) {
            CalidadProducto calidadDB = (CalidadProducto) sesion.load(
                    CalidadProducto.class, calidad.getOid());
            float stockActual = calidad.getStockActual();
            calidadDB.setStockActual(stockActual);
            sesion.update(calidadDB);
            calidad = calidadDB;
        }
        sesion.getTransaction().commit();
    }
}
