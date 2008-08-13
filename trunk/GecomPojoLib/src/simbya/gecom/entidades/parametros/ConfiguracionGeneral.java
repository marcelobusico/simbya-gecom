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
package simbya.gecom.entidades.parametros;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import simbya.framework.persistencia.HibernateUtil;
import simbya.gecom.interfaces.Normalizable;

/**
 * Clase de Entidad que guarda la configuración del sistema.
 * @author Marcelo Busico.
 */
public class ConfiguracionGeneral implements Normalizable {

    private static final Logger log = Logger.getLogger(ConfiguracionGeneral.class);
    private static ConfiguracionGeneral instancia = null;
    private List<ParametroSistema> parametros = new LinkedList<ParametroSistema>();
    //Declaraciones estáticas. 
    //No olvidar de actualizar en el método verificarParametros.
    public static final String nombreEmpresa = "Nombre de la Empresa";
    public static final String actividadEmpresa = "Actividad de la Empresa";
    public static final String ultimoNroRemito = "Numeracion - Ultimo Numero de Remito";
    public static final String ultimoCodigoProducto = "Numeracion - Ultimo Codigo Producto";
    public static final String oidCobroContado = "Forma Cobro - Id Contado";
    public static final String oidCobroTarjeta = "Forma Cobro - Id Tarjeta de Credito";
    public static final String oidCobroCuentaCorriente = "Forma Cobro - Id Cuenta Corriente";
    public static final String descuentoContado = "Forma Cobro - % Descuento Contado";
    public static final String recargoTarjeta = "Forma Cobro - % Recargo Tarjeta de Credito";
    public static final String oidPagoContado = "Forma Pago - Id Contado";    
    public static final String oidPagoCuentaCorriente = "Forma Pago - Id Cuenta Corriente";

    /**
     * Verifica que todos los parámetros estén en la BD cargados.
     * Si alguno no se encuentra lo crea con valores por defecto.
     */
    private void verificarParametros() {
        int cambios = 0;
        cambios += chequearParametro(nombreEmpresa, new String("Empresa S.A."));
        cambios += chequearParametro(actividadEmpresa, new String("Venta de Productos"));
        cambios += chequearParametro(ultimoNroRemito, new Integer(0));
        cambios += chequearParametro(ultimoCodigoProducto, new Long(0));
        cambios += chequearParametro(oidCobroContado, new Long(1));
        cambios += chequearParametro(oidCobroTarjeta, new Long(2));
        cambios += chequearParametro(oidCobroCuentaCorriente, new Long(3));
        cambios += chequearParametro(recargoTarjeta, new Float(10));
        cambios += chequearParametro(descuentoContado, new Float(0));
        cambios += chequearParametro(oidPagoContado, new Long(1));
        cambios += chequearParametro(oidPagoCuentaCorriente, new Long(2));

        //Persiste los nuevos parámetros de sistema en caso de que haya cambios.
        if (cambios > 0) {
            persistir();
            log.warn("Se han detectado parámetros del sistema faltantes y se" +
                    " han creado con valores por defecto.");
        }
    }

    /**
     * Chequea un parámetro individual en la BD y si no lo encuentra lo crea
     * con el valor por defecto indicado.
     * @param clave Nombre del parámetro a chequear.
     * @param valorPorDefecto Valor por defecto del parámetro en caso de no ser
     * encontrado.
     */
    private int chequearParametro(String clave, Object valorPorDefecto) {
        if (getParametro(clave) == null) {
            parametros.add(new ParametroSistema(
                    clave, valorPorDefecto.toString(), valorPorDefecto.getClass()));
            return 1;
        }
        return 0;
    }

    /**
     * Crea una nueva instancia de la clase.
     */
    private ConfiguracionGeneral() {
    }

    /**
     * Establece el valor del atributo parámetros.
     * @param parametros Lista con ParametroSistema a setear.
     */
    private void setParametros(List<ParametroSistema> parametros) {
        this.parametros = parametros;
    }

    /**
     * Devuelve todos los parámetros del sistema. Usado solo para caso de uso
     * de configuración del sistema.
     * @return Lista con todos los parámetros del sistema.
     */
    public List<ParametroSistema> getParametros() {
        return parametros;
    }
    
    /**
     * Devuelve la única instancia de la clase, creandola si es la primera vez
     * a partir de la Base de Datos.
     */
    @SuppressWarnings("unchecked")
    public static final ConfiguracionGeneral getInstancia() {
        if (instancia == null) {
            renovarInstancia();
        }
        return instancia;
    }

