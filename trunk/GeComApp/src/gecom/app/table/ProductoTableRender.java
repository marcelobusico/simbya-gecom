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

/**
 * Clase para renderizar el codigo de un producto encapsulado.
 * @author Marcelo Busico.
 */
public class ProductoTableRender extends JLabel implements TableCellRenderer {

    private JLabel lblCodigo;

    public ProductoTableRender(Color fondo) {
        lblCodigo = new JLabel("");
        lblCodigo.setBackground(fondo);
        lblCodigo.setFont(Font.getFont("Application.font.text"));
        lblCodigo.setHorizontalAlignment(JLabel.RIGHT);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        ProductoTableWrapper productoActual = null;
        lblCodigo.setText("");
        if (value != null) {
            productoActual = (ProductoTableWrapper) value;
            lblCodigo.setText(String.valueOf(productoActual.getProducto().getCodigo()));
        }

        if (hasFocus) {
            lblCodigo.setBorder(BorderFactory.createLineBorder(Color.RED));
        } else {
            lblCodigo.setBorder(null);
        }

        if (isSelected) {
            lblCodigo.setOpaque(true);
        } else {
            lblCodigo.setOpaque(false);
        }
        return lblCodigo;
    }
}