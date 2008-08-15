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
package gecom.app.compras;

import gecom.app.VentanaMenuPrincipal;
import gecom.app.buscador.Buscador;
import gecom.app.configuracion.ParamSistema;
import gecom.app.proveedores.VentanaActualizarProveedor;
import gecom.app.table.CalidadesTableEditor;
import gecom.app.table.CalidadesTableRender;
import gecom.app.table.CalidadesTableWrapper;
import gecom.app.table.FloatTableEditor;
import gecom.app.table.ProductoTableEditor;
import gecom.app.table.ProductoTableRender;
import gecom.app.table.ProductoTableWrapper;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.naming.NamingException;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.InternationalFormatter;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.formateadores.FormateadorEstandar;
import simbya.framework.interfaces.VentanaInterna;
import simbya.framework.validadores.ComboUtil;
import simbya.framework.validadores.TextValidator;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Compra;
import simbya.gecom.entidades.DetalleCompra;
import simbya.gecom.entidades.Producto;
import simbya.gecom.entidades.Proveedor;
import simbya.gecom.entidades.TipoFormaPago;
import simbya.gecom.entidades.parametros.ConfiguracionGeneral;
import simbya.gecom.gestores.compras.GestorRegistrarCompraRemote;

/**
 * Ventana para registrar compras.
 * @author Marcelo Busico.
 */
public class VentanaRegistrarCompra extends VentanaInterna {

    /** Cantidad de fila de la tabla */
    private static final int CANT_FILAS = 50;
    private static final Logger log = Logger.getLogger(
            VentanaRegistrarCompra.class);
    private GestorRegistrarCompraRemote gestor;
    private Proveedor proveedor = null;
    private DefaultTableModel modeloTabla;
    private boolean actualizandoTabla = false;
    private static final int COLUMNA_CODIGO = 0;
    private static final int COLUMNA_CALIDAD = 1;
    private static final int COLUMNA_DESCRIPCION = 2;
    private static final int COLUMNA_CANTIDAD = 3;
    private static final int COLUMNA_PRECIOUNITARIO = 4;
    private static final int COLUMNA_UNIDADMEDIDA = 5;
    private static final int COLUMNA_SUBTOTAL = 6;
    private int formaSeleccionada = 0;
    private static final int FORMA_CONTADO = 1;
    private static final int FORMA_CUENTACORRIENTE = 2;
    private float importeTotal;

    /** Creates new form VentanaRegistrarCompra */
    public VentanaRegistrarCompra() {
        initComponents();
        modeloTabla = (DefaultTableModel) tblTransaccion.getModel();
        modeloTabla.setRowCount(CANT_FILAS);
        //Para Productos
        tblTransaccion.setDefaultEditor(ProductoTableWrapper.class,
                new ProductoTableEditor(true));
        tblTransaccion.setDefaultRenderer(ProductoTableWrapper.class,
                new ProductoTableRender(tblTransaccion.getSelectionBackground()));
        //Para calidades
        CalidadesTableEditor calidadesTableEditor = new CalidadesTableEditor();
        tblTransaccion.setDefaultEditor(CalidadesTableWrapper.class,
                calidadesTableEditor);
        tblTransaccion.addKeyListener(calidadesTableEditor);
        tblTransaccion.setDefaultRenderer(CalidadesTableWrapper.class,
                new CalidadesTableRender(tblTransaccion.getSelectionBackground()));
        //Para float.
        tblTransaccion.setDefaultEditor(Float.class,
                new FloatTableEditor());
        initTableListener();
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorRegistrarCompraRemote) gc.getObjetoRemoto(
                GestorRegistrarCompraRemote.class);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    public void inicializarVentana() {
        importeTotal = 0;
        proveedor = null;
        optDesconocido.setSelected(true);
        btnNuevoProveedor.setEnabled(false);
        btnBuscarProveedor.setEnabled(false);
        txtProveedor.setText("");
        txtFacturaNumero.setText("");
        txtFacturaSucursal.setText("");
        List<TipoFormaPago> formasPago = gestor.cargarObjetosPersistentes(
                TipoFormaPago.class);
        new ComboUtil<TipoFormaPago>(cboFormaPago).cleanAndLoad(formasPago);
        if (cboFormaPago.getItemCount() != 0) {
            cboFormaPago.setSelectedIndex(0);
        }
        txtTotalCompra.setText("");
        for (int i = 0; i < CANT_FILAS; i++) {
            limpiarLineaDeTabla(i);
        }
        tblTransaccion.getSelectionModel().setSelectionInterval(0, 0);
        tblTransaccion.requestFocus();
    }

