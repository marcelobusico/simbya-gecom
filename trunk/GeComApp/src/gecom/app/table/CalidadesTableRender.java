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
 * Clase para renderizar las calidades de un producto encapsulado en un combobox.
 * @author Marcelo Busico.
 */
public class CalidadesTableRender extends JLabel implements TableCellRenderer {

    private JLabel lblCalidades;

    public CalidadesTableRender(Color fondo) {
        lblCalidades = new JLabel("");
        lblCalidades.setBackground(fondo);
        lblCalidades.setFont(Font.getFont("Application.font.text"));
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        CalidadesTableWrapper productoActual = null;
        lblCalidades.setText("");
        if (value != null) {
            productoActual = (CalidadesTableWrapper) value;
            lblCalidades.setText(productoActual.getCalidad().getCalidad().getNombre());
        }

        if (hasFocus) {
            lblCalidades.setBorder(BorderFactory.createLineBorder(Color.RED));
        } else {
            lblCalidades.setBorder(null);
        }

        if (isSelected) {
            lblCalidades.setOpaque(true);
        } else {
            lblCalidades.setOpaque(false);
        }
        return lblCalidades;
    }
}
