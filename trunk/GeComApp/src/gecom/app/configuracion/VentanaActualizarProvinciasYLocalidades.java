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

import java.util.List;
import java.util.TreeSet;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.interfaces.VentanaInterna;
import simbya.gecom.entidades.Localidad;
import simbya.gecom.entidades.Provincia;
import simbya.gecom.gestores.configuracion.GestorActualizarProvinciasYLocalidadesRemote;

/**
 * Ventana para actualizar provincias y localidad.
 * @author Marcelo Busico.
 */
public class VentanaActualizarProvinciasYLocalidades extends VentanaInterna {

    private GestorActualizarProvinciasYLocalidadesRemote gestor = null;
    private DefaultListModel modeloProvincias;
    private DefaultListModel modeloLocalidades;

    /** Creates new form VentanaActualizarProvinciasYLocalidades */
    public VentanaActualizarProvinciasYLocalidades() {
        initComponents();
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorActualizarProvinciasYLocalidadesRemote) gc.getObjetoRemoto(
                GestorActualizarProvinciasYLocalidadesRemote.class);
        modeloProvincias = new DefaultListModel();
        modeloLocalidades = new DefaultListModel();
        lstProvincias.setModel(modeloProvincias);
        lstLocalidades.setModel(modeloLocalidades);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    public void inicializarVentana() {
        cargarProvincias();
        btnProvinciaModificar.setEnabled(false);
        btnProvinciaEliminar.setEnabled(false);
        btnLocalidadRegistrar.setEnabled(false);
        btnLocalidadModificar.setEnabled(false);
        btnLocalidadEliminar.setEnabled(false);
    }

    private void cargarProvincias() {
        modeloProvincias.removeAllElements();
        //Carga solo las provincias ordenadas.
        List<Provincia> lstPr = gestor.cargarObjetosPersistentes(Provincia.class);
        TreeSet<Provincia> provincias = new TreeSet<Provincia>(lstPr);
        for (Provincia provincia : provincias) {
            modeloProvincias.addElement(provincia);
        }
        lstProvincias.setSelectedIndex(-1);
        cargarLocalidades();
    }

    private void cargarLocalidades() {
        modeloLocalidades.removeAllElements();
        Provincia prov = null;
        if (lstProvincias.getSelectedIndex() != -1) {
            prov = (Provincia) modeloProvincias.getElementAt(
                    lstProvincias.getSelectedIndex());
        } else {
            return;
        }
        //Carga solo las localidades ordenadas.
        TreeSet<Localidad> localidades = new TreeSet<Localidad>(prov.getLocalidades());
        for (Localidad localidad : localidades) {
            modeloLocalidades.addElement(localidad);
        }
        lstLocalidades.setSelectedIndex(-1);
    }

    /**
     * Actualiza la provincia (alta o modificación) en la BD.
     * @param p Provincia a modificar, null para nueva provincia.
     */
    private void actualizarProvincia(Provincia p) {
        String res = JOptionPane.showInputDialog(this,
                "Ingrese el nombre de la provincia:", (p == null ? "" : p.getNombre()));
        if (res == null) {
            return;
        }
        Provincia provincia = (p == null ? new Provincia() : p);
        provincia.setNombre(res);
        gestor.confirmarActualizacion(provincia);
        cargarProvincias();
    }

