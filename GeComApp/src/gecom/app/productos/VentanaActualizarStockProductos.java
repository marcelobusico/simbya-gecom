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

import gecom.app.table.CantidadStockTableEditor;
import gecom.app.table.CantidadStockTableRender;
import gecom.app.table.CantidadStockTableWrapper;
import java.util.LinkedList;
import java.util.List;
import javax.naming.NamingException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.interfaces.VentanaInterna;
import simbya.framework.validadores.ComboUtil;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Producto;
import simbya.gecom.entidades.TipoMarca;
import simbya.gecom.entidades.TipoRubro;
import simbya.gecom.gestores.productos.GestorActualizarStockProductosRemote;

/**
 * Ventana para actualizar el stock de productos existentes.
 * @author Marcelo Busico.
 */
public class VentanaActualizarStockProductos extends VentanaInterna {

    private GestorActualizarStockProductosRemote gestor = null;
    private DefaultTableModel modeloTablaProductos = null;
    private Producto producto = null;
    private byte[] imagenProducto = null;
    private static final int COLUMNA_CODIGO = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_MARCA = 2;
    private static final int COLUMNA_CALIDAD = 3;
    private static final int COLUMNA_PRECIOUNITARIO = 4;
    private static final int COLUMNA_STOCK = 5;
    private static final int COLUMNA_UNIDADMEDIDA = 6;

    /** Creates new form VentanaActualizarStockProductos */
    public VentanaActualizarStockProductos() {
        initComponents();
        modeloTablaProductos = (DefaultTableModel) tblResultados.getModel();
        //Para Stock
        tblResultados.setDefaultEditor(CantidadStockTableWrapper.class,
                new CantidadStockTableEditor());
        tblResultados.setDefaultRenderer(CantidadStockTableWrapper.class,
                new CantidadStockTableRender(tblResultados.getSelectionBackground()));
        initTableListener();
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorActualizarStockProductosRemote) gc.getObjetoRemoto(
                GestorActualizarStockProductosRemote.class);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    public void inicializarVentana() {
        modeloTablaProductos.setRowCount(0);
        txtDescripcion.setText("");
        cargarCombos();
    }

