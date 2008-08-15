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
package gecom.app.proveedores;

import gecom.app.buscador.Buscador;
import gecom.app.compras.VentanaRegistrarCompra;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.naming.NamingException;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.text.InternationalFormatter;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.interfaces.VentanaInterna;
import simbya.framework.validadores.TextValidator;
import simbya.gecom.entidades.Localidad;
import simbya.gecom.entidades.Proveedor;
import simbya.gecom.entidades.Provincia;
import simbya.gecom.gestores.proveedores.GestorActualizarProveedorRemote;

/**
 * Ventana para actualizar proveedor.
 * @author Marcelo Busico.
 */
public class VentanaActualizarProveedor extends VentanaInterna {

    private GestorActualizarProveedorRemote gestor;
    private Proveedor seleccion = null;
    private VentanaRegistrarCompra ventanaInvocadora = null;
    private boolean registrado = false;

    /** Crea un nuevo formulario VentanaActualizarProveedor */
    public VentanaActualizarProveedor() {
        initComponents();
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorActualizarProveedorRemote) gc.getObjetoRemoto(
                GestorActualizarProveedorRemote.class);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    public void inicializarVentana() {
        seleccion = null;
        btnBuscar.setEnabled(true);
        btnNuevo.setEnabled(true);
        btnModificar.setEnabled(false);
        btnQuitar.setEnabled(false);
        btnAceptar.setEnabled(false);
        btnCancelar.setEnabled(false);

        txtCP.setEditable(false);
        txtCuit.setEditable(false);
        txtCalle.setEditable(false);
        txtNumero.setEditable(false);
        txtEmail.setEditable(false);
        txtDpto.setEditable(false);
        txtFax.setEditable(false);
        txtPiso.setEditable(false);
        txtRazonSocial.setEditable(false);
        txtTelefono.setEditable(false);
        cboLocalidad.setEnabled(false);
        cboProvincia.setEnabled(false);
        limpiarVentana();
    }

    public void asistirRegistrarNuevoProveedor(VentanaRegistrarCompra ventana) {
        ventanaInvocadora = ventana;
        opcionRegistrarNuevo();
    }

    private void limpiarVentana() {
        txtSeleccion.setText("");
        txtCP.setText("");
        txtCuit.setText("");
        txtCalle.setText("");
        txtNumero.setText("");
        txtEmail.setText("");
        txtDpto.setText("");
        txtFax.setText("");
        txtPiso.setText("");
        txtRazonSocial.setText("");
        txtTelefono.setText("");
        cboLocalidad.removeAllItems();
        cboProvincia.removeAllItems();
    }

    private void tomarCancelacion() {
        if (ventanaInvocadora == null) {
            inicializarVentana();
        } else {
            dispose();
        }
    }

    private void opcionRegistrarNuevo() {
        limpiarVentana();
        seleccion = new Proveedor();
        txtCP.setEditable(true);
        txtCuit.setEditable(true);
        txtCalle.setEditable(true);
        txtNumero.setEditable(true);
        txtEmail.setEditable(true);
        txtDpto.setEditable(true);
        txtFax.setEditable(true);
        txtPiso.setEditable(true);
        txtRazonSocial.setEditable(true);
        txtTelefono.setEditable(true);
        cboProvincia.setEnabled(true);
        cboLocalidad.setEnabled(true);
        btnNuevo.setEnabled(false);
        btnModificar.setEnabled(false);
        btnQuitar.setEnabled(false);
        btnBuscar.setEnabled(false);
        btnAceptar.setEnabled(true);
        btnCancelar.setEnabled(true);
        mostrarProvincias(gestor.cargarProvincias());
    }

    private void mostrarProvincias(List provincias) {
        cboProvincia.removeAllItems();
        ListIterator<Provincia> li1 = provincias.listIterator();
        Provincia p = null;
        while (li1.hasNext()) {
            p = li1.next();
            cboProvincia.addItem(p);
        }
        cboProvincia.setSelectedIndex(-1);
    }

    private void mostrarLocalidades(Set<Localidad> localidades) {
        cboLocalidad.removeAllItems();
        if (localidades == null) {
            return;
        }

        for (Localidad localidad : localidades) {
            cboLocalidad.addItem(localidad);
        }
        cboLocalidad.setSelectedIndex(-1);
    }