    /**
     * Actualiza la localidad (alta o modificación) en la BD.
     * @param l Localidad a modificar, null para nueva localidad.
     */
    private void actualizarLocalidad(Localidad l) {
        if (lstProvincias.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar la provincia para actualizar localidades.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String res = JOptionPane.showInputDialog(this,
                "Ingrese el nombre de la localidad:", (l == null ? "" : l.getNombre()));
        if (res == null) {
            return;
        }
        Localidad localidad = null;
        Provincia provincia = (Provincia) modeloProvincias.getElementAt(
                lstProvincias.getSelectedIndex());
        if (l == null) {
            localidad = new Localidad();
        } else {
            localidad = l;
        }
        localidad.setNombre(res);
        localidad.setProvincia(provincia);
        if (l == null) {
            provincia.getLocalidades().add(localidad);
        }
        gestor.confirmarActualizacion(localidad);
        cargarLocalidades();
    }

    private void eliminarProvincia(Provincia p) {
        int res = JOptionPane.showConfirmDialog(this,
                "Está a pundo de eliminar la provincia " + p.getNombre() + " de la base" +
                " de datos.\n" +
                "Esta acción eliminará todas las localidades asociadas.\n" +
                "¿Desea continuar?", "Confirmación Requerida", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.NO_OPTION) {
            return;
        }
        try {
            gestor.confirmarEliminar(p);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar la provincia.\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Provincia eliminada con éxito.",
                "Información", JOptionPane.INFORMATION_MESSAGE);
        cargarProvincias();
    }

    private void eliminarLocalidad(Localidad l) {
        int res = JOptionPane.showConfirmDialog(this,
                "Está a pundo de eliminar la localidad " + l.getNombre() + " de la base" +
                " de datos.\n¿Desea continuar?", "Confirmación Requerida", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.NO_OPTION) {
            return;
        }
        try {
            gestor.confirmarEliminar(l);
            l.getProvincia().getLocalidades().remove(l);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar la localidad.\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Localidad eliminada con éxito.",
                "Información", JOptionPane.INFORMATION_MESSAGE);
        cargarLocalidades();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnCerrar = new javax.swing.JButton();
        pnlContenedor = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btnProvinciaModificar = new javax.swing.JButton();
        btnProvinciaRegistrar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnProvinciaEliminar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstProvincias = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        btnLocalidadRegistrar = new javax.swing.JButton();
        btnLocalidadEliminar = new javax.swing.JButton();
        btnLocalidadModificar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstLocalidades = new javax.swing.JList();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaActualizarProvinciasYLocalidades.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setMinimumSize(new java.awt.Dimension(650, 500));
        setName("Form"); // NOI18N

        btnCerrar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCerrar.setIcon(resourceMap.getIcon("btnCerrar.icon")); // NOI18N
        btnCerrar.setText(resourceMap.getString("btnCerrar.text")); // NOI18N
        btnCerrar.setName("btnCerrar"); // NOI18N
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        pnlContenedor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlContenedor.setName("pnlContenedor"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(resourceMap.getColor("jPanel1.border.lineColor"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        btnProvinciaModificar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnProvinciaModificar.setIcon(resourceMap.getIcon("btnProvinciaModificar.icon")); // NOI18N
        btnProvinciaModificar.setText(resourceMap.getString("btnProvinciaModificar.text")); // NOI18N
        btnProvinciaModificar.setName("btnProvinciaModificar"); // NOI18N
        btnProvinciaModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProvinciaModificarActionPerformed(evt);
            }
        });

        btnProvinciaRegistrar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnProvinciaRegistrar.setIcon(resourceMap.getIcon("btnProvinciaRegistrar.icon")); // NOI18N
        btnProvinciaRegistrar.setText(resourceMap.getString("btnProvinciaRegistrar.text")); // NOI18N
        btnProvinciaRegistrar.setName("btnProvinciaRegistrar"); // NOI18N
        btnProvinciaRegistrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProvinciaRegistrarActionPerformed(evt);
            }
        });

        jLabel1.setFont(resourceMap.getFont("Application.font.big")); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        btnProvinciaEliminar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnProvinciaEliminar.setIcon(resourceMap.getIcon("btnProvinciaEliminar.icon")); // NOI18N
        btnProvinciaEliminar.setText(resourceMap.getString("btnProvinciaEliminar.text")); // NOI18N
        btnProvinciaEliminar.setName("btnProvinciaEliminar"); // NOI18N
        btnProvinciaEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProvinciaEliminarActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        lstProvincias.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lstProvincias.setName("lstProvincias"); // NOI18N
        lstProvincias.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstProvinciasValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstProvincias);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnProvinciaRegistrar, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                    .addComponent(btnProvinciaEliminar, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                    .addComponent(btnProvinciaModificar, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(btnProvinciaRegistrar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProvinciaModificar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProvinciaEliminar)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(resourceMap.getColor("jPanel2.border.lineColor"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("Application.font.big")); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        btnLocalidadRegistrar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnLocalidadRegistrar.setIcon(resourceMap.getIcon("btnLocalidadRegistrar.icon")); // NOI18N
        btnLocalidadRegistrar.setText(resourceMap.getString("btnLocalidadRegistrar.text")); // NOI18N
        btnLocalidadRegistrar.setName("btnLocalidadRegistrar"); // NOI18N
        btnLocalidadRegistrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLocalidadRegistrarActionPerformed(evt);
            }
        });

        btnLocalidadEliminar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnLocalidadEliminar.setIcon(resourceMap.getIcon("btnLocalidadEliminar.icon")); // NOI18N
        btnLocalidadEliminar.setText(resourceMap.getString("btnLocalidadEliminar.text")); // NOI18N
        btnLocalidadEliminar.setName("btnLocalidadEliminar"); // NOI18N
        btnLocalidadEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLocalidadEliminarActionPerformed(evt);
            }
        });

        btnLocalidadModificar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnLocalidadModificar.setIcon(resourceMap.getIcon("btnLocalidadModificar.icon")); // NOI18N
        btnLocalidadModificar.setText(resourceMap.getString("btnLocalidadModificar.text")); // NOI18N
        btnLocalidadModificar.setName("btnLocalidadModificar"); // NOI18N
        btnLocalidadModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLocalidadModificarActionPerformed(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        lstLocalidades.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lstLocalidades.setName("lstLocalidades"); // NOI18N
        lstLocalidades.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstLocalidadesValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(lstLocalidades);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                    .addComponent(btnLocalidadRegistrar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                    .addComponent(btnLocalidadEliminar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                    .addComponent(btnLocalidadModificar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLocalidadRegistrar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLocalidadModificar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLocalidadEliminar)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlContenedorLayout = new javax.swing.GroupLayout(pnlContenedor);
        pnlContenedor.setLayout(pnlContenedorLayout);
        pnlContenedorLayout.setHorizontalGroup(
            pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContenedorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlContenedorLayout.setVerticalGroup(
            pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlContenedorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlContenedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCerrar))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlContenedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCerrar)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void btnProvinciaRegistrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProvinciaRegistrarActionPerformed
    actualizarProvincia(null);//GEN-LAST:event_btnProvinciaRegistrarActionPerformed
    }

private void btnProvinciaModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProvinciaModificarActionPerformed
    if (lstProvincias.getSelectedIndex() == -1) {
        JOptionPane.showMessageDialog(this,
                "Debe seleccionar la provincia a modificar.",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }
    Provincia p = (Provincia) modeloProvincias.getElementAt(lstProvincias.getSelectedIndex());
    actualizarProvincia(p);
}//GEN-LAST:event_btnProvinciaModificarActionPerformed

private void btnLocalidadModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocalidadModificarActionPerformed
    if (lstLocalidades.getSelectedIndex() == -1) {
        JOptionPane.showMessageDialog(this,
                "Debe seleccionar la localidad a modificar.",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }
    Localidad l = (Localidad) modeloLocalidades.getElementAt(lstLocalidades.getSelectedIndex());
    actualizarLocalidad(l);
}//GEN-LAST:event_btnLocalidadModificarActionPerformed

private void btnLocalidadRegistrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocalidadRegistrarActionPerformed
    actualizarLocalidad(null);
}//GEN-LAST:event_btnLocalidadRegistrarActionPerformed

