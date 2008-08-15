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
package gecom.app.productos;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.naming.NamingException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.InternationalFormatter;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.decoradores.ArchivoUtil;
import simbya.framework.decoradores.Fecha;
import simbya.framework.decoradores.Imagen;
import simbya.framework.excepciones.FaltanDatosRequeridosException;
import simbya.framework.excepciones.RegistroFallidoException;
import simbya.framework.validadores.ComboUtil;
import simbya.framework.validadores.TextValidator;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Producto;
import simbya.gecom.entidades.Proveedor;
import simbya.gecom.entidades.TipoCalidad;
import simbya.gecom.entidades.TipoCilindradaMoto;
import simbya.gecom.entidades.TipoMarca;
import simbya.gecom.entidades.TipoModelo;
import simbya.gecom.entidades.TipoRubro;
import simbya.gecom.entidades.TipoUnidadMedida;
import simbya.gecom.gestores.productos.GestorActualizarProductoRemote;

/**
 * Diálogo para registrar nuevos productos.
 * @author Marcelo Busico.
 */
public class DialogoRegistrarNuevoProducto extends javax.swing.JDialog {

    private static final Logger log = Logger.getLogger(
            DialogoRegistrarNuevoProducto.class);
    private GestorActualizarProductoRemote gestor = null;
    private DefaultTableModel modeloTablaCalidades = null;
    private Producto producto = null;
    private CalidadProducto calidadProducto = null;
    private int accionProducto = 0;
    private int accionCalidad = 0;
    private Set<CalidadProducto> calidades = null;
    private boolean imagenCargada = false;
    private File archivo = null;
    private static final int NADA = 0;
    private static final int ALTA = 1;
    private static final int MODIFICACION = 2;

    /** Creates new form DialogoRegistrarNuevoProducto */
    public DialogoRegistrarNuevoProducto(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        try {
            enlazarGestorRemoto(GestorConexion.getInstancia());
            inicializarVentana();
            this.setVisible(true);
        } catch (NamingException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al enlazar con gestor remoto:\n" + ex.getMessage());
            dispose();
        }
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    private void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorActualizarProductoRemote) gc.getObjetoRemoto(
                GestorActualizarProductoRemote.class);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    private void inicializarVentana() {
        modeloTablaCalidades = (DefaultTableModel) tblCalidades.getModel();
        accionCalidad = NADA;
        calidadProducto = null;
        imagenCargada = false;
        initTableListener();
        producto = new Producto();
        calidades = new TreeSet<CalidadProducto>();
        accionProducto = ALTA;
        limpiarContenido();
        cargarCombos();
        editarContenido(true);
        pnlSolapas.setSelectedIndex(0);
        txtDescripcion.requestFocus();
    }

    /**
     * Devuelve el producto creado por el usuario, null si se canceló.
     * @return Producto creado.
     */
    public Producto getProductoRegistrado() {
        return producto;
    }

