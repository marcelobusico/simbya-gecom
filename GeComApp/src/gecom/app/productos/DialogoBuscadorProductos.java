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

import gecom.app.VentanaMenuPrincipal;
import gecom.app.buscador.Buscador;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.naming.NamingException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.validadores.ComboUtil;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Producto;
import simbya.gecom.entidades.TipoMarca;
import simbya.gecom.entidades.TipoRubro;
import simbya.gecom.gestores.productos.GestorConsultarProductosRemote;

/**
 * Ventana que permite buscar productos y seleccionar uno.
 * @author Marcelo Busico.
 */
public class DialogoBuscadorProductos extends javax.swing.JDialog {

    private GestorConsultarProductosRemote gestor = null;
    private DefaultTableModel modeloTablaProductos = null;
    private Producto producto = null;
    private CalidadProducto calidad = null;
    private byte[] imagenProducto = null;
    private boolean seleccionTomada = false;

    /** Creates new form DialogoBuscadorProductos */
    public DialogoBuscadorProductos(java.awt.Frame parent, boolean modal,
            boolean mostrarRegistroProducto) {

        super(parent, modal);
        initComponents();
        try {
            enlazarGestorRemoto(GestorConexion.getInstancia());
            inicializarVentana();
            btnRegistrarNuevoProducto.setVisible(mostrarRegistroProducto);
            setVisible(true);
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
        gestor = (GestorConsultarProductosRemote) gc.getObjetoRemoto(
                GestorConsultarProductosRemote.class);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    private void inicializarVentana() {
        modeloTablaProductos = (DefaultTableModel) tblResultados.getModel();
        cargarCombos();
        initTableListener();
    }

    /**
     * Inicializa el listener que notifica de cambios 
     * en la selección de la tabla.
     */
    private void initTableListener() {
        tblResultados.getSelectionModel().addListSelectionListener(
                new javax.swing.event.ListSelectionListener() {

                    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                        tblCalidadesValueChanged(e);
                    }
                });
    }

    private void tblCalidadesValueChanged(javax.swing.event.ListSelectionEvent evt) {
        if (seleccionTomada) {
            return;
        }
        if (modeloTablaProductos.getRowCount() != 0 && tblResultados.getSelectedRow() > -1) {
            producto = (Producto) tblResultados.getValueAt(
                    tblResultados.getSelectedRow(), 1);
            calidad = (CalidadProducto) tblResultados.getValueAt(
                    tblResultados.getSelectedRow(), 3);
            mostrarProductoSeleccionado();
        }
    }

    private void cargarCombos() {
        //Marcas
        new ComboUtil(cboMarca).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoMarca.class));

        //Rubros
        new ComboUtil(cboRubro).cleanAndLoad(gestor.cargarObjetosPersistentes(TipoRubro.class));