    private void tomarConfirmacion() {
        if (!verificarDatos()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor complete todos los datos marcados" +
                    " como requeridos con un *.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            long oid = gestor.confirmarActualizacion(seleccion);
            seleccion.setOid(oid);
            informarActualizacionExitosa(seleccion.getIdentidad());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar en la base de datos.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tomarSeleccionProveedor() {
        if (seleccion == null) {
            return;
        }
        txtSeleccion.setText(seleccion.getIdentidad());
        txtRazonSocial.setText(seleccion.getRazonSocial());
        txtCuit.setText(seleccion.getCuit());
        txtCalle.setText(seleccion.getCalle());
        if (seleccion.getNumero() < 0) {
            txtNumero.setText(null);
        } else {
            txtNumero.setText(String.valueOf(seleccion.getNumero()));
        }
        if (seleccion.getPiso() < 0) {
            txtPiso.setText(null);
        } else {
            txtPiso.setText(String.valueOf(seleccion.getPiso()));
        }
        txtDpto.setText(seleccion.getDepartamento());
        txtCP.setText(seleccion.getCodigoPostal());
        
        mostrarProvincias(gestor.cargarProvincias());
        
        //Selecciona la provincia correspondiente en el combo Provincia.
        cboProvincia.setSelectedIndex(-1);
        cboLocalidad.removeAllItems();
        if (seleccion.getLocalidad() != null) {
            for (int i = 0; i < cboProvincia.getItemCount(); i++) {
                if (((Provincia) cboProvincia.getItemAt(i)).sosProvinciaDeEstaLocalidad(
                        seleccion.getLocalidad())) {
                    cboProvincia.setSelectedIndex(i);
                    break;
                }
            }
            mostrarLocalidades(((Provincia) cboProvincia.getSelectedItem()).getLocalidades());
        }

        //Selecciona la localidad correspondiente en el combo Localidades.
        cboLocalidad.setSelectedIndex(-1);
        for (int i = 0; i < cboLocalidad.getItemCount(); i++) {
            if (((Localidad) cboLocalidad.getItemAt(i)).getOid() == seleccion.getLocalidad().getOid()) {
                cboLocalidad.setSelectedIndex(i);
                break;
            }
        }

        txtTelefono.setText(seleccion.getTelefono());
        txtFax.setText(seleccion.getFax());
        txtEmail.setText(seleccion.getEmail());
    }

    private void opcionBaja() {
        int res = JOptionPane.showConfirmDialog(this,
                "Se dará de baja al Proveedor seleccionado.\n" +
                "¿Desea continuar?", "Confirmación de Usuario",
                JOptionPane.YES_NO_OPTION);

        //Si el usuario cancela la baja salir.
        if (res == JOptionPane.NO_OPTION) {
            return;
        }
        try {
            gestor.confirmarBaja(seleccion);
            informarBajaExitosa(seleccion.getIdentidad());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar en la base de datos.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void informarBajaExitosa(String nombre) {
        JOptionPane.showMessageDialog(this,
                "Se ha registrado con éxito la BAJA del Proveedor:\n   " +
                nombre, "Información",
                JOptionPane.INFORMATION_MESSAGE);
        tomarCancelacion();
    }

    private void informarActualizacionExitosa(String nombre) {
        if (ventanaInvocadora == null) {
            JOptionPane.showMessageDialog(this,
                    "Se han guardado con éxito los datos del Proveedor:\n    " +
                    nombre, "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            inicializarVentana();
        } else {
            registrado = true;
            dispose();
        }
    }

    private boolean verificarDatos() {
        if (seleccion == null) {
            return false;
        }

        if ((seleccion.getRazonSocial() == null) || (seleccion.getRazonSocial().isEmpty())) {
            return false;
        }
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSeleccion = new javax.swing.JPanel();
        txtSeleccion = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnQuitar = new javax.swing.JButton();
        pnlDatos = new javax.swing.JPanel();
        txtRazonSocial = new javax.swing.JTextField();
        lblRazon = new javax.swing.JLabel();
        lblCuit = new javax.swing.JLabel();
        txtCuit = new javax.swing.JTextField();
        pnlDomicilio = new javax.swing.JPanel();
        lblCalle = new javax.swing.JLabel();
        txtCalle = new javax.swing.JTextField();
        lblNumero = new javax.swing.JLabel();
        lblPiso = new javax.swing.JLabel();
        lblDpto = new javax.swing.JLabel();
        txtDpto = new javax.swing.JTextField();
        lblProvincia = new javax.swing.JLabel();
        cboProvincia = new javax.swing.JComboBox();
        lblLocalidad = new javax.swing.JLabel();
        cboLocalidad = new javax.swing.JComboBox();
        lblCP = new javax.swing.JLabel();
        txtCP = new javax.swing.JTextField();
        InternationalFormatter formatPiso = new InternationalFormatter();
        formatPiso.setMaximum(new Integer(100));
        formatPiso.setMinimum(new Integer(0));
        txtPiso = new JFormattedTextField(formatPiso);
        txtPiso.setValue(new Integer(0));
        txtPiso.setText("");
        InternationalFormatter formatNroCalle = new InternationalFormatter();
        formatNroCalle.setMaximum(new Integer(15000));
        formatNroCalle.setMinimum(new Integer(0));
        txtNumero = new JFormattedTextField(formatNroCalle);
        txtNumero.setValue(new Integer(1));
        txtNumero.setText("");
        btnQuitarSeleccionProvincia = new javax.swing.JButton();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblTelefono = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        lblFax = new javax.swing.JLabel();
        txtFax = new javax.swing.JTextField();
        btnCancelar = new javax.swing.JButton();
        btnAceptar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaActualizarProveedor.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setName("Form"); // NOI18N
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        pnlSeleccion.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlSeleccion.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("pnlSeleccion.border.titleFont"))); // NOI18N
        pnlSeleccion.setName("pnlSeleccion"); // NOI18N

        txtSeleccion.setEditable(false);
        txtSeleccion.setFont(resourceMap.getFont("txtSeleccion.font")); // NOI18N
        txtSeleccion.setText(resourceMap.getString("txtSeleccion.text")); // NOI18N
        txtSeleccion.setName("txtSeleccion"); // NOI18N

        btnBuscar.setFont(resourceMap.getFont("btnBuscar.font")); // NOI18N
        btnBuscar.setIcon(resourceMap.getIcon("btnBuscar.icon")); // NOI18N
        btnBuscar.setMnemonic('b');
        btnBuscar.setText(resourceMap.getString("btnBuscar.text")); // NOI18N
        btnBuscar.setName("btnBuscar"); // NOI18N
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        btnNuevo.setFont(resourceMap.getFont("btnNuevo.font")); // NOI18N
        btnNuevo.setIcon(resourceMap.getIcon("btnNuevo.icon")); // NOI18N
        btnNuevo.setMnemonic('n');
        btnNuevo.setText(resourceMap.getString("btnNuevo.text")); // NOI18N
        btnNuevo.setName("btnNuevo"); // NOI18N
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnModificar.setFont(resourceMap.getFont("btnModificar.font")); // NOI18N
        btnModificar.setIcon(resourceMap.getIcon("btnModificar.icon")); // NOI18N
        btnModificar.setMnemonic('m');
        btnModificar.setText(resourceMap.getString("btnModificar.text")); // NOI18N
        btnModificar.setEnabled(false);
        btnModificar.setName("btnModificar"); // NOI18N
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });

        btnQuitar.setFont(resourceMap.getFont("btnQuitar.font")); // NOI18N
        btnQuitar.setIcon(resourceMap.getIcon("btnQuitar.icon")); // NOI18N
        btnQuitar.setMnemonic('e');
        btnQuitar.setText(resourceMap.getString("btnQuitar.text")); // NOI18N
        btnQuitar.setEnabled(false);
        btnQuitar.setName("btnQuitar"); // NOI18N
        btnQuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSeleccionLayout = new javax.swing.GroupLayout(pnlSeleccion);
        pnlSeleccion.setLayout(pnlSeleccionLayout);
        pnlSeleccionLayout.setHorizontalGroup(
            pnlSeleccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSeleccionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSeleccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSeleccionLayout.createSequentialGroup()
                        .addComponent(txtSeleccion, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBuscar))
                    .addGroup(pnlSeleccionLayout.createSequentialGroup()
                        .addComponent(btnNuevo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnModificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitar)))
                .addContainerGap())
        );
        pnlSeleccionLayout.setVerticalGroup(
            pnlSeleccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSeleccionLayout.createSequentialGroup()
                .addGroup(pnlSeleccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSeleccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addGroup(pnlSeleccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevo)
                    .addComponent(btnModificar)
                    .addComponent(btnQuitar))
                .addContainerGap())
        );

        pnlDatos.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlDatos.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("pnlDatos.border.titleFont"))); // NOI18N
        pnlDatos.setName("pnlDatos"); // NOI18N

        txtRazonSocial.setEditable(false);
        txtRazonSocial.setFont(resourceMap.getFont("txtRazonSocial.font")); // NOI18N
        txtRazonSocial.setText(resourceMap.getString("txtRazonSocial.text")); // NOI18N
        txtRazonSocial.setName("txtRazonSocial"); // NOI18N
        txtRazonSocial.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRazonSocialFocusLost(evt);
            }
        });

        lblRazon.setFont(resourceMap.getFont("lblRazon.font")); // NOI18N
        lblRazon.setText(resourceMap.getString("lblRazon.text")); // NOI18N
        lblRazon.setName("lblRazon"); // NOI18N

        lblCuit.setFont(resourceMap.getFont("lblCuit.font")); // NOI18N
        lblCuit.setText(resourceMap.getString("lblCuit.text")); // NOI18N
        lblCuit.setName("lblCuit"); // NOI18N

        txtCuit.setEditable(false);
        txtCuit.setFont(resourceMap.getFont("txtCuit.font")); // NOI18N
        txtCuit.setText(resourceMap.getString("txtCuit.text")); // NOI18N
        txtCuit.setName("txtCuit"); // NOI18N
        txtCuit.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCuitFocusLost(evt);
            }
        });

        pnlDomicilio.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlDomicilio.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("pnlDomicilio.border.titleFont"))); // NOI18N
        pnlDomicilio.setName("pnlDomicilio"); // NOI18N

        lblCalle.setFont(resourceMap.getFont("lblCalle.font")); // NOI18N
        lblCalle.setText(resourceMap.getString("lblCalle.text")); // NOI18N
        lblCalle.setName("lblCalle"); // NOI18N

        txtCalle.setEditable(false);
        txtCalle.setFont(resourceMap.getFont("txtCalle.font")); // NOI18N
        txtCalle.setText(resourceMap.getString("txtCalle.text")); // NOI18N
        txtCalle.setName("txtCalle"); // NOI18N
        txtCalle.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCalleFocusLost(evt);
            }
        });

        lblNumero.setFont(resourceMap.getFont("lblNumero.font")); // NOI18N
        lblNumero.setText(resourceMap.getString("lblNumero.text")); // NOI18N
        lblNumero.setName("lblNumero"); // NOI18N

        lblPiso.setFont(resourceMap.getFont("lblPiso.font")); // NOI18N
        lblPiso.setText(resourceMap.getString("lblPiso.text")); // NOI18N
        lblPiso.setName("lblPiso"); // NOI18N

        lblDpto.setFont(resourceMap.getFont("lblDpto.font")); // NOI18N
        lblDpto.setText(resourceMap.getString("lblDpto.text")); // NOI18N
        lblDpto.setName("lblDpto"); // NOI18N

        txtDpto.setEditable(false);
        txtDpto.setFont(resourceMap.getFont("txtDpto.font")); // NOI18N
        txtDpto.setName("txtDpto"); // NOI18N
        txtDpto.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtDptoFocusLost(evt);
            }
        });

        lblProvincia.setFont(resourceMap.getFont("lblProvincia.font")); // NOI18N
        lblProvincia.setText(resourceMap.getString("lblProvincia.text")); // NOI18N
        lblProvincia.setName("lblProvincia"); // NOI18N

        cboProvincia.setFont(resourceMap.getFont("cboProvincia.font")); // NOI18N
        cboProvincia.setEnabled(false);
        cboProvincia.setName("cboProvincia"); // NOI18N
        cboProvincia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboProvinciaActionPerformed(evt);
            }
        });

        lblLocalidad.setFont(resourceMap.getFont("lblLocalidad.font")); // NOI18N
        lblLocalidad.setText(resourceMap.getString("lblLocalidad.text")); // NOI18N
        lblLocalidad.setName("lblLocalidad"); // NOI18N

        cboLocalidad.setFont(resourceMap.getFont("cboLocalidad.font")); // NOI18N
        cboLocalidad.setEnabled(false);
        cboLocalidad.setName("cboLocalidad"); // NOI18N
        cboLocalidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboLocalidadActionPerformed(evt);
            }
        });

        lblCP.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblCP.setText(resourceMap.getString("lblCP.text")); // NOI18N
        lblCP.setName("lblCP"); // NOI18N

        txtCP.setEditable(false);
        txtCP.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtCP.setText(resourceMap.getString("txtCP.text")); // NOI18N
        txtCP.setName("txtCP"); // NOI18N
        txtCP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCPFocusLost(evt);
            }
        });

        txtPiso.setEditable(false);
        txtPiso.setText(resourceMap.getString("txtPiso.text")); // NOI18N
        txtPiso.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
        txtPiso.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtPiso.setName("txtPiso"); // NOI18N
        txtPiso.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPisoFocusLost(evt);
            }
        });

        txtNumero.setEditable(false);
        txtNumero.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
        txtNumero.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtNumero.setName("txtNumero"); // NOI18N
        txtNumero.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNumeroFocusLost(evt);
            }
        });

        btnQuitarSeleccionProvincia.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnQuitarSeleccionProvincia.setIcon(resourceMap.getIcon("btnQuitarSeleccionProvincia.icon")); // NOI18N
        btnQuitarSeleccionProvincia.setName("btnQuitarSeleccionProvincia"); // NOI18N
        btnQuitarSeleccionProvincia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarSeleccionProvinciaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlDomicilioLayout = new javax.swing.GroupLayout(pnlDomicilio);
        pnlDomicilio.setLayout(pnlDomicilioLayout);
        pnlDomicilioLayout.setHorizontalGroup(
            pnlDomicilioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDomicilioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDomicilioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCalle, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlDomicilioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lblProvincia, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblLocalidad, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblCP, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(pnlDomicilioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlDomicilioLayout.createSequentialGroup()
                        .addComponent(txtCP, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblPiso, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPiso, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDpto)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDpto, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlDomicilioLayout.createSequentialGroup()
                        .addComponent(txtCalle, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNumero)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlDomicilioLayout.createSequentialGroup()
                        .addComponent(cboProvincia, 0, 271, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitarSeleccionProvincia))
                    .addComponent(cboLocalidad, javax.swing.GroupLayout.Alignment.LEADING, 0, 327, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlDomicilioLayout.setVerticalGroup(
            pnlDomicilioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDomicilioLayout.createSequentialGroup()
                .addGroup(pnlDomicilioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCalle)
                    .addComponent(lblNumero)
                    .addComponent(txtCalle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDomicilioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDpto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDpto)
                    .addComponent(lblCP)
                    .addComponent(lblPiso)
                    .addComponent(txtCP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPiso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDomicilioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDomicilioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblProvincia)
                        .addComponent(cboProvincia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnQuitarSeleccionProvincia))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDomicilioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLocalidad)
                    .addComponent(cboLocalidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblEmail.setFont(resourceMap.getFont("lblEmail.font")); // NOI18N
        lblEmail.setText(resourceMap.getString("lblEmail.text")); // NOI18N
        lblEmail.setName("lblEmail"); // NOI18N

        txtEmail.setEditable(false);
        txtEmail.setFont(resourceMap.getFont("txtEmail.font")); // NOI18N
        txtEmail.setText(resourceMap.getString("txtEmail.text")); // NOI18N
        txtEmail.setName("txtEmail"); // NOI18N
        txtEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtEmailFocusLost(evt);
            }
        });

        lblTelefono.setFont(resourceMap.getFont("lblTelefono.font")); // NOI18N
        lblTelefono.setText(resourceMap.getString("lblTelefono.text")); // NOI18N
        lblTelefono.setName("lblTelefono"); // NOI18N

        txtTelefono.setEditable(false);
        txtTelefono.setFont(resourceMap.getFont("txtTelefono.font")); // NOI18N
        txtTelefono.setText(resourceMap.getString("txtTelefono.text")); // NOI18N
        txtTelefono.setName("txtTelefono"); // NOI18N
        txtTelefono.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTelefonoFocusLost(evt);
            }
        });

        lblFax.setFont(resourceMap.getFont("lblFax.font")); // NOI18N
        lblFax.setText(resourceMap.getString("lblFax.text")); // NOI18N
        lblFax.setName("lblFax"); // NOI18N

        txtFax.setEditable(false);
        txtFax.setFont(resourceMap.getFont("txtFax.font")); // NOI18N
        txtFax.setText(resourceMap.getString("txtFax.text")); // NOI18N
        txtFax.setName("txtFax"); // NOI18N
        txtFax.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtFaxFocusLost(evt);
            }
        });

        javax.swing.GroupLayout pnlDatosLayout = new javax.swing.GroupLayout(pnlDatos);
        pnlDatos.setLayout(pnlDatosLayout);
        pnlDatosLayout.setHorizontalGroup(
            pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlDomicilio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatosLayout.createSequentialGroup()
                        .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRazon)
                            .addGroup(pnlDatosLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(lblCuit)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCuit, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtRazonSocial, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatosLayout.createSequentialGroup()
                        .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblEmail)
                            .addComponent(lblTelefono))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlDatosLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblFax)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFax, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        pnlDatosLayout.setVerticalGroup(
            pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosLayout.createSequentialGroup()
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRazon, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .addComponent(txtRazonSocial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCuit)
                    .addComponent(txtCuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDomicilio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmail)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTelefono)
                    .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFax)
                    .addComponent(txtFax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        btnCancelar.setFont(resourceMap.getFont("btnCancelar.font")); // NOI18N
        btnCancelar.setIcon(resourceMap.getIcon("btnCancelar.icon")); // NOI18N
        btnCancelar.setMnemonic('c');
        btnCancelar.setText(resourceMap.getString("btnCancelar.text")); // NOI18N
        btnCancelar.setEnabled(false);
        btnCancelar.setName("btnCancelar"); // NOI18N
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        btnAceptar.setFont(resourceMap.getFont("btnAceptar.font")); // NOI18N
        btnAceptar.setIcon(resourceMap.getIcon("btnAceptar.icon")); // NOI18N
        btnAceptar.setMnemonic('a');
        btnAceptar.setText(resourceMap.getString("btnAceptar.text")); // NOI18N
        btnAceptar.setEnabled(false);
        btnAceptar.setName("btnAceptar"); // NOI18N
        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarActionPerformed(evt);
            }
        });

        btnCerrar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCerrar.setIcon(resourceMap.getIcon("btnCerrar.icon")); // NOI18N
        btnCerrar.setText(resourceMap.getString("btnCerrar.text")); // NOI18N
        btnCerrar.setName("btnCerrar"); // NOI18N
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
                    .addComponent(pnlDatos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSeleccion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCerrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 157, Short.MAX_VALUE)
                        .addComponent(btnAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSeleccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDatos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnAceptar)
                    .addComponent(btnCerrar))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
    tomarCancelacion();
}//GEN-LAST:event_btnCancelarActionPerformed

