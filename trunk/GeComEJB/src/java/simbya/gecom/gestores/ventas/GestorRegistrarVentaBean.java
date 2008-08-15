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
package simbya.gecom.gestores.ventas;

import java.util.List;
import javax.ejb.Stateful;
import org.hibernate.Session;
import simbya.framework.persistencia.HibernateUtil;
import simbya.framework.tipos.PersistentClassLoader;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.DetalleVenta;
import simbya.gecom.entidades.Venta;

/**
 * Gestor para registrar ventas de productos a clientes.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorRegistrarVentaBean implements GestorRegistrarVentaRemote {

    /**
     * Carga los elementos disponibles desde la base de datos.
     * @param clasePersistente Clase persistente a traer de la BD.
     * @return Lista con los elementos encontrados.
     */
    public List cargarObjetosPersistentes(Class clasePersistente) {
        return PersistentClassLoader.cargarObjetosPersistentes(clasePersistente);
    }

    /**
     * Registra la venta en la base de datos y actualiza el stock.
     * @param venta Venta a persistir.
     * @throws java.lang.Exception Si sucede algún error durante el registro.
     */
    public void registrarVenta(Venta venta) throws Exception {
        if (venta == null) {
            throw new IllegalArgumentException(
                    "El parámetro venta es nulo.");
        }
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        sesion.save(venta);
        for (DetalleVenta detalle : venta.getDetalles()) {
            CalidadProducto calidadDB = (CalidadProducto) sesion.load(
                    CalidadProducto.class, detalle.getCalidad().getOid());
            float cantidad = detalle.getCantidad();
            float stockActual = calidadDB.getStockActual();
            calidadDB.setStockActual(stockActual - cantidad);
            sesion.update(calidadDB);
            detalle.setCalidad(calidadDB);
        }
        sesion.getTransaction().commit();
    }
}