    /**
     * Inicializa el listener que notifica de cambios 
     * en la selección de la tabla.
     */
    private void initTableListener() {
        tblTransaccion.getModel().addTableModelListener(new InteractiveTableModelListener());
    }

    /**
     * Clase para monitorear los cambios en la tabla.
     */
    class InteractiveTableModelListener implements TableModelListener {

        /**
         * Valor cambiado.
         */
        public void tableChanged(TableModelEvent evt) {
            if (actualizandoTabla) {
                return;
            }
            if (evt.getType() == TableModelEvent.UPDATE) {
                int column = evt.getColumn();
                int row = evt.getFirstRow();
                if (column == 0) {
                    mostrarDatosProducto(row);
                }
                if (column == 1) {
                    mostrarCalidadProducto(row);
                }
                calcularTotales();
            //table.setColumnSelectionInterval(column + 1, column + 1);
            //table.setRowSelectionInterval(row, row);
            }
        }
    }

    public void proveedorRegistrado(Proveedor proveedor) {
        this.proveedor = proveedor;
        if (proveedor != null) {
            txtProveedor.setText(proveedor.getIdentidad());
        } else {
            txtProveedor.setText("");
        }
        setVisible(true);
    }

    /**
     * Elimina el contenido de una fila.
     * @param nroLinea Número de línea (desde 0 a CANT_FILAS-1).
     */
    private void limpiarLineaDeTabla(int nroLinea) {
        actualizandoTabla = true;
        if (nroLinea > CANT_FILAS) {
            throw new IllegalArgumentException("El número de línea indicado (" +
                    nroLinea + ") es mayor" +
                    " a la cantidad de filas de la tabla (" + CANT_FILAS + ").");
        }
        for (int i = 0; i < modeloTabla.getColumnCount(); i++) {
            modeloTabla.setValueAt(null, nroLinea, i);
        }
        actualizandoTabla = false;
    }

    private void quitarSeleccion() {
        if (tblTransaccion.getSelectionModel().isSelectionEmpty()) {
            return;
        }
        int[] selectedRows = tblTransaccion.getSelectedRows();
        for (int i = 0; i < selectedRows.length; i++) {
            int j = selectedRows[i];
            limpiarLineaDeTabla(j);
        }
    }

    private void opcionNuevoProveedor() {
        setVisible(false);
        VentanaActualizarProveedor actualizarProveedor =
                (VentanaActualizarProveedor) VentanaMenuPrincipal.getInstancia().getGestor().iniciarCU(
                VentanaActualizarProveedor.class);
        actualizarProveedor.asistirRegistrarNuevoProveedor(this);
    }

    private void buscarProveedor() {
        Proveedor res = Buscador.buscarProveedor(false);
        if (res != null) {
            proveedor = res;
            txtProveedor.setText(proveedor.getIdentidad());
        }
    }

