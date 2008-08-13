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

import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.io.Serializable;
import java.sql.Blob;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

/**
 * Clase que encapsula un archivo de imagen.
 * @author Marcelo Busico.
 */
public class Imagen implements Serializable {

    public Imagen() {
    }

    /**
     * Muestra un JFileChooser que solo permite seleccionar imágenes.
     * @return Referencia al archivo seleccionado, null si el usuario no
     * seleccionó archivo.
     */
    public static File verDialogoSeleccion(Component padre) {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(new ImageFileFilter());
        int res = jfc.showOpenDialog(padre);
        if (res == JFileChooser.APPROVE_OPTION) {
            return jfc.getSelectedFile();
        }
        return null;
    }

    /**
     * Convierte un Blob de SQL a una Imagen para visualizar en swing.
     * @param datos Flujo de datos de la imagen.
     * @return Imagen convertida.
     */
    public static Image blobToImage(Blob datos) {
        Image image = null;
        try {
            image = ImageIO.read(datos.getBinaryStream());
            return image;
        } catch (Exception e) {
            return null;
        }
    }

}
