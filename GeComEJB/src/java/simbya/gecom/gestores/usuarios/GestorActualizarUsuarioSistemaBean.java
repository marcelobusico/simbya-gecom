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

import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.ejb.Stateful;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import simbya.framework.excepciones.RegistroFallidoException;
import simbya.framework.password.PasswordUtil;
import simbya.framework.password.WrongPasswordException;
import simbya.framework.persistencia.HibernateUtil;
import simbya.gecom.entidades.seguridad.TipoUsuarioSistema;
import simbya.gecom.entidades.seguridad.UsuarioSistema;

/**
 * Gestor para actualizar usuarios del sistema.
 * @author Marcelo Busico.
 */
@Stateful
public class GestorActualizarUsuarioSistemaBean implements GestorActualizarUsuarioSistemaRemote {    // Add business logic below. (Right-click in editor and choose

    private Session sesion;
    private boolean modificacion = false;
    private boolean baja = false;
    private TipoUsuarioSistema tipoUsuarioSistema;
    private UsuarioSistema usuario;

    /**
     * Carga los tipos de usuario del sistema desde la base de datos.
     * @return todos los tipos de usuario del sistema.
     */
    public List<TipoUsuarioSistema> cargarTiposUsuario() {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        List<TipoUsuarioSistema> tipos = sesion.createQuery("from TipoUsuarioSistema").list();
        Hibernate.initialize(tipos);
        sesion.getTransaction().commit();
        for (TipoUsuarioSistema tus : tipos) {
            tus.normalizarObjeto();
        }
        return tipos;
    }

    /**
     * Selecciona un tipo de usuario y muestra en la ventana los usuarios
     * que se encuentran cargados que corresponden al tipo seleccionado.
     * @param idTipoUsuario Identificador del tipo de usuario.
     * @return Lista con usuarios del sistema, null si el tipo de usuario es null.
     */
    public List<UsuarioSistema> seleccionarTipoUsuario(long idTipoUsuario) {
        if (idTipoUsuario != 0) {
            sesion = HibernateUtil.getSessionFactory().openSession();
            sesion.getTransaction().begin();
            tipoUsuarioSistema = (TipoUsuarioSistema) sesion.load(
                    TipoUsuarioSistema.class, idTipoUsuario);
            List<UsuarioSistema> users = sesion.createQuery(
                    "from UsuarioSistema where tipo = ?").
                    setEntity(0, tipoUsuarioSistema).list();
            for (UsuarioSistema usuarioSistema : users) {
                Hibernate.initialize(usuarioSistema.getTipo());
            }
            sesion.getTransaction().commit();
            for (UsuarioSistema usuarioSistema : users) {
                usuarioSistema.getTipo().normalizarObjeto();
            }
            return users;
        } else {
            return null;
        }
    }

    public void seleccionarUsuario(UsuarioSistema usuario) {
        modificacion = true;
        baja = false;
        this.usuario = usuario;
        this.usuario.getTipo().normalizarObjeto();
    }

    public void bajaDeUsuario() throws RegistroFallidoException {
        modificacion = false;
        baja = true;

        confirmarActualizacion();
    }

    public TipoUsuarioSistema getTipoUsuarioSistema() {
        return tipoUsuarioSistema;
    }

    public UsuarioSistema getUsuario() {
        return usuario;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean isModificacion() {
        return modificacion;
    }

    /**
     * Actualiza los datos de la empresa en la BBDD.
     */
    public void confirmarActualizacion() throws RegistroFallidoException {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        try {
            if (modificacion) {
                // MODIFICACION
                sesion.update(usuario);
            } else {
                if (baja) {
                    // BAJA
                    sesion.delete(usuario);
                } else {
                    // ALTA
                    usuario.setTipo(tipoUsuarioSistema);
                    sesion.persist(usuario);
                }
            }
            sesion.getTransaction().commit();
            usuario = null;
        } catch (HibernateException e) {
            sesion.getTransaction().rollback();
            RegistroFallidoException rfe;
            if (e instanceof org.hibernate.exception.ConstraintViolationException) {
                if (baja) {
                    rfe = new RegistroFallidoException("No se pudo eliminar este Usuario,\n" +
                            "debido a que está siendo utilizado por el sistema para otros fines.");
                } else {
                    rfe = new RegistroFallidoException("No se pudo actualizar este Usuario.\n" +
                            "Ya hay otra persona con el mismo nombre de usuario.");
                }
            } else {
                rfe = new RegistroFallidoException(
                        "Error al actualizar los datos del Usuario: " +
                        e.getMessage());
            }

            throw rfe;
        }
    }

    /**
     * Verifica los datos requeridos ingresados.
     * @return true si los datos son correctos, false de lo contrario.
     */
    public boolean verificarDatos() {
        if (usuario == null) {
            return false;
        }

        if (tipoUsuarioSistema == null) {
            return false;
        }

        if (usuario.getNombreUsuario() == null ||
                usuario.getNombreUsuario().length() == 0) {
            return false;
        }

        if (usuario.getPassword() == null ||
                usuario.getPassword().length == 0) {
            return false;
        }

        return true;
    }

    public void registrarNuevoUsuario() {
        modificacion = false;
        baja = false;
        usuario = new UsuarioSistema();
    }

    public void tomarNombreUsuario(String nombre) {
        usuario.setNombreUsuario(nombre);
    }

    /**
     * Toma la contraseña del usuario ingresado.
     * @param password1 Contraseña Ingresada por primera vez.
     * @param password2 Contraseña Repetida que debería ser igual a la primera.
     * @throws soporte.WrongPasswordException En el caso de que password1 sea
     * distinta a password2.
     * @throws java.security.NoSuchAlgorithmException Si no se encuentra el
     * algoritmo de encriptación de claves MD5 en el sistema.
     */
    public void tomarContrasenia(char[] password1, char[] password2)
            throws WrongPasswordException, NoSuchAlgorithmException {
        //Verificar que las passwords no sean nulas, sino sale porque
        //se considera que no hay que tomar una clave nula.
        if (password1 == null && password2 == null) {
            return;
        }
        //Si alguna de las contraseñas es nula.
        if (password1 == null || password2 == null) {
            throw new WrongPasswordException();
        }
        //Verificar password que sean iguales
        String str1 = new String(password1);
        String str2 = new String(password2);
        if (!str1.equals(str2)) {
            throw new WrongPasswordException();
        }
        //Encriptar contraseña y setearla al usuario.
        usuario.setPassword(PasswordUtil.encriptarPassword(password1));
    }
}
