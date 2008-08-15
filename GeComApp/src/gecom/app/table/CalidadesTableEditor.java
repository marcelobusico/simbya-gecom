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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import simbya.gecom.entidades.CalidadProducto;

/**
 * Editor de celdas para calidades de un producto.
 * @author Marcelo Busico.
 */
public class CalidadesTableEditor extends AbstractCellEditor
        implements TableCellEditor, KeyListener {

    private CalidadesTableWrapper actual;
    private JComboBox cboCalidades;

    public CalidadesTableEditor() {
        cboCalidades = new JComboBox();
        cboCalidades.setFont(new Font("Dialog", Font.PLAIN, 10));
        cboCalidades.addKeyListener(this);
    }

    public Object getCellEditorValue() {
        if (cboCalidades.getSelectedIndex() != -1) {
            actual.setCalidad((CalidadProducto) cboCalidades.getSelectedItem());
        } else {
            if (actual != null) {
                actual.setCalidad(null);
            }
        }
        return actual;
    }

    public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {

        cboCalidades.removeAllItems();
        if (value != null) {
            actual = (CalidadesTableWrapper) value;
            if (actual.getProducto().getCalidades().size() == 1) {
                return null;
            }
            for (CalidadProducto cal : actual.getProducto().getCalidades()) {
                cboCalidades.addItem(cal);
            }            
            cboCalidades.setSelectedItem(actual.getCalidad());
            return cboCalidades;
        } else {
            actual = null;
            return null;
        }

    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        int cant = cboCalidades.getItemCount();
        if (cant == 0) {
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            int next = cboCalidades.getSelectedIndex() + 1;
            if (next < cant) {
                cboCalidades.setSelectedIndex(next);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_A) {
            int prev = cboCalidades.getSelectedIndex() - 1;
            if (prev >= 0) {
                cboCalidades.setSelectedIndex(prev);
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }
}
