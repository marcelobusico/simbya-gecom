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
package simbya.gecom.gestores.buscador;

import java.util.Date;
import java.util.List;
import javax.ejb.Stateful;
import org.hibernate.Session;
import simbya.framework.persistencia.HibernateUtil;
import simbya.gecom.interfaces.Normalizable;

/**
 * Gestor que implementa los métodos para el buscador genérico.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorBuscadorBean implements GestorBuscadorRemote {

    public List buscar(String consultaHQL) {
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        List<Normalizable> res = sesion.createQuery(consultaHQL).list();
        sesion.getTransaction().commit();
        for (Normalizable obj : res) {
            obj.normalizarObjeto();
        }
        return res;
    }

    public Object buscarUnicoResultado(String consultaHQL) {
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        Object res = sesion.createQuery(consultaHQL).uniqueResult();
        Normalizable obj = null;
        if (res != null) {
            obj = (Normalizable) res;
        }
        sesion.getTransaction().commit();
        if (res != null) {
            obj.normalizarObjeto();
        }
        return obj;
    }

    public List buscar(String consultaHQL, Date fecha) {
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        List<Normalizable> res = sesion.createQuery(consultaHQL).setDate(0, fecha).list();
        sesion.getTransaction().commit();
        for (Normalizable obj : res) {
            obj.normalizarObjeto();
        }
        return res;
    }
}