    /**
     * Inicializa el listener que notifica de cambios 
     * en la selección de la tabla.
     */
    private void initTableListener() {
        tblCalidades.getSelectionModel().addListSelectionListener(
                new javax.swing.event.ListSelectionListener() {

                    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                        tblCalidadesValueChanged(e);
                    }
                });
    }

    private void tblCalidadesValueChanged(javax.swing.event.ListSelectionEvent evt) {
        if (tblCalidades.getSelectedRow() > -1) {
            calidadProducto = (CalidadProducto) tblCalidades.getValueAt(
                    tblCalidades.getSelectedRow(), 0);
            mostrarCalidadSeleccionada();
        }
    }

    private void mostrarCalidadSeleccionada() {
        cboCalidad.setSelectedItem(calidadProducto.getCalidad());
        if (calidadProducto.getProveedor() != null) {
            cboProveedorCalidad.setSelectedItem(calidadProducto.getProveedor());
        }
        txtCodigoProveedor.setText(calidadProducto.getCodigoProveedor());
        if (calidadProducto.getStockMinimo() != null) {
            txtStockMinimo.setValue(calidadProducto.getStockMinimo());
        } else {
            txtStockMinimo.setValue(0f);
        }
        if (calidadProducto.getStockMaximo() != null) {
            txtStockMaximo.setValue(calidadProducto.getStockMaximo());
        } else {
            txtStockMaximo.setValue(0f);
        }
        if (calidadProducto.isPrecioVentaFijo()) {
            optPrecioFijo.setSelected(true);
            txtValor.setValue(calidadProducto.getPrecioVenta());
        } else {
            optPrecioPorcentaje.setSelected(true);
            txtValor.setValue(calidadProducto.getPorcentajeGanancia());
        }
        btnCalidadModificar.setEnabled(true);
        btnCalidadQuitar.setEnabled(true);
    }

    private void editarContenido(boolean editable) {
        txtDescripcion.setEditable(editable);
        cboMarca.setEnabled(editable);
        cboRubro.setEnabled(editable);
        cboUnidadMedida.setEnabled(editable);
        cboCilindradaMoto.setEnabled(editable);
        cboModelo.setEnabled(editable);
        txtUbicacion.setEditable(editable);
        txtAnio.setEnabled(editable);
        tblCalidades.getSelectionModel().clearSelection();
        tblCalidades.setEnabled(false);
        editarCalidad(false);
        btnCalidadNueva.setEnabled(editable);
        cboDimensionesUM.setEnabled(editable);
        txtInterior.setEditable(editable);
        txtExterior.setEditable(editable);
        txtEspesor.setEditable(editable);
        btnFotografiaCargar.setEnabled(editable);
        btnAceptar.setEnabled(editable);
        btnCancelar.setEnabled(editable);
        btnRecargarListas.setEnabled(editable);
        if (lblFoto.getIcon() != null) {
            btnFotografiaQuitar.setEnabled(editable);
        } else {
            btnFotografiaQuitar.setEnabled(false);
        }
        if (editable) {
            if (opcionesPrecioVenta.getSelection() == null) {
                optPrecioFijo.setSelected(true);
            }
        }
    }

    private void editarCalidad(boolean editable) {
        pnlSolapas.setEnabledAt(0, !editable);
        pnlSolapas.setEnabledAt(2, !editable);
        cboCalidad.setEnabled(editable);
        cboProveedorCalidad.setEnabled(editable);
        btnQuitarSeleccionProveedorCalidad.setEnabled(editable);
        txtCodigoProveedor.setEditable(editable);
        txtStockMaximo.setEnabled(editable);
        txtStockMinimo.setEnabled(editable);
        optPrecioFijo.setEnabled(editable);
        optPrecioPorcentaje.setEnabled(editable);
        txtValor.setEditable(editable);
        btnCalidadNueva.setEnabled(!editable);
        btnCalidadAceptar.setEnabled(editable);
        btnCalidadCancelar.setEnabled(editable);
        btnAceptar.setEnabled(!editable);
        btnCancelar.setEnabled(!editable);
        if (editable) {
            btnCalidadModificar.setEnabled(false);
            btnCalidadQuitar.setEnabled(false);
            cboCalidad.requestFocus();
            tblCalidades.setEnabled(false);
        } else {
            calidadProducto = null;
            opcionesPrecioVenta.clearSelection();
            accionCalidad = NADA;
            tblCalidades.setEnabled(true);
        }
        tblCalidades.setEnabled(false);
    }

    private void limpiarContenido() {
        txtDescripcion.setText("");
        txtUbicacion.setText("");
        txtAnio.setValue(Fecha.getAnioActualSistema());
        txtInterior.setText("");
        txtExterior.setText("");
        txtEspesor.setText("");
        modeloTablaCalidades.setRowCount(0);
        setFotografia(null);
        limpiarCalidad();
    }

    private void limpiarCalidad() {
        txtCodigoProveedor.setText("");
        txtStockMaximo.setValue(0f);
        txtStockMinimo.setValue(0f);
        txtValor.setText("");
        cboCalidad.setSelectedIndex(-1);
        cboProveedorCalidad.setSelectedIndex(-1);
        optPrecioFijo.setSelected(true);
        tblCalidades.clearSelection();
        btnCalidadModificar.setEnabled(false);
        btnCalidadQuitar.setEnabled(false);
    }

    private void cargarCombos() {
        //Marcas
        new ComboUtil(cboMarca).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoMarca.class));

        //Rubros
        new ComboUtil(cboRubro).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoRubro.class));

        //Cilindradas Moto
        new ComboUtil(cboCilindradaMoto).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoCilindradaMoto.class));

        //Modelos
        new ComboUtil(cboModelo).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoModelo.class));

        //Proveedores Calidades
        new ComboUtil(cboProveedorCalidad).cleanAndLoad(gestor.cargarObjetosPersistentes(Proveedor.class));

        //Calidades
        new ComboUtil(cboCalidad).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoCalidad.class));

        //Unidades de Medida de Producto y Dimensiones
        List ums = gestor.cargarObjetosPersistentes(TipoUnidadMedida.class);
        new ComboUtil(cboUnidadMedida).cleanAndLoad(ums);
        new ComboUtil(cboDimensionesUM).cleanAndLoad(ums);

        //Limpiar selección
        cboRubro.setSelectedIndex(-1);
        cboMarca.setSelectedIndex(-1);
        cboCilindradaMoto.setSelectedIndex(-1);
        cboModelo.setSelectedIndex(-1);
        cboCalidad.setSelectedIndex(-1);
        cboProveedorCalidad.setSelectedIndex(-1);
        if (cboUnidadMedida.getItemCount() > 0) {
            cboUnidadMedida.setSelectedIndex(0);
        } else {
            cboUnidadMedida.setSelectedIndex(-1);
        }
        if (cboUnidadMedida.getItemCount() > 1) {
            cboDimensionesUM.setSelectedIndex(1);
        } else {
            cboDimensionesUM.setSelectedIndex(-1);
        }
    }

    private void cargarCalidades() {
        modeloTablaCalidades.setRowCount(0);
        if (producto == null || calidades == null) {
            return;
        }
        for (CalidadProducto cp : calidades) {
            calidadProducto = cp;
            agregarCalidadATabla();
        }
        calidadProducto = null;
    }

    private void agregarCalidadATabla() {
        if (calidadProducto == null) {
            throw new NullPointerException(
                    "El atributo calidadProducto no puede ser null al agregar" +
                    " calidad en tabla.");
        }
        Object[] fila = new Object[6];
        //Calidad
        fila[0] = calidadProducto;
        //Cod. Prov.
        fila[1] = calidadProducto.getCodigoProveedor();
        //Stock Min.
        fila[2] = calidadProducto.getStockMinimo();
        //Stock Max.
        fila[3] = calidadProducto.getStockMaximo();
        if (!calidadProducto.isPrecioVentaFijo()) {
            //% Ganancia
            fila[4] = calidadProducto.getPorcentajeGanancia();
            fila[5] = null;
        } else {
            fila[4] = null;
            //$ Venta Fijo
            fila[5] = calidadProducto.getPrecioVenta();
        }
        modeloTablaCalidades.addRow(fila);
    }

    private void setFotografia(File path) {
        archivo = path;
        if (path != null) {
            Icon icono = new ImageIcon(path.getPath());
            lblFoto.setIcon(icono);
            lblFoto.setText("");
            lblFoto.setBackground(new Color(255, 255, 255));
            imagenCargada = true;
        } else {
            lblFoto.setIcon(null);
            lblFoto.setText("No disponible");
            lblFoto.setBackground(new Color(0, 0, 153));
            btnFotografiaQuitar.setEnabled(false);
            imagenCargada = false;
        }
    }

    private void verificarDatosRequeridos() throws FaltanDatosRequeridosException {
        String finMensaje = "\nDebe completar todos los datos requeridos para continuar.";
        //Descripción
        if (producto.getDescripcion() == null || producto.getDescripcion().isEmpty()) {
            throw new FaltanDatosRequeridosException(
                    "Falta ingresar descripción." + finMensaje);
        }
        //Marca
        if (producto.getMarca() == null) {
            throw new FaltanDatosRequeridosException(
                    "Falta seleccionar marca." + finMensaje);
        }

        //Rubro
        if (producto.getRubro() == null) {
            throw new FaltanDatosRequeridosException(
                    "Falta seleccionar rubro." + finMensaje);
        }

        //UM
        if (producto.getProductoUM() == null) {
            throw new FaltanDatosRequeridosException(
                    "Falta seleccionar la unidad de medida del producto." + finMensaje);
        }

        //Al menos una calidad
        if (calidades == null || calidades.size() == 0) {
            throw new FaltanDatosRequeridosException(
                    "Falta ingresar al menos una calidad del producto." + finMensaje);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        opcionesPrecioVenta = new javax.swing.ButtonGroup();
        pnlSolapas = new javax.swing.JTabbedPane();
        pnlDatosPrincipales = new javax.swing.JPanel();
        pnlDatosObligatorios = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        cboRubro = new javax.swing.JComboBox();
        cboMarca = new javax.swing.JComboBox();
        cboUnidadMedida = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        pnlDatosOptativos = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        cboCilindradaMoto = new javax.swing.JComboBox();
        cboModelo = new javax.swing.JComboBox();
        txtAnio = new javax.swing.JSpinner();
        txtUbicacion = new javax.swing.JTextField();
        btnQuitarSeleccionCilindrada = new javax.swing.JButton();
        btnQuitarSeleccionModelo = new javax.swing.JButton();
        pnlCalidadesYPrecios = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCalidades = new javax.swing.JTable();
        pnlCalidadIndividual = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        cboCalidad = new javax.swing.JComboBox();
        pnlPreciodeVenta = new javax.swing.JPanel();
        optPrecioFijo = new javax.swing.JRadioButton();
        optPrecioPorcentaje = new javax.swing.JRadioButton();
        jLabel15 = new javax.swing.JLabel();
        InternationalFormatter formatoValor = new InternationalFormatter();
        formatoValor.setMinimum(new Float(0));
        txtValor = new javax.swing.JFormattedTextField(formatoValor);
        txtValor.setValue(new Float(0));
        txtValor.setText("");
        jLabel16 = new javax.swing.JLabel();
        txtCodigoProveedor = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtStockMinimo = new javax.swing.JSpinner();
        txtStockMaximo = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        cboProveedorCalidad = new javax.swing.JComboBox();
        btnQuitarSeleccionProveedorCalidad = new javax.swing.JButton();
        btnCalidadNueva = new javax.swing.JButton();
        btnCalidadModificar = new javax.swing.JButton();
        btnCalidadQuitar = new javax.swing.JButton();
        btnCalidadCancelar = new javax.swing.JButton();
        btnCalidadAceptar = new javax.swing.JButton();
        pnlMedidasYFotografia = new javax.swing.JPanel();
        pnlFotografia = new javax.swing.JPanel();
        btnFotografiaCargar = new javax.swing.JButton();
        btnFotografiaQuitar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        lblFoto = new javax.swing.JLabel();
        pnlMedidas = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        cboDimensionesUM = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        InternationalFormatter formatoMedidaInterior = new InternationalFormatter();
        formatoMedidaInterior.setMinimum(new Float(0));
        txtInterior = new javax.swing.JFormattedTextField(formatoMedidaInterior);
        InternationalFormatter formatoMedidaExterior = new InternationalFormatter();
        formatoMedidaExterior.setMinimum(new Float(0));
        txtExterior = new javax.swing.JFormattedTextField(formatoMedidaExterior);
        InternationalFormatter formatoMedidaEspesor = new InternationalFormatter();
        formatoMedidaEspesor.setMinimum(new Float(0));
        txtEspesor = new javax.swing.JFormattedTextField(formatoMedidaEspesor);
        btnRecargarListas = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnAceptar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(DialogoRegistrarNuevoProducto.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(771, 389));
        setName("Form"); // NOI18N

        pnlSolapas.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        pnlSolapas.setName("pnlSolapas"); // NOI18N

        pnlDatosPrincipales.setName("pnlDatosPrincipales"); // NOI18N

        pnlDatosObligatorios.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        pnlDatosObligatorios.setName("pnlDatosObligatorios"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        txtDescripcion.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtDescripcion.setName("txtDescripcion"); // NOI18N

        cboRubro.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboRubro.setName("cboRubro"); // NOI18N

        cboMarca.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboMarca.setName("cboMarca"); // NOI18N

        cboUnidadMedida.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboUnidadMedida.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Unidades" }));
        cboUnidadMedida.setName("cboUnidadMedida"); // NOI18N

        jLabel13.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        javax.swing.GroupLayout pnlDatosObligatoriosLayout = new javax.swing.GroupLayout(pnlDatosObligatorios);
        pnlDatosObligatorios.setLayout(pnlDatosObligatoriosLayout);
        pnlDatosObligatoriosLayout.setHorizontalGroup(
            pnlDatosObligatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosObligatoriosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatosObligatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosObligatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatosObligatoriosLayout.createSequentialGroup()
                        .addComponent(cboRubro, 0, 362, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboUnidadMedida, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cboMarca, 0, 582, Short.MAX_VALUE)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlDatosObligatoriosLayout.setVerticalGroup(
            pnlDatosObligatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosObligatoriosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatosObligatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosObligatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosObligatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(cboRubro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboUnidadMedida, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlDatosOptativos.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        pnlDatosOptativos.setName("pnlDatosOptativos"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel8.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        cboCilindradaMoto.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboCilindradaMoto.setName("cboCilindradaMoto"); // NOI18N

        cboModelo.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboModelo.setName("cboModelo"); // NOI18N

        txtAnio.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtAnio.setName("txtAnio"); // NOI18N

        txtUbicacion.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtUbicacion.setName("txtUbicacion"); // NOI18N

        btnQuitarSeleccionCilindrada.setFont(resourceMap.getFont("btnQuitarSeleccionCilindrada.font")); // NOI18N
        btnQuitarSeleccionCilindrada.setIcon(resourceMap.getIcon("btnQuitarSeleccionCilindrada.icon")); // NOI18N
        btnQuitarSeleccionCilindrada.setEnabled(false);
        btnQuitarSeleccionCilindrada.setName("btnQuitarSeleccionCilindrada"); // NOI18N
        btnQuitarSeleccionCilindrada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarSeleccionCilindradaActionPerformed(evt);
            }
        });

        btnQuitarSeleccionModelo.setFont(resourceMap.getFont("btnQuitarSeleccionModelo.font")); // NOI18N
        btnQuitarSeleccionModelo.setIcon(resourceMap.getIcon("btnQuitarSeleccionModelo.icon")); // NOI18N
        btnQuitarSeleccionModelo.setEnabled(false);
        btnQuitarSeleccionModelo.setName("btnQuitarSeleccionModelo"); // NOI18N
        btnQuitarSeleccionModelo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarSeleccionModeloActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlDatosOptativosLayout = new javax.swing.GroupLayout(pnlDatosOptativos);
        pnlDatosOptativos.setLayout(pnlDatosOptativosLayout);
        pnlDatosOptativosLayout.setHorizontalGroup(
            pnlDatosOptativosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosOptativosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatosOptativosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosOptativosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatosOptativosLayout.createSequentialGroup()
                        .addComponent(cboModelo, 0, 365, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitarSeleccionModelo)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAnio, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatosOptativosLayout.createSequentialGroup()
                        .addComponent(cboCilindradaMoto, 0, 506, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitarSeleccionCilindrada))
                    .addComponent(txtUbicacion, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlDatosOptativosLayout.setVerticalGroup(
            pnlDatosOptativosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosOptativosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatosOptativosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlDatosOptativosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(cboCilindradaMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnQuitarSeleccionCilindrada))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosOptativosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDatosOptativosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtAnio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6)
                        .addComponent(jLabel5)
                        .addComponent(cboModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnQuitarSeleccionModelo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatosOptativosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUbicacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlDatosPrincipalesLayout = new javax.swing.GroupLayout(pnlDatosPrincipales);
        pnlDatosPrincipales.setLayout(pnlDatosPrincipalesLayout);
        pnlDatosPrincipalesLayout.setHorizontalGroup(
            pnlDatosPrincipalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosPrincipalesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatosPrincipalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlDatosObligatorios, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDatosOptativos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlDatosPrincipalesLayout.setVerticalGroup(
            pnlDatosPrincipalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosPrincipalesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlDatosObligatorios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlDatosOptativos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        pnlSolapas.addTab(resourceMap.getString("pnlDatosPrincipales.TabConstraints.tabTitle"), pnlDatosPrincipales); // NOI18N

        pnlCalidadesYPrecios.setName("pnlCalidadesYPrecios"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tblCalidades.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        tblCalidades.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Calidad", "Cod. Proveedor", "Stock Mínimo", "Stock Máximo", "% Ganancia", "$ Venta Fijo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCalidades.setName("tblCalidades"); // NOI18N
        tblCalidades.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tblCalidades);

        pnlCalidadIndividual.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlCalidadIndividual.setName("pnlCalidadIndividual"); // NOI18N

        jLabel14.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        cboCalidad.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboCalidad.setName("cboCalidad"); // NOI18N

        pnlPreciodeVenta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Precio de Venta:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 14))); // NOI18N
        pnlPreciodeVenta.setName("pnlPreciodeVenta"); // NOI18N

        opcionesPrecioVenta.add(optPrecioFijo);
        optPrecioFijo.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        optPrecioFijo.setSelected(true);
        optPrecioFijo.setText(resourceMap.getString("optPrecioFijo.text")); // NOI18N
        optPrecioFijo.setName("optPrecioFijo"); // NOI18N

        opcionesPrecioVenta.add(optPrecioPorcentaje);
        optPrecioPorcentaje.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        optPrecioPorcentaje.setText(resourceMap.getString("optPrecioPorcentaje.text")); // NOI18N
        optPrecioPorcentaje.setToolTipText(resourceMap.getString("optPrecioPorcentaje.toolTipText")); // NOI18N
        optPrecioPorcentaje.setName("optPrecioPorcentaje"); // NOI18N

        jLabel15.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        txtValor.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
        txtValor.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtValor.setName("txtValor"); // NOI18N
        txtValor.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtValorFocusLost(evt);
            }
        });

        javax.swing.GroupLayout pnlPreciodeVentaLayout = new javax.swing.GroupLayout(pnlPreciodeVenta);
        pnlPreciodeVenta.setLayout(pnlPreciodeVentaLayout);
        pnlPreciodeVentaLayout.setHorizontalGroup(
            pnlPreciodeVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPreciodeVentaLayout.createSequentialGroup()
                .addGroup(pnlPreciodeVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlPreciodeVentaLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(optPrecioFijo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(optPrecioPorcentaje))
                    .addGroup(pnlPreciodeVentaLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValor)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlPreciodeVentaLayout.setVerticalGroup(
            pnlPreciodeVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPreciodeVentaLayout.createSequentialGroup()
                .addGroup(pnlPreciodeVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(optPrecioFijo)
                    .addComponent(optPrecioPorcentaje))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlPreciodeVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jLabel16.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        txtCodigoProveedor.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtCodigoProveedor.setName("txtCodigoProveedor"); // NOI18N

        jLabel17.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        jLabel18.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        txtStockMinimo.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtStockMinimo.setName("txtStockMinimo"); // NOI18N

        txtStockMaximo.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtStockMaximo.setName("txtStockMaximo"); // NOI18N

        jLabel19.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        cboProveedorCalidad.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboProveedorCalidad.setName("cboProveedorCalidad"); // NOI18N

        btnQuitarSeleccionProveedorCalidad.setFont(resourceMap.getFont("btnQuitarSeleccionProveedorCalidad.font")); // NOI18N
        btnQuitarSeleccionProveedorCalidad.setIcon(resourceMap.getIcon("btnQuitarSeleccionProveedorCalidad.icon")); // NOI18N
        btnQuitarSeleccionProveedorCalidad.setEnabled(false);
        btnQuitarSeleccionProveedorCalidad.setName("btnQuitarSeleccionProveedorCalidad"); // NOI18N
        btnQuitarSeleccionProveedorCalidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarSeleccionProveedorCalidadActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlCalidadIndividualLayout = new javax.swing.GroupLayout(pnlCalidadIndividual);
        pnlCalidadIndividual.setLayout(pnlCalidadIndividualLayout);
        pnlCalidadIndividualLayout.setHorizontalGroup(
            pnlCalidadIndividualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCalidadIndividualLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCalidadIndividualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel19)
                    .addComponent(jLabel14)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCalidadIndividualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCalidadIndividualLayout.createSequentialGroup()
                        .addComponent(txtStockMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtStockMaximo, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCalidadIndividualLayout.createSequentialGroup()
                        .addComponent(cboCalidad, 0, 194, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCodigoProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCalidadIndividualLayout.createSequentialGroup()
                        .addComponent(cboProveedorCalidad, 0, 316, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitarSeleccionProveedorCalidad)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlPreciodeVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlCalidadIndividualLayout.setVerticalGroup(
            pnlCalidadIndividualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCalidadIndividualLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCalidadIndividualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCalidadIndividualLayout.createSequentialGroup()
                        .addGroup(pnlCalidadIndividualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(jLabel16)
                            .addComponent(txtCodigoProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cboCalidad, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlCalidadIndividualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlCalidadIndividualLayout.createSequentialGroup()
                                .addGroup(pnlCalidadIndividualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel19)
                                    .addComponent(cboProveedorCalidad, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlCalidadIndividualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel17)
                                    .addComponent(jLabel18)
                                    .addComponent(txtStockMaximo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtStockMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(btnQuitarSeleccionProveedorCalidad)))
                    .addComponent(pnlPreciodeVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        btnCalidadNueva.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCalidadNueva.setIcon(resourceMap.getIcon("btnCalidadNueva.icon")); // NOI18N
        btnCalidadNueva.setText(resourceMap.getString("btnCalidadNueva.text")); // NOI18N
        btnCalidadNueva.setEnabled(false);
        btnCalidadNueva.setName("btnCalidadNueva"); // NOI18N
        btnCalidadNueva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalidadNuevaActionPerformed(evt);
            }
        });

        btnCalidadModificar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCalidadModificar.setIcon(resourceMap.getIcon("btnCalidadModificar.icon")); // NOI18N
        btnCalidadModificar.setText(resourceMap.getString("btnCalidadModificar.text")); // NOI18N
        btnCalidadModificar.setEnabled(false);
        btnCalidadModificar.setName("btnCalidadModificar"); // NOI18N
        btnCalidadModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalidadModificarActionPerformed(evt);
            }
        });

        btnCalidadQuitar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCalidadQuitar.setIcon(resourceMap.getIcon("btnCalidadQuitar.icon")); // NOI18N
        btnCalidadQuitar.setText(resourceMap.getString("btnCalidadQuitar.text")); // NOI18N
        btnCalidadQuitar.setEnabled(false);
        btnCalidadQuitar.setName("btnCalidadQuitar"); // NOI18N
        btnCalidadQuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalidadQuitarActionPerformed(evt);
            }
        });

        btnCalidadCancelar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCalidadCancelar.setIcon(resourceMap.getIcon("btnCalidadCancelar.icon")); // NOI18N
        btnCalidadCancelar.setMnemonic('c');
        btnCalidadCancelar.setText(resourceMap.getString("btnCalidadCancelar.text")); // NOI18N
        btnCalidadCancelar.setEnabled(false);
        btnCalidadCancelar.setName("btnCalidadCancelar"); // NOI18N
        btnCalidadCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalidadCancelarActionPerformed(evt);
            }
        });

        btnCalidadAceptar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCalidadAceptar.setIcon(resourceMap.getIcon("btnCalidadAceptar.icon")); // NOI18N
        btnCalidadAceptar.setMnemonic('a');
        btnCalidadAceptar.setText(resourceMap.getString("btnCalidadAceptar.text")); // NOI18N
        btnCalidadAceptar.setEnabled(false);
        btnCalidadAceptar.setName("btnCalidadAceptar"); // NOI18N
        btnCalidadAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalidadAceptarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlCalidadesYPreciosLayout = new javax.swing.GroupLayout(pnlCalidadesYPrecios);
        pnlCalidadesYPrecios.setLayout(pnlCalidadesYPreciosLayout);
        pnlCalidadesYPreciosLayout.setHorizontalGroup(
            pnlCalidadesYPreciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCalidadesYPreciosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCalidadesYPreciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
                    .addGroup(pnlCalidadesYPreciosLayout.createSequentialGroup()
                        .addComponent(btnCalidadNueva)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCalidadModificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCalidadQuitar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 157, Short.MAX_VALUE)
                        .addComponent(btnCalidadAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCalidadCancelar))
                    .addComponent(pnlCalidadIndividual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlCalidadesYPreciosLayout.setVerticalGroup(
            pnlCalidadesYPreciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCalidadesYPreciosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCalidadIndividual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCalidadesYPreciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCalidadNueva)
                    .addComponent(btnCalidadModificar)
                    .addComponent(btnCalidadQuitar)
                    .addComponent(btnCalidadCancelar)
                    .addComponent(btnCalidadAceptar))
                .addContainerGap())
        );

        pnlSolapas.addTab(resourceMap.getString("pnlCalidadesYPrecios.TabConstraints.tabTitle"), pnlCalidadesYPrecios); // NOI18N

        pnlMedidasYFotografia.setName("pnlMedidasYFotografia"); // NOI18N

        pnlFotografia.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Fotografía:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 14))); // NOI18N
        pnlFotografia.setName("pnlFotografia"); // NOI18N

        btnFotografiaCargar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnFotografiaCargar.setIcon(resourceMap.getIcon("btnFotografiaCargar.icon")); // NOI18N
        btnFotografiaCargar.setText(resourceMap.getString("btnFotografiaCargar.text")); // NOI18N
        btnFotografiaCargar.setName("btnFotografiaCargar"); // NOI18N
        btnFotografiaCargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFotografiaCargarActionPerformed(evt);
            }
        });

        btnFotografiaQuitar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnFotografiaQuitar.setIcon(resourceMap.getIcon("btnFotografiaQuitar.icon")); // NOI18N
        btnFotografiaQuitar.setText(resourceMap.getString("btnFotografiaQuitar.text")); // NOI18N
        btnFotografiaQuitar.setName("btnFotografiaQuitar"); // NOI18N
        btnFotografiaQuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFotografiaQuitarActionPerformed(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        lblFoto.setBackground(resourceMap.getColor("lblFoto.background")); // NOI18N
        lblFoto.setFont(resourceMap.getFont("Application.font.big")); // NOI18N
        lblFoto.setForeground(resourceMap.getColor("lblFoto.foreground")); // NOI18N
        lblFoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFoto.setText(resourceMap.getString("lblFoto.text")); // NOI18N
        lblFoto.setName("lblFoto"); // NOI18N
        lblFoto.setOpaque(true);
        jScrollPane2.setViewportView(lblFoto);

        javax.swing.GroupLayout pnlFotografiaLayout = new javax.swing.GroupLayout(pnlFotografia);
        pnlFotografia.setLayout(pnlFotografiaLayout);
        pnlFotografiaLayout.setHorizontalGroup(
            pnlFotografiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFotografiaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFotografiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnFotografiaQuitar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnFotografiaCargar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlFotografiaLayout.setVerticalGroup(
            pnlFotografiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFotografiaLayout.createSequentialGroup()
                .addGroup(pnlFotografiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addGroup(pnlFotografiaLayout.createSequentialGroup()
                        .addContainerGap(169, Short.MAX_VALUE)
                        .addComponent(btnFotografiaQuitar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFotografiaCargar)))
                .addContainerGap())
        );

        pnlMedidas.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Medidas:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 14))); // NOI18N
        pnlMedidas.setName("pnlMedidas"); // NOI18N

        jLabel9.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel11.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        cboDimensionesUM.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboDimensionesUM.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Milímetros", "Centímetros", "Pulgadas" }));
        cboDimensionesUM.setName("cboDimensionesUM"); // NOI18N

        jLabel12.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        txtInterior.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
        txtInterior.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtInterior.setName("txtInterior"); // NOI18N
        txtInterior.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtInteriorFocusLost(evt);
            }
        });

        txtExterior.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
        txtExterior.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtExterior.setName("txtExterior"); // NOI18N
        txtExterior.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtExteriorFocusLost(evt);
            }
        });

        txtEspesor.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
        txtEspesor.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtEspesor.setName("txtEspesor"); // NOI18N
        txtEspesor.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtEspesorFocusLost(evt);
            }
        });

        javax.swing.GroupLayout pnlMedidasLayout = new javax.swing.GroupLayout(pnlMedidas);
        pnlMedidas.setLayout(pnlMedidasLayout);
        pnlMedidasLayout.setHorizontalGroup(
            pnlMedidasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMedidasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMedidasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtInterior, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboDimensionesUM, javax.swing.GroupLayout.Alignment.LEADING, 0, 106, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtExterior, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtEspesor, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlMedidasLayout.setVerticalGroup(
            pnlMedidasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMedidasLayout.createSequentialGroup()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cboDimensionesUM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addGap(3, 3, 3)
                .addComponent(txtInterior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtExterior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEspesor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlMedidasYFotografiaLayout = new javax.swing.GroupLayout(pnlMedidasYFotografia);
        pnlMedidasYFotografia.setLayout(pnlMedidasYFotografiaLayout);
        pnlMedidasYFotografiaLayout.setHorizontalGroup(
            pnlMedidasYFotografiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMedidasYFotografiaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlMedidas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlFotografia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlMedidasYFotografiaLayout.setVerticalGroup(
            pnlMedidasYFotografiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMedidasYFotografiaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMedidasYFotografiaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlFotografia, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlMedidas, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pnlSolapas.addTab(resourceMap.getString("pnlMedidasYFotografia.TabConstraints.tabTitle"), pnlMedidasYFotografia); // NOI18N

        btnRecargarListas.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnRecargarListas.setIcon(resourceMap.getIcon("btnRecargarListas.icon")); // NOI18N
        btnRecargarListas.setText(resourceMap.getString("btnRecargarListas.text")); // NOI18N
        btnRecargarListas.setEnabled(false);
        btnRecargarListas.setName("btnRecargarListas"); // NOI18N
        btnRecargarListas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecargarListasActionPerformed(evt);
            }
        });

        btnCancelar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
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

        btnAceptar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSolapas, javax.swing.GroupLayout.DEFAULT_SIZE, 747, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnRecargarListas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 345, Short.MAX_VALUE)
                        .addComponent(btnAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSolapas, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRecargarListas)
                    .addComponent(btnCancelar)
                    .addComponent(btnAceptar))
                .addContainerGap())
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-781)/2, (screenSize.height-419)/2, 781, 419);
    }// </editor-fold>//GEN-END:initComponents

private void btnQuitarSeleccionCilindradaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarSeleccionCilindradaActionPerformed
    cboCilindradaMoto.setSelectedIndex(-1);
}//GEN-LAST:event_btnQuitarSeleccionCilindradaActionPerformed

private void btnQuitarSeleccionModeloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarSeleccionModeloActionPerformed
    cboModelo.setSelectedIndex(-1);
}//GEN-LAST:event_btnQuitarSeleccionModeloActionPerformed

private void txtValorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtValorFocusLost
    TextValidator.validarFloat(this, txtValor, null);
}//GEN-LAST:event_txtValorFocusLost

private void btnQuitarSeleccionProveedorCalidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarSeleccionProveedorCalidadActionPerformed
    cboProveedorCalidad.setSelectedIndex(-1);
}//GEN-LAST:event_btnQuitarSeleccionProveedorCalidadActionPerformed

private void btnCalidadNuevaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalidadNuevaActionPerformed
    calidadProducto = new CalidadProducto();
    accionCalidad = ALTA;
    editarCalidad(true);
}//GEN-LAST:event_btnCalidadNuevaActionPerformed

private void btnCalidadModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalidadModificarActionPerformed
    accionCalidad = MODIFICACION;
    editarCalidad(true);
}//GEN-LAST:event_btnCalidadModificarActionPerformed

