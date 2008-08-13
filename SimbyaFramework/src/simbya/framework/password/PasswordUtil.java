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
package simbya.framework.password;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase con métodos útiles para verificar contraseñas.
 * @author Emiliano Bianchetti y Marcelo Busico.
 */
public abstract class PasswordUtil {

    /**
     * Verifica 2 contraseñas, una encriptada y la otra no, para verificar si
     * son coincidentes, mediante encriptación MD5.
     * @param passwordEncriptada Contraseña encriptada en MD5 en un vector de byte.
     * @param passwordIngresada Contraseña sin encriptar en un vector de char.
     * @return true si las contraseñas son iguales, false si son distintas.
     * @throws java.security.NoSuchAlgorithmException Si el algorito MD5 no
     * puede utilizarse en el sistema.
     */
    public static boolean sonClavesIguales(
            byte[] passwordEncriptada, char[] passwordIngresada)
            throws NoSuchAlgorithmException {

        // Establece un vector con 0 elementos a los vectores nulos para
        // comprobaciones posteriores.
        if (passwordEncriptada == null) {
            passwordEncriptada = new byte[0];
        }

        if (passwordIngresada == null) {
            return false;
        }
        byte[] ingresoEncriptado = encriptarPassword(passwordIngresada);
        if (ingresoEncriptado.length != passwordEncriptada.length) {
            //devuelve falso si las longitudes de los vectores no coinciden.
            return false;
        }
        for (int i = 0; i < ingresoEncriptado.length; i++) {
            if (ingresoEncriptado[i] != passwordEncriptada[i]) {
                //Hay un byte que difiere y retorna.
                return false;
            }
        }
        //Las contraseñas son iguales.
        return true;
    }

    /**
     * Encripta una contraseña a través del algoritmo MD5.
     * @param password Vector char con contraseña a encriptar.
     * @return Vector byte con la contraseña encriptada, 
     * null si el vector es null.
     * @throws java.security.NoSuchAlgorithmException
     */
    public static byte[] encriptarPassword(char[] password)
            throws NoSuchAlgorithmException {
        if (password == null) {
            return null;
        }
        MessageDigest encriptador = MessageDigest.getInstance("MD5");

        byte[] res = encriptador.digest(TypeConverter.getBytes(password));

        return res;
    }

    /**
     * Genera el vector byte con la nueva contraseña si se cumple con
     * los requisitos de cambio de clave.
     * @param actualGuardada Vector de byte de la clave actual guarda y cifrada,
     * o valor nulo si no hay contraseña guardada.
     * @param actualIngresada Vector de char de la clave actual ingresada, o valor
     * nulo si se considera que no hay contraseña actual.
     * @param nueva Vector de char con la clave nueva.
     * @param nuevaRepetida Vector de char con la clave nueva reingresada.
     * @return El vector de byte con la nueva contraseña cifrada, o bien un
     * vector nulo si las contraseñas nuevas son ambas nulas.
     * @throws tesauro.excepciones.WrongPasswordException No se ha podido cambiar
     * la contraseña debido a que se ha ingresado una clave anterior incorrecta
     * o bien las contraseñas nuevas no coinciden.
     * @throws java.security.NoSuchAlgorithmException Si no se ha podido encriptar
     * la contraseña debido a que el algoritmo MD5 no está disponible.
     */
    public static byte[] cambiarPassword(
            byte[] actualGuardada,
            char[] actualIngresada,
            char[] nueva,
            char[] nuevaRepetida)
            throws WrongPasswordException, NoSuchAlgorithmException {

        // Establece un vector con 0 elementos a los vectores nulos para
        // comprobaciones posteriores.
        if (actualGuardada == null) {
            actualGuardada = new byte[0];
        }
        if (actualIngresada == null) {
            actualIngresada = new char[0];
        }
        if (nueva == null) {
            nueva = new char[0];
        }
        if (nuevaRepetida == null) {
            nuevaRepetida = new char[0];
        }

        //Verificar que la contraseña actual sea correcta            
        if (actualGuardada.length > 0 && actualIngresada.length > 0) {
            byte[] encrip = PasswordUtil.encriptarPassword(actualIngresada);
            for (int i = 0; i < actualGuardada.length; i++) {
                if (encrip[i] != actualGuardada[i]) {
                    WrongPasswordException e = new WrongPasswordException();
                    e.setDetalle("La contraseña actual no es correcta.");
                    throw e;
                }
            }
        } else {
            if (actualGuardada.length != actualIngresada.length) {
                WrongPasswordException e = new WrongPasswordException();
                e.setDetalle("La contraseña actual no es correcta.");
                throw e;
            }
        }

        //Si las contraseñas nuevas son ambas vacías retornar null
        if (nueva.length == 0 && nuevaRepetida.length == 0) {
            return null;
        }

        //Verificar que las contraseñas nuevas coincidan
        if (nueva.length != nuevaRepetida.length) {
            WrongPasswordException e = new WrongPasswordException();
            e.setDetalle("La contraseña nueva tiene distinta longitud que la repetida.");
            throw e;
        }
        for (int i = 0; i < nueva.length; i++) {
            if (nueva[i] != nuevaRepetida[i]) {
                WrongPasswordException e = new WrongPasswordException();
                e.setDetalle("La contraseña nueva no se ha repetido correctamente.");
                throw e;
            }
        }

        //Devolver la nueva contraseña encriptada.
        byte[] res = PasswordUtil.encriptarPassword(nueva);
        return res;
    }
}
