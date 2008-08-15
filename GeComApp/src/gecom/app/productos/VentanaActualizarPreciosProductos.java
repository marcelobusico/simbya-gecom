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

import gecom.app.buscador.Buscador;
import java.util.List;
import javax.naming.NamingException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.interfaces.VentanaInterna;
import simbya.framework.validadores.ComboUtil;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Proveedor;
import simbya.gecom.entidades.TipoCalidad;
import simbya.gecom.entidades.TipoRubro;
import simbya.gecom.gestores.productos.GestorActualizarPreciosProductosRemote;

/**
 * Ventana para actualizar precios de productos de un proveedor por rubro.
 * @author Marcelo Busico.
 */
public class VentanaActualizarPreciosProductos extends VentanaInterna {

    private GestorActualizarPreciosProductosRemote gestor = null;
    private DefaultTableModel modeloTablaProductos = null;
    private Proveedor proveedor;
    private List<CalidadProducto> calidades;
    private static final int COLUMNA_CODIGO = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_MARCA = 2;
    private static final int COLUMNA_CALIDAD = 3;
    private static final int COLUMNA_STOCK = 4;
    private static final int COLUMNA_UNIDADMEDIDA = 5;
    private static final int COLUMNA_PRECIOVENTA = 6;

    /** Creates new form VentanaActualizarPreciosProductos */
    public VentanaActualizarPreciosProductos() {
        initComponents();
        modeloTablaProductos = (DefaultTableModel) tblProductos.getModel();
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorActualizarPreciosProductosRemote) gc.getObjetoRemoto(
                GestorActualizarPreciosProductosRemote.class);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    public void inicializarVentana() {
        txtProveedor.setText("");
        proveedor = null;
        limpiar();
    }

    private void limpiar() {
        modeloTablaProductos.setRowCount(0);
        calidades = null;
        cargarCombos();
        consultaHecha(false);
        optVariacionAumento.setSelected(true);
        txtPorcentaje.setValue(0f);
    }

    private void consultaHecha(boolean activado) {
        btnBuscar.setEnabled(!activado);
        btnConsultar.setEnabled(!activado);
        btnQuitarSeleccionCalidad.setEnabled(!activado);
        btnQuitarSeleccionProveedor.setEnabled(!activado);
        btnQuitarSeleccionRubro.setEnabled(!activado);
        cboRubro.setEnabled(!activado);
        cboCalidad.setEnabled(!activado);
        btnAceptar.setEnabled(activado);
        btnCancelar.setEnabled(activado);
        lblPorcentaje.setEnabled(activado);
        txtPorcentaje.setEnabled(activado);
        optVariacionAumento.setEnabled(activado);
        optVariacionReduccion.setEnabled(activado);
        tblProductos.setEnabled(activado);
    }