private void btnCalidadQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalidadQuitarActionPerformed
//Mensaje
    String mensaje = "Esta acción quitará la calidad seleccionada de la tabla.\n" +
            "¿Desea continuar?";
    int res = JOptionPane.showConfirmDialog(this, mensaje, "Confirme", JOptionPane.YES_NO_OPTION);
    if (res == JOptionPane.OK_OPTION) {
        if (!calidades.remove(calidadProducto)) {
            throw new RuntimeException("No se pudo quitar la calidad del producto.");
        }
        modeloTablaCalidades.removeRow(tblCalidades.getSelectedRow());
        limpiarCalidad();
    }
}//GEN-LAST:event_btnCalidadQuitarActionPerformed

private void btnCalidadCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalidadCancelarActionPerformed
    editarCalidad(false);
}//GEN-LAST:event_btnCalidadCancelarActionPerformed

private void btnCalidadAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalidadAceptarActionPerformed
//Verificar datos obligatorios
    if (cboCalidad.getSelectedIndex() == -1) {
        JOptionPane.showMessageDialog(this,
                "Debe seleccionar el tipo de calidad a agregar.");
        return;
    }
    Float min = TextValidator.objectToFloat(txtStockMinimo.getValue());
    Float max = TextValidator.objectToFloat(txtStockMaximo.getValue());
    if (min > max) {
        JOptionPane.showMessageDialog(this,
                "El stock máximo no puede ser menor al stock mínimo.");
        return;
    }
    float valor = TextValidator.validarFloat(this, txtValor, null);
    if (valor <= 0) {
        JOptionPane.showMessageDialog(this,
                "Debe ingresar un valor adecuado para el precio de venta.");
        return;
    }
    //Verificar que la calidad no esté en la tabla.
    for (int i = 0; i < tblCalidades.getRowCount(); i++) {
        if (accionCalidad != MODIFICACION || i != tblCalidades.getSelectedRow()) {
            Object celda = modeloTablaCalidades.getValueAt(i, 0);
            CalidadProducto cp = (CalidadProducto) celda;
            if (cp.getCalidad().equals(cboCalidad.getSelectedItem())) {
                JOptionPane.showMessageDialog(this,
                        "La calidad seleccionada no es un nuevo tipo de calidad\n" +
                        "para el producto actual.\nSeleccione otra calidad.");
                return;
            }
        }
    }

    //Agregar calidad
    calidadProducto.setCalidad((TipoCalidad) cboCalidad.getSelectedItem());
    calidadProducto.setCodigoProveedor(txtCodigoProveedor.getText());
    if (optPrecioPorcentaje.isSelected()) {
        calidadProducto.setPorcentajeGanancia(valor);
        calidadProducto.setPrecioVenta(0f);
        calidadProducto.setPrecioVentaFijo(false);
    } else {
        calidadProducto.setPorcentajeGanancia(0f);
        calidadProducto.setPrecioVenta(valor);
        calidadProducto.setPrecioVentaFijo(true);
    }
    if (cboProveedorCalidad.getSelectedIndex() != -1) {
        calidadProducto.setProveedor((Proveedor) cboProveedorCalidad.getSelectedItem());
    } else {
        calidadProducto.setProveedor(null);
    }
    calidadProducto.setStockMaximo(max);
    calidadProducto.setStockMinimo(min);
    if (accionCalidad == ALTA) {
        agregarCalidadATabla();
        calidades.add(calidadProducto);
    } else {
        cargarCalidades();
    }
    editarCalidad(false);
}//GEN-LAST:event_btnCalidadAceptarActionPerformed