private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
    tomarConfirmacion();
}//GEN-LAST:event_btnAceptarActionPerformed

private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
    dispose();
}//GEN-LAST:event_btnCerrarActionPerformed

private void txtEmailFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtEmailFocusLost
    seleccion.setEmail(TextValidator.validarEMail(this, txtEmail));
}//GEN-LAST:event_txtEmailFocusLost

private void txtFaxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtFaxFocusLost
    seleccion.setFax(TextValidator.validarTelefono(this, txtFax));
}//GEN-LAST:event_txtFaxFocusLost

private void txtTelefonoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTelefonoFocusLost
    seleccion.setTelefono(TextValidator.validarTelefono(this, txtTelefono));
}//GEN-LAST:event_txtTelefonoFocusLost

private void txtPisoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPisoFocusLost
    seleccion.setPiso(TextValidator.validarInt(this, txtPiso, null));
}//GEN-LAST:event_txtPisoFocusLost

private void txtNumeroFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNumeroFocusLost
    seleccion.setNumero(TextValidator.validarInt(this, txtNumero, null));
}//GEN-LAST:event_txtNumeroFocusLost

private void txtCPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCPFocusLost
    seleccion.setCodigoPostal(TextValidator.validarTexto(this, txtCP, 4, 8));
}//GEN-LAST:event_txtCPFocusLost

