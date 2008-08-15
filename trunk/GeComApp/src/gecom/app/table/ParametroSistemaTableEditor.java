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
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import simbya.gecom.entidades.parametros.ParametroSistema;

/**
 * Editor de celdas para parametro del sistema.
 * @author Marcelo Busico.
 */
public class ParametroSistemaTableEditor extends AbstractCellEditor
        implements TableCellEditor {

    private ParametroSistema valorActual;
    private JTextField txtValor;

    public ParametroSistemaTableEditor() {
        txtValor = new JFormattedTextField();
        txtValor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    public Object getCellEditorValue() {
        confirmar();
        return new ParametroSistemaTableWrapper(valorActual);
    }

    public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {

        if (value != null) {
            valorActual = ((ParametroSistemaTableWrapper) value).getParametro();
        } else {
            valorActual = null;
        }
        txtValor.setText("");
        return txtValor;
    }

    private void confirmar() {
        if (valorActual.getTipoValor().equals(String.class)) {
            valorActual.setValor(txtValor.getText());
        }
        if (valorActual.getTipoValor().equals(Float.class)) {
            try {
                Float valor = Float.valueOf(txtValor.getText());
                valorActual.setValor(String.valueOf(valor));
                return;
            } catch (Exception e) {
            }
        }
        if (valorActual.getTipoValor().equals(Integer.class)) {
            try {
                Integer valor = Integer.valueOf(txtValor.getText());
                valorActual.setValor(String.valueOf(valor));
                return;
            } catch (Exception e) {
            }
        }
        if (valorActual.getTipoValor().equals(Long.class)) {
            try {
                Long valor = Long.valueOf(txtValor.getText());
                valorActual.setValor(String.valueOf(valor));
                return;
            } catch (Exception e) {
            }
        }
    }
}
