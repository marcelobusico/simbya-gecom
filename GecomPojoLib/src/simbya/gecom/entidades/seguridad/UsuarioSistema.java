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
package simbya.gecom.entidades.seguridad;

import org.hibernate.Hibernate;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que representa un usuario del sistema.
 * @author Marcelo Busico.
 */
public class UsuarioSistema implements Normalizable {

    private long oid; //Requerido pero automático
    private String nombreUsuario; //Requerido
    private byte[] password; //Requerido
    private TipoUsuarioSistema tipo; //Requerido

    /** 
     * Crea una nueva instancia de UsuarioSistema.
     */
    public UsuarioSistema() {
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public TipoUsuarioSistema getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuarioSistema tipo) {
        this.tipo = tipo;
    }

    /**
     * Devuelve la contraseña del usuario encriptada.
     * @return Vector de Bytes con contraseña encriptada.
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * Establece la contraseña del usuario encriptada.
     * @param password Vector de Bytes con contraseña encriptada.
     */
    public void setPassword(byte[] password) {
        this.password = password;
    }
    
    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        tipo.normalizarObjeto();
    }

    @Override
    public String toString() {
        return this.getNombreUsuario();
    }
}
