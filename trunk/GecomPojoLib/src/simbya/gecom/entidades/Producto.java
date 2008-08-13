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
package simbya.gecom.entidades;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import org.hibernate.Hibernate;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que representa una Localidad de una Provincia.
 * @author Marcelo Busico.
 */
public class Producto implements Normalizable {

    private long oid; //Requerido pero automático
    private long codigo; //Requerido pero automático
    private boolean baja = false; //Requerido
    private Date fechaBaja = null;
    private String descripcion; //Requerido
    private TipoRubro rubro; //Requerido
    private TipoMarca marca; //Requerido
    private TipoCilindradaMoto cilindradaMoto;
    private TipoModelo modelo;
    private Integer anio;
    private Set<CalidadProducto> calidades; //Requerido
    private String ubicacionEstanteria;
    private transient Blob imagen;
    private TipoUnidadMedida productoUM;
    private TipoUnidadMedida dimensionUM;
    private Float medidaInterior;
    private Float medidaExterior;
    private Float medidaEspesor;

    public Producto() {
    }

    public long getCodigo() {
        return codigo;
    }

    public void setCodigo(long codigo) {
        this.codigo = codigo;
    }

    public TipoUnidadMedida getDimensionUM() {
        return dimensionUM;
    }

    public void setDimensionUM(TipoUnidadMedida dimensionUM) {
        this.dimensionUM = dimensionUM;
    }

    public TipoUnidadMedida getProductoUM() {
        return productoUM;
    }

    public void setProductoUM(TipoUnidadMedida productoUM) {
        this.productoUM = productoUM;
    }

    public Date getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public boolean isBaja() {
        return baja;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Set<CalidadProducto> getCalidades() {
        return calidades;
    }

    public void setCalidades(Set<CalidadProducto> calidades) {
        this.calidades = calidades;
    }

    public TipoCilindradaMoto getCilindradaMoto() {
        return cilindradaMoto;
    }

    public void setCilindradaMoto(TipoCilindradaMoto cilindradaMoto) {
        this.cilindradaMoto = cilindradaMoto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Blob getImagen() {
        return imagen;
    }

    public void setImagen(Blob imagen) {
        this.imagen = imagen;
    }

    public Float getMedidaEspesor() {
        return medidaEspesor;
    }

    public TipoMarca getMarca() {
        return marca;
    }

    public void setMarca(TipoMarca marca) {
        this.marca = marca;
    }

    public void setMedidaEspesor(Float medidaEspesor) {
        this.medidaEspesor = medidaEspesor;
    }

    public Float getMedidaExterior() {
        return medidaExterior;
    }

    public void setMedidaExterior(Float medidaExterior) {
        this.medidaExterior = medidaExterior;
    }

    public Float getMedidaInterior() {
        return medidaInterior;
    }

    public void setMedidaInterior(Float medidaInterior) {
        this.medidaInterior = medidaInterior;
    }

    public TipoModelo getModelo() {
        return modelo;
    }

    public void setModelo(TipoModelo modelo) {
        this.modelo = modelo;
    }

    public TipoRubro getRubro() {
        return rubro;
    }

    public void setRubro(TipoRubro rubro) {
        this.rubro = rubro;
    }

    public String getUbicacionEstanteria() {
        return ubicacionEstanteria;
    }

    public void setUbicacionEstanteria(String ubicacionEstanteria) {
        this.ubicacionEstanteria = ubicacionEstanteria;
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
        Hibernate.initialize(imagen);
        if (cilindradaMoto != null) {
            cilindradaMoto.normalizarObjeto();
        }
        if (dimensionUM != null) {
            dimensionUM.normalizarObjeto();
        }
        if (marca != null) {
            marca.normalizarObjeto();
        }
        if (modelo != null) {
            modelo.normalizarObjeto();
        }
        if (productoUM != null) {
            productoUM.normalizarObjeto();
        }
        if (rubro != null) {
            rubro.normalizarObjeto();
        }
        TreeSet<CalidadProducto> arbol = new TreeSet<CalidadProducto>();
        for (CalidadProducto cal : calidades) {
            cal.normalizarObjetoDesdeProducto();
            arbol.add(cal);
        }
        calidades = arbol;
    }

    public boolean hasImage() {
        return (imagen == null ? false : true);
    }

    public byte[] getImageData() throws SQLException {
        if (imagen == null) {
            return null;
        }
        return imagen.getBytes(1, (int) imagen.length());
    }

    /**
     * Muestra la descripción del producto.
     */
    @Override
    public String toString() {
        return descripcion;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Producto other = (Producto) obj;
        if (this.oid != other.oid) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.descripcion != null ? this.descripcion.hashCode() : 0);
        return hash;
    }
}
