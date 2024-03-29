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
package gecom.app.configuracion;

import gecom.app.table.ParametroSistemaTableEditor;
import gecom.app.table.ParametroSistemaTableWrapper;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.naming.NamingException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.interfaces.VentanaInterna;
import simbya.gecom.entidades.parametros.ParametroSistema;
import simbya.gecom.gestores.configuracion.GestorActualizarParametrosGeneralesRemote;

/**
 * Ventana para actualizar los parámetros del sistema.
 * @author Marcelo Busico.
 */
public class VentanaActualizarParametrosGenerales
        extends VentanaInterna {

    private static final Logger log = Logger.getLogger(
            VentanaActualizarParametrosGenerales.class);
    private GestorActualizarParametrosGeneralesRemote gestor;

    /** 
     * Crea un nuevo formulario VentanaActualizarParametrosGenerales.
     */
    public VentanaActualizarParametrosGenerales() {
        //Inicializa los componentes de la ventana.
        initComponents();
        //Para Editar Valores
        tblParametros.setDefaultEditor(ParametroSistemaTableWrapper.class,
                new ParametroSistemaTableEditor());
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorActualizarParametrosGeneralesRemote) gc.getObjetoRemoto(
                GestorActualizarParametrosGeneralesRemote.class);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    public void inicializarVentana() {
        mostrarParametros(gestor.getParametros());
    }

    public void mostrarParametros(List<ParametroSistema> parametros) {
        DefaultTableModel modelo = (DefaultTableModel) tblParametros.getModel();
        ListIterator<ParametroSistema> li = parametros.listIterator();
        modelo.setRowCount(0);
        while (li.hasNext()) {
            ParametroSistema ps = li.next();
            Object[] filaNueva = new Object[2];
            filaNueva[0] = ps;
            filaNueva[1] = new ParametroSistemaTableWrapper(ps);
            modelo.addRow(filaNueva);
        }
    }

    public void tomarConfirmacion() {
        List<ParametroSistema> parametros = new LinkedList<ParametroSistema>();
        for (int i = 0; i < tblParametros.getRowCount(); i++) {
            ParametroSistema ps = (ParametroSistema) tblParametros.getValueAt(i, 0);
            ParametroSistemaTableWrapper psw =
                    (ParametroSistemaTableWrapper) tblParametros.getValueAt(i, 1);
            ps.setValor(psw.getParametro().getValor());
            parametros.add(ps);
        }

        try {
            gestor.actualizarParametros(parametros);
            JOptionPane.showMessageDialog(this,
                    "Se han actualizado los parámetros con éxito.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            inicializarVentana();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar" +
                    " los parámetros del sistema en la base de datos:\n" + e.getMessage(),
                    "Error de Registro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelDesplazamiento = new javax.swing.JScrollPane();
        tblParametros = new javax.swing.JTable();
        btnCerrar = new javax.swing.JButton();
        btnAceptar = new javax.swing.JButton();
        lblInstrucciones = new javax.swing.JLabel();
        btnCancelar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Actualizar Parámetros Generales...");
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/wrench_orange.png"))); // NOI18N
        setMinimumSize(new java.awt.Dimension(451, 289));
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaActualizarParametrosGenerales.class);
        tblParametros.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        tblParametros.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Parámetro", "Valor"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, ParametroSistemaTableWrapper.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        panelDesplazamiento.setViewportView(tblParametros);
        tblParametros.getColumnModel().getColumn(1).setMinWidth(100);
        tblParametros.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblParametros.getColumnModel().getColumn(1).setMaxWidth(200);

        btnCerrar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/close.gif"))); // NOI18N
        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        btnAceptar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnAceptar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/accept.png"))); // NOI18N
        btnAceptar.setText("Aceptar");
        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarActionPerformed(evt);
            }
        });

        lblInstrucciones.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblInstrucciones.setText("Doble Click en la tabla sobre el valor que desea modificar.");

        btnCancelar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/cancel.png"))); // NOI18N
        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblInstrucciones)
                        .addGap(42, 42, 42))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelDesplazamiento, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnCerrar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 222, Short.MAX_VALUE)
                                .addComponent(btnCancelar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAceptar)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblInstrucciones)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelDesplazamiento, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCerrar)
                    .addComponent(btnAceptar)
                    .addComponent(btnCancelar))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
        tomarConfirmacion();
    }//GEN-LAST:event_btnAceptarActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        dispose();
    }//GEN-LAST:event_btnCerrarActionPerformed

private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
    inicializarVentana();
}//GEN-LAST:event_btnCancelarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JLabel lblInstrucciones;
    private javax.swing.JScrollPane panelDesplazamiento;
    private javax.swing.JTable tblParametros;
    // End of variables declaration//GEN-END:variables
}