private void btnFotografiaCargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFotografiaCargarActionPerformed
    File f = Imagen.verDialogoSeleccion(this);
    if (f == null) {
        return;
    }
    setFotografia(f);
    btnFotografiaQuitar.setEnabled(true);
}//GEN-LAST:event_btnFotografiaCargarActionPerformed

private void btnFotografiaQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFotografiaQuitarActionPerformed
//Mensaje
    String mensaje = "Esta acción quitará la fotografía del producto.\n" +
            "¿Desea continuar?";
    int res = JOptionPane.showConfirmDialog(this, mensaje, "Confirme", JOptionPane.YES_NO_OPTION);
    if (res == JOptionPane.OK_OPTION) {
        setFotografia(null);
    }
}//GEN-LAST:event_btnFotografiaQuitarActionPerformed

private void txtInteriorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtInteriorFocusLost
    TextValidator.validarFloat(this, txtInterior, null);
}//GEN-LAST:event_txtInteriorFocusLost

private void txtExteriorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtExteriorFocusLost
    TextValidator.validarFloat(this, txtExterior, null);
}//GEN-LAST:event_txtExteriorFocusLost

private void txtEspesorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtEspesorFocusLost
    TextValidator.validarFloat(this, txtEspesor, null);
}//GEN-LAST:event_txtEspesorFocusLost

