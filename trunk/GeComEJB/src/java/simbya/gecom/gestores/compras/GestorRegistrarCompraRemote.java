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
import javax.ejb.Remote;
import simbya.gecom.entidades.Compra;

/**
 * Gestor para registrar compras de productos a proveedores.
 * @author Marcelo Busico.
 */
@Remote
public interface GestorRegistrarCompraRemote {

    /**
     * Carga los elementos disponibles desde la base de datos.
     * @param clasePersistente Clase persistente a traer de la BD.
     * @return Lista con los elementos encontrados.
     */
    List cargarObjetosPersistentes(Class clasePersistente);

    /**
     * Registra la compra en la base de datos y actualiza el stock y precio de
     * última compra.
     * @param venta Compra a persistir.
     * @throws java.lang.Exception Si sucede algún error durante el registro.
     */
    void registrarCompra(Compra compra) throws Exception;
}
