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

import java.util.Set;
import java.util.TreeSet;
import org.hibernate.Hibernate;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que representa un Tipo de usuario del sistema.
 * @author Marcelo Busico.
 */
public class TipoUsuarioSistema implements Normalizable {

    private long oid; //Requerido pero automático
    private String nombre; //Requerido
    private String descripcion;
    private Set<CasoDeUso> privilegiosCU;

    /** 
     * Crea una nueva instancia de TipoUsuarioSistema.
     */
    public TipoUsuarioSistema() {
    }

    public String getNombre() {
        return nombre;
    }

    public long getOid() {
        return oid;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Set<CasoDeUso> getPrivilegiosCU() {
        return privilegiosCU;
    }

    public Set<CasoDeUso> getPrivilegiosCUOrdenados() {
        TreeSet<CasoDeUso> arbol = new TreeSet<CasoDeUso>();
        for (CasoDeUso casoDeUso : privilegiosCU) {
            arbol.add(casoDeUso);
        }
        return arbol;
    }

    public void setPrivilegiosCU(Set<CasoDeUso> privilegiosCU) {
        this.privilegiosCU = privilegiosCU;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        setPrivilegiosCU(getPrivilegiosCUOrdenados());
    }

    /**
     * Muestra el nombre del Tipo de Usuario del sistema.
     */
    @Override
    public String toString() {
        return this.getNombre();
    }
}