    private void mostrarDatosProducto(int nroFila) {
        if (nroFila < 0 || nroFila >= modeloTabla.getRowCount()) {
            throw new IllegalArgumentException("Numero de fila inválido: " + nroFila);
        }
        Object valor = modeloTabla.getValueAt(nroFila, COLUMNA_CODIGO);
        if (valor == null) {
            //No hay dato ingresado
            limpiarLineaDeTabla(nroFila);
            return;
        }
        ProductoTableWrapper ptw = (ProductoTableWrapper) valor;
        Producto p = ptw.getProducto();
        if (p == null) {
            limpiarLineaDeTabla(nroFila);
            return;
        }
        modeloTabla.setValueAt(p.getDescripcion(), nroFila, COLUMNA_DESCRIPCION);
        modeloTabla.setValueAt(p.getProductoUM().getNombre(), nroFila, COLUMNA_UNIDADMEDIDA);
        CalidadProducto calidad = null;
        if (ptw.getCalidad() != null) {
            calidad = ptw.getCalidad();
        } else {
            for (CalidadProducto cp : p.getCalidades()) {
                calidad = cp;
                break;
            }
        }
        CalidadesTableWrapper ctw = new CalidadesTableWrapper(p, calidad);
        modeloTabla.setValueAt(ctw, nroFila, COLUMNA_CALIDAD);
        mostrarCalidadProducto(p, calidad, nroFila);
    }

    private void mostrarCalidadProducto(int nroFila) {
        Object valor = modeloTabla.getValueAt(nroFila, COLUMNA_CALIDAD);
        if (valor == null) {
            //No hay dato ingresado
            limpiarLineaDeTabla(nroFila);
            return;
        }
        CalidadesTableWrapper ctw = (CalidadesTableWrapper) valor;
        Producto p = ctw.getProducto();
        CalidadProducto calidad = null;
        if (ctw.getCalidad() != null) {
            calidad = ctw.getCalidad();
        } else {
            for (CalidadProducto cp : p.getCalidades()) {
                calidad = cp;
                break;
            }
        }
        mostrarCalidadProducto(p, calidad, nroFila);
    }

    private void mostrarCalidadProducto(Producto p, CalidadProducto cal, int nroFila) {
        if (p == null || p.getCalidades() == null) {
            throw new IllegalArgumentException("Producto o calidad null.");
        }
        modeloTabla.setValueAt(Float.valueOf(1f), nroFila, COLUMNA_CANTIDAD);
        modeloTabla.setValueAt(cal.getPrecioUltimaCompra(), nroFila,
                COLUMNA_PRECIOUNITARIO);
        calcularTotales();
    }

