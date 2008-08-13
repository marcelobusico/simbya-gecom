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
package simbya.framework.decoradores;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Filtro que permite seleccionar solo imágenes.
 * @author Marcelo Busico.
 */
public class ImageFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        int index = f.getName().lastIndexOf(".");
        if (index == -1) {
            return false;
        }
        String ext = null;
        try {
            ext = f.getName().substring(index + 1);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        if (ext.equalsIgnoreCase("jpg")) {
            return true;
        }
        if (ext.equalsIgnoreCase("jpeg")) {
            return true;
        }
        if (ext.equalsIgnoreCase("png")) {
            return true;
        }
        if (ext.equalsIgnoreCase("gif")) {
            return true;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Archivos de imágen JPG, PNG y GIF";
    }
}
