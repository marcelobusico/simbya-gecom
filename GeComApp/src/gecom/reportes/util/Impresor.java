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
package gecom.reportes.util;

import gecom.app.configuracion.ParamSistema;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
import simbya.gecom.entidades.parametros.ConfiguracionGeneral;

/**
 * Clase que se utiliza para imprimir informes en JasperReports.
 * @author Marcelo Busico.
 */
public class Impresor {

    Map params;
    String nombreReporte;
    Collection datos;

    /**
     * Crea una nueva instancia de impresor.
     * @param params Parámetros que serán pasados al informe de JasperReports.
     * @param nombreReporte Nombre del Reporte Jasper, 
     * <b> SIN RUTA NI EXTENSION y sin el sufijo BN </b>.
     * @param datos Colección de datos que será mostrada en el informe. Por ejemplo:
     * cada una de las filas de una tabla. Podría ser una Lista de Objetos.
     */
    public Impresor(Map params, String nombreReporte, Collection datos) {
        this.params = params;
        this.nombreReporte = nombreReporte;
        this.datos = datos;
    }

    /**
     * Imprime un reporte en un visor de Jasper Reports a partir del cual se puede
     * exportar o enviar a la impresora.
     * @param ImpresionEconomica 
     * true: imprimir en blanco y negro (en realidad es económica).
     * false: imprimir en color.
     * @throws java.lang.Exception Si sucede algún error en la impresión.
     */
    public void imprimirInforme(boolean impresionEconomica) throws Exception {
        if (params == null) {
            throw new IllegalArgumentException("Los parámetros son nulos.");
        }
        params.put("NOMBRE_EMPRESA", 
                ParamSistema.getValorParametro(ConfiguracionGeneral.nombreEmpresa));
        params.put("ACTIVIDAD_EMPRESA", 
                ParamSistema.getValorParametro(ConfiguracionGeneral.actividadEmpresa));
        String reporte;
        if (impresionEconomica) {
            reporte = "/gecom/reportes/" + nombreReporte + "BN.jrxml";
        } else {
            reporte = "/gecom/reportes/" + nombreReporte + ".jrxml";
        }
        InputStream reportStream = getClass().getResourceAsStream(reporte);
        JasperDesign jasperDesign = JRXmlLoader.load(reportStream);
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(datos);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, ds);
        JasperViewer.viewReport(jasperPrint, false);
    }
}
