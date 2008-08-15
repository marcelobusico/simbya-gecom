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
package simbya.gecom.gestores.proveedores;

import java.util.Date;
import java.util.List;
import javax.ejb.Stateful;
import org.hibernate.Session;
import simbya.framework.persistencia.HibernateUtil;
import simbya.gecom.entidades.MovimientoProveedor;
import simbya.gecom.entidades.Proveedor;

/**
 * Gestor para consultar la cuenta corriente de proveedores.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorConsultarCuentaCorrienteProveedorBean
        implements GestorConsultarCuentaCorrienteProveedorRemote {

    /**
     * Busca y devuelve todos los movimientos registrados entre las fechas.
     * @param proveedor Proveedor a buscar movimientos.
     * @param fechaDesde Fecha desde cuando buscar.
     * @param fechaHasta Fecha hasta cuando buscar.
     * @return Todas las ventas que concuerdan con los parámetros.
     */
    public List<MovimientoProveedor> getMovimientosDelPeriodo(Proveedor proveedor,
            Date fechaDesde, Date fechaHasta) {
        if (proveedor == null) {
            throw new IllegalArgumentException(
                    "El parámetro proveedor es nulo.");
        }
        if (fechaDesde == null) {
            throw new IllegalArgumentException(
                    "El parámetro fechaDesde es nulo.");
        }
        if (fechaHasta == null) {
            throw new IllegalArgumentException(
                    "El parámetro fechaHasta es nulo.");
        }
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
//TODO: Cambiar funcion.
        return null;
//        List<MovimientoProveedor> movimientos = sesion.createQuery(
//                "from MovimientoProveedor where fecha between ? and ?").
//                setTimestamp(0, fechaDesde).setTimestamp(1, fechaHasta).list();
//        sesion.getTransaction().commit();
//        for (MovimientoProveedor movimiento : movimientos) {
//            movimiento.normalizarObjeto();
//        }
//        return movimientos;
    }
}
