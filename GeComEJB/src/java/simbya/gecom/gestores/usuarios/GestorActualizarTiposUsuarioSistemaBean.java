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
package simbya.gecom.gestores.usuarios;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateful;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import simbya.framework.excepciones.RegistroFallidoException;
import simbya.framework.persistencia.HibernateUtil;
import simbya.gecom.entidades.seguridad.CasoDeUso;
import simbya.gecom.entidades.seguridad.TipoUsuarioSistema;

/**
 * Gestor para actualizar los privilegios de los tipos de usuario del sistema.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorActualizarTiposUsuarioSistemaBean implements GestorActualizarTiposUsuarioSistemaRemote {

    private Session sesion;
    private boolean modificacion = false;
    private boolean baja = false;
    private TipoUsuarioSistema tipoUsuarioSistema;

    /**
     * Carga los tipos de usuario del sistema desde la base de datos.
     * @return todos los tipos de usuario del sistema.
     */
    public List<TipoUsuarioSistema> cargarTiposUsuario() {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        List tipos = sesion.createQuery("from TipoUsuarioSistema").list();
        sesion.getTransaction().commit();
        List<TipoUsuarioSistema> resultado = new LinkedList<TipoUsuarioSistema>(tipos);
        for (TipoUsuarioSistema tus : resultado) {
            tus.normalizarObjeto();
        }
        return resultado;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean isModificacion() {
        return modificacion;
    }

    public TipoUsuarioSistema getTipoUsuarioSistema() {
        tipoUsuarioSistema.normalizarObjeto();
        return tipoUsuarioSistema;
    }

    /**
     * Indica al gestor cual fue el TipoUsuarioSistema seleccionado en la
     * ventana por el usuario.
     */
    public void seleccionarTipoUsuario(TipoUsuarioSistema tus) {
        modificacion = true;
        baja = false;
        tipoUsuarioSistema = tus;
    }

    /**
     * Opción para registrar un nuevo tipo de usuario del sistema.
     */
    public void registrarNuevoUsuario() {
        modificacion = false;
        baja = false;
        tipoUsuarioSistema = new TipoUsuarioSistema();
    }

    /**
     * Carga los privilegios disponibles (CasoDeUso) desde la base de datos
     * quitando de la lista los privilegios asignados del usuario seleccionado.
     * @return Lista con los privilegios disponibles.
     */
    public List<CasoDeUso> cargarPrivilegiosDisponibles() {
        List<CasoDeUso> todos = CasoDeUso.getCasosDeUso();

        //Si no hay ningun tipo seleccionado devuelve todos los privilegios.
        if (tipoUsuarioSistema == null ||
                tipoUsuarioSistema.getPrivilegiosCU() == null) {
            return todos;
        }

        //Quita de la lista de disponibles los privilegios que ya están
        //asignados.

        List<CasoDeUso> disponibles = new LinkedList<CasoDeUso>();
        for (CasoDeUso casoDeUso : todos) {
            disponibles.add(casoDeUso);
        }
        for (CasoDeUso cu : tipoUsuarioSistema.getPrivilegiosCU()) {
            disponibles.remove(cu);
        }

        return disponibles;
    }

    /**
     * Guarda el nombre en el atributo nombre del objeto TipoUsuarioSistema.
     */
    public void tomarNombreTipoUsuario(String nombre) {
        tipoUsuarioSistema.setNombre(nombre);
    }

    /**
     * Guarda la descripción en el atributo descripcion del objeto 
     * TipoUsuarioSistema.
     */
    public void tomarDescripcionTipoUsuario(String desc) {
        tipoUsuarioSistema.setDescripcion(desc);
    }

    /**
     * Guarda los privilegios asignados en el atributo privilegios del objeto 
     * TipoUsuarioSistema.
     */
    public void tomarPrivilegiosAsignados(Set<CasoDeUso> privilegios) {
        tipoUsuarioSistema.setPrivilegiosCU(privilegios);
    }

    /**
     * Verifica los datos requeridos del tipo de usuario del sistema.
     * @return true si es correcto, false de lo contrario.
     */
    private boolean verificarDatos() {
        if ((tipoUsuarioSistema.getNombre() == null) ||
                (tipoUsuarioSistema.getNombre().length() == 0)) {
            return false;
        }

        return true;
    }

    /**
     * Actualiza el tipo de usuario del sistema en la BBDD.
     * @return true si todo fue correcto, false si sucede algún error.
     */
    public boolean confirmarActualizacion() throws RegistroFallidoException {
        if (verificarDatos() == true) {
            sesion = HibernateUtil.getSessionFactory().openSession();
            sesion.getTransaction().begin();
            try {
                if (modificacion) {
                    // MODIFICACION
                    TipoUsuarioSistema tus =
                            (TipoUsuarioSistema) sesion.load(
                            TipoUsuarioSistema.class,
                            tipoUsuarioSistema.getOid());
                    sesion.update(tipoUsuarioSistema);
                    sesion.getTransaction().commit();
                    return true;
                } else {
                    if (baja) {
                        // BAJA
                        TipoUsuarioSistema tus =
                                (TipoUsuarioSistema) sesion.load(
                                TipoUsuarioSistema.class,
                                tipoUsuarioSistema.getOid());
                        sesion.delete(tus);
                        sesion.getTransaction().commit();
                        return true;
                    } else {
                        // ALTA
                        sesion.persist(tipoUsuarioSistema);
                        sesion.getTransaction().commit();
                        return true;
                    }
                }
            } catch (HibernateException e) {
                sesion.getTransaction().rollback();
                RegistroFallidoException rfe;
                if (e instanceof org.hibernate.exception.ConstraintViolationException) {
                    if (baja) {
                        rfe = new RegistroFallidoException(
                                "No se pudo eliminar este Tipo de Usuario,\n" +
                                "debido a que está siendo utilizado por el sistema para otros fines.");
                    } else {
                        rfe = new RegistroFallidoException(
                                "No se pudo actualizar este Tipo de Usuario.\n" +
                                "Ya hay otro Tipo de Usuario con el mismo nombre.");
                    }
                } else {
                    rfe = new RegistroFallidoException(
                            "Error al actualizar los datos del Tipo de Usuario: " +
                            e.getMessage());
                }

                throw rfe;
            }
        } else {
            return false;
        }
    }

    /**
     * Borra el tipo de usuario del sistema seleccionado en el gestor.
     */
    public void borrarTipo() throws RegistroFallidoException {
        modificacion = false;
        baja = true;

        confirmarActualizacion();
    }
}