    private void calcularTotales() {
        actualizandoTabla = true;
        float total = 0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (modeloTabla.getValueAt(i, COLUMNA_CODIGO) != null) {
                Float cant = (Float) modeloTabla.getValueAt(i, COLUMNA_CANTIDAD);
                Float precio = (Float) modeloTabla.getValueAt(i, COLUMNA_PRECIOUNITARIO);
                Float subtotal = 0f;
                if (cant != null && precio != null) {
                    subtotal = precio * cant;
                    modeloTabla.setValueAt(
                            FormateadorEstandar.formatearDinero(subtotal),
                            i, COLUMNA_SUBTOTAL);
                    total += subtotal;
                }
            }
        }
        if (total == 0) {
            txtTotalCompra.setText("");
            importeTotal = 0;
        } else {
            importeTotal = total;
            txtTotalCompra.setText(FormateadorEstandar.formatearDinero(importeTotal));
        }
        actualizandoTabla = false;
    }

    private void tomarSeleccionFormaPago() {
        TipoFormaPago selected = new ComboUtil<TipoFormaPago>(cboFormaPago).getSelected();
        if (selected == null) {
            return;
        }
//TODO: Ver.
        Long oidContado = ParamSistema.getValorParametroLong(ConfiguracionGeneral.oidPagoContado);
        Long oidCuentaCorriente = ParamSistema.getValorParametroLong(ConfiguracionGeneral.oidPagoCuentaCorriente);

        if (selected.getOid() == oidContado) {
            formaSeleccionada = FORMA_CONTADO;
        }
        if (selected.getOid() == oidCuentaCorriente) {
            formaSeleccionada = FORMA_CUENTACORRIENTE;
        }
        calcularTotales();
    }

    /**
     * Confirma la compra actual.
     */
    private void tomarConfirmacion() {
        //Verificar que haya datos cargados
        Set<DetalleCompra> detalles = new TreeSet<DetalleCompra>();
        int item = 0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (modeloTabla.getValueAt(i, COLUMNA_CODIGO) != null) {
                Float cant = (Float) modeloTabla.getValueAt(i, COLUMNA_CANTIDAD);
                Float precio = (Float) modeloTabla.getValueAt(i, COLUMNA_PRECIOUNITARIO);
                CalidadesTableWrapper prodYCalidad =
                        (CalidadesTableWrapper) modeloTabla.getValueAt(i, COLUMNA_CALIDAD);
                if (prodYCalidad != null && cant != null && precio != null) {
                    DetalleCompra dc = new DetalleCompra();
                    item++;
                    dc.setCantidad(cant);
                    dc.setItem(item);
                    dc.setProducto(prodYCalidad.getProducto());
                    dc.setCalidad(prodYCalidad.getCalidad());
                    dc.setImporteUnitario(precio);
                    detalles.add(dc);
                }
            }
        }
        if (item == 0 || importeTotal == 0) {
            JOptionPane.showMessageDialog(this,
                    "Para registrar una compra debe cargar al menos un detalle\n" +
                    "y asegurarse de que el importe de los productos no sean '0'.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Solicitar confirmación de usuario.
        int res = JOptionPane.showConfirmDialog(this,
                "Está a punto de confirmar la compra.\n" +
                "¿Desea continuar?", "Confirmación requerida",
                JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.NO_OPTION) {
            return;
        }

        //Crea la compra y setea los datos.
        Compra compra = new Compra();
        if (optDesconocido.isSelected()) {
            compra.setProveedor(null);
        } else {
            compra.setProveedor(proveedor);
        }
        String tipoFactura = new ComboUtil<String>(cboTipoFactura).getSelected();
        compra.setTipoFactura(tipoFactura);
        int nroFactura = TextValidator.validarInt(this, txtFacturaNumero, null);
        compra.setFactura((nroFactura == -1 ? null : nroFactura));
        int nroSucursal = TextValidator.validarInt(this, txtFacturaSucursal, null);
        compra.setSucursal((nroSucursal == -1 ? null : nroSucursal));
        compra.setFecha(new Date());
        TipoFormaPago formaPago = new ComboUtil<TipoFormaPago>(cboFormaPago).getSelected();
        compra.setFormaPago(formaPago);
        compra.setImporte(importeTotal);
        //Agregar los detalles de la compra (solo los completos)
        compra.setDetalles(detalles);

        //Persistir la compra.
        try {
            gestor.registrarCompra(compra);
        } catch (Exception ex) {
            log.error("Error al registrar la compra en la base de datos", ex);
            JOptionPane.showMessageDialog(this,
                    "Error al registrar la compra en la base de datos:\n" +
                    ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        //Informar al usuario.
        JOptionPane.showMessageDialog(this,
                "La compra fue registrada con éxito.",
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

        opcionesProveedor = new javax.swing.ButtonGroup();
        pnlProveedor = new javax.swing.JPanel();
        optDesconocido = new javax.swing.JRadioButton();
        optSeleccion = new javax.swing.JRadioButton();
        txtProveedor = new javax.swing.JTextField();
        btnBuscarProveedor = new javax.swing.JButton();
        btnNuevoProveedor = new javax.swing.JButton();
        pnlTransaccion = new javax.swing.JPanel();
        pnlScrollTabla = new javax.swing.JScrollPane();
        tblTransaccion = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        txtTotalCompra = new javax.swing.JTextField();
        btnQuitarSeleccion = new javax.swing.JButton();
        lblAyuda = new javax.swing.JLabel();
        btnCancelar = new javax.swing.JButton();
        btnRegistrar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        pnlFormaPago = new javax.swing.JPanel();
        cboFormaPago = new javax.swing.JComboBox();
        lblFactura = new javax.swing.JLabel();
        cboTipoFactura = new javax.swing.JComboBox();
        InternationalFormatter formatNroSucursal = new InternationalFormatter();
        formatNroSucursal.setMaximum(new Integer(9999));
        formatNroSucursal.setMinimum(new Integer(0));
        txtFacturaSucursal = new JFormattedTextField(formatNroSucursal);
        txtFacturaSucursal.setValue(new Integer(1));
        txtFacturaSucursal.setText("");
        InternationalFormatter formatNroFactura = new InternationalFormatter();
        formatNroFactura.setMaximum(new Integer(99999999));
        formatNroFactura.setMinimum(new Integer(0));
        txtFacturaNumero = new JFormattedTextField(formatNroFactura);
        txtFacturaNumero.setValue(new Integer(1));
        txtFacturaNumero.setText("");

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaRegistrarCompra.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setMinimumSize(new java.awt.Dimension(780, 500));
        setName("Form"); // NOI18N
        setNextFocusableComponent(tblTransaccion);

        pnlProveedor.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlProveedor.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlProveedor.setName("pnlProveedor"); // NOI18N

        opcionesProveedor.add(optDesconocido);
        optDesconocido.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        optDesconocido.setSelected(true);
        optDesconocido.setText(resourceMap.getString("optDesconocido.text")); // NOI18N
        optDesconocido.setName("optDesconocido"); // NOI18N
        optDesconocido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optDesconocidoActionPerformed(evt);
            }
        });

        opcionesProveedor.add(optSeleccion);
        optSeleccion.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        optSeleccion.setText(resourceMap.getString("optSeleccion.text")); // NOI18N
        optSeleccion.setName("optSeleccion"); // NOI18N
        optSeleccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optSeleccionActionPerformed(evt);
            }
        });

        txtProveedor.setEditable(false);
        txtProveedor.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtProveedor.setText(resourceMap.getString("txtProveedor.text")); // NOI18N
        txtProveedor.setName("txtProveedor"); // NOI18N

        btnBuscarProveedor.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnBuscarProveedor.setIcon(resourceMap.getIcon("btnBuscarProveedor.icon")); // NOI18N
        btnBuscarProveedor.setMnemonic('b');
        btnBuscarProveedor.setText(resourceMap.getString("btnBuscarProveedor.text")); // NOI18N
        btnBuscarProveedor.setEnabled(false);
        btnBuscarProveedor.setName("btnBuscarProveedor"); // NOI18N
        btnBuscarProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarProveedorActionPerformed(evt);
            }
        });

        btnNuevoProveedor.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnNuevoProveedor.setIcon(resourceMap.getIcon("btnNuevoProveedor.icon")); // NOI18N
        btnNuevoProveedor.setMnemonic('n');
        btnNuevoProveedor.setText(resourceMap.getString("btnNuevoProveedor.text")); // NOI18N
        btnNuevoProveedor.setName("btnNuevoProveedor"); // NOI18N
        btnNuevoProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoProveedorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlProveedorLayout = new javax.swing.GroupLayout(pnlProveedor);
        pnlProveedor.setLayout(pnlProveedorLayout);
        pnlProveedorLayout.setHorizontalGroup(
            pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProveedorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProveedorLayout.createSequentialGroup()
                        .addComponent(optDesconocido)
                        .addGap(18, 18, 18)
                        .addComponent(optSeleccion)
                        .addGap(4, 4, 4))
                    .addGroup(pnlProveedorLayout.createSequentialGroup()
                        .addComponent(txtProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnNuevoProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnBuscarProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlProveedorLayout.setVerticalGroup(
            pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProveedorLayout.createSequentialGroup()
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(optDesconocido)
                    .addComponent(optSeleccion)
                    .addComponent(btnNuevoProveedor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBuscarProveedor)
                    .addComponent(txtProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        pnlTransaccion.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlTransaccion.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlTransaccion.setName("pnlTransaccion"); // NOI18N

        pnlScrollTabla.setName("pnlScrollTabla"); // NOI18N

        tblTransaccion.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        tblTransaccion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
            },
            new String [] {
                "Código", "Calidad", "Descripción", "Cantidad", "Precio Unitario", "U. Medida", "Subtotal"
            }
        ) {
            Class[] types = new Class [] {
                ProductoTableWrapper.class, CalidadesTableWrapper.class, java.lang.Object.class, java.lang.Float.class, java.lang.Float.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, false, true, true, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTransaccion.setColumnSelectionAllowed(true);
        tblTransaccion.setName("tblTransaccion"); // NOI18N
        tblTransaccion.getTableHeader().setReorderingAllowed(false);
        tblTransaccion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tblTransaccionKeyPressed(evt);
            }
        });
        pnlScrollTabla.setViewportView(tblTransaccion);
        tblTransaccion.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblTransaccion.getColumnModel().getColumn(0).setMinWidth(80);
        tblTransaccion.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblTransaccion.getColumnModel().getColumn(0).setMaxWidth(100);
        tblTransaccion.getColumnModel().getColumn(1).setMinWidth(80);
        tblTransaccion.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblTransaccion.getColumnModel().getColumn(1).setMaxWidth(200);
        tblTransaccion.getColumnModel().getColumn(2).setMinWidth(150);
        tblTransaccion.getColumnModel().getColumn(2).setPreferredWidth(300);
        tblTransaccion.getColumnModel().getColumn(3).setMinWidth(70);
        tblTransaccion.getColumnModel().getColumn(3).setPreferredWidth(70);
        tblTransaccion.getColumnModel().getColumn(3).setMaxWidth(100);
        tblTransaccion.getColumnModel().getColumn(4).setMinWidth(80);
        tblTransaccion.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblTransaccion.getColumnModel().getColumn(4).setMaxWidth(100);
        tblTransaccion.getColumnModel().getColumn(5).setMinWidth(80);
        tblTransaccion.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblTransaccion.getColumnModel().getColumn(5).setMaxWidth(100);
        tblTransaccion.getColumnModel().getColumn(6).setMinWidth(80);
        tblTransaccion.getColumnModel().getColumn(6).setPreferredWidth(100);
        tblTransaccion.getColumnModel().getColumn(6).setMaxWidth(200);

        lblTotal.setFont(resourceMap.getFont("Application.font.big")); // NOI18N
        lblTotal.setText(resourceMap.getString("lblTotal.text")); // NOI18N
        lblTotal.setName("lblTotal"); // NOI18N

        txtTotalCompra.setEditable(false);
        txtTotalCompra.setFont(resourceMap.getFont("Application.font.big")); // NOI18N
        txtTotalCompra.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtTotalCompra.setText(resourceMap.getString("txtTotalCompra.text")); // NOI18N
        txtTotalCompra.setName("txtTotalCompra"); // NOI18N

        btnQuitarSeleccion.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnQuitarSeleccion.setIcon(resourceMap.getIcon("btnQuitarSeleccion.icon")); // NOI18N
        btnQuitarSeleccion.setMnemonic('s');
        btnQuitarSeleccion.setText(resourceMap.getString("btnQuitarSeleccion.text")); // NOI18N
        btnQuitarSeleccion.setName("btnQuitarSeleccion"); // NOI18N
        btnQuitarSeleccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarSeleccionActionPerformed(evt);
            }
        });

        lblAyuda.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblAyuda.setIcon(resourceMap.getIcon("lblAyuda.icon")); // NOI18N
        lblAyuda.setText(resourceMap.getString("lblAyuda.text")); // NOI18N
        lblAyuda.setToolTipText(resourceMap.getString("lblAyuda.toolTipText")); // NOI18N
        lblAyuda.setName("lblAyuda"); // NOI18N

        javax.swing.GroupLayout pnlTransaccionLayout = new javax.swing.GroupLayout(pnlTransaccion);
        pnlTransaccion.setLayout(pnlTransaccionLayout);
        pnlTransaccionLayout.setHorizontalGroup(
            pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTransaccionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlScrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTransaccionLayout.createSequentialGroup()
                        .addComponent(btnQuitarSeleccion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblAyuda)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
                        .addComponent(lblTotal)
                        .addGap(14, 14, 14)
                        .addComponent(txtTotalCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlTransaccionLayout.setVerticalGroup(
            pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTransaccionLayout.createSequentialGroup()
                .addComponent(pnlScrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                .addGap(9, 9, 9)
                .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtTotalCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTotal))
                    .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnQuitarSeleccion)
                        .addComponent(lblAyuda)))
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

        btnRegistrar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnRegistrar.setIcon(resourceMap.getIcon("btnRegistrar.icon")); // NOI18N
        btnRegistrar.setMnemonic('r');
        btnRegistrar.setText(resourceMap.getString("btnRegistrar.text")); // NOI18N
        btnRegistrar.setName("btnRegistrar"); // NOI18N
        btnRegistrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarActionPerformed(evt);
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

        pnlFormaPago.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlFormaPago.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlFormaPago.setName("pnlFormaPago"); // NOI18N

        cboFormaPago.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboFormaPago.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Contado" }));
        cboFormaPago.setName("cboFormaPago"); // NOI18N
        cboFormaPago.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboFormaPagoActionPerformed(evt);
            }
        });

        lblFactura.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblFactura.setText(resourceMap.getString("lblFactura.text")); // NOI18N
        lblFactura.setName("lblFactura"); // NOI18N

        cboTipoFactura.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboTipoFactura.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A", "B", "C", "X" }));
        cboTipoFactura.setName("cboTipoFactura"); // NOI18N

        txtFacturaSucursal.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
        txtFacturaSucursal.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtFacturaSucursal.setName("txtFacturaSucursal"); // NOI18N
        txtFacturaSucursal.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtFacturaSucursalFocusLost(evt);
            }
        });

        txtFacturaNumero.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
        txtFacturaNumero.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtFacturaNumero.setName("txtFacturaNumero"); // NOI18N
        txtFacturaNumero.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtFacturaNumeroFocusLost(evt);
            }
        });

        javax.swing.GroupLayout pnlFormaPagoLayout = new javax.swing.GroupLayout(pnlFormaPago);
        pnlFormaPago.setLayout(pnlFormaPagoLayout);
        pnlFormaPagoLayout.setHorizontalGroup(
            pnlFormaPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormaPagoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormaPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboFormaPago, 0, 246, Short.MAX_VALUE)
                    .addGroup(pnlFormaPagoLayout.createSequentialGroup()
                        .addComponent(lblFactura)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboTipoFactura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFacturaSucursal, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFacturaNumero, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlFormaPagoLayout.setVerticalGroup(
            pnlFormaPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormaPagoLayout.createSequentialGroup()
                .addComponent(cboFormaPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFormaPagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFactura)
                    .addComponent(cboTipoFactura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFacturaNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFacturaSucursal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlTransaccion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCerrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 229, Short.MAX_VALUE)
                        .addComponent(btnRegistrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(10, 10, 10)
                        .addComponent(pnlFormaPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlFormaPago, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTransaccion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnRegistrar)
                    .addComponent(btnCerrar))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void optSeleccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optSeleccionActionPerformed
    if (optSeleccion.isSelected()) {
        btnNuevoProveedor.setEnabled(true);
        btnBuscarProveedor.setEnabled(true);
        txtProveedor.setText("");
    }
}//GEN-LAST:event_optSeleccionActionPerformed

private void optDesconocidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optDesconocidoActionPerformed
    if (optDesconocido.isSelected()) {
        btnNuevoProveedor.setEnabled(false);
        btnBuscarProveedor.setEnabled(false);
        txtProveedor.setText("");
    }
}//GEN-LAST:event_optDesconocidoActionPerformed

