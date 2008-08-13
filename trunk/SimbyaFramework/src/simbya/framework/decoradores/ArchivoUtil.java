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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import org.hibernate.Hibernate;

/**
 * Clase para trabajar con flujos de bytes de archivos.
 * @author Marcelo Busico.
 */
public class ArchivoUtil {

    /**
     * Devuelve un vector de datos del archivo indicado.
     * @param archivo Archivo a leer.
     * @return Vector de datos leidos.
     */
    public static byte[] fileToArray(File archivo) {
        if (archivo.length() > Integer.MAX_VALUE) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(archivo);
            BufferedInputStream in = new BufferedInputStream(fis);
            int tam = (int) archivo.length();
            byte[] res = new byte[tam];
            in.read(res);
            return res;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Escribe en los datos el contenido de un InputStream.
     * @param sourceStream Stream a ser transformado.
     * @throws java.io.IOException
     */
    public static Blob streamToBlob(InputStream sourceStream) throws IOException {
        return Hibernate.createBlob(sourceStream);
    }
}
