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
package simbya.gecom.gestores.clientes;

import java.util.Date;
import java.util.List;
import javax.ejb.Stateful;
import org.hibernate.Session;
import simbya.framework.persistencia.HibernateUtil;
import simbya.gecom.entidades.Cliente;
import simbya.gecom.entidades.Provincia;

/**
 * Gestor para actualizar clientes.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorActualizarClienteBean implements GestorActualizarClienteRemote {

    private Session sesion;

    public List<Provincia> cargarProvincias() {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        List<Provincia> provincias = sesion.createCriteria(Provincia.class).list();
        sesion.getTransaction().commit();
        for (Provincia provincia : provincias) {
            provincia.normalizarObjeto();
        }
        return provincias;
    }

    /**
     * Actualizar el cliente en la base de datos.
     * @param cliente Cliente a actualizar.
     * @return Oid del cliente.
     */
    public long confirmarActualizacion(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente null");
        }
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        sesion.saveOrUpdate(cliente);
        sesion.getTransaction().commit();
        return cliente.getOid();
    }

    public void confirmarBaja(Cliente cliente) throws Exception {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente null");
        }
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        //Baja del producto.
        Cliente cli = (Cliente) sesion.load(Cliente.class, cliente.getOid());
        cli.setBaja(true);
        cli.setFechaBaja(new Date());
        sesion.saveOrUpdate(cli);
        sesion.getTransaction().commit();
    }
}