private void btnNuevoProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoProveedorActionPerformed
    opcionNuevoProveedor();
}//GEN-LAST:event_btnNuevoProveedorActionPerformed

private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
    inicializarVentana();
}//GEN-LAST:event_btnCancelarActionPerformed

private void btnQuitarSeleccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarSeleccionActionPerformed
    quitarSeleccion();
}//GEN-LAST:event_btnQuitarSeleccionActionPerformed

private void btnBuscarProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarProveedorActionPerformed
    buscarProveedor();
}//GEN-LAST:event_btnBuscarProveedorActionPerformed

private void tblTransaccionKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblTransaccionKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_F1) {
        //Opción Desconocido
        optDesconocido.setSelected(true);
        return;
    }
    if (evt.getKeyCode() == KeyEvent.VK_F2) {
        //Opción Seleccionar Proveedor
        optSeleccion.setSelected(true);
        return;
    }
    if (optSeleccion.isSelected()) {
        if (evt.getKeyCode() == KeyEvent.VK_F3) {
            //Registrar Nuevo Proveedor
            opcionNuevoProveedor();
            return;
        }
        if (evt.getKeyCode() == KeyEvent.VK_F4) {
            //Buscar Proveedor
            buscarProveedor();
            return;
        }
    }
    if (evt.getKeyCode() == KeyEvent.VK_F5) {
        //Quitar Selección
        quitarSeleccion();
        return;
    }
    if (evt.getKeyCode() == KeyEvent.VK_F6) {
        //Aceptar
        tomarConfirmacion();
        return;
    }
    if (evt.getKeyCode() == KeyEvent.VK_F7) {
        //Cancelar
        inicializarVentana();
        return;
    }
}//GEN-LAST:event_tblTransaccionKeyPressed