        //Limpiar selección
        cboRubro.setSelectedIndex(-1);
        cboMarca.setSelectedIndex(-1);
    }

    /**
     * Devuelve el producto seleccionado por el usuario, null si no hay ninguno.
     * @return Producto seleccionado de la lista.
     */
    public Producto getSeleccion() {
        return producto;
    }

    /**
     * Devuelve el producto seleccionado por el usuario, null si no hay ninguno.
     * @return Producto seleccionado de la lista.
     */
    public CalidadProducto getCalidadSeleccionada() {
        return calidad;
    }

    private void mostrarProductoSeleccionado() {
        try {
            imagenProducto = gestor.getImagenProducto(producto);
            if (imagenProducto != null) {
                btnVerFotografia.setEnabled(true);
            } else {
                btnVerFotografia.setEnabled(false);
            }
            String dimensiones = "Dimensiones en " + producto.getDimensionUM();
            boolean hayDim = false;
            if (producto.getMedidaEspesor() != null) {
                hayDim = true;
                dimensiones += " - " + producto.getMedidaEspesor() + " de espesor";
            }
            if (producto.getMedidaInterior() != null) {
                hayDim = true;
                dimensiones += " - " + producto.getMedidaInterior() + " interior";
            }
            if (producto.getMedidaExterior() != null) {
                hayDim = true;
                dimensiones += " - " + producto.getMedidaExterior() + " exterior";
            }

            lblDimensiones.setVisible(hayDim);
            lblDimensiones.setText(dimensiones);
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
                    fila[0] = p.getCodigo();
                    //Descripción
                    fila[1] = p;
                    //Marca
                    fila[2] = p.getMarca();
                    //Calidad
                    fila[3] = calidadProducto;
                    //$ Venta
                    fila[4] = calidadProducto.calcularPrecioVenta();
                    //Stock
                    fila[5] = calidadProducto.getStockActual();
                    //Ubicación
                    fila[6] = p.getUbicacionEstanteria();

                    modeloTablaProductos.addRow(fila);
                }
            } else {
                Object[] fila = new Object[7];
                //Codigo
                fila[0] = p.getCodigo();
                //Descripción
                fila[1] = p;
                //Marca
                fila[2] = p.getMarca();
                //Calidad
                fila[3] = null;
                //$ Venta
                fila[4] = null;
                //Stock
                fila[5] = null;
                //Ubicación
                fila[6] = p.getUbicacionEstanteria();
                modeloTablaProductos.addRow(fila);
            }
        }
        if (modeloTablaProductos.getRowCount() != 0) {
            tblResultados.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    /**
     * Toma la selección de la lista y sale del diálogo.
     */
    private void tomarSeleccion() {
        if (producto == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un elemento de la lista.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
        } else {
            seleccionTomada = true;
            dispose();
        }
    }

    private void buscarProductos() {
        TipoRubro rubro = new ComboUtil<TipoRubro>(cboRubro).getSelected();
        TipoMarca marca = new ComboUtil<TipoMarca>(cboMarca).getSelected();
        List<Producto> productos = gestor.getProductos(rubro, marca, txtDescripcion.getText());
        agregarProductosATabla(productos);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFiltros = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cboRubro = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        cboMarca = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnQuitarRubro = new javax.swing.JButton();
        btnQuitarMarca = new javax.swing.JButton();
        btnAvanzada = new javax.swing.JButton();
        pnlResultados = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblResultados = new javax.swing.JTable();
        btnVerFotografia = new javax.swing.JButton();
        lblDimensiones = new javax.swing.JLabel();
        btnCancelar = new javax.swing.JButton();
        btnSeleccionar = new javax.swing.JButton();
        btnRegistrarNuevoProducto = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(DialogoBuscadorProductos.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(750, 500));
        setName("Form"); // NOI18N

        pnlFiltros.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlFiltros.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlFiltros.setName("pnlFiltros"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cboRubro.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboRubro.setName("cboRubro"); // NOI18N
        cboRubro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboRubroActionPerformed(evt);
            }
        });

        jLabel2.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        cboMarca.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboMarca.setName("cboMarca"); // NOI18N
        cboMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboMarcaActionPerformed(evt);
            }
        });

        jLabel3.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        txtDescripcion.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtDescripcion.setText(resourceMap.getString("txtDescripcion.text")); // NOI18N
        txtDescripcion.setName("txtDescripcion"); // NOI18N
        txtDescripcion.setNextFocusableComponent(tblResultados);
        txtDescripcion.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                txtDescripcionCaretUpdate(evt);
            }
        });
        txtDescripcion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtDescripcionKeyPressed(evt);
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

        btnQuitarRubro.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnQuitarRubro.setIcon(resourceMap.getIcon("btnQuitarRubro.icon")); // NOI18N
        btnQuitarRubro.setText(resourceMap.getString("btnQuitarRubro.text")); // NOI18N
        btnQuitarRubro.setName("btnQuitarRubro"); // NOI18N
        btnQuitarRubro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarRubroActionPerformed(evt);
            }
        });

        btnQuitarMarca.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnQuitarMarca.setIcon(resourceMap.getIcon("btnQuitarMarca.icon")); // NOI18N
        btnQuitarMarca.setText(resourceMap.getString("btnQuitarMarca.text")); // NOI18N
        btnQuitarMarca.setName("btnQuitarMarca"); // NOI18N
        btnQuitarMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarMarcaActionPerformed(evt);
            }
        });

        btnAvanzada.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnAvanzada.setIcon(resourceMap.getIcon("btnAvanzada.icon")); // NOI18N
        btnAvanzada.setMnemonic('a');
        btnAvanzada.setText(resourceMap.getString("btnAvanzada.text")); // NOI18N
        btnAvanzada.setToolTipText(resourceMap.getString("btnAvanzada.toolTipText")); // NOI18N
        btnAvanzada.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnAvanzada.setName("btnAvanzada"); // NOI18N
        btnAvanzada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAvanzadaActionPerformed(evt);
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
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDescripcion, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBuscar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAvanzada))
                    .addGroup(pnlFiltrosLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboRubro, 0, 229, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitarRubro)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboMarca, 0, 228, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuitarMarca)))
                .addContainerGap())
        );
        pnlFiltrosLayout.setVerticalGroup(
            pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltrosLayout.createSequentialGroup()
                .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAvanzada, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(cboRubro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnQuitarRubro, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(cboMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnQuitarMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlResultados.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlResultados.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlResultados.setName("pnlResultados"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tblResultados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Descripción", "Marca", "Calidad", "$ Venta", "Stock", "Ubicación"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblResultados.setName("tblResultados"); // NOI18N
        tblResultados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblResultadosMouseClicked(evt);
            }
        });
        tblResultados.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tblResultadosKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(tblResultados);
        tblResultados.getColumnModel().getColumn(0).setMinWidth(50);
        tblResultados.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblResultados.getColumnModel().getColumn(0).setMaxWidth(100);
        tblResultados.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title0")); // NOI18N
        tblResultados.getColumnModel().getColumn(1).setMinWidth(200);
        tblResultados.getColumnModel().getColumn(1).setPreferredWidth(300);
        tblResultados.getColumnModel().getColumn(1).setMaxWidth(500);
        tblResultados.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title1")); // NOI18N
        tblResultados.getColumnModel().getColumn(2).setMinWidth(80);
        tblResultados.getColumnModel().getColumn(2).setPreferredWidth(80);
        tblResultados.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title2")); // NOI18N
        tblResultados.getColumnModel().getColumn(3).setMinWidth(80);
        tblResultados.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblResultados.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title3")); // NOI18N
        tblResultados.getColumnModel().getColumn(4).setMinWidth(50);
        tblResultados.getColumnModel().getColumn(4).setPreferredWidth(70);
        tblResultados.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title4")); // NOI18N
        tblResultados.getColumnModel().getColumn(5).setMinWidth(50);
        tblResultados.getColumnModel().getColumn(5).setPreferredWidth(50);
        tblResultados.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title5")); // NOI18N
        tblResultados.getColumnModel().getColumn(6).setMinWidth(150);
        tblResultados.getColumnModel().getColumn(6).setPreferredWidth(200);
        tblResultados.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("tblResultados.columnModel.title6")); // NOI18N

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

        lblDimensiones.setText(resourceMap.getString("lblDimensiones.text")); // NOI18N
        lblDimensiones.setName("lblDimensiones"); // NOI18N

        javax.swing.GroupLayout pnlResultadosLayout = new javax.swing.GroupLayout(pnlResultados);
        pnlResultados.setLayout(pnlResultadosLayout);
        pnlResultadosLayout.setHorizontalGroup(
            pnlResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResultadosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlResultadosLayout.createSequentialGroup()
                        .addComponent(lblDimensiones)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 442, Short.MAX_VALUE)
                        .addComponent(btnVerFotografia)))
                .addContainerGap())
        );
        pnlResultadosLayout.setVerticalGroup(
            pnlResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResultadosLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVerFotografia)
                    .addComponent(lblDimensiones))
                .addContainerGap())
        );

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

        btnSeleccionar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnSeleccionar.setIcon(resourceMap.getIcon("btnSeleccionar.icon")); // NOI18N
        btnSeleccionar.setMnemonic('s');
        btnSeleccionar.setText(resourceMap.getString("btnSeleccionar.text")); // NOI18N
        btnSeleccionar.setName("btnSeleccionar"); // NOI18N
        btnSeleccionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSeleccionarActionPerformed(evt);
            }
        });

        btnRegistrarNuevoProducto.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnRegistrarNuevoProducto.setIcon(resourceMap.getIcon("btnRegistrarNuevoProducto.icon")); // NOI18N
        btnRegistrarNuevoProducto.setMnemonic('r');
        btnRegistrarNuevoProducto.setText(resourceMap.getString("btnRegistrarNuevoProducto.text")); // NOI18N
        btnRegistrarNuevoProducto.setToolTipText(resourceMap.getString("btnRegistrarNuevoProducto.toolTipText")); // NOI18N
        btnRegistrarNuevoProducto.setName("btnRegistrarNuevoProducto"); // NOI18N
        btnRegistrarNuevoProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarNuevoProductoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlResultados, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnRegistrarNuevoProducto)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 273, Short.MAX_VALUE)
                        .addComponent(btnSeleccionar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar))
                    .addComponent(pnlFiltros, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResultados, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnSeleccionar)
                    .addComponent(btnRegistrarNuevoProducto))
                .addContainerGap())
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-760)/2, (screenSize.height-530)/2, 760, 530);
    }// </editor-fold>//GEN-END:initComponents

