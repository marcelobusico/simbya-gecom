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
import simbya.gecom.decoradores.ProductoVendido;

/**
 * Clase para renderizar un producto vendido.
 * @author Marcelo Busico.
 */
public class ProductoVendidoTableRender extends JLabel implements TableCellRenderer {

    private JLabel lblDescripcion;

    public ProductoVendidoTableRender(Color fondo) {
        lblDescripcion = new JLabel("");
        lblDescripcion.setBackground(fondo);
        lblDescripcion.setFont(Font.getFont("Application.font.text"));
        lblDescripcion.setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        ProductoVendido productoActual = null;
        lblDescripcion.setText("");
        if (value != null) {
            productoActual = (ProductoVendido) value;
            lblDescripcion.setText(productoActual.toString());
            if (productoActual.getClaseProducto().equals(ProductoVendido.CLASE_A)) {
                lblDescripcion.setBackground(new Color(0, 204, 0));
            }
            if (productoActual.getClaseProducto().equals(ProductoVendido.CLASE_B)) {
                lblDescripcion.setBackground(new Color(255, 255, 51));
            }
            if (productoActual.getClaseProducto().equals(ProductoVendido.CLASE_C)) {
                lblDescripcion.setBackground(new Color(255, 102, 102));
            }
        }

        if (hasFocus) {
            lblDescripcion.setBorder(BorderFactory.createLineBorder(Color.RED));
        } else {
            if (isSelected) {
                lblDescripcion.setBorder(BorderFactory.createLineBorder(Color.BLUE));
            } else {
                lblDescripcion.setBorder(null);
            }
        }
        return lblDescripcion;
    }
}
