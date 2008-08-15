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
package gecom.app.tipos;

import java.util.List;
import java.util.ListIterator;
import javax.naming.NamingException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.gecom.gestores.tipos.GestorActualizarTiposRemote;
import simbya.gecom.interfaces.InterfazTipoGenerico;

/**
 * Ventana para actualizar tipos genéricos.
 * @author Marcelo Busico.
 */
public class VentanaActualizarTipos extends javax.swing.JInternalFrame {

    private static final Logger log = Logger.getLogger(
            VentanaActualizarTipos.class);
    private InterfazTipoGenerico itemSeleccionado;
    private String estadoABM = "normal";
    private GestorActualizarTiposRemote gestor;

    /** 
     * Crea un nuevo formulario VentanaActualizarTipos.
     */
    public VentanaActualizarTipos() {
        //Inicializa los componentes de la ventana.
        initComponents();
        try {
            //Asocia la ventana con el gestor
            gestor = (GestorActualizarTiposRemote) GestorConexion.getInstancia().getObjetoRemoto(
                    GestorActualizarTiposRemote.class);
        } catch (NamingException ex) {
            String mensaje = "No se pudo conectar con el gestor remoto.";
            log.error(mensaje, ex);
            JOptionPane.showMessageDialog(this, mensaje + "\n" + ex.getMessage());
        }
        initTableListener();
    }

