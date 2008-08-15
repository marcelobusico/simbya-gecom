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
package gecom.app.ventas;

import gecom.reportes.util.Impresor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import simbya.gecom.entidades.DetalleVenta;
import simbya.gecom.entidades.Venta;

/**
 * Clase que permite imprimir el remito de una venta realizada.
 * @author Marcelo Busico.
 */
public class ImpresorRemito implements Runnable {

    private static final Logger log = Logger.getLogger(ImpresorRemito.class);
    private Venta venta;
    private List<DetalleVentaDecorator> detalles;

    /**
     * Imprime el remito de la venta indicada.
     * @param venta Venta a generar remito impreso.
     */
    public static void imprimirRemitoVenta(Venta venta) {
        new Thread(new ImpresorRemito(venta)).start();
    }

    public ImpresorRemito(Venta venta) {
        this.venta = venta;
        detalles = new LinkedList<DetalleVentaDecorator>();
        //Carga los detalles apropiadamente.
        for (DetalleVenta detalle : venta.getDetalles()) {
            DetalleVentaDecorator dvd = new DetalleVentaDecorator();
            dvd.setCantidad(detalle.getCantidad());
            dvd.setCodigo(detalle.getProducto().getCodigo());
            String marcaDesc = detalle.getProducto().getDescripcion() + " - " +
                    detalle.getCalidad().getCalidad().getNombre();
            dvd.setDescripcion(marcaDesc);
            dvd.setUm(detalle.getProducto().getProductoUM().getNombre());
            dvd.setUnitario(detalle.getImporteUnitario());
            detalles.add(dvd);
        }
    }

    public void run() {
        try {
            imprimirInforme(false);
        } catch (Exception ex) {
            log.error("Error al imprimir remito", ex);
            String mensaje = "Error al imprimir remito:\n" + ex.getMessage();
            JOptionPane.showMessageDialog(null, mensaje, "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Imprime un reporte en un visor de Jasper Reports a partir del cual se puede
     * exportar o enviar a la impresora. Recibe como parámetro un valor booleano que
     * indica si se realizara impresion económica o no.
     * @param ImpresionEconomica 
     * true: imprimir en blanco y negro
     * false. imprimir en color.
     * @throws java.lang.Exception Si sucede algún error de impresión.
     */
    public void imprimirInforme(boolean impresionEconomica) throws Exception {
        Map params = new HashMap();
        params.put("FECHA", venta.getFecha());
        params.put("CLIENTE",
                (venta.getCliente() != null ? venta.getCliente().getIdentidad() : "Consumidor Final"));
        if (venta.getImporteRecargoDescuento() == null || venta.getImporteRecargoDescuento() == 0) {
            params.put("REC_DESC", null);
            params.put("VALOR_REC_DESC", null);
        } else {
            if (venta.getImporteRecargoDescuento() > 0) {
                params.put("REC_DESC", "Recargo:");
                params.put("VALOR_REC_DESC", venta.getImporteRecargoDescuento());
            } else {
                params.put("REC_DESC", "Descuento:");
                params.put("VALOR_REC_DESC", (-1) * venta.getImporteRecargoDescuento());
            }
        }
        params.put("FORMA_PAGO", venta.getFormaCobro().getNombre());
        params.put("SUBTOTAL", venta.getImporteTotal() - venta.getImporteRecargoDescuento());
        params.put("TOTAL", venta.getImporteTotal());

        //Imprime el informe.
        Impresor impresor = new Impresor(params,
                "RemitoVenta", detalles);
        impresor.imprimirInforme(impresionEconomica);
    }
}