private void btnProvinciaEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProvinciaEliminarActionPerformed
    if (lstProvincias.getSelectedIndex() == -1) {
        JOptionPane.showMessageDialog(this,
                "Debe seleccionar la provincia a eliminar.",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }
    Provincia p = (Provincia) modeloProvincias.getElementAt(lstProvincias.getSelectedIndex());
    eliminarProvincia(p);
}//GEN-LAST:event_btnProvinciaEliminarActionPerformed

private void btnLocalidadEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocalidadEliminarActionPerformed
    if (lstLocalidades.getSelectedIndex() == -1) {
        JOptionPane.showMessageDialog(this,
                "Debe seleccionar la localidad a eliminar.",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }
    Localidad l = (Localidad) modeloLocalidades.getElementAt(lstLocalidades.getSelectedIndex());
    eliminarLocalidad(l);
}//GEN-LAST:event_btnLocalidadEliminarActionPerformed

private void lstProvinciasValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstProvinciasValueChanged
    if (lstProvincias.getSelectedIndex() != -1) {
        btnProvinciaModificar.setEnabled(true);
        btnProvinciaEliminar.setEnabled(true);
        btnLocalidadRegistrar.setEnabled(true);
    } else {
        btnProvinciaModificar.setEnabled(false);
        btnProvinciaEliminar.setEnabled(false);
        btnLocalidadRegistrar.setEnabled(false);
    }
    lstLocalidades.setSelectedIndex(-1);
    cargarLocalidades();
}//GEN-LAST:event_lstProvinciasValueChanged

private void lstLocalidadesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstLocalidadesValueChanged
    if (lstLocalidades.getSelectedIndex() != -1) {
        btnLocalidadModificar.setEnabled(true);
        btnLocalidadEliminar.setEnabled(true);
    } else {
        btnLocalidadModificar.setEnabled(false);
        btnLocalidadEliminar.setEnabled(false);
    }
}//GEN-LAST:event_lstLocalidadesValueChanged

private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
    dispose();
}//GEN-LAST:event_btnCerrarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnLocalidadEliminar;
    private javax.swing.JButton btnLocalidadModificar;
    private javax.swing.JButton btnLocalidadRegistrar;
    private javax.swing.JButton btnProvinciaEliminar;
    private javax.swing.JButton btnProvinciaModificar;
    private javax.swing.JButton btnProvinciaRegistrar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lstLocalidades;
    private javax.swing.JList lstProvincias;
    private javax.swing.JPanel pnlContenedor;
    // End of variables declaration//GEN-END:variables
}
