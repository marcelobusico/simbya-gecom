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

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.excepciones.RegistroFallidoException;
import simbya.framework.interfaces.VentanaInterna;
import simbya.framework.validadores.TextValidator;
import simbya.gecom.entidades.seguridad.CasoDeUso;
import simbya.gecom.entidades.seguridad.TipoUsuarioSistema;
import simbya.gecom.gestores.usuarios.GestorActualizarTiposUsuarioSistemaRemote;

/**
 * Ventana para actualizar los privilegios de los tipos de usuario del sistema.
 * @author Marcelo Busico.
 */
public class VentanaActualizarTiposUsuarioSistema extends VentanaInterna {

    private static final Logger log = Logger.getLogger(
            VentanaActualizarTiposUsuarioSistema.class);
    private GestorActualizarTiposUsuarioSistemaRemote gestor;
    private DefaultListModel modeloDisponibles;
    private DefaultListModel modeloAsignados;

    /** 
     * Crea un nuevo formulario VentanaActualizarTiposUsuarioSistema.
     */
    public VentanaActualizarTiposUsuarioSistema() {
        //Inicializa los componentes de la ventana.
        initComponents();

        //Lógica del Caso de Uso a partir de acá.
        modeloDisponibles = new DefaultListModel();
        modeloAsignados = new DefaultListModel();
        lstPrivilegiosDisponibles.setModel(modeloDisponibles);
        lstPrivilegiosAsignados.setModel(modeloAsignados);
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorActualizarTiposUsuarioSistemaRemote) GestorConexion.getInstancia().getObjetoRemoto(
                GestorActualizarTiposUsuarioSistemaRemote.class);
    }

    /**
     * Inicializa la ventana al estado inicial borrando todos los datos
     * ingresados por el usuario.
     */
    public void inicializarVentana() {
        cboTipoUsuario.setEnabled(true);
        btnNuevo.setEnabled(true);
        btnModificar.setEnabled(false);
        btnBorrar.setEnabled(false);

        btnAgregar.setEnabled(false);
        btnQuitar.setEnabled(false);
        lstPrivilegiosAsignados.setEnabled(false);
        lstPrivilegiosDisponibles.setEnabled(false);
        modeloAsignados.clear();
        modeloDisponibles.clear();
        txtNombre.setText("");
        txtNombre.setEditable(false);
        txtDescripcion.setText("");
        txtDescripcion.setEditable(false);

        btnAceptar.setEnabled(false);

        mostrarTiposUsuarioSistema(gestor.cargarTiposUsuario());
    }

    /**
     * Muestra los tipos de usuario del sistema en el combo de la ventana.
     */
    public void mostrarTiposUsuarioSistema(List<TipoUsuarioSistema> tipos) {
        cboTipoUsuario.removeAllItems();
        if (tipos == null) {
            return;
        }
        ListIterator<TipoUsuarioSistema> li1 = tipos.listIterator();
        TipoUsuarioSistema tus = null;
        while (li1.hasNext()) {
            tus = li1.next();
            cboTipoUsuario.addItem(tus);
        }
        cboTipoUsuario.setSelectedIndex(-1);
    }

    /**
     * Toma la selección del TipoUsuarioSistema elegido por el usuario y se
     * lo informa al gestor, mostrando los datos actuales del tipo de usuario.
     */
    private void tomarSeleccionTipoUsuario(TipoUsuarioSistema tus) {
        gestor.seleccionarTipoUsuario(tus);
        modeloDisponibles.removeAllElements();
        modeloAsignados.removeAllElements();
        if (tus != null) {
            txtNombre.setText(tus.getNombre());
            txtDescripcion.setText(tus.getDescripcion());
            //Mostrar los privilegios asignados.
            for (CasoDeUso cu : tus.getPrivilegiosCUOrdenados()) {
                modeloAsignados.addElement(cu);
            }
            //Mostrar los privilegios disponibles.
            for (CasoDeUso cu : gestor.cargarPrivilegiosDisponibles()) {
                modeloDisponibles.addElement(cu);
            }
        } else {
            txtNombre.setText("");
            txtDescripcion.setText("");
        }
    }

    /**
     * Toma la opción del usuario para registrar un nuevo tipo y se lo
     * informa al gestor.
     */
    private void opcionRegistrarNuevoTipo() {
        gestor.registrarNuevoUsuario();
        limpiarFormularioParaAlta();
        mostrarPrivilegiosDisponibles(gestor.cargarPrivilegiosDisponibles());
    }

    /**
     * Limpia la ventana para realizar un nuevo alta de usuario y ubica
     * el cursor en el cuadro de texto del nombre.
     */
    public void limpiarFormularioParaAlta() {
        cboTipoUsuario.setEnabled(false);
        btnNuevo.setEnabled(false);
        btnBorrar.setEnabled(false);
        btnModificar.setEnabled(false);
        btnAceptar.setEnabled(true);

        lstPrivilegiosAsignados.setEnabled(true);
        modeloAsignados.clear();
        lstPrivilegiosDisponibles.setEnabled(true);
        modeloDisponibles.clear();
        txtNombre.setText("");
        txtNombre.setEditable(true);
        txtDescripcion.setText("");
        txtDescripcion.setEditable(true);
        txtNombre.requestFocus();
    }

    /**
     * Toma la cancelación del usuario.
     */
    private void tomarCancelacion() {
        inicializarVentana();
    }

    /**
     * Muestra los privilegios disponibles en la lista correspondiente y quita
     * los privilegios que a la vez se encuentran en la lista de asignados.
     */
    public void mostrarPrivilegiosDisponibles(List<CasoDeUso> privilegios) {
        modeloDisponibles.clear();
        if (privilegios == null) {
            return;
        }
        for (CasoDeUso cu : privilegios) {
            modeloDisponibles.addElement(cu);
        }
        btnAgregar.setEnabled(true);
        lstPrivilegiosDisponibles.setSelectedIndex(-1);
    }

    /**
     * Toma el nombre del tipo de usuario.
     */
    private void tomarNombre(String nombre) {
        gestor.tomarNombreTipoUsuario(nombre);
    }

    /**
     * Toma la descripción del tipo de usuario.
     */
    private void tomarDescripcion(String desc) {
        gestor.tomarDescripcionTipoUsuario(desc);
    }

    /**
     * Agrega los privilegios seleccionados de la lista de disponibles
     * en la lista de asignados y activa en caso de corresponder el botón
     * de quitar privilegios.
     */
    private void opcionAgregarPrivilegios() {
        Object[] elementos = lstPrivilegiosDisponibles.getSelectedValues();
        if (elementos.length > 0 && !btnQuitar.isEnabled()) {
            btnQuitar.setEnabled(true);
        }
        //Agrega los elementos a la lista de asignados.
        for (int i = 0; i < elementos.length; i++) {
            Object elem = elementos[i];
            modeloAsignados.addElement(elem);
            modeloDisponibles.removeElement(elem);
        }
        if (modeloDisponibles.size() == 0) {
            btnAgregar.setEnabled(false);
        }
    }

    /**
     * Quita de la lista de privilegios asignados a la lista de privilegios
     * disponibles, activando y desactivando los botones de quitar y agregar
     * en el caso que corresponda.
     */
    private void opcionQuitarPrivilegios() {
        Object[] elementos = lstPrivilegiosAsignados.getSelectedValues();
        if (elementos.length > 0 && !btnAgregar.isEnabled()) {
            btnAgregar.setEnabled(true);
        }
        //Agrega los elementos a la lista de disponibles.
        for (int i = 0; i < elementos.length; i++) {
            Object elem = elementos[i];
            modeloDisponibles.addElement(elem);
            modeloAsignados.removeElement(elem);
        }
        if (modeloAsignados.size() == 0) {
            btnQuitar.setEnabled(false);
        }
    }

    /**
     * Toma la confirmación del usuario para dar el alta, modificar o borrar
     * el Tipo de Usuario del Sistema actual.
     */
    private void tomarConfirmacion() {
        //Recorre el modelo de privilegios asignados y los agrega al set
        Set<CasoDeUso> privAsig = new HashSet();
        for (int i = 0; i < modeloAsignados.getSize(); i++) {
            CasoDeUso elem = (CasoDeUso) modeloAsignados.get(i);
            privAsig.add(elem);
        }
        //Pasar privilegios asignados al gestor.
        gestor.tomarPrivilegiosAsignados(privAsig);
        //Confirma la actualización al gestor.
        try {
            if (gestor.confirmarActualizacion()) {
                if (gestor.isModificacion()) {
                    // MODIFICACION
                    informarModificacionExitosa(gestor.getTipoUsuarioSistema().getNombre());
                } else {
                    if (gestor.isBaja()) {
                        // BAJA
                        informarBajaExitosa(gestor.getTipoUsuarioSistema().getNombre());
                    } else {
                        // ALTA
                        informarRegistro(gestor.getTipoUsuarioSistema().getNombre());
                    }
                }
            }
        } catch (RegistroFallidoException e) {
            log.error("Error al actualizar tipo de usuario del sistema.", e);
            JOptionPane.showMessageDialog(this,
                    e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Informa al usuario que hay datos requeridos faltantes.
     * @param desc Mensaje a mostrar al usuario.
     */
    public void informarDatosFaltantes(String desc) {
        JOptionPane.showMessageDialog(this, desc,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Informa al usuario un error de baja del tipo de usuario.
     * @param desc Mensaje a mostrar al usuario.
     */
    public void informarErrorBaja(String desc) {
        JOptionPane.showMessageDialog(this, desc,
                "Error", JOptionPane.ERROR_MESSAGE);
        tomarCancelacion();
    }

    /**
     * Informa al usuario la modificación exitosa del tipo de usuario.
     * @param nombre Nombre del Tipo de Usuario.
     */
    public void informarModificacionExitosa(String nombre) {
        JOptionPane.showMessageDialog(this,
                "Se han modificado con éxito los datos del Tipo de Usuario:\n " +
                nombre + "\nLos cambios se verán reflejados cuando los usuarios\n" +
                "vuelvan a iniciar sesión en el sistema.",
                "Información", JOptionPane.INFORMATION_MESSAGE);
        inicializarVentana();
    }

    /**
     * Informa al usuario la baja exitosa del tipo de usuario.
     * @param nombre Nombre del Tipo de Usuario.
     */
    public void informarBajaExitosa(String nombre) {
        JOptionPane.showMessageDialog(this,
                "Se ha registrado con éxito la BAJA del Tipo de Usuario:\n " +
                nombre, "Información", JOptionPane.INFORMATION_MESSAGE);
        inicializarVentana();
    }

    /**
     * Informa al usuario la alta exitosa del tipo de usuario.
     * @param nombre Nombre del Tipo de Usuario.
     */
    public void informarRegistro(String nombre) {
        JOptionPane.showMessageDialog(this,
                "Se ha registrado con éxito al nuevo Tipo de Usuario:\n " +
                nombre, "Información", JOptionPane.INFORMATION_MESSAGE);
        inicializarVentana();
    }

    /**
     * Toma la opción del usuario para modificar un tipo de usuario y se lo
     * informa al gestor.
     */
    private void opcionModificarTipoUsuario() {
        cboTipoUsuario.setEnabled(false);
        btnNuevo.setEnabled(false);
        btnBorrar.setEnabled(false);
        btnModificar.setEnabled(false);
        btnAceptar.setEnabled(true);

        lstPrivilegiosAsignados.setEnabled(true);
        lstPrivilegiosDisponibles.setEnabled(true);
        txtNombre.setEditable(true);
        txtDescripcion.setEditable(true);
        txtNombre.requestFocus();
        if (!modeloDisponibles.isEmpty()) {
            btnAgregar.setEnabled(true);
        }
        if (!modeloAsignados.isEmpty()) {
            btnQuitar.setEnabled(true);
        }
    }

    /**
     * Toma la opción del usuario para borrar un tipo de usuario y se lo
     * informa al gestor.
     */
    private void opcionBorrarTipoUsuario() {
        int res = JOptionPane.showConfirmDialog(this, "Se dará de baja al Tipo de Usuario seleccionado.\n" + "¿Desea continuar?", "Confirmación de Usuario", JOptionPane.YES_NO_OPTION);

        //Si el usuario cancela la baja salir.
        if (res == JOptionPane.NO_OPTION) {
            return;
        }

        try {
            gestor.borrarTipo();
            informarBajaExitosa(gestor.getTipoUsuarioSistema().getNombre());
        } catch (RegistroFallidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            tomarCancelacion();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        fraTipoUsuario = new javax.swing.JPanel();
        cboTipoUsuario = new javax.swing.JComboBox();
        btnModificar = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnBorrar = new javax.swing.JButton();
        panelDesplazamiento1 = new javax.swing.JScrollPane();
        lstPrivilegiosAsignados = new javax.swing.JList();
        lblPrivilegiosAsignados = new javax.swing.JLabel();
        lblPrivilegiosDisponibles = new javax.swing.JLabel();
        panelDesplazamiento2 = new javax.swing.JScrollPane();
        lstPrivilegiosDisponibles = new javax.swing.JList();
        btnAgregar = new javax.swing.JButton();
        btnQuitar = new javax.swing.JButton();
        btnAceptar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        txtNombre = new javax.swing.JTextField();
        lblNombre = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        lblDescripcion = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Actualizar Tipos de Usuario del Sistema...");
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/user-multiple.png"))); // NOI18N
        setMinimumSize(new java.awt.Dimension(715, 475));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaActualizarTiposUsuarioSistema.class);
        fraTipoUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Tipo de Usuario del Sistema:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N

        cboTipoUsuario.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboTipoUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTipoUsuarioActionPerformed(evt);
            }
        });

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

        btnNuevo.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/add.gif"))); // NOI18N
        btnNuevo.setMnemonic('n');
        btnNuevo.setText("Nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnBorrar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnBorrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/subtract.gif"))); // NOI18N
        btnBorrar.setMnemonic('b');
        btnBorrar.setText("Borrar");
        btnBorrar.setEnabled(false);
        btnBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBorrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fraTipoUsuarioLayout = new javax.swing.GroupLayout(fraTipoUsuario);
        fraTipoUsuario.setLayout(fraTipoUsuarioLayout);
        fraTipoUsuarioLayout.setHorizontalGroup(
            fraTipoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fraTipoUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fraTipoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cboTipoUsuario, javax.swing.GroupLayout.Alignment.LEADING, 0, 645, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, fraTipoUsuarioLayout.createSequentialGroup()
                        .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnModificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBorrar)))
                .addContainerGap())
        );

        fraTipoUsuarioLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnBorrar, btnModificar, btnNuevo});

        fraTipoUsuarioLayout.setVerticalGroup(
            fraTipoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fraTipoUsuarioLayout.createSequentialGroup()
                .addComponent(cboTipoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(fraTipoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(btnNuevo)
                    .addComponent(btnModificar)
                    .addComponent(btnBorrar))
                .addGap(14, 14, 14))
        );

        lstPrivilegiosAsignados.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lstPrivilegiosAsignados.setEnabled(false);
        panelDesplazamiento1.setViewportView(lstPrivilegiosAsignados);

        lblPrivilegiosAsignados.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblPrivilegiosAsignados.setText("Privilegios Asignados:");

        lblPrivilegiosDisponibles.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblPrivilegiosDisponibles.setText("Privilegios Disponibles:");

        lstPrivilegiosDisponibles.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lstPrivilegiosDisponibles.setEnabled(false);
        panelDesplazamiento2.setViewportView(lstPrivilegiosDisponibles);

        btnAgregar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnAgregar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/arrow-single-left-green.png"))); // NOI18N
        btnAgregar.setToolTipText("Agrega los privilegios de usuarios seleccionados.");
        btnAgregar.setEnabled(false);
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });

        btnQuitar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnQuitar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/arrow-single-right-green.png"))); // NOI18N
        btnQuitar.setToolTipText("Quita los privilegios de usuarios seleccionados.");
        btnQuitar.setEnabled(false);
        btnQuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarActionPerformed(evt);
            }
        });

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

        txtNombre.setEditable(false);
        txtNombre.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtNombre.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNombreFocusLost(evt);
            }
        });

        lblNombre.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblNombre.setText("Nombre del Tipo de Usuario: ");

        txtDescripcion.setEditable(false);
        txtDescripcion.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtDescripcion.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtDescripcionFocusLost(evt);
            }
        });

        lblDescripcion.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblDescripcion.setText("Descripción:");

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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fraTipoUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPrivilegiosAsignados)
                            .addComponent(panelDesplazamiento1, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnQuitar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnAgregar))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPrivilegiosDisponibles)
                            .addComponent(panelDesplazamiento2, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCerrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 354, Short.MAX_VALUE)
                        .addComponent(btnAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblDescripcion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDescripcion, javax.swing.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblNombre)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fraTipoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombre)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDescripcion)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPrivilegiosAsignados)
                            .addComponent(lblPrivilegiosDisponibles))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelDesplazamiento1, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                            .addComponent(panelDesplazamiento2, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCerrar)
                            .addComponent(btnCancelar)
                            .addComponent(btnAceptar)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addComponent(btnAgregar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitar)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void cboTipoUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTipoUsuarioActionPerformed
        if (cboTipoUsuario.getSelectedIndex() > -1) {
            btnModificar.setEnabled(true);
            btnBorrar.setEnabled(true);
            try {
                Object seleccion = cboTipoUsuario.getSelectedItem();
                TipoUsuarioSistema tus = ((TipoUsuarioSistema) seleccion);
                tomarSeleccionTipoUsuario(tus);
            } catch (ClassCastException e) {
                log.warn("Error al seleccionar tipo de usuario.", e);
            }
        } else {
            tomarSeleccionTipoUsuario(null);
            btnModificar.setEnabled(false);
            btnBorrar.setEnabled(false);
        }
    }//GEN-LAST:event_cboTipoUsuarioActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        opcionRegistrarNuevoTipo();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        tomarCancelacion();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void txtNombreFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNombreFocusLost
        tomarNombre(TextValidator.validarTexto(this, txtNombre, 4, 50));
    }//GEN-LAST:event_txtNombreFocusLost

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        opcionAgregarPrivilegios();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        opcionQuitarPrivilegios();
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void txtDescripcionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDescripcionFocusLost
        tomarDescripcion(TextValidator.validarTexto(this, txtDescripcion, 0, 200));
    }//GEN-LAST:event_txtDescripcionFocusLost

    private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
        tomarConfirmacion();
    }//GEN-LAST:event_btnAceptarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        opcionModificarTipoUsuario();
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBorrarActionPerformed
        opcionBorrarTipoUsuario();
    }//GEN-LAST:event_btnBorrarActionPerformed

private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
    dispose();
}//GEN-LAST:event_btnCerrarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBorrar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JComboBox cboTipoUsuario;
    private javax.swing.JPanel fraTipoUsuario;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblPrivilegiosAsignados;
    private javax.swing.JLabel lblPrivilegiosDisponibles;
    private javax.swing.JList lstPrivilegiosAsignados;
    private javax.swing.JList lstPrivilegiosDisponibles;
    private javax.swing.JScrollPane panelDesplazamiento1;
    private javax.swing.JScrollPane panelDesplazamiento2;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables
}