private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarActionPerformed
    tomarConfirmacion();
}//GEN-LAST:event_btnRegistrarActionPerformed

private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
    dispose();
}//GEN-LAST:event_btnCerrarActionPerformed

private void cboFormaPagoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboFormaPagoActionPerformed
    tomarSeleccionFormaPago();
}//GEN-LAST:event_cboFormaPagoActionPerformed

private void txtFacturaNumeroFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtFacturaNumeroFocusLost
    TextValidator.validarInt(this, txtFacturaNumero, null);
}//GEN-LAST:event_txtFacturaNumeroFocusLost

private void txtFacturaSucursalFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtFacturaSucursalFocusLost
    TextValidator.validarInt(this, txtFacturaSucursal, null);
}//GEN-LAST:event_txtFacturaSucursalFocusLost
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarProveedor;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnNuevoProveedor;
    private javax.swing.JButton btnQuitarSeleccion;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JComboBox cboFormaPago;
    private javax.swing.JComboBox cboTipoFactura;
    private javax.swing.JLabel lblAyuda;
    private javax.swing.JLabel lblFactura;
    private javax.swing.JLabel lblTotal;
    private javax.swing.ButtonGroup opcionesProveedor;
    private javax.swing.JRadioButton optDesconocido;
    private javax.swing.JRadioButton optSeleccion;
    private javax.swing.JPanel pnlFormaPago;
    private javax.swing.JPanel pnlProveedor;
    private javax.swing.JScrollPane pnlScrollTabla;
    private javax.swing.JPanel pnlTransaccion;
    private javax.swing.JTable tblTransaccion;
    private javax.swing.JFormattedTextField txtFacturaNumero;
    private javax.swing.JFormattedTextField txtFacturaSucursal;
    private javax.swing.JTextField txtProveedor;
    private javax.swing.JTextField txtTotalCompra;
    // End of variables declaration//GEN-END:variables
}
