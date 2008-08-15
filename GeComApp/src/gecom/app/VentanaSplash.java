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
package gecom.app;

import gecom.app.configuracion.GestorConfigurarConexion;
import gecom.app.usuarios.VentanaInicioSesion;
import java.awt.event.*;
import javax.swing.*;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.appserver.ServerConf;
import simbya.framework.excepciones.ServerVersionException;
import simbya.gecom.VersionId;
import simbya.gecom.gestores.conexion.GestorConexionRemote;

/**
 * Ventana de presentación y carga del sistema.
 * @author Marcelo Busico.
 */
public class VentanaSplash extends javax.swing.JFrame {

    private static final Logger log = Logger.getLogger(VentanaSplash.class);
    private Timer timer;
    private int veces = 0;

    /** 
     * Crea un nuevo formulario VentanaSplash.
     */
    public VentanaSplash() {
        timer = new Timer(100, new TimerListener());
        initComponents();
        lblVersion.setText("Versión " + VersionId.ID_VERSION_MAYOR +
                "." +
                (VersionId.ID_VERSION_MENOR < 10 ? "0" +
                VersionId.ID_VERSION_MENOR : VersionId.ID_VERSION_MENOR));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelContorno = new javax.swing.JPanel();
        panelImagen = new javax.swing.JPanel();
        lblImagen = new javax.swing.JLabel();
        lblVersion = new javax.swing.JLabel();
        lblCargando = new javax.swing.JLabel();

        setTitle("Iniciando aplicación..."); // NOI18N
        setAlwaysOnTop(true);
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        setResizable(false);
        setUndecorated(true);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        panelContorno.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.blue));

        panelImagen.setBackground(new java.awt.Color(255, 255, 255));
        panelImagen.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        panelImagen.setLayout(new java.awt.BorderLayout());

        lblImagen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblImagen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/SIMBYA-GECOM-logo-mini.png"))); // NOI18N
        panelImagen.add(lblImagen, java.awt.BorderLayout.CENTER);

        lblVersion.setFont(new java.awt.Font("Dialog", 3, 18));
        lblVersion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblVersion.setText("Versión 2.01");

        lblCargando.setFont(new java.awt.Font("Dialog", 1, 18));
        lblCargando.setForeground(new java.awt.Color(0, 153, 153));
        lblCargando.setText("Cargando...");

        javax.swing.GroupLayout panelContornoLayout = new javax.swing.GroupLayout(panelContorno);
        panelContorno.setLayout(panelContornoLayout);
        panelContornoLayout.setHorizontalGroup(
            panelContornoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelContornoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelContornoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelImagen, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                    .addGroup(panelContornoLayout.createSequentialGroup()
                        .addComponent(lblCargando, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 234, Short.MAX_VALUE)
                        .addComponent(lblVersion)))
                .addContainerGap())
        );
        panelContornoLayout.setVerticalGroup(
            panelContornoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelContornoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelImagen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(panelContornoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCargando)
                    .addComponent(lblVersion))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelContorno, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelContorno, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-505)/2, (screenSize.height-432)/2, 505, 432);
    }// </editor-fold>//GEN-END:initComponents
    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        timer.start();
    }//GEN-LAST:event_formComponentShown

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new VentanaSplash().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblCargando;
    private javax.swing.JLabel lblImagen;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel panelContorno;
    private javax.swing.JPanel panelImagen;
    // End of variables declaration//GEN-END:variables
    /** 
     * SubClase TimerListener que hace las veces de temporizador.
     */
    class TimerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            //Aca va el codigo que se repite en cada intervalo del Timer
            veces++;
            if (veces == 5) {
                timer.stop();
                ServerConf conf = null;
                try {
                    conf = ServerConf.cargarDeArchivo(ServerConf.nombreArchivo);
                    GestorConexion.conectar(conf.getDireccion(), conf.getPuerto());
                    //Valida la version del software con el servidor.
                    GestorConexionRemote gestorRemoto =
                            (GestorConexionRemote) GestorConexion.getInstancia().getObjetoRemoto(
                            GestorConexionRemote.class);
                    gestorRemoto.validarVersionCliente(VersionId.ID_VERSION_MAYOR,
                            VersionId.ID_VERSION_MENOR);
                    dispose();
                    new VentanaInicioSesion().setVisible(true);
                    log.info("El sistema ha iniciado.");
                } catch (ServerVersionException sve) {
                    log.warn("Error al conectar con servidor.", sve);
                    JOptionPane.showMessageDialog(VentanaSplash.this,
                            "Error al conectar con servidor:\n" +
                            sve.getMessage() + "\n" +
                            "Por favor contacte con el administrador para actualizar" +
                            " el software.\n" +
                            "Se saldrá del sistema ahora.", "Error de versión",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                } catch (Throwable t) {
                    log.warn("Error al conectar con servidor.", t);
                    int res = JOptionPane.showConfirmDialog(VentanaSplash.this,
                            "Error al conectar con servidor:\n" +
                            t.getMessage() + "\n" +
                            "¿Desea cargar una nueva configuración de conexión?",
                            "Error de conexión", JOptionPane.YES_NO_OPTION);
                    VentanaSplash.this.dispose();
                    if (res == JOptionPane.YES_OPTION) {
                        try {
                            new GestorConfigurarConexion(null, conf);
                        } catch (Exception ex) {
                            new GestorConfigurarConexion(null);
                        }
                    } else {
                        log.warn("Se sale del sistema ahora.");
                        JOptionPane.showMessageDialog(VentanaSplash.this,
                                "Se saldrá del sistema ahora.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }

                }
            }
        }
    }
}