    /**
     * Hace que la instancia de la clase se renueve a partir de los datos de la
     * BD. Este método se utiliza cuando se actualizan los datos de los diversos
     * parámetros por un medio que no son los propios métodos de esta clase.
     */
    @SuppressWarnings("unchecked")
    public static final void renovarInstancia() {
        instancia = new ConfiguracionGeneral();
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        instancia.setParametros(sesion.createQuery("from ParametroSistema").list());
        sesion.getTransaction().commit();
        instancia.verificarParametros();
        log.debug("Cargada instancia desde la BD.");
    }

    /**
     * Busca un parámetro en funcion del nombre pasado.
     * @return ParametroSistema con parámetro correspondiente al nombre, null
     * si no se encuentra.
     * @param nombreParametro Nombre o clave del parámetro a buscar.
     * El parámetro String es un atributo estático de la clase que puede
     * accederse de forma pública.
     */
    private ParametroSistema getParametro(String nombreParametro) {
        ParametroSistema ps;

        ListIterator<ParametroSistema> li = parametros.listIterator();
        while (li.hasNext()) {
            ps = li.next();
            if (ps.getClave().equalsIgnoreCase(nombreParametro) == true) {
                return ps;
            }
        }
        return null;
    }

    /**
     * Persiste los parámetros del sistema en la base de datos.
     */
    public void persistir() {
        Session sesion = HibernateUtil.getSessionFactory().openSession();
        sesion.getTransaction().begin();
        ListIterator<ParametroSistema> it = parametros.listIterator();
        while (it.hasNext()) {
            ParametroSistema elem = it.next();
            sesion.saveOrUpdate(elem);
        }
        sesion.getTransaction().commit();
    }

    /**
     * Devuelve el valor del parámetro con el nombre de la clave.
     * @return Valor del parámetro solicitado.
     * @param clave Cadena que representa la clave buscada.
     * El parámetro String es un atributo estático de la clase que puede
     * accederse de forma pública.
     */
    public Object getValorParametro(String clave) {
        ParametroSistema ps = getParametro(clave);
        if (ps == null) {
            ps = new ParametroSistema();
            ps.setClave(clave);
            ps.setValor("0");
            parametros.add(ps);
        }
        Class tipoValor = ps.getTipoValor();
        String valor = ps.getValor();
        if (tipoValor == null) {
            return valor;
        }
        if (tipoValor.getName().equals(Integer.class.getName())) {
            return Integer.valueOf(valor);
        }
        if (tipoValor.getName().equals(Long.class.getName())) {
            return Long.valueOf(valor);
        }
        if (tipoValor.getName().equals(Float.class.getName())) {
            return Float.valueOf(valor);
        }
        if (tipoValor.getName().equals(Double.class.getName())) {
            return Double.valueOf(valor);
        }
        return valor;
    }

    /**
     * Establece el valor del parámetro con el nombre de la clave.
     * @param clave Cadena que representa la clave buscada.
     * El parámetro String es un atributo estático de la clase que puede
     * accederse de forma pública.
     * @param valor Valor del parámetro a establecer.
     */
    public void setValorParametro(String clave, Object valor) {
        ParametroSistema ps = getParametro(clave);
        if (ps == null) {
            ps = new ParametroSistema();
            ps.setClave(clave);
            parametros.add(ps);
        }
        ps.setValor(valor.toString());
        ps.setTipoValor(valor.getClass());
    }

    /**
     * Establece el valor del parámetro con el nombre de la clave.
     * @param clave Cadena que representa la clave buscada.
     * El parámetro String es un atributo estático de la clase que puede
     * accederse de forma pública.
     * @param valor Valor del parámetro a establecer.
     * @param tipoValor Clase del tipo del valor.
     */
    public void setValorParametro(String clave, Object valor, Class tipoValor) {
        ParametroSistema ps = getParametro(clave);
        if (ps == null) {
            ps = new ParametroSistema();
            ps.setClave(clave);
            parametros.add(ps);
        }
        ps.setValor(valor.toString());
        ps.setTipoValor(tipoValor);
    }

    /**
     * Transforma el objeto a un objeto normalizado para transmitirse a través
     * de un flujo de datos, quitando referencias a clases propias del servidor.
     */
    public void normalizarObjeto() {
        Hibernate.initialize(this);
    }

    public long getOid() {
        throw new UnsupportedOperationException("Esta método no debe ser accedido.");
    }

    public void setOid(long oid) {
        throw new UnsupportedOperationException("Esta método no debe ser accedido.");
    }
}
