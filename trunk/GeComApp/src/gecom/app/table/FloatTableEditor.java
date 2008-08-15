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
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import simbya.framework.validadores.TextValidator;

/**
 * Editor de celdas para valores float.
 * @author Marcelo Busico.
 */
public class FloatTableEditor extends AbstractCellEditor
        implements TableCellEditor {

    private Float valorActual;
    private JFormattedTextField txtValor;

    public FloatTableEditor() {
        txtValor = new JFormattedTextField();
        txtValor.setFocusLostBehavior(JFormattedTextField.COMMIT);
        txtValor.setValue(new Float(0));
        txtValor.setText("");
        txtValor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    public Object getCellEditorValue() {
        confirmar();
        return valorActual;
    }

    public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {

        if (value != null) {
            valorActual = (Float) value;
            txtValor.setValue(valorActual);
            txtValor.setText("");
        } else {
            valorActual = null;
            txtValor.setValue(new Float(0));
            txtValor.setText("");
        }
        return txtValor;
    }

    private void confirmar() {
        float value = TextValidator.validarFloat(null, txtValor, null);
        txtValor.setForeground(new Color(0, 0, 0));
        if (value > 0) {
            valorActual = value;
        }
    }
}
