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
package simbya.gecom.gestores.seguridad;

import java.security.NoSuchAlgorithmException;
import javax.ejb.Stateless;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import simbya.framework.password.PasswordUtil;
import simbya.framework.persistencia.HibernateUtil;
import simbya.gecom.entidades.seguridad.UsuarioSistema;

/**
 * Gestor para iniciar sesión.
 * @author Marcelo Busico.
 */
@Stateless
public class GestorIniciarSesionBean implements GestorIniciarSesionRemote {

    private static final Logger log = Logger.getLogger(GestorIniciarSesionBean.class);

    public GestorIniciarSesionBean() {
    }

    /**
     * Valida al usuario y contraseña ingresados.
     * @param user Nombre de usuario a validar.
     * @param password Contraseña del usuario.
     * @return Objeto UsuarioSistema validado, null si no fue válido.
     */
    public UsuarioSistema iniciarSesion(String user, char[] password) {
        boolean resultado = false;
        if (user.equals("admin")) {
            //Compara con la password por defecto:
            String passOriginal = "2607";
            String passIngresada = new String(password);
            if (passOriginal.equals(passIngresada)) {
                //Permite el ingreso para el administrador con password
                //por defecto.
                log.warn("El administrador ha iniciado sesión con la password por defecto.");
                UsuarioSistema admin = new UsuarioSistema();
                admin.setNombreUsuario("Administrador del Sistema");
                return admin;
            }
        }

        Session sesion = null;
        UsuarioSistema usuario = null;
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        try {
            usuario = (UsuarioSistema) sesion.createQuery(
                    "from UsuarioSistema as us " +
                    "where us.nombreUsuario=?").setString(0, user).uniqueResult();
            if (usuario != null) {
                try {
                    if (PasswordUtil.sonClavesIguales(usuario.getPassword(), password)) {
                        log.info("El usuario " + user + " ha iniciado sesión correctamente.");
                        resultado = true;
                    }
                } catch (NoSuchAlgorithmException ex) {
                    log.error("No se encuentra el algoritmo de encriptación MD5", ex);
                    resultado = false;
                }
            }
        } catch (Exception ex) {
            log.warn("Error al buscar usuarios del sistema en la BD.", ex);
        }

        sesion.getTransaction().commit();
        if (resultado) {
            usuario.normalizarObjeto();
            return usuario;
        } else {
            return null;
        }
    }

    public void salir() {
        log.info("El cliente ha salido del sistema sin iniciar sesión.");
    }
}