private void btnVerFotografiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerFotografiaActionPerformed
    new DialogoVerImagen(null, new ImageIcon(imagenProducto));
}//GEN-LAST:event_btnVerFotografiaActionPerformed

private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
    buscarProductos();
}//GEN-LAST:event_btnBuscarActionPerformed

private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
    producto = null;
    calidad = null;
    dispose();
}//GEN-LAST:event_btnCancelarActionPerformed

private void btnSeleccionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSeleccionarActionPerformed
    tomarSeleccion();
}//GEN-LAST:event_btnSeleccionarActionPerformed

private void tblResultadosKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblResultadosKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        tomarSeleccion();
    }
    if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
        producto = null;
        calidad = null;
        dispose();
    }
}//GEN-LAST:event_tblResultadosKeyPressed

private void tblResultadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblResultadosMouseClicked
    if (evt.getClickCount() == 2) {
        tomarSeleccion();
    }
}//GEN-LAST:event_tblResultadosMouseClicked

private void btnQuitarRubroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarRubroActionPerformed
    cboRubro.setSelectedIndex(-1);
}//GEN-LAST:event_btnQuitarRubroActionPerformed

private void btnQuitarMarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarMarcaActionPerformed
    cboMarca.setSelectedIndex(-1);
}//GEN-LAST:event_btnQuitarMarcaActionPerformed

