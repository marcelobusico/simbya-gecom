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
import java.util.Set;
import javax.ejb.Remote;
import simbya.gecom.decoradores.ProductoVendido;

/**
 * Gestor para generar informes de productos vendidos.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorGenerarInformeProductosVendidosRemote {

    /**
     * Busca y devuelve todos los productos vendidos entre las fechas.
     * @param fechaDesde Fecha desde cuando buscar.
     * @param fechaHasta Fecha hasta cuando buscar.
     * @return Todas las ventas de productos que concuerdan con los parámetros.
     */
    Set<ProductoVendido> getProductosDelPeriodo(Date fechaDesde, Date fechaHasta);
}
