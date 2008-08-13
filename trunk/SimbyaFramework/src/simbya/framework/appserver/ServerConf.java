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
package simbya.framework.appserver;

import java.io.*;

/**
 * Clase con informacion de conexión al servidor de aplicaciones.
 * @author Marcelo Busico.
 */
public class ServerConf implements Serializable {

    /**
     * Nombre del archivo donde se guardará la configuración del servidor
     * de Base de Datos por defecto.
     */
    public static final transient String nombreArchivo = "appserver.dat";
    private String direccion;
    private int puerto;

    /** 
     * Crea una nueva instancia de ServerConf. 
     */
    public ServerConf() {
    }

    public ServerConf(String direccion, int puerto) {
        this.direccion = direccion;
        this.puerto = puerto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    /**
     * Carga la configuracion desde un archivo y retorna el objeto cargado.
     */
    public static ServerConf cargarDeArchivo(String archivo) throws Exception {
        File f = new File(archivo);
        FileInputStream f2 = new FileInputStream(f);
        ObjectInputStream f3 = new ObjectInputStream(f2);
        ServerConf sc = (ServerConf) f3.readObject();
        f3.close();
        f2.close();
        return sc;
    }

    /**
     * Guarda la configuracion en un archivo.
     */
    public void guardarEnArchivo(String archivo) throws IOException {
        File f = new File(archivo);
        FileOutputStream f2 = new FileOutputStream(f);
        ObjectOutputStream f3 = new ObjectOutputStream(f2);
        f3.writeObject(this);
        f3.close();
        f2.close();
    }
}