private void txtDescripcionCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtDescripcionCaretUpdate
    if (txtDescripcion.getText() != null && !txtDescripcion.getText().isEmpty()) {
        buscarProductos();
    } else {
        if (modeloTablaProductos != null) {
            modeloTablaProductos.setRowCount(0);
        }
    }
}//GEN-LAST:event_txtDescripcionCaretUpdate

private void txtDescripcionKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescripcionKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        tomarSeleccion();
    }
    if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
        producto = null;
        calidad = null;
        dispose();
    }
}//GEN-LAST:event_txtDescripcionKeyPressed

private void btnRegistrarNuevoProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarNuevoProductoActionPerformed
    producto = new DialogoRegistrarNuevoProducto(
            VentanaMenuPrincipal.getInstancia().getFrame(), true).getProductoRegistrado();
    if (producto != null) {
        seleccionTomada = true;
        dispose();
    }
}//GEN-LAST:event_btnRegistrarNuevoProductoActionPerformed

private void btnAvanzadaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAvanzadaActionPerformed
    producto = Buscador.buscarProducto(false);
    if (producto != null) {
        dispose();
    }
}//GEN-LAST:event_btnAvanzadaActionPerformed

private void cboRubroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboRubroActionPerformed
    buscarProductos();
}//GEN-LAST:event_cboRubroActionPerformed

private void cboMarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboMarcaActionPerformed
    buscarProductos();
}//GEN-LAST:event_cboMarcaActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAvanzada;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnQuitarMarca;
    private javax.swing.JButton btnQuitarRubro;
    private javax.swing.JButton btnRegistrarNuevoProducto;
    private javax.swing.JButton btnSeleccionar;
    private javax.swing.JButton btnVerFotografia;
    private javax.swing.JComboBox cboMarca;
    private javax.swing.JComboBox cboRubro;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDimensiones;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlResultados;
    private javax.swing.JTable tblResultados;
    private javax.swing.JTextField txtDescripcion;
    // End of variables declaration//GEN-END:variables
}
