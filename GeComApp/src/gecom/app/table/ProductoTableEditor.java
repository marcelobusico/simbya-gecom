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

import gecom.app.buscador.Buscador;
import gecom.app.productos.DialogoBuscadorProductos;
import java.awt.Color;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import simbya.framework.validadores.TextValidator;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Producto;

/**
 * Editor de celdas para códigos de producto con el buscador.
 * @author Marcelo Busico.
 */
public class ProductoTableEditor extends AbstractCellEditor
        implements TableCellEditor {

    private ProductoTableWrapper productoActual;
    private JFormattedTextField txtCodigo;
    private boolean mostrarRegistrarNuevo;

    public ProductoTableEditor(boolean mostrarRegistrarNuevo) {
        this.mostrarRegistrarNuevo = mostrarRegistrarNuevo;
        txtCodigo = new JFormattedTextField();
        txtCodigo.setFocusLostBehavior(JFormattedTextField.COMMIT);
        txtCodigo.setValue(new Long(0));
        txtCodigo.setText("");
        txtCodigo.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    public Object getCellEditorValue() {
        confirmar();
        return productoActual;
    }

    public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {

        if (value != null) {
            productoActual = (ProductoTableWrapper) value;
            txtCodigo.setValue(productoActual.getProducto().getCodigo());
            txtCodigo.setText("");
        } else {
            productoActual = null;
            txtCodigo.setValue(new Long(0));
            txtCodigo.setText("");
        }
        return txtCodigo;
    }

    private void confirmar() {
        Producto p = null;
        long value = TextValidator.validarLong(null, txtCodigo, null);
        txtCodigo.setForeground(new Color(0, 0, 0));
        if (value != -1) {
            if (value == 0) {
                buscarEnDialogo();
            } else {
                p = Buscador.encontrarProducto(value);
                if (p == null) {
                    buscarEnDialogo();
                } else {
                    productoActual = new ProductoTableWrapper(p);
                }
            }
        }
    }

    private void buscarEnDialogo() {
        DialogoBuscadorProductos dialogo = new DialogoBuscadorProductos(
                null, true, mostrarRegistrarNuevo);
        Producto p = dialogo.getSeleccion();
        CalidadProducto cp = dialogo.getCalidadSeleccionada();
        if (p != null) {
            productoActual = new ProductoTableWrapper(p, cp);
        }
    }
}
