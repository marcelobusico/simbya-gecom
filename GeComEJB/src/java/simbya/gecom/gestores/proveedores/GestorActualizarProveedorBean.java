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
import simbya.gecom.entidades.Proveedor;
import simbya.gecom.entidades.Provincia;

/**
 * Gestor para actualizar proveedores.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorActualizarProveedorBean implements GestorActualizarProveedorRemote {

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
     * Actualizar el proveedor en la base de datos.
     * @param proveedor Proveedor a actualizar.
     * @return Oid del proveedor.
     */
    public long confirmarActualizacion(Proveedor proveedor) {
        if (proveedor == null) {
            throw new IllegalArgumentException("Proveedor null");
        }
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        sesion.saveOrUpdate(proveedor);
        sesion.getTransaction().commit();
        return proveedor.getOid();
    }

    public void confirmarBaja(Proveedor proveedor) throws Exception {
        if (proveedor == null) {
            throw new IllegalArgumentException("Proveedor null");
        }
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        //Baja del producto.
        Proveedor prov = (Proveedor) sesion.load(Proveedor.class, proveedor.getOid());
        prov.setBaja(true);
        prov.setFechaBaja(new Date());
        sesion.saveOrUpdate(prov);
        sesion.getTransaction().commit();
    }
}
