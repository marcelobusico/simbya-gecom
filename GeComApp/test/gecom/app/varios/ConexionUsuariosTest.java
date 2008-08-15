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
package gecom.app.varios;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.appserver.ServerConf;
import simbya.gecom.entidades.seguridad.TipoUsuarioSistema;
import simbya.gecom.entidades.seguridad.UsuarioSistema;
import simbya.gecom.gestores.usuarios.GestorActualizarUsuarioSistemaRemote;
import static org.junit.Assert.*;

/**
 *
 * @author Marcelo Busico.
 */
public class ConexionUsuariosTest {

    public ConexionUsuariosTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConectarYTraerUsuarios() {
        GestorActualizarUsuarioSistemaRemote gestor = null;
        try {
            ServerConf conf;
            conf = ServerConf.cargarDeArchivo(ServerConf.nombreArchivo);
            GestorConexion.conectar(conf.getDireccion(), conf.getPuerto());
            //Asocia la ventana con el gestor
            gestor = (GestorActualizarUsuarioSistemaRemote) GestorConexion.getInstancia().getObjetoRemoto(
                    GestorActualizarUsuarioSistemaRemote.class);
        } catch (Exception ex) {
            String mensaje = "No se pudo conectar con el gestor remoto.";
            fail(mensaje);
        }
        List<TipoUsuarioSistema> lista = gestor.cargarTiposUsuario();
        TipoUsuarioSistema tus = null;
        for (TipoUsuarioSistema tipoUsuarioSistema : lista) {
            tus = tipoUsuarioSistema;
            List<UsuarioSistema> usuarios = gestor.seleccionarTipoUsuario(tus.getOid());
            for (UsuarioSistema user : usuarios) {
                System.out.println(user);
                System.out.println(user.getTipo());
            }
        }
    }
}