    public void cargarCombos() {
        //Rubros
        new ComboUtil(cboRubro).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoRubro.class));

        //Calidades
        new ComboUtil(cboCalidad).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoCalidad.class));

        //Limpiar selección
        cboRubro.setSelectedIndex(-1);
        cboCalidad.setSelectedIndex(-1);
    }

    private void buscarProveedor() {
        Proveedor res = Buscador.buscarProveedor(false);
        if (res != null) {
            proveedor = res;
            txtProveedor.setText(proveedor.getIdentidad());
        }
    }

    private void consultarProductos() {
        //Obtener el rubro
        TipoRubro rubro = new ComboUtil<TipoRubro>(cboRubro).getSelected();
        TipoCalidad calidad = new ComboUtil<TipoCalidad>(cboCalidad).getSelected();

        //Solicitar productos al gestor
        calidades = gestor.getCalidadesProductos(rubro, calidad, proveedor);

        //Si no hay productos avisar
        if (calidades == null || calidades.size() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No se encontraron productos para el proveedor, calidad y rubro" +
                    " seleccionados.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Mostrar productos en tabla.
        agregarProductosATabla();

        //Si hay comenzar la edicion
        consultaHecha(true);
        txtPorcentaje.requestFocus();
    }

    private void agregarProductosATabla() {
        if (calidades == null) {
            throw new NullPointerException(
                    "El atributo calidades no puede ser null al agregarlo en tabla.");
        }
        if (modeloTablaProductos != null) {
            modeloTablaProductos.setRowCount(0);
        }
        for (CalidadProducto cp : calidades) {
            Object[] fila = new Object[7];
            //Codigo
            fila[COLUMNA_CODIGO] = cp.getProducto().getCodigo();
            //Descripción
            fila[COLUMNA_DESCRIPCION] = cp.getProducto();
            //Marca
            fila[COLUMNA_MARCA] = cp.getProducto().getMarca();
            //Calidad
            fila[COLUMNA_CALIDAD] = cp;
            //Stock
            fila[COLUMNA_STOCK] = cp.getStockActual();
            //U. Medida
            fila[COLUMNA_UNIDADMEDIDA] = cp.getProducto().getProductoUM();
            //$ Venta
            fila[COLUMNA_PRECIOVENTA] = cp.calcularPrecioVenta();

            modeloTablaProductos.addRow(fila);
        }
    }

    private void calcularPrecios() {
        Float porcentaje = (Float) txtPorcentaje.getValue();

        //Calcular factor de aumento/disminución de precios
        Float factor = null;
        if (optVariacionAumento.isSelected()) {
            factor = (porcentaje / 100) + 1;
        } else {
            factor = -(porcentaje / 100) + 1;
            if (factor < 0f) {
                factor = 0f;
            }
        }

        for (int i = 0; i < modeloTablaProductos.getRowCount(); i++) {
            CalidadProducto cp = (CalidadProducto) modeloTablaProductos.getValueAt(
                    i, COLUMNA_CALIDAD);

            CalidadProducto cpTemp = new CalidadProducto();
            cpTemp.setPrecioVentaFijo(cp.isPrecioVentaFijo());
            cpTemp.setPorcentajeGanancia(cp.getPorcentajeGanancia());
            if (cp.isPrecioVentaFijo()) {
                //Precio de venta fijo
                float original = cp.getPrecioVenta();
                cpTemp.setPrecioVenta(original * factor);
            } else {
                //Precio de venta según ultima compra
                float original = cp.getPrecioUltimaCompra();
                cpTemp.setPrecioUltimaCompra(original * factor);
            }
            modeloTablaProductos.setValueAt(cpTemp.calcularPrecioVenta(), i,
                    COLUMNA_PRECIOVENTA);
        }
    }

    /**
     * Confirma la actualización de precios.
     */
    private void tomarConfirmacion() {
        //Tomar los datos        
        Float porcentaje = (Float) txtPorcentaje.getValue();
        //Verificar.
        if (porcentaje <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar un porcentaje mayor a '0' para continuar.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Calcular factor de aumento/disminución de precios definitivos
        Float factor = null;
        if (optVariacionAumento.isSelected()) {
            factor = (porcentaje / 100) + 1;
        } else {
            factor = -(porcentaje / 100) + 1;
            if (factor < 0f) {
                factor = 0f;
            }
        }
        for (CalidadProducto cp : calidades) {
            if (cp.isPrecioVentaFijo()) {
                //Precio de venta fijo
                float original = cp.getPrecioVenta();
                cp.setPrecioVenta(original * factor);
            } else {
                //Precio de venta según ultima compra
                float original = cp.getPrecioUltimaCompra();
                cp.setPrecioUltimaCompra(original * factor);
            }
        }

        //Actualizar
        try {
            gestor.confirmarActualizacion(calidades);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar los precios de los productos en la base de datos:\n" +
                    ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Informar al usuario.
        JOptionPane.showMessageDialog(this,
                "Los precios de los productos fueron actualizados con éxito.",
                "Información", JOptionPane.INFORMATION_MESSAGE);
        inicializarVentana();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpVariacion = new javax.swing.ButtonGroup();
        pnlFiltro = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtProveedor = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        cboRubro = new javax.swing.JComboBox();
        btnConsultar = new javax.swing.JButton();
        lblCalidad = new javax.swing.JLabel();
        cboCalidad = new javax.swing.JComboBox();
        btnQuitarSeleccionCalidad = new javax.swing.JButton();
        btnQuitarSeleccionRubro = new javax.swing.JButton();
        btnQuitarSeleccionProveedor = new javax.swing.JButton();
        pnlProductos = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        pnlVariacionAAplicarALaSeleccion = new javax.swing.JPanel();
        optVariacionAumento = new javax.swing.JRadioButton();
        optVariacionReduccion = new javax.swing.JRadioButton();
        lblPorcentaje = new javax.swing.JLabel();
        txtPorcentaje = new javax.swing.JSpinner();
        btnCancelar = new javax.swing.JButton();
        btnAceptar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaActualizarPreciosProductos.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setMinimumSize(new java.awt.Dimension(750, 500));
        setName("Form"); // NOI18N

        pnlFiltro.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlFiltro.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlFiltro.setName("pnlFiltro"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        txtProveedor.setEditable(false);
        txtProveedor.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtProveedor.setText(resourceMap.getString("txtProveedor.text")); // NOI18N
        txtProveedor.setName("txtProveedor"); // NOI18N

        btnBuscar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnBuscar.setIcon(resourceMap.getIcon("btnBuscar.icon")); // NOI18N
        btnBuscar.setMnemonic('b');
        btnBuscar.setText(resourceMap.getString("btnBuscar.text")); // NOI18N
        btnBuscar.setName("btnBuscar"); // NOI18N
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        jLabel2.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        cboRubro.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboRubro.setName("cboRubro"); // NOI18N

        btnConsultar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnConsultar.setIcon(resourceMap.getIcon("btnConsultar.icon")); // NOI18N
        btnConsultar.setText(resourceMap.getString("btnConsultar.text")); // NOI18N
        btnConsultar.setName("btnConsultar"); // NOI18N
        btnConsultar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsultarActionPerformed(evt);
            }
        });

        lblCalidad.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblCalidad.setText(resourceMap.getString("lblCalidad.text")); // NOI18N
        lblCalidad.setName("lblCalidad"); // NOI18N

        cboCalidad.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboCalidad.setName("cboCalidad"); // NOI18N

        btnQuitarSeleccionCalidad.setFont(resourceMap.getFont("btnQuitarSeleccionCalidad.font")); // NOI18N
        btnQuitarSeleccionCalidad.setIcon(resourceMap.getIcon("btnQuitarSeleccionCalidad.icon")); // NOI18N
        btnQuitarSeleccionCalidad.setEnabled(false);
        btnQuitarSeleccionCalidad.setName("btnQuitarSeleccionCalidad"); // NOI18N
        btnQuitarSeleccionCalidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarSeleccionCalidadActionPerformed(evt);
            }
        });

        btnQuitarSeleccionRubro.setFont(resourceMap.getFont("btnQuitarSeleccionRubro.font")); // NOI18N
        btnQuitarSeleccionRubro.setIcon(resourceMap.getIcon("btnQuitarSeleccionRubro.icon")); // NOI18N
        btnQuitarSeleccionRubro.setEnabled(false);
        btnQuitarSeleccionRubro.setName("btnQuitarSeleccionRubro"); // NOI18N
        btnQuitarSeleccionRubro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarSeleccionRubroActionPerformed(evt);
            }
        });

        btnQuitarSeleccionProveedor.setFont(resourceMap.getFont("btnQuitarSeleccionProveedor.font")); // NOI18N
        btnQuitarSeleccionProveedor.setIcon(resourceMap.getIcon("btnQuitarSeleccionProveedor.icon")); // NOI18N
        btnQuitarSeleccionProveedor.setEnabled(false);
        btnQuitarSeleccionProveedor.setName("btnQuitarSeleccionProveedor"); // NOI18N
        btnQuitarSeleccionProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarSeleccionProveedorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFiltroLayout = new javax.swing.GroupLayout(pnlFiltro);
        pnlFiltro.setLayout(pnlFiltroLayout);
        pnlFiltroLayout.setHorizontalGroup(
            pnlFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltroLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFiltroLayout.createSequentialGroup()
                        .addComponent(cboRubro, 0, 171, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitarSeleccionRubro, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCalidad)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboCalidad, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuitarSeleccionCalidad, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuitarSeleccionProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnConsultar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlFiltroLayout.setVerticalGroup(
            pnlFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltroLayout.createSequentialGroup()
                .addGroup(pnlFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(btnBuscar)
                        .addComponent(txtProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnQuitarSeleccionProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnQuitarSeleccionCalidad, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addComponent(btnQuitarSeleccionRubro, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addGroup(pnlFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnConsultar)
                        .addComponent(jLabel2)
                        .addComponent(lblCalidad)
                        .addComponent(cboRubro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cboCalidad, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pnlProductos.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlProductos.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlProductos.setName("pnlProductos"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Código", "Descripción", "Marca", "Calidad", "Stock Actual", "U. Medida", "$ Venta"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Float.class, java.lang.Object.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProductos.setEnabled(false);
        tblProductos.setName("tblProductos"); // NOI18N
        jScrollPane1.setViewportView(tblProductos);
        tblProductos.getColumnModel().getColumn(0).setMinWidth(60);
        tblProductos.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblProductos.getColumnModel().getColumn(0).setMaxWidth(100);
        tblProductos.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tblProductos.columnModel.title0")); // NOI18N
        tblProductos.getColumnModel().getColumn(1).setMinWidth(150);
        tblProductos.getColumnModel().getColumn(1).setPreferredWidth(300);
        tblProductos.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tblProductos.columnModel.title1")); // NOI18N
        tblProductos.getColumnModel().getColumn(2).setMinWidth(80);
        tblProductos.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblProductos.getColumnModel().getColumn(2).setMaxWidth(150);
        tblProductos.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tblProductos.columnModel.title5")); // NOI18N
        tblProductos.getColumnModel().getColumn(3).setMinWidth(80);
        tblProductos.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblProductos.getColumnModel().getColumn(3).setMaxWidth(150);
        tblProductos.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("tblProductos.columnModel.title4")); // NOI18N
        tblProductos.getColumnModel().getColumn(4).setMinWidth(80);
        tblProductos.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblProductos.getColumnModel().getColumn(4).setMaxWidth(100);
        tblProductos.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("tblProductos.columnModel.title2")); // NOI18N
        tblProductos.getColumnModel().getColumn(5).setMinWidth(80);
        tblProductos.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblProductos.getColumnModel().getColumn(5).setMaxWidth(80);
        tblProductos.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("tblProductos.columnModel.title6")); // NOI18N
        tblProductos.getColumnModel().getColumn(6).setMinWidth(80);
        tblProductos.getColumnModel().getColumn(6).setPreferredWidth(80);
        tblProductos.getColumnModel().getColumn(6).setMaxWidth(100);
        tblProductos.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("tblProductos.columnModel.title3")); // NOI18N

        javax.swing.GroupLayout pnlProductosLayout = new javax.swing.GroupLayout(pnlProductos);
        pnlProductos.setLayout(pnlProductosLayout);
        pnlProductosLayout.setHorizontalGroup(
            pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlProductosLayout.setVerticalGroup(
            pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlVariacionAAplicarALaSeleccion.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlVariacionAAplicarALaSeleccion.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlVariacionAAplicarALaSeleccion.setName("pnlVariacionAAplicarALaSeleccion"); // NOI18N

        grpVariacion.add(optVariacionAumento);
        optVariacionAumento.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        optVariacionAumento.setSelected(true);
        optVariacionAumento.setText(resourceMap.getString("optVariacionAumento.text")); // NOI18N
        optVariacionAumento.setEnabled(false);
        optVariacionAumento.setName("optVariacionAumento"); // NOI18N
        optVariacionAumento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optVariacionAumentoActionPerformed(evt);
            }
        });

        grpVariacion.add(optVariacionReduccion);
        optVariacionReduccion.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        optVariacionReduccion.setText(resourceMap.getString("optVariacionReduccion.text")); // NOI18N
        optVariacionReduccion.setEnabled(false);
        optVariacionReduccion.setName("optVariacionReduccion"); // NOI18N
        optVariacionReduccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optVariacionReduccionActionPerformed(evt);
            }
        });

        lblPorcentaje.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblPorcentaje.setText(resourceMap.getString("lblPorcentaje.text")); // NOI18N
        lblPorcentaje.setEnabled(false);
        lblPorcentaje.setName("lblPorcentaje"); // NOI18N

        txtPorcentaje.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtPorcentaje.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(10000.0f), Float.valueOf(1.0f)));
        txtPorcentaje.setEnabled(false);
        txtPorcentaje.setName("txtPorcentaje"); // NOI18N
        txtPorcentaje.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                txtPorcentajeStateChanged(evt);
            }
        });

        javax.swing.GroupLayout pnlVariacionAAplicarALaSeleccionLayout = new javax.swing.GroupLayout(pnlVariacionAAplicarALaSeleccion);
        pnlVariacionAAplicarALaSeleccion.setLayout(pnlVariacionAAplicarALaSeleccionLayout);
        pnlVariacionAAplicarALaSeleccionLayout.setHorizontalGroup(
            pnlVariacionAAplicarALaSeleccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVariacionAAplicarALaSeleccionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(optVariacionAumento)
                .addGap(46, 46, 46)
                .addComponent(optVariacionReduccion)
                .addGap(137, 137, 137)
                .addComponent(lblPorcentaje)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPorcentaje, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(199, 199, 199))
        );
        pnlVariacionAAplicarALaSeleccionLayout.setVerticalGroup(
            pnlVariacionAAplicarALaSeleccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVariacionAAplicarALaSeleccionLayout.createSequentialGroup()
                .addGroup(pnlVariacionAAplicarALaSeleccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(optVariacionAumento)
                    .addComponent(txtPorcentaje, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPorcentaje)
                    .addComponent(optVariacionReduccion))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlVariacionAAplicarALaSeleccion, javax.swing.GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnCerrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 409, Short.MAX_VALUE)
                        .addComponent(btnAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlFiltro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(pnlProductos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProductos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlVariacionAAplicarALaSeleccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnAceptar)
                    .addComponent(btnCerrar))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
    dispose();
}//GEN-LAST:event_btnCerrarActionPerformed

