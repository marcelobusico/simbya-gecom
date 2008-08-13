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
package simbya.framework.validadores;

import java.util.Collection;
import javax.swing.JComboBox;

/**
 * Clase que permite validar la selección del usuario en un combo (dropdown).
 * @author Marcelo Busico.
 */
public class ComboUtil<E> {

    JComboBox combo;

    public ComboUtil(JComboBox combo) {
        this.combo = combo;
    }

    /**
     * Devuelve la selección del usuario en el combo box.
     * @return objeto seleccionado, null si no hay selección o el combo está 
     * vacío.
     */
    public E getSelected() {
        if (combo.getSelectedIndex() == -1) {
            return null;
        }
        return (E) combo.getSelectedItem();
    }

    /**
     * Limpia el combo y carga todos los objetos de la lista.
     * @param objects Colección con elementos a cargar.
     */
    public void cleanAndLoad(Collection<E> objects) {
        if (objects == null) {
            throw new IllegalArgumentException("No se admite argumento nulo");
        }
        combo.removeAllItems();
        for (Object object : objects) {
            combo.addItem(object);
        }
    }
}
