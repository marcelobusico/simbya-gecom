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
package simbya.gecom.gestores.compras;

import java.util.List;
import javax.ejb.Stateful;
import org.hibernate.Session;
import simbya.framework.persistencia.HibernateUtil;
import simbya.framework.tipos.PersistentClassLoader;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Compra;
import simbya.gecom.entidades.DetalleCompra;

/**
 * Gestor para registrar compras de productos a proveedores.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorRegistrarCompraBean implements GestorRegistrarCompraRemote {

    /**
     * Carga los elementos disponibles desde la base de datos.
     * @param clasePersistente Clase persistente a traer de la BD.
     * @return Lista con los elementos encontrados.
     */
    public List cargarObjetosPersistentes(Class clasePersistente) {
        return PersistentClassLoader.cargarObjetosPersistentes(clasePersistente);
    }

    /**
     * Registra la compra en la base de datos y actualiza el stock y precio de
     * última compra.
     * @param venta Compra a persistir.
     * @throws java.lang.Exception Si sucede algún error durante el registro.
     */
    public void registrarCompra(Compra compra) throws Exception {
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        sesion.save(compra);
        for (DetalleCompra detalle : compra.getDetalles()) {
            CalidadProducto calidadDB = (CalidadProducto) sesion.load(
                    CalidadProducto.class, detalle.getCalidad().getOid());
            float cantidad = detalle.getCantidad();
            float importe = detalle.getImporteUnitario();
            float stockActual = calidadDB.getStockActual();
            calidadDB.setStockActual(stockActual + cantidad);
            calidadDB.setPrecioUltimaCompra(importe);
            sesion.update(calidadDB);
            detalle.setCalidad(calidadDB);
        }
        sesion.getTransaction().commit();
    }
}
