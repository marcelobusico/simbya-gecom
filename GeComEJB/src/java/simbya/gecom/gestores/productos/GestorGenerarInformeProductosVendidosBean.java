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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.ejb.Stateful;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import simbya.framework.persistencia.HibernateUtil;
import simbya.gecom.decoradores.ProductoVendido;
import simbya.gecom.entidades.DetalleVenta;
import simbya.gecom.entidades.Venta;

/**
 * Gestor para generar informes de productos vendidos.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorGenerarInformeProductosVendidosBean
        implements GestorGenerarInformeProductosVendidosRemote {

    private static final Logger log = Logger.getLogger(
            GestorGenerarInformeProductosVendidosBean.class);

    /**
     * Busca y devuelve todos los productos vendidos entre las fechas.
     * @param fechaDesde Fecha desde cuando buscar.
     * @param fechaHasta Fecha hasta cuando buscar.
     * @return Todas las ventas de productos que concuerdan con los parámetros.
     */
    public Set<ProductoVendido> getProductosDelPeriodo(Date fechaDesde, Date fechaHasta) {
        if (fechaDesde == null) {
            throw new IllegalArgumentException(
                    "El parámetro fechaDesde es nulo.");
        }
        if (fechaHasta == null) {
            throw new IllegalArgumentException(
                    "El parámetro fechaHasta es nulo.");
        }
        List<ProductoVendido> productosVendidos = new LinkedList<ProductoVendido>();
        float totalPromedioCostos = 0;
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        //Traer todas las ventas del periodo.
        List<Venta> ventas = sesion.createQuery("from Venta where fecha between ? and ?").
                setTimestamp(0, fechaDesde).setTimestamp(1, fechaHasta).list();
        sesion.getTransaction().commit();

        //Analiza las ventas.
        for (Venta venta : ventas) {
            venta.normalizarObjeto();
            for (DetalleVenta detalle : venta.getDetalles()) {
                ProductoVendido pv = new ProductoVendido();
                pv.setProducto(detalle.getProducto());
                int pos = productosVendidos.indexOf(pv);
                if (pos != -1) {
                    //El producto ya fue vendido.
                    pv = productosVendidos.get(pos);
                } else {
                    //El producto es vendido por primera vez.
                    productosVendidos.add(pv);
                }
                //Acumular resultados
                pv.sumarACantidadVendida(detalle.getCantidad());
                pv.agregarYPromediarVenta(detalle.getImporteUnitario());
                if (pos == -1) {
                    //Acumular solo si el producto es vendido por primera vez.
                    totalPromedioCostos += pv.getImportePromedioVenta();
                }
            }
        }

        //Analizar los productos vendidos y categorizar.
        TreeSet<ProductoVendido> prodOrdenados =
                new TreeSet<ProductoVendido>(productosVendidos);
        float topeProductoClaseA = totalPromedioCostos * 0.9f;
        float sumaProductosClaseA = 0;
        float topeProductoClaseB = totalPromedioCostos * 0.08f;
        float sumaProductosClaseB = 0;

        boolean claseALlena = false;
        boolean claseBLlena = false;
        for (ProductoVendido pv : prodOrdenados) {
            if (!claseALlena) {
                //Clase A
                sumaProductosClaseA += pv.getImportePromedioVenta();
                pv.setClaseProducto(ProductoVendido.CLASE_A);
                if (sumaProductosClaseA > topeProductoClaseA) {
                    claseALlena = true;
                }
            } else {
                if (!claseBLlena) {
                    //Clase B
                    sumaProductosClaseB += pv.getImportePromedioVenta();
                    pv.setClaseProducto(ProductoVendido.CLASE_B);
                    if (sumaProductosClaseB > topeProductoClaseB) {
                        claseBLlena = true;
                    }
                } else {
                    //Clase C
                    pv.setClaseProducto(ProductoVendido.CLASE_C);
                }
            }
        }

        return prodOrdenados;
    }
}