    /**
     * Inicializa el listener que notifica de cambios 
     * en la selección de la tabla.
     */
    private void initTableListener() {
        tblTipos.getSelectionModel().addListSelectionListener(
                new javax.swing.event.ListSelectionListener() {

                    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                        tblTiposValueChanged(e);
                    }
                });
    }

    private void tblTiposValueChanged(javax.swing.event.ListSelectionEvent evt) {
        if (tblTipos.getSelectedRow() > -1) {
            itemSeleccionado = (InterfazTipoGenerico) tblTipos.getValueAt(tblTipos.getSelectedRow(), 0);
            txtNombre.setText(itemSeleccionado.getNombre());
            txtDesc.setText(itemSeleccionado.getDescripcion());
        } else {
            itemSeleccionado = null;
            txtNombre.setText(null);
            txtDesc.setText(null);
        }
    }

    public void opcionActualizarTipos(String tituloVentana, InterfazTipoGenerico tg) {
        gestor.setTipoGenerico(tg);
        this.setTitle("Actualizar " + tituloVentana + "...");
        mostrarTipos(gestor.cargarTipos());
    }

    /**
     * Cuando se selecciona el modo de modifcacion se habilitan los Jtext para
     * poder recibir teclazos, y se deshablilitan los botones Modificar, Borrar
     * y la grilla. Por ultimo el boton Nuevo cambia su texto a Guardar.
     */
    public void opcionModificar() {
        itemSeleccionado = (InterfazTipoGenerico) tblTipos.getModel().getValueAt(tblTipos.getSelectedRow(), 0);
        btnBorrar.setEnabled(false);
        txtNombre.setEditable(true);
        txtDesc.setEditable(true);
        btnNuevoGuardar.setText("Guardar");
        txtNombre.setText(itemSeleccionado.getNombre());
        txtDesc.setText(itemSeleccionado.getDescripcion());
        estadoABM = "modificado";
        btnModificar.setText("Cancelar");
    }

    public void tomarConfirmacionModificar() {
        itemSeleccionado.setNombre(txtNombre.getText());
        itemSeleccionado.setDescripcion(txtDesc.getText());

        if (gestor.verificarDatos(itemSeleccionado.getNombre(), itemSeleccionado.getDescripcion())) {
            gestor.modificarTipo(itemSeleccionado);
            JOptionPane.showMessageDialog(this, "Modificación exitosa", "Información", JOptionPane.INFORMATION_MESSAGE);
            this.opcionReestablecerVentana();
        } else {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Error de Validación", JOptionPane.INFORMATION_MESSAGE);
            txtNombre.requestFocusInWindow();
        }
    }

    /**
     * Se encarga de cargar una lista en un objeto table de la interfaz grafica de usuario
     */
    private void mostrarTipos(List<InterfazTipoGenerico> tipos) {
        DefaultTableModel modelo = (DefaultTableModel) tblTipos.getModel();
        ListIterator<InterfazTipoGenerico> li = tipos.listIterator();
        modelo.setRowCount(0);
        txtNombre.setText(null);
        txtDesc.setText(null);
        while (li.hasNext()) {
            InterfazTipoGenerico tipoGenerico = li.next();
            Object[] filaNueva = new Object[2];
            filaNueva[0] = tipoGenerico;
            filaNueva[1] = tipoGenerico.getDescripcion();
            modelo.addRow(filaNueva);
        }
    }

    private void opcionNuevoTipo() {
        txtNombre.setText("");
        txtDesc.setText("");
        txtNombre.setEditable(true);
        txtDesc.setEditable(true);
        tblTipos.setEnabled(false);
        btnBorrar.setEnabled(false);
        btnModificar.setText("Cancelar");
        btnNuevoGuardar.setText("Guardar");
        estadoABM = "nuevo";
    }

    private void tomarConfirmacionNuevo() {
        if (gestor.verificarDatos(txtNombre.getText(), txtDesc.getText())) {
            gestor.registrarTipo();
            JOptionPane.showMessageDialog(this, "Registro exitoso.", "Información", JOptionPane.INFORMATION_MESSAGE);
            this.opcionReestablecerVentana();
        } else {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Error de Validación", JOptionPane.INFORMATION_MESSAGE);
            txtNombre.requestFocusInWindow();
        }
    }

    /**
     * Reestablcer toda la interfaz.
     */
    private void opcionReestablecerVentana() {
        txtNombre.setEditable(false);
        txtDesc.setEditable(false);
        btnBorrar.setEnabled(true);
        btnModificar.setEnabled(true);
        btnModificar.setText("Modificar");
        btnNuevoGuardar.setEnabled(true);
        btnNuevoGuardar.setText("Nuevo");
        tblTipos.setEnabled(true);
        estadoABM = "normal";
        mostrarTipos(gestor.cargarTipos());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtDesc = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblTipos = new javax.swing.JTable();
        btnBorrar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnNuevoGuardar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/database.png"))); // NOI18N
        setMinimumSize(new java.awt.Dimension(515, 355));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaActualizarTipos.class);
        jLabel1.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Nombre:");

        jLabel2.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Descripción:");

        txtDesc.setEditable(false);
        txtDesc.setFont(resourceMap.getFont("Application.font.text")); // NOI18N

        txtNombre.setEditable(false);
        txtNombre.setFont(resourceMap.getFont("Application.font.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblTipos.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        tblTipos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nombre", "Descripcion"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTipos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(tblTipos);

        btnBorrar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnBorrar.setMnemonic('b');
        btnBorrar.setText("Borrar");
        btnBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBorrarActionPerformed(evt);
            }
        });

        btnModificar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnModificar.setMnemonic('a');
        btnModificar.setText("Modificar");
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });

        btnNuevoGuardar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnNuevoGuardar.setMnemonic('u');
        btnNuevoGuardar.setText("Nuevo");
        btnNuevoGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoGuardarActionPerformed(evt);
            }
        });

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
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCerrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 125, Short.MAX_VALUE)
                        .addComponent(btnNuevoGuardar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnModificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBorrar))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnBorrar, btnModificar, btnNuevoGuardar});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBorrar)
                    .addComponent(btnModificar)
                    .addComponent(btnNuevoGuardar)
                    .addComponent(btnCerrar))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void btnBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBorrarActionPerformed
        if (tblTipos.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un elemento de la tabla a borrar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        //Preguntar si se desea eliminar realmente
        int result = JOptionPane.showConfirmDialog(this, "Realmente desea eliminar la fila seleccionada?", "Confirmación", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                gestor.eliminarTipo(itemSeleccionado);
                mostrarTipos(gestor.cargarTipos());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "El elemento está siendo utilizado actualmente.", "No se puede eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnBorrarActionPerformed

    private void btnNuevoGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoGuardarActionPerformed
        /**Guarda en la base de datos solamente si estadoABM=modificado o nuevo */
        if (estadoABM.compareTo("modificado") == 0) {
            this.tomarConfirmacionModificar();
            return;
        }
        if (estadoABM.compareTo("nuevo") == 0) {
            this.tomarConfirmacionNuevo();
            return;
        }
        if (estadoABM.compareTo("normal") == 0) {
            this.opcionNuevoTipo();
            return;
        }
    }//GEN-LAST:event_btnNuevoGuardarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        if (estadoABM.compareTo("normal") == 0) {
            if (tblTipos.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un elemento de la tabla a borrar.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            tblTipos.setEnabled(false);
            this.opcionModificar();
        } else {
            //Habilitar botones y table, deshabilitar Jtext.
            this.opcionReestablecerVentana();
        }
    }//GEN-LAST:event_btnModificarActionPerformed

private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
    dispose();
}//GEN-LAST:event_btnCerrarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBorrar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnNuevoGuardar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblTipos;
    private javax.swing.JTextField txtDesc;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables
}