private void cboLocalidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboLocalidadActionPerformed
    if (cboLocalidad.getSelectedIndex() > -1) {
        Object sel = cboLocalidad.getSelectedItem();
        Localidad l = ((Localidad) sel);
        seleccion.setLocalidad(l);
    } else {
        seleccion.setLocalidad(null);
    }
}//GEN-LAST:event_cboLocalidadActionPerformed

private void cboProvinciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboProvinciaActionPerformed
    if (cboProvincia.getSelectedIndex() > -1) {
        Object sel = cboProvincia.getSelectedItem();
        Provincia p = ((Provincia) sel);
        mostrarLocalidades(p.getLocalidades());
    } else {
        mostrarLocalidades(null);
    }
}//GEN-LAST:event_cboProvinciaActionPerformed

private void txtCalleFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCalleFocusLost
    seleccion.setCalle(TextValidator.validarTexto(this, txtCalle, 2, 50));
}//GEN-LAST:event_txtCalleFocusLost

private void txtDptoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDptoFocusLost
    seleccion.setDepartamento(TextValidator.validarTexto(this, txtDpto, 1, 2));
}//GEN-LAST:event_txtDptoFocusLost

private void txtCuitFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCuitFocusLost
    seleccion.setCuit(TextValidator.validarCuit(this, txtCuit));
}//GEN-LAST:event_txtCuitFocusLost

