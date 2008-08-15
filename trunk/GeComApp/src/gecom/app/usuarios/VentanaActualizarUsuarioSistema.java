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
package gecom.app.usuarios;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ListIterator;
import javax.naming.NamingException;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.excepciones.RegistroFallidoException;
import simbya.framework.interfaces.VentanaInterna;
import simbya.framework.password.WrongPasswordException;
import simbya.framework.validadores.TextValidator;
import simbya.gecom.entidades.seguridad.TipoUsuarioSistema;
import simbya.gecom.entidades.seguridad.UsuarioSistema;
import simbya.gecom.gestores.usuarios.GestorActualizarUsuarioSistemaRemote;

/**
 * Ventana para actualizar usuarios del sistema.
 * @author Marcelo Busico
 */
public class VentanaActualizarUsuarioSistema extends VentanaInterna {

    private static final Logger log = Logger.getLogger(
            VentanaActualizarUsuarioSistema.class);
    private GestorActualizarUsuarioSistemaRemote gestor;
    private TipoUsuarioSistema tipoUsuario = null;
    private UsuarioSistema usuario = null;
    private String nombreUsuario;
    private char[] password1;
    private char[] password2;
    private boolean cambiarPassword = false;

    /** 
     * Crea un nuevo formulario VentanaActualizarUsuarioSistema.
     */
    public VentanaActualizarUsuarioSistema() {
        //Inicializa los componentes de la ventana.
        initComponents();
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorActualizarUsuarioSistemaRemote) GestorConexion.getInstancia().getObjetoRemoto(
                GestorActualizarUsuarioSistemaRemote.class);
    }

    /**
     * Inicializa la ventana del CU.
     */
    public void inicializarVentana() {
        usuario = null;
        tipoUsuario = null;
        nombreUsuario = null;
        password1 = null;
        password2 = null;
        cambiarPassword = false;

        txtPassword1.setEditable(false);
        txtPassword2.setEditable(false);
        txtPassword1.setText("");
        txtPassword2.setText("");
        txtNombreUsuario.setText("");
        txtNombreUsuario.setEditable(false);
        btnNuevo.setEnabled(false);
        btnModificar.setEnabled(false);
        btnBaja.setEnabled(false);
        btnReestablecer.setEnabled(false);
        cboUsuarios.setEnabled(true);
        cboTiposUsuario.setEnabled(true);
        btnAceptar.setEnabled(false);
        btnCancelar.setEnabled(false);

        mostrarTiposUsuario(gestor.cargarTiposUsuario());
    }

    /**
     * Muestra en la lista de Tipos de Usuario del Sistema los tipos de la 
     * lista pasada por parámetro.
     * @param tipos Lista con objetos TipoUsuarioSistema a mostrar, que debería
     * ser obtenida del gestor del CU.
     */
    public void mostrarTiposUsuario(List<TipoUsuarioSistema> tipos) {
        cboTiposUsuario.removeAllItems();
        cboUsuarios.removeAllItems();
        if (tipos == null) {
            return;
        }
        ListIterator<TipoUsuarioSistema> li1 = tipos.listIterator();
        TipoUsuarioSistema tu = null;
        while (li1.hasNext()) {
            tu = li1.next();
            cboTiposUsuario.addItem(tu);
        }
        cboTiposUsuario.setSelectedIndex(-1);
    }

    /**
     * Muestra en la lista de Usuarios del Sistema los usuarios de la 
     * lista pasada por parámetro.
     * @param usuarios Lista con objetos UsuarioSistema a mostrar.
     */
    public void mostrarUsuarios(List<UsuarioSistema> usuarios) {
        cboUsuarios.removeAllItems();
        if (usuarios == null) {
            return;
        }
        ListIterator<UsuarioSistema> li1 = usuarios.listIterator();
        UsuarioSistema actual = null;
        while (li1.hasNext()) {
            actual = li1.next();
            cboUsuarios.addItem(actual);
        }
        cboUsuarios.setSelectedIndex(-1);
        txtNombreUsuario.setText("");
    }

    public void tomarSeleccionTipoUsuario(TipoUsuarioSistema tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
        if (tipoUsuario != null) {
            mostrarUsuarios(gestor.seleccionarTipoUsuario(tipoUsuario.getOid()));
            btnNuevo.setEnabled(true);
        } else {
            mostrarUsuarios(null);
            btnNuevo.setEnabled(false);
        }
    }

    public void tomarSeleccionUsuario(UsuarioSistema usuario) {
        this.usuario = usuario;
        password1 = null;
        password2 = null;
        if (usuario == null) {
            nombreUsuario = null;
        } else {
            nombreUsuario = usuario.getNombreUsuario();
        }
        gestor.seleccionarUsuario(usuario);
        txtNombreUsuario.setText(nombreUsuario);
        txtPassword1.setText(null);
        txtPassword2.setText(null);
    }

    public void tomarCancelacion() {
        inicializarVentana();
    }

    public void opcionBajaUsuario() {
        int res = JOptionPane.showConfirmDialog(this,
                "Se dará de baja al Usuario del Sistema seleccionado.\n" +
                "¿Desea continuar?", "Confirmación de Usuario",
                JOptionPane.YES_NO_OPTION);

        //Si el usuario cancela la baja salir.
        if (res == JOptionPane.NO_OPTION) {
            return;
        }

        cboUsuarios.setEnabled(false);
        cboTiposUsuario.setEnabled(false);
        btnNuevo.setEnabled(false);
        btnModificar.setEnabled(false);
        btnBaja.setEnabled(false);
        try {
            gestor.bajaDeUsuario();
            informarBajaExitosa(nombreUsuario);
        } catch (RegistroFallidoException rfe) {
            informarErrorBajaUsuario(rfe.getMessage());
        }

    }

    public void informarErrorBajaUsuario(String desc) {
        JOptionPane.showMessageDialog(this, desc, "Error",
                JOptionPane.ERROR_MESSAGE);
        tomarCancelacion();
    }

    public void informarModificacionExitosa(String nombrePersona) {
        JOptionPane.showMessageDialog(this,
                "Se han modificado con éxito los datos de Usuario de:\n    " +
                nombrePersona, "Información", JOptionPane.INFORMATION_MESSAGE);
        inicializarVentana();
    }

    public void informarBajaExitosa(String nombrePersona) {
        JOptionPane.showMessageDialog(this,
                "Se ha registrado con éxito la BAJA del Usuario de:\n   " +
                nombrePersona, "Información", JOptionPane.INFORMATION_MESSAGE);
        inicializarVentana();
    }

    public void informarRegistro(String nombreUsuario) {
        JOptionPane.showMessageDialog(this,
                "Se ha registrado con éxito al nuevo Usuario:\n   " +
                nombreUsuario, "Información", JOptionPane.INFORMATION_MESSAGE);
        inicializarVentana();
    }

    public void informarDatosFaltantes(String desc) {
        JOptionPane.showMessageDialog(this, desc, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void opcionRegistrarNuevoUsuario() {
        gestor.registrarNuevoUsuario();
        limpiarFormularioParaAlta();
    }

    public void limpiarFormularioParaAlta() {
        cboUsuarios.setSelectedIndex(-1);
        cboUsuarios.setEnabled(false);
        cboTiposUsuario.setEnabled(false);
        nombreUsuario = null;
        password1 = null;
        password2 = null;
        cambiarPassword = true;

        btnNuevo.setEnabled(false);
        btnBaja.setEnabled(false);
        btnAceptar.setEnabled(true);
        btnCancelar.setEnabled(true);
        btnReestablecer.setEnabled(false);
        txtNombreUsuario.setText(null);
        txtPassword1.setText(null);
        txtPassword2.setText(null);
        txtNombreUsuario.setEditable(true);
        txtPassword1.setEditable(true);
        txtPassword2.setEditable(true);
        txtNombreUsuario.requestFocus();
    }

    /**
     * Toma la confirmación del usuario.
     */
    public void tomarConfirmacion() {
        //Toma el nombre de usuario.
        gestor.tomarNombreUsuario(nombreUsuario);
        //Toma las cotraseñas ingresadas y las verifica.
        if (cambiarPassword == true) {
            //Verifica que se haya ingresado alguna contraseña.
            if (password1.length == 0 && password2.length == 0) {
                JOptionPane.showMessageDialog(this,
                        "Debe ingresar una contraseña no nula.",
                        "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                gestor.tomarContrasenia(password1, password2);
            } catch (WrongPasswordException e) {
                JOptionPane.showMessageDialog(this,
                        "Las contraseñas ingresadas no son iguales.",
                        "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            } catch (NoSuchAlgorithmException e) {
                String msj = "No se ha encontrado el algoritmo de encriptación" +
                        "MD5 en el sistema.";
                //Registrar el error en el archivo.
                log.error(msj, e);
                JOptionPane.showMessageDialog(this, msj + "\n Detalles:\n" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        //Tomar la actualización
        try {
            if (!gestor.verificarDatos()) {
                informarDatosFaltantes(
                        "Verifique los datos y complete los datos faltante y/o erroneos.");
                return;
            }
            gestor.confirmarActualizacion();
            if (gestor.isModificacion()) {
                //MODIFICACION
                informarModificacionExitosa(nombreUsuario);
                return;
            }
            if (!gestor.isBaja()) {
                //ALTA
                informarRegistro(nombreUsuario);
                return;
            }
        } catch (RegistroFallidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void tomarNombreUsuario(String nombre) {
        this.nombreUsuario = nombre;
    }

    public void tomarPassword1(char[] password) {
        this.password1 = password;
    }

    public void tomarPassword2(char[] password) {
        this.password2 = password;
    }

    public void opcionModificarUsuario() {
        cambiarPassword = false;

        btnNuevo.setEnabled(false);
        btnBaja.setEnabled(false);
        btnAceptar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnReestablecer.setEnabled(true);
        btnCancelar.setEnabled(true);
        cboTiposUsuario.setEnabled(false);
        cboUsuarios.setEnabled(false);
        txtNombreUsuario.setEditable(true);
    }

    public void opcionReestablecerContrasenia() {
        cambiarPassword = true;

        btnReestablecer.setEnabled(false);
        txtPassword1.setText(null);
        txtPassword2.setText(null);
        txtPassword1.setEditable(true);
        txtPassword2.setEditable(true);
        txtPassword1.requestFocus();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fraUsuario = new javax.swing.JPanel();
        cboTiposUsuario = new javax.swing.JComboBox();
        lblSeleccionTipoUsuario = new javax.swing.JLabel();
        btnModificar = new javax.swing.JButton();
        cboUsuarios = new javax.swing.JComboBox();
        lblSeleccionUsuario = new javax.swing.JLabel();
        btnNuevo = new javax.swing.JButton();
        btnBaja = new javax.swing.JButton();
        btnAceptar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        fraDatosUsuario = new javax.swing.JPanel();
        lblNombreUsuario = new javax.swing.JLabel();
        txtNombreUsuario = new javax.swing.JTextField();
        lblPassword1 = new javax.swing.JLabel();
        btnReestablecer = new javax.swing.JButton();
        txtPassword1 = new javax.swing.JPasswordField();
        txtPassword2 = new javax.swing.JPasswordField();
        lblPassword2 = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Actualizar Usuario del Sistema...");
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/user-plain-green.png"))); // NOI18N
        setMinimumSize(new java.awt.Dimension(582, 401));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaActualizarUsuarioSistema.class);
        fraUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Usuario del Sistema:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.text"))); // NOI18N
        fraUsuario.setPreferredSize(new java.awt.Dimension(0, 0));

        cboTiposUsuario.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboTiposUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTiposUsuarioActionPerformed(evt);
            }
        });

        lblSeleccionTipoUsuario.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblSeleccionTipoUsuario.setText("Seleccione el tipo de usuario del sistema:");

        btnModificar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnModificar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/tools.gif"))); // NOI18N
        btnModificar.setMnemonic('m');
        btnModificar.setText("Modificar");
        btnModificar.setEnabled(false);
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });

        cboUsuarios.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboUsuariosActionPerformed(evt);
            }
        });

        lblSeleccionUsuario.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblSeleccionUsuario.setText("Seleccione el usuario del sistema:");

        btnNuevo.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/add.gif"))); // NOI18N
        btnNuevo.setMnemonic('n');
        btnNuevo.setText("Nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnBaja.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnBaja.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/subtract.gif"))); // NOI18N
        btnBaja.setMnemonic('b');
        btnBaja.setText("Quitar");
        btnBaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBajaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fraUsuarioLayout = new javax.swing.GroupLayout(fraUsuario);
        fraUsuario.setLayout(fraUsuarioLayout);
        fraUsuarioLayout.setHorizontalGroup(
            fraUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fraUsuarioLayout.createSequentialGroup()
                .addGroup(fraUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, fraUsuarioLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cboUsuarios, 0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, fraUsuarioLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cboTiposUsuario, 0, 512, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, fraUsuarioLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(lblSeleccionUsuario))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, fraUsuarioLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(lblSeleccionTipoUsuario))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, fraUsuarioLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnNuevo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnModificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBaja)))
                .addContainerGap())
        );

        fraUsuarioLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnBaja, btnModificar, btnNuevo});

        fraUsuarioLayout.setVerticalGroup(
            fraUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fraUsuarioLayout.createSequentialGroup()
                .addComponent(lblSeleccionTipoUsuario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cboTiposUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSeleccionUsuario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cboUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fraUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevo)
                    .addComponent(btnModificar)
                    .addComponent(btnBaja))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnAceptar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnAceptar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/accept.png"))); // NOI18N
        btnAceptar.setMnemonic('a');
        btnAceptar.setText("Aceptar");
        btnAceptar.setEnabled(false);
        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarActionPerformed(evt);
            }
        });

        btnCancelar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/cancel.png"))); // NOI18N
        btnCancelar.setMnemonic('c');
        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        fraDatosUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Datos del Usuario:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.text"))); // NOI18N

        lblNombreUsuario.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblNombreUsuario.setText("Nombre de usuario:");

        txtNombreUsuario.setEditable(false);
        txtNombreUsuario.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtNombreUsuario.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNombreUsuarioFocusLost(evt);
            }
        });

        lblPassword1.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblPassword1.setText("Nueva contraseña:");

        btnReestablecer.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnReestablecer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/undo.gif"))); // NOI18N
        btnReestablecer.setMnemonic('r');
        btnReestablecer.setText("Restablecer");
        btnReestablecer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReestablecerActionPerformed(evt);
            }
        });

        txtPassword1.setEditable(false);
        txtPassword1.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtPassword1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPassword1FocusLost(evt);
            }
        });

        txtPassword2.setEditable(false);
        txtPassword2.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtPassword2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPassword2FocusLost(evt);
            }
        });

        lblPassword2.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblPassword2.setText("Repetir contraseña:");

        javax.swing.GroupLayout fraDatosUsuarioLayout = new javax.swing.GroupLayout(fraDatosUsuario);
        fraDatosUsuario.setLayout(fraDatosUsuarioLayout);
        fraDatosUsuarioLayout.setHorizontalGroup(
            fraDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fraDatosUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fraDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblNombreUsuario)
                    .addComponent(lblPassword1)
                    .addComponent(lblPassword2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fraDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtPassword2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addComponent(txtPassword1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addComponent(txtNombreUsuario, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReestablecer)
                .addContainerGap())
        );
        fraDatosUsuarioLayout.setVerticalGroup(
            fraDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fraDatosUsuarioLayout.createSequentialGroup()
                .addGroup(fraDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombreUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNombreUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fraDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword1)
                    .addComponent(btnReestablecer)
                    .addComponent(txtPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fraDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPassword2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnCerrar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/close.gif"))); // NOI18N
        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(fraUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(fraDatosUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(btnCerrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 221, Short.MAX_VALUE)
                        .addComponent(btnAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fraUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fraDatosUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCerrar)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnAceptar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCancelar)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void btnReestablecerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReestablecerActionPerformed
        opcionReestablecerContrasenia();
    }//GEN-LAST:event_btnReestablecerActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        dispose();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void txtNombreUsuarioFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNombreUsuarioFocusLost
        tomarNombreUsuario(TextValidator.validarTexto(this, txtNombreUsuario, 4, 25));
    }//GEN-LAST:event_txtNombreUsuarioFocusLost

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        tomarCancelacion();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
        tomarConfirmacion();
    }//GEN-LAST:event_btnAceptarActionPerformed

    private void cboUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboUsuariosActionPerformed
        if (cboUsuarios.getSelectedIndex() > -1) {
            btnModificar.setEnabled(true);
            btnBaja.setEnabled(true);
            try {
                Object seleccion = cboUsuarios.getSelectedItem();
                UsuarioSistema user = ((UsuarioSistema) seleccion);
                tomarSeleccionUsuario(user);
            } catch (ClassCastException e) {
                log.warn("Error al seleccionar usuario del sistema.", e);
            }
        } else {
            btnModificar.setEnabled(false);
            btnBaja.setEnabled(false);
        }
}//GEN-LAST:event_cboUsuariosActionPerformed

    private void btnBajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBajaActionPerformed
        opcionBajaUsuario();
    }//GEN-LAST:event_btnBajaActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        opcionRegistrarNuevoUsuario();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        opcionModificarUsuario();
    }//GEN-LAST:event_btnModificarActionPerformed

    private void txtPassword1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPassword1FocusLost
        tomarPassword1(txtPassword1.getPassword());
    }//GEN-LAST:event_txtPassword1FocusLost

    private void txtPassword2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPassword2FocusLost
        tomarPassword2(txtPassword2.getPassword());
    }//GEN-LAST:event_txtPassword2FocusLost

    private void cboTiposUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTiposUsuarioActionPerformed
        if (cboTiposUsuario.getSelectedIndex() > -1) {
            try {
                Object seleccion = cboTiposUsuario.getSelectedItem();
                TipoUsuarioSistema tipo = ((TipoUsuarioSistema) seleccion);
                tomarSeleccionTipoUsuario(tipo);
            } catch (ClassCastException e) {
                log.warn("Error al seleccionar tipo de usuario del sistema.", e);
            }
        } else {
            tomarSeleccionTipoUsuario(null);
        }
    }//GEN-LAST:event_cboTiposUsuarioActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnBaja;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnReestablecer;
    private javax.swing.JComboBox cboTiposUsuario;
    private javax.swing.JComboBox cboUsuarios;
    private javax.swing.JPanel fraDatosUsuario;
    private javax.swing.JPanel fraUsuario;
    private javax.swing.JLabel lblNombreUsuario;
    private javax.swing.JLabel lblPassword1;
    private javax.swing.JLabel lblPassword2;
    private javax.swing.JLabel lblSeleccionTipoUsuario;
    private javax.swing.JLabel lblSeleccionUsuario;
    private javax.swing.JTextField txtNombreUsuario;
    private javax.swing.JPasswordField txtPassword1;
    private javax.swing.JPasswordField txtPassword2;
    // End of variables declaration//GEN-END:variables
}
