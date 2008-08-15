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
package simbya.gecom.gestores.productos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateful;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import simbya.framework.decoradores.ArchivoUtil;
import simbya.framework.excepciones.RegistroFallidoException;
import simbya.framework.persistencia.HibernateUtil;
import simbya.framework.tipos.PersistentClassLoader;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Producto;
import simbya.gecom.entidades.parametros.ConfiguracionGeneral;
import simbya.gecom.interfaces.Normalizable;

/**
 * Gestor para actualizar los datos de los productos.
 * @author Marcelo Busico
 */
@Stateful
public class GestorActualizarProductoBean implements GestorActualizarProductoRemote {

    private static final Logger log = Logger.getLogger(
            GestorActualizarProductoBean.class);
    private Session sesion;
    private Blob imagen;

    public GestorActualizarProductoBean() {
    }

    /**
     * Carga los elementos disponibles desde la base de datos.
     * @param clasePersistente Clase persistente a traer de la BD.
     * @return Lista con los elementos encontrados.
     */
    public List cargarObjetosPersistentes(Class clasePersistente) {
        if(clasePersistente==null) {
            throw new IllegalArgumentException("Objeto clase nulo.");
        }
        List<Normalizable> lista = PersistentClassLoader.cargarObjetosPersistentes(clasePersistente);
        for (Normalizable normalizable : lista) {
            normalizable.normalizarObjeto();
        }
        return lista;
    }

    /**
     * Actualiza el producto en la BD.
     * @param nuevo Indica si el producto es nuevo o es una modificación.
     * @param producto Producto a actualizar.
     * @return Producto registrado en BD.
     * @throws simbya.framework.excepciones.RegistroFallidoException
     * Si sucede algún error al guardar el producto.
     */
    public Producto confirmarActualizacion(boolean nuevo, Producto producto)
            throws RegistroFallidoException {

        if(producto==null) {
            throw new IllegalArgumentException("Producto null.");
        }
        if(producto.getCalidades()==null) {
            throw new IllegalArgumentException("Calidades del Producto null.");
        }
        if(producto.getCalidades().size()==0) {
            throw new IllegalArgumentException("Calidades con 0 elementos.");
        }
        producto.setImagen(imagen);

        Long ultCodigo = null;
        if (nuevo) {
            //Generar código si corresponde.
            ultCodigo = (Long) ConfiguracionGeneral.getInstancia().getValorParametro(
                    ConfiguracionGeneral.ultimoCodigoProducto);
            ultCodigo++;
            producto.setCodigo(ultCodigo);
        }
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        //Asigna el producto a todas sus calidades
        for (CalidadProducto calidad : producto.getCalidades()) {
            calidad.setProducto(producto);
        }
        //Guarda el producto.
        sesion.saveOrUpdate(producto);
        sesion.getTransaction().commit();
        //Actualiza el código
        if (nuevo) {
            ConfiguracionGeneral.getInstancia().setValorParametro(
                    ConfiguracionGeneral.ultimoCodigoProducto, ultCodigo);
            ConfiguracionGeneral.getInstancia().persistir();
        }
        producto.normalizarObjeto();
        return producto;
    }

    public void bajaDeProducto(Producto p) throws Exception {
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        //Baja del producto.
        Producto prod = (Producto) sesion.load(Producto.class, p.getOid());
        prod.setBaja(true);
        prod.setFechaBaja(new Date());
        sesion.saveOrUpdate(prod);
        sesion.getTransaction().commit();
    }

    public void cargarImagen(byte[] data) throws IOException {
        if (data == null) {
            imagen = null;
            return;
        }
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        imagen = ArchivoUtil.streamToBlob(is);
    }

    public byte[] getImagenProducto(Producto p) throws Exception {
        if (p == null) {
            throw new IllegalArgumentException("El producto es nulo.");
        }
        sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        Object obj = sesion.load(Producto.class, p.getOid());
        Producto prod = (Producto) obj;
        try {
            return prod.getImageData();
        } catch (SQLException ex) {
            log.error("Error al obtener imagen de producto " + prod.toString(), ex);
            throw new SQLException("Error al obtener imagen de producto " + prod.toString(), ex);
        } finally {
            sesion.getTransaction().commit();
        }
    }
}