private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
    buscarProveedor();
}//GEN-LAST:event_btnBuscarActionPerformed

private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
    consultarProductos();
}//GEN-LAST:event_btnConsultarActionPerformed

private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
    tomarConfirmacion();
}//GEN-LAST:event_btnAceptarActionPerformed

private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
    limpiar();
}//GEN-LAST:event_btnCancelarActionPerformed

private void txtPorcentajeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_txtPorcentajeStateChanged
    calcularPrecios();
}//GEN-LAST:event_txtPorcentajeStateChanged

private void optVariacionAumentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optVariacionAumentoActionPerformed
    if (optVariacionAumento.isSelected()) {
        calcularPrecios();
    }
}//GEN-LAST:event_optVariacionAumentoActionPerformed

private void optVariacionReduccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optVariacionReduccionActionPerformed
    if (optVariacionReduccion.isSelected()) {
        calcularPrecios();
    }
}//GEN-LAST:event_optVariacionReduccionActionPerformed

private void btnQuitarSeleccionCalidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarSeleccionCalidadActionPerformed
    cboCalidad.setSelectedIndex(-1);
}//GEN-LAST:event_btnQuitarSeleccionCalidadActionPerformed

private void btnQuitarSeleccionRubroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarSeleccionRubroActionPerformed
    cboRubro.setSelectedIndex(-1);
}//GEN-LAST:event_btnQuitarSeleccionRubroActionPerformed

private void btnQuitarSeleccionProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarSeleccionProveedorActionPerformed
    txtProveedor.setText("");
    proveedor = null;
}//GEN-LAST:event_btnQuitarSeleccionProveedorActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnQuitarSeleccionCalidad;
    private javax.swing.JButton btnQuitarSeleccionProveedor;
    private javax.swing.JButton btnQuitarSeleccionRubro;
    private javax.swing.JComboBox cboCalidad;
    private javax.swing.JComboBox cboRubro;
    private javax.swing.ButtonGroup grpVariacion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCalidad;
    private javax.swing.JLabel lblPorcentaje;
    private javax.swing.JRadioButton optVariacionAumento;
    private javax.swing.JRadioButton optVariacionReduccion;
    private javax.swing.JPanel pnlFiltro;
    private javax.swing.JPanel pnlProductos;
    private javax.swing.JPanel pnlVariacionAAplicarALaSeleccion;
    private javax.swing.JTable tblProductos;
    private javax.swing.JSpinner txtPorcentaje;
    private javax.swing.JTextField txtProveedor;
    // End of variables declaration//GEN-END:variables
}
