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
import javax.ejb.Remote;
import simbya.gecom.entidades.MovimientoProveedor;
import simbya.gecom.entidades.Proveedor;

/**
 * Gestor para consultar la cuenta corriente de proveedores.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorConsultarCuentaCorrienteProveedorRemote {

    /**
     * Busca y devuelve todos los movimientos registrados entre las fechas.
     * @param proveedor Proveedor a buscar movimientos.
     * @param fechaDesde Fecha desde cuando buscar.
     * @param fechaHasta Fecha hasta cuando buscar.
     * @return Todas las ventas que concuerdan con los parámetros.
     */
    List<MovimientoProveedor> getMovimientosDelPeriodo(Proveedor proveedor,
            Date fechaDesde, Date fechaHasta);
}