private void txtRazonSocialFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRazonSocialFocusLost
    seleccion.setRazonSocial(TextValidator.validarTexto(this, txtRazonSocial, 2, 50));
}//GEN-LAST:event_txtRazonSocialFocusLost

private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
    opcionBaja();
}//GEN-LAST:event_btnQuitarActionPerformed

private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
    opcionRegistrarNuevo();
}//GEN-LAST:event_btnNuevoActionPerformed

private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
    btnNuevo.setEnabled(false);
    btnQuitar.setEnabled(false);
    btnAceptar.setEnabled(true);
    btnCancelar.setEnabled(true);
    btnModificar.setEnabled(false);
    btnBuscar.setEnabled(false);
    txtCP.setEditable(true);
    txtCuit.setEditable(true);
    txtCalle.setEditable(true);
    txtNumero.setEditable(true);
    txtEmail.setEditable(true);
    txtDpto.setEditable(true);
    txtFax.setEditable(true);
    txtPiso.setEditable(true);
    txtRazonSocial.setEditable(true);
    txtTelefono.setEditable(true);
    cboLocalidad.setEnabled(true);
    cboProvincia.setEnabled(true);
}//GEN-LAST:event_btnModificarActionPerformed

private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
    seleccion = Buscador.buscarProveedor(false);
    tomarSeleccionProveedor();
    if (seleccion != null) {
        btnModificar.setEnabled(true);
        btnQuitar.setEnabled(true);
    } else {
        btnModificar.setEnabled(false);
        btnQuitar.setEnabled(false);
        txtCP.setEditable(false);
        txtCuit.setEditable(false);
        txtCalle.setEditable(false);
        txtNumero.setEditable(false);
        txtEmail.setEditable(false);
        txtDpto.setEditable(false);
        txtFax.setEditable(false);
        txtPiso.setEditable(false);
        txtRazonSocial.setEditable(false);
        txtTelefono.setEditable(false);
        cboLocalidad.setEnabled(false);
        cboProvincia.setEnabled(false);
    }
}//GEN-LAST:event_btnBuscarActionPerformed

private void btnQuitarSeleccionProvinciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarSeleccionProvinciaActionPerformed
    cboProvincia.setSelectedIndex(-1);
}//GEN-LAST:event_btnQuitarSeleccionProvinciaActionPerformed

private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed
    if (ventanaInvocadora != null) {
        if (registrado) {
            ventanaInvocadora.proveedorRegistrado(seleccion);
        } else {
            ventanaInvocadora.proveedorRegistrado(null);
        }
    }
}//GEN-LAST:event_formInternalFrameClosed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JButton btnQuitarSeleccionProvincia;
    private javax.swing.JComboBox cboLocalidad;
    private javax.swing.JComboBox cboProvincia;
    private javax.swing.JLabel lblCP;
    private javax.swing.JLabel lblCalle;
    private javax.swing.JLabel lblCuit;
    private javax.swing.JLabel lblDpto;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblFax;
    private javax.swing.JLabel lblLocalidad;
    private javax.swing.JLabel lblNumero;
    private javax.swing.JLabel lblPiso;
    private javax.swing.JLabel lblProvincia;
    private javax.swing.JLabel lblRazon;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JPanel pnlDatos;
    private javax.swing.JPanel pnlDomicilio;
    private javax.swing.JPanel pnlSeleccion;
    private javax.swing.JTextField txtCP;
    private javax.swing.JTextField txtCalle;
    private javax.swing.JTextField txtCuit;
    private javax.swing.JTextField txtDpto;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFax;
    private javax.swing.JFormattedTextField txtNumero;
    private javax.swing.JFormattedTextField txtPiso;
    private javax.swing.JTextField txtRazonSocial;
    private javax.swing.JTextField txtSeleccion;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