private void btnRecargarListasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecargarListasActionPerformed
//Mensaje
    String mensaje = "Esta opción cargará nuevamente todas las listas\n" +
            "desplegables de la ventana. Al hacerlo se borrará todo lo que\n" +
            "se haya seleccionado con anterioridad en las mismas.\n" +
            "¿Desea continuar?";
    int res = JOptionPane.showConfirmDialog(this, mensaje, "Confirme", JOptionPane.YES_NO_OPTION);
    if (res == JOptionPane.OK_OPTION) {
        cargarCombos();
    }
}//GEN-LAST:event_btnRecargarListasActionPerformed

private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
    producto = null;
    dispose();
}//GEN-LAST:event_btnCancelarActionPerformed

private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
    //Tomar todos los datos del producto.
    producto.setAnio((Integer) txtAnio.getValue());
    producto.setBaja(false);
    producto.setCilindradaMoto(new ComboUtil<TipoCilindradaMoto>(cboCilindradaMoto).getSelected());
    producto.setDescripcion(TextValidator.validarTexto(this, txtDescripcion, 1, 1000));
    producto.setDimensionUM(new ComboUtil<TipoUnidadMedida>(cboDimensionesUM).getSelected());
    producto.setMarca(new ComboUtil<TipoMarca>(cboMarca).getSelected());
    producto.setModelo(new ComboUtil<TipoModelo>(cboModelo).getSelected());
    producto.setProductoUM(new ComboUtil<TipoUnidadMedida>(cboUnidadMedida).getSelected());
    producto.setRubro(new ComboUtil<TipoRubro>(cboRubro).getSelected());
    producto.setUbicacionEstanteria(txtUbicacion.getText());
    producto.setMedidaEspesor(TextValidator.getFloatValue(txtEspesor));
    producto.setMedidaExterior(TextValidator.getFloatValue(txtExterior));
    producto.setMedidaInterior(TextValidator.getFloatValue(txtInterior));
    try {
        //Verificar datos obligatorios.
        verificarDatosRequeridos();
    } catch (FaltanDatosRequeridosException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage());
        return;
    }

    //Cargar la imagen si corresponde.
    try {
        if (imagenCargada) {
            gestor.cargarImagen(ArchivoUtil.fileToArray(archivo));
        } else {
            if (lblFoto.getIcon() == null) {
                gestor.cargarImagen(null);
            }
        }
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Error al leer la imagen del producto.\n" +
                "Se cancela la actualización.");
        return;
    }

    producto.setCalidades(calidades);

    try {
        Producto pdb = gestor.confirmarActualizacion(true, producto);
        producto = pdb;
        dispose();
    } catch (RegistroFallidoException rfe) {
        JOptionPane.showMessageDialog(this, rfe);
    }
}//GEN-LAST:event_btnAceptarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnCalidadAceptar;
    private javax.swing.JButton btnCalidadCancelar;
    private javax.swing.JButton btnCalidadModificar;
    private javax.swing.JButton btnCalidadNueva;
    private javax.swing.JButton btnCalidadQuitar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnFotografiaCargar;
    private javax.swing.JButton btnFotografiaQuitar;
    private javax.swing.JButton btnQuitarSeleccionCilindrada;
    private javax.swing.JButton btnQuitarSeleccionModelo;
    private javax.swing.JButton btnQuitarSeleccionProveedorCalidad;
    private javax.swing.JButton btnRecargarListas;
    private javax.swing.JComboBox cboCalidad;
    private javax.swing.JComboBox cboCilindradaMoto;
    private javax.swing.JComboBox cboDimensionesUM;
    private javax.swing.JComboBox cboMarca;
    private javax.swing.JComboBox cboModelo;
    private javax.swing.JComboBox cboProveedorCalidad;
    private javax.swing.JComboBox cboRubro;
    private javax.swing.JComboBox cboUnidadMedida;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblFoto;
    private javax.swing.ButtonGroup opcionesPrecioVenta;
    private javax.swing.JRadioButton optPrecioFijo;
    private javax.swing.JRadioButton optPrecioPorcentaje;
    private javax.swing.JPanel pnlCalidadIndividual;
    private javax.swing.JPanel pnlCalidadesYPrecios;
    private javax.swing.JPanel pnlDatosObligatorios;
    private javax.swing.JPanel pnlDatosOptativos;
    private javax.swing.JPanel pnlDatosPrincipales;
    private javax.swing.JPanel pnlFotografia;
    private javax.swing.JPanel pnlMedidas;
    private javax.swing.JPanel pnlMedidasYFotografia;
    private javax.swing.JPanel pnlPreciodeVenta;
    private javax.swing.JTabbedPane pnlSolapas;
    private javax.swing.JTable tblCalidades;
    private javax.swing.JSpinner txtAnio;
    private javax.swing.JTextField txtCodigoProveedor;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JFormattedTextField txtEspesor;
    private javax.swing.JFormattedTextField txtExterior;
    private javax.swing.JFormattedTextField txtInterior;
    private javax.swing.JSpinner txtStockMaximo;
    private javax.swing.JSpinner txtStockMinimo;
    private javax.swing.JTextField txtUbicacion;
    private javax.swing.JFormattedTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
