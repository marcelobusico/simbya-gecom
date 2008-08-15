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
package simbya.gecom.gestores.tipos;

import java.util.List;
import javax.ejb.Stateful;
import org.hibernate.Session;
import simbya.framework.persistencia.HibernateUtil;
import simbya.gecom.interfaces.InterfazTipoGenerico;

/**
 * Gestor para actualizar tipos genéricos.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorActualizarTiposBean implements GestorActualizarTiposRemote {

    private Session sesion;
    private InterfazTipoGenerico tipoGenerico;
    private String nombreTipo = null;
    private String descTipo = null;

    public List opcionActualizarTipos(InterfazTipoGenerico tg) {
        this.tipoGenerico = tg;
        return cargarTipos();
    }

    public InterfazTipoGenerico getTipoGenerico() {
        return tipoGenerico;
    }

    public void setTipoGenerico(InterfazTipoGenerico tipoGenerico) {
        this.tipoGenerico = tipoGenerico;
    }

    public List cargarTipos() {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        List tipos = sesion.createQuery("from " +
                tipoGenerico.getClass().getSimpleName()).list();
        sesion.getTransaction().commit();
        return tipos;
    }

    public boolean verificarDatos(String nombre, String descripcion) {
        this.nombreTipo = nombre;
        this.descTipo = descripcion;

        if (nombre.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public void registrarTipo() {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        tipoGenerico.setOid(0);
        tipoGenerico.setNombre(this.nombreTipo);
        tipoGenerico.setDescripcion(this.descTipo);
        sesion.persist(tipoGenerico);
        sesion.getTransaction().commit();
    }

    /**
     * Registra o actualiza el tipo genérico especificado.
     * @param tg Objeto a registrar.
     * @return oid del objeto actualizado.
     */
    public long actualizarTipo(InterfazTipoGenerico tg) {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        sesion.saveOrUpdate(tg);
        sesion.getTransaction().commit();
        return tg.getOid();
    }

    /**
     * Registra o actualiza los objetos tipo genérico especificado.
     * @param lista Objetos a registrar.
     */
    public void actualizarTipos(List<InterfazTipoGenerico> lista) {
        if (lista == null) {
            throw new IllegalArgumentException("Lista null.");
        }
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        for (InterfazTipoGenerico tg : lista) {
            sesion.saveOrUpdate(tg);
        }
        sesion.getTransaction().commit();
    }

    public void modificarTipo(InterfazTipoGenerico tg) {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        sesion.update(tg);
        sesion.getTransaction().commit();
    }

    public void eliminarTipo(InterfazTipoGenerico tg) throws Exception {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        sesion.delete(tg);
        sesion.getTransaction().commit();
    }
}