    /**
     * Inicializa el listener que notifica de cambios 
     * en la selección de la tabla.
     */
    private void initTableListener() {
        tblResultados.getSelectionModel().addListSelectionListener(
                new javax.swing.event.ListSelectionListener() {

                    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                        tblResultadosValueChanged(e);
                    }
                });
    }

    private void tblResultadosValueChanged(javax.swing.event.ListSelectionEvent evt) {
        if (modeloTablaProductos.getRowCount() != 0 && tblResultados.getSelectedRow() > -1) {
            producto = (Producto) tblResultados.getValueAt(
                    tblResultados.getSelectedRow(), 1);
            mostrarProductoSeleccionado();
        }
    }

    public void cargarCombos() {
        //Marcas
        new ComboUtil(cboMarca).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoMarca.class));

        //Rubros
        new ComboUtil(cboRubro).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoRubro.class));

        //Limpiar selección
        cboRubro.setSelectedIndex(-1);
        cboMarca.setSelectedIndex(-1);
    }

    private void mostrarProductoSeleccionado() {
        try {
            imagenProducto = gestor.getImagenProducto(producto);
            if (imagenProducto != null) {
                btnVerFotografia.setEnabled(true);
            } else {
                btnVerFotografia.setEnabled(false);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al obtener la imagen del producto:\n" + ex.getMessage());
        }
    }

    private void agregarProductosATabla(List<Producto> productos) {
        if (productos == null) {
            throw new NullPointerException(
                    "El atributo producto no puede ser null al agregarlo en tabla.");
        }
        if (modeloTablaProductos != null) {
            modeloTablaProductos.setRowCount(0);
        }
        for (Producto p : productos) {
            if (p.getCalidades() != null) {
                for (CalidadProducto calidadProducto : p.getCalidades()) {
                    Object[] fila = new Object[7];
                    //Codigo
                    fila[COLUMNA_CODIGO] = p.getCodigo();
                    //Descripción
                    fila[COLUMNA_DESCRIPCION] = p;
                    //Marca
                    fila[COLUMNA_MARCA] = p.getMarca();
                    //Calidad
                    fila[COLUMNA_CALIDAD] = calidadProducto;
                    //$ Venta
                    fila[COLUMNA_PRECIOUNITARIO] = calidadProducto.calcularPrecioVenta();
                    //Stock
                    fila[COLUMNA_STOCK] = new CantidadStockTableWrapper(calidadProducto,
                            calidadProducto.getStockActual(), true);
                    //U. Medida
                    fila[COLUMNA_UNIDADMEDIDA] = p.getProductoUM();

                    modeloTablaProductos.addRow(fila);
                }
            } else {
                Object[] fila = new Object[6];
                //Codigo
                fila[COLUMNA_CODIGO] = p.getCodigo();
                //Descripción
                fila[COLUMNA_DESCRIPCION] = p;
                //Marca
                fila[COLUMNA_MARCA] = p.getMarca();
                //Calidad
                fila[COLUMNA_CALIDAD] = null;
                //$ Venta
                fila[COLUMNA_PRECIOUNITARIO] = null;
                //Stock
                fila[COLUMNA_STOCK] = null;
                //U. Medida
                fila[COLUMNA_UNIDADMEDIDA] = p.getProductoUM();
            }
        }
        if (modeloTablaProductos.getRowCount() != 0) {
            tblResultados.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    private void buscarProductos() {
        TipoRubro rubro = new ComboUtil<TipoRubro>(cboRubro).getSelected();
        TipoMarca marca = new ComboUtil<TipoMarca>(cboMarca).getSelected();
        List<Producto> productos = gestor.getProductos(rubro, marca, txtDescripcion.getText());
        agregarProductosATabla(productos);
    }

    private void tomarConfirmacion() {
        //Verificar que haya productos con stock modificado.
        List<CalidadProducto> calidadesModificadas = new LinkedList<CalidadProducto>();
        for (int i = 0; i < modeloTablaProductos.getRowCount(); i++) {
            if (modeloTablaProductos.getValueAt(i, COLUMNA_CODIGO) != null) {
                CantidadStockTableWrapper cantStock =
                        (CantidadStockTableWrapper) modeloTablaProductos.getValueAt(i, COLUMNA_STOCK);
                Float cantOriginal = cantStock.getCantidadOriginal();
                Float cantNueva = cantStock.getCantidadSolicitada();
                Producto p =
                        (Producto) modeloTablaProductos.getValueAt(i, COLUMNA_DESCRIPCION);
                CalidadProducto calidad =
                        (CalidadProducto) modeloTablaProductos.getValueAt(i, COLUMNA_CALIDAD);
                if (calidad != null && p != null && cantOriginal != null && cantNueva != null) {
                    if (cantNueva != cantOriginal) {
                        calidad.setStockActual(cantNueva);
                        //Se modifica la cantidad y agregar la calidad del producto a la lista.
                        calidadesModificadas.add(calidad);
                    }
                }
            }
        }
        if (calidadesModificadas.size() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Para actualizar el stock debe cambiar al menos la cantidad\n" +
                    "en stock de un producto.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Solicitar confirmación de usuario.
        int res = JOptionPane.showConfirmDialog(this,
                "Está a punto de actualizar el stock de " +
                calidadesModificadas.size() + " productos.\n" +
                "¿Desea continuar?", "Confirmación requerida",
                JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.NO_OPTION) {
            return;
        }

        //Actualizar el stock en la BD.
        try {
            gestor.actualizarStock(calidadesModificadas);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar el stock en la base de datos:\n" +
                    ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Informar al usuario.
        JOptionPane.showMessageDialog(this,
                "El stock fue actualizado con éxito.",
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

        pnlProductos = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblResultados = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        btnAceptar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        pnlFiltros = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cboRubro = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        cboMarca = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnQuitarRubro = new javax.swing.JButton();
        btnQuitarMarca = new javax.swing.JButton();
        btnVerFotografia = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaActualizarStockProductos.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setMinimumSize(new java.awt.Dimension(780, 500));
        setName("Form"); // NOI18N

        pnlProductos.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlProductos.setName("pnlProductos"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tblResultados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Descripción", "Marca", "Calidad", "$ Venta", "Stock", "U. Medida"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Float.class, CantidadStockTableWrapper.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblResultados.setName("tblResultados");
        tblResultados.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tblResultados);
        tblResultados.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblResultados.getColumnModel().getColumn(0).setMinWidth(80);
        tblResultados.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblResultados.getColumnModel().getColumn(0).setMaxWidth(100);
        tblResultados.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title0")); // NOI18N
        tblResultados.getColumnModel().getColumn(1).setMinWidth(120);
        tblResultados.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblResultados.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title1")); // NOI18N
        tblResultados.getColumnModel().getColumn(2).setMinWidth(70);
        tblResultados.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblResultados.getColumnModel().getColumn(2).setMaxWidth(100);
        tblResultados.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title3")); // NOI18N
        tblResultados.getColumnModel().getColumn(3).setMinWidth(70);
        tblResultados.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblResultados.getColumnModel().getColumn(3).setMaxWidth(100);
        tblResultados.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title6")); // NOI18N
        tblResultados.getColumnModel().getColumn(4).setMinWidth(70);
        tblResultados.getColumnModel().getColumn(4).setPreferredWidth(70);
        tblResultados.getColumnModel().getColumn(4).setMaxWidth(90);
        tblResultados.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title5")); // NOI18N
        tblResultados.getColumnModel().getColumn(5).setMinWidth(70);
        tblResultados.getColumnModel().getColumn(5).setPreferredWidth(70);
        tblResultados.getColumnModel().getColumn(5).setMaxWidth(90);
        tblResultados.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title4")); // NOI18N
        tblResultados.getColumnModel().getColumn(6).setMinWidth(70);
        tblResultados.getColumnModel().getColumn(6).setPreferredWidth(100);
        tblResultados.getColumnModel().getColumn(6).setMaxWidth(120);
        tblResultados.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title6")); // NOI18N

        jLabel3.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout pnlProductosLayout = new javax.swing.GroupLayout(pnlProductos);
        pnlProductos.setLayout(pnlProductosLayout);
        pnlProductosLayout.setHorizontalGroup(
            pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        pnlProductosLayout.setVerticalGroup(
            pnlProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnAceptar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnAceptar.setIcon(resourceMap.getIcon("btnAceptar.icon")); // NOI18N
        btnAceptar.setMnemonic('a');
        btnAceptar.setText(resourceMap.getString("btnAceptar.text")); // NOI18N
        btnAceptar.setName("btnAceptar"); // NOI18N
        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarActionPerformed(evt);
            }
        });

        btnCancelar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCancelar.setIcon(resourceMap.getIcon("btnCancelar.icon")); // NOI18N
        btnCancelar.setMnemonic('c');
        btnCancelar.setText(resourceMap.getString("btnCancelar.text")); // NOI18N
        btnCancelar.setName("btnCancelar"); // NOI18N
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        pnlFiltros.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlFiltros.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlFiltros.setName("pnlFiltros"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cboRubro.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboRubro.setName("cboRubro"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        cboMarca.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboMarca.setName("cboMarca"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        txtDescripcion.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtDescripcion.setName("txtDescripcion"); // NOI18N
        txtDescripcion.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                txtDescripcionCaretUpdate(evt);
            }
        });

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

        btnQuitarRubro.setFont(resourceMap.getFont("btnQuitarRubro.font")); // NOI18N
        btnQuitarRubro.setIcon(resourceMap.getIcon("btnQuitarRubro.icon")); // NOI18N
        btnQuitarRubro.setName("btnQuitarRubro"); // NOI18N
        btnQuitarRubro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarRubroActionPerformed(evt);
            }
        });

        btnQuitarMarca.setFont(resourceMap.getFont("btnQuitarMarca.font")); // NOI18N
        btnQuitarMarca.setIcon(resourceMap.getIcon("btnQuitarMarca.icon")); // NOI18N
        btnQuitarMarca.setName("btnQuitarMarca"); // NOI18N
        btnQuitarMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarMarcaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFiltrosLayout = new javax.swing.GroupLayout(pnlFiltros);
        pnlFiltros.setLayout(pnlFiltrosLayout);
        pnlFiltrosLayout.setHorizontalGroup(
            pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltrosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFiltrosLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDescripcion, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBuscar))
                    .addGroup(pnlFiltrosLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboRubro, 0, 238, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitarRubro)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboMarca, 0, 239, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitarMarca)))
                .addContainerGap())
        );
        pnlFiltrosLayout.setVerticalGroup(
            pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltrosLayout.createSequentialGroup()
                .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBuscar)
                    .addComponent(jLabel4)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(btnQuitarRubro)
                        .addComponent(cboRubro, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(btnQuitarMarca)
                        .addComponent(cboMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnVerFotografia.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnVerFotografia.setIcon(resourceMap.getIcon("btnVerFotografia.icon")); // NOI18N
        btnVerFotografia.setMnemonic('v');
        btnVerFotografia.setText(resourceMap.getString("btnVerFotografia.text")); // NOI18N
        btnVerFotografia.setEnabled(false);
        btnVerFotografia.setName("btnVerFotografia"); // NOI18N
        btnVerFotografia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerFotografiaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlProductos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnVerFotografia)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 345, Short.MAX_VALUE)
                        .addComponent(btnAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCancelar))
                    .addComponent(pnlFiltros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(pnlProductos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnAceptar)
                    .addComponent(btnVerFotografia))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void txtDescripcionCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtDescripcionCaretUpdate
    if (txtDescripcion.getText() != null && !txtDescripcion.getText().isEmpty()) {
        buscarProductos();
    } else {
        if (modeloTablaProductos != null) {
            modeloTablaProductos.setRowCount(0);
        }
    }
}//GEN-LAST:event_txtDescripcionCaretUpdate

private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
    buscarProductos();
}//GEN-LAST:event_btnBuscarActionPerformed

private void btnQuitarRubroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarRubroActionPerformed
    cboRubro.setSelectedIndex(-1);
}//GEN-LAST:event_btnQuitarRubroActionPerformed

private void btnQuitarMarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarMarcaActionPerformed
    cboMarca.setSelectedIndex(-1);
}//GEN-LAST:event_btnQuitarMarcaActionPerformed

private void btnVerFotografiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerFotografiaActionPerformed
    new DialogoVerImagen(null, new ImageIcon(imagenProducto));
}//GEN-LAST:event_btnVerFotografiaActionPerformed

private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
    inicializarVentana();
}//GEN-LAST:event_btnCancelarActionPerformed

private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
    tomarConfirmacion();
}//GEN-LAST:event_btnAceptarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnQuitarMarca;
    private javax.swing.JButton btnQuitarRubro;
    private javax.swing.JButton btnVerFotografia;
    private javax.swing.JComboBox cboMarca;
    private javax.swing.JComboBox cboRubro;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlProductos;
    private javax.swing.JTable tblResultados;
    private javax.swing.JTextField txtDescripcion;
    // End of variables declaration//GEN-END:variables
}
