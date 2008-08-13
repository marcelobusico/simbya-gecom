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
package simbya.framework.tipos;

import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import simbya.framework.persistencia.HibernateUtil;

/**
 * Clase que permite obtener de la base de datos los tipos genéricos.
 * @author Marcelo Busico
 */
public abstract class PersistentClassLoader {

    /**
     * Carga los elementos disponibles desde la base de datos.
     * @param clasePersistente Clase persistente a traer de la BD.
     * @return Lista con los elementos encontrados.
     */
    public static List cargarObjetosPersistentes(Class clasePersistente) {
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        List tipos = sesion.createCriteria(clasePersistente).list();
        Hibernate.initialize(tipos);
        sesion.getTransaction().commit();
        return tipos;
    }
}
