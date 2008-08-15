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
package gecom.app.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import simbya.gecom.entidades.CalidadProducto;

/**
 * Clase para renderizar las calidades de un producto encapsulado en un combobox.
 * @author Marcelo Busico.
 */
public class CantidadStockTableRender extends JLabel implements TableCellRenderer {

    private JLabel lblCantidad;
    private Color fondo;

    public CantidadStockTableRender(Color fondo) {
        this.fondo = fondo;
        lblCantidad = new JLabel("");
        lblCantidad.setBackground(fondo);
        lblCantidad.setHorizontalAlignment(JLabel.RIGHT);
        lblCantidad.setFont(Font.getFont("Application.font.text"));
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        boolean mostrarFondo = false;
        CantidadStockTableWrapper actual = null;
        lblCantidad.setText("");
        lblCantidad.setToolTipText("");
        lblCantidad.setBackground(fondo);
        if (value != null) {
            actual = (CantidadStockTableWrapper) value;
            Float cantidadSolicitada = actual.getCantidadSolicitada();
            Float cantidadOriginal = actual.getCantidadOriginal();
            CalidadProducto calidad = actual.getCalidad();
            if (cantidadSolicitada != null && calidad != null) {
                if (actual.isActualizarStock()) {
                    //CU Actualizar Stock
                    if (cantidadSolicitada != cantidadOriginal) {
                        mostrarFondo = true;
                        lblCantidad.setBackground(Color.GREEN);
                        lblCantidad.setToolTipText("<html>Stock Actualizado<br/>" +
                                "Stock original: " + cantidadOriginal +
                                "</html>");
                    }
                } else {
                    //CU Vender
                    if (cantidadSolicitada > calidad.getStockActual()) {
                        mostrarFondo = true;
                        lblCantidad.setBackground(Color.ORANGE);
                        lblCantidad.setToolTipText("<html>No hay stock suficiente<br/>" +
                                "Stock disponible: " + calidad.getStockActual() +
                                "</html>");
                    }
                }
                lblCantidad.setText(cantidadSolicitada.toString());
            } else {
                lblCantidad.setText("");
            }
        }

        if (hasFocus) {
            lblCantidad.setBorder(BorderFactory.createLineBorder(Color.RED));
        } else {
            lblCantidad.setBorder(null);
        }

        lblCantidad.setOpaque(true);
        if (!isSelected && !mostrarFondo) {
            lblCantidad.setOpaque(false);
        }
        return lblCantidad;
    }
}
