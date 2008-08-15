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
package gecom.app.ventas;

import gecom.app.VentanaMenuPrincipal;
import gecom.app.buscador.Buscador;
import gecom.app.clientes.VentanaActualizarCliente;
import gecom.app.configuracion.ParamSistema;
import gecom.app.table.CalidadesTableEditor;
import gecom.app.table.CalidadesTableRender;
import gecom.app.table.CalidadesTableWrapper;
import gecom.app.table.CantidadStockTableEditor;
import gecom.app.table.CantidadStockTableRender;
import gecom.app.table.CantidadStockTableWrapper;
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
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.formateadores.FormateadorEstandar;
import simbya.framework.interfaces.VentanaInterna;
import simbya.framework.validadores.ComboUtil;
import simbya.gecom.entidades.CalidadProducto;
import simbya.gecom.entidades.Cliente;
import simbya.gecom.entidades.DetalleVenta;
import simbya.gecom.entidades.Producto;
import simbya.gecom.entidades.TipoFormaCobro;
import simbya.gecom.entidades.Venta;
import simbya.gecom.entidades.parametros.ConfiguracionGeneral;
import simbya.gecom.entidades.seguridad.CasoDeUso;
import simbya.gecom.entidades.seguridad.UsuarioSistema;
import simbya.gecom.gestores.ventas.GestorRegistrarVentaRemote;

/**
 * Ventana para registrar ventas.
 * @author Marcelo Busico.
 */
public class VentanaRegistrarVenta extends VentanaInterna {

    /** Cantidad de fila de la tabla */
    private static final int CANT_FILAS = 50;
    private static final Logger log = Logger.getLogger(
            VentanaRegistrarVenta.class);
    private GestorRegistrarVentaRemote gestor;
    private Cliente comprador = null;
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
    private static final int FORMA_TARJETA = 3;
    private boolean usuarioConPrivilegios;
    private static final String TIPO_FACTURA = "Factura";
    private static final String TIPO_REMITO = "Remito";
    private float importeSubtotal;
    private float importeRecargoDescuento;
    private float importeTotal;

    /** Creates new form VentanaRegistrarVenta */
    public VentanaRegistrarVenta() {
        cargarPrivilegiosUsuario();
        initComponents();
        modeloTabla = (DefaultTableModel) tblTransaccion.getModel();
        modeloTabla.setRowCount(CANT_FILAS);
        //Para Productos
        tblTransaccion.setDefaultEditor(ProductoTableWrapper.class,
                new ProductoTableEditor(false));
        tblTransaccion.setDefaultRenderer(ProductoTableWrapper.class,
                new ProductoTableRender(tblTransaccion.getSelectionBackground()));
        //Para calidades
        CalidadesTableEditor calidadesTableEditor = new CalidadesTableEditor();
        tblTransaccion.setDefaultEditor(CalidadesTableWrapper.class,
                calidadesTableEditor);
        tblTransaccion.addKeyListener(calidadesTableEditor);
        tblTransaccion.setDefaultRenderer(CalidadesTableWrapper.class,
                new CalidadesTableRender(tblTransaccion.getSelectionBackground()));
        //Para Stock
        tblTransaccion.setDefaultEditor(CantidadStockTableWrapper.class,
                new CantidadStockTableEditor());
        tblTransaccion.setDefaultRenderer(CantidadStockTableWrapper.class,
                new CantidadStockTableRender(tblTransaccion.getSelectionBackground()));
        //Para float.
        tblTransaccion.setDefaultEditor(Float.class,
                new FloatTableEditor());
        initTableListener();
    }

    /**
     * Setear los privilegios del usuario.
     */
    private void cargarPrivilegiosUsuario() {
        usuarioConPrivilegios = false;
        UsuarioSistema usuarioSistema = VentanaMenuPrincipal.getInstancia().
                getGestor().getUsuarioSistema();

        if (usuarioSistema.getOid() == 0) {
            //Administrados del sistema.
            usuarioConPrivilegios = true;
            return;
        }

        if (usuarioSistema != null) {
            Set<CasoDeUso> privilegiosCU = usuarioSistema.getTipo().getPrivilegiosCU();
            if (privilegiosCU.contains(CasoDeUso.getCasoDeUso(
                    CasoDeUso.Particular_Venta_TodosLosPrivilegios))) {
                usuarioConPrivilegios = true;
            }
        }
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorRegistrarVentaRemote) gc.getObjetoRemoto(
                GestorRegistrarVentaRemote.class);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    public void inicializarVentana() {
        importeRecargoDescuento = 0;
        importeSubtotal = 0;
        importeTotal = 0;
        comprador = null;
        optConsumidorFinal.setSelected(true);
        btnNuevoCliente.setEnabled(false);
        btnBuscarCliente.setEnabled(false);
        txtCliente.setText("");
        List<TipoFormaCobro> formasCobro = gestor.cargarObjetosPersistentes(
                TipoFormaCobro.class);
        new ComboUtil<TipoFormaCobro>(cboFormaCobro).cleanAndLoad(formasCobro);
        if (cboFormaCobro.getItemCount() != 0) {
            cboFormaCobro.setSelectedIndex(0);
        }
        txtFormaRecDesc.setValue(new Float(0f));
        txtFormaRecDesc.setEnabled(usuarioConPrivilegios);
        txtSubtotal.setText("");
        txtRecargoDescuento.setText("");
        txtTotalVenta.setText("");
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
            }
        }
    }

    public void clienteRegistrado(Cliente cliente) {
        this.comprador = cliente;
        if (cliente != null) {
            txtCliente.setText(comprador.getIdentidad());
        } else {
            txtCliente.setText("");
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

    private void opcionNuevoCliente() {
//        if (usuarioConPrivilegios) {
        setVisible(false);
        VentanaActualizarCliente actualizarCliente =
                (VentanaActualizarCliente) VentanaMenuPrincipal.getInstancia().getGestor().iniciarCU(
                VentanaActualizarCliente.class);
        actualizarCliente.asistirRegistrarNuevoCliente(this);
//        } else {
//            JOptionPane.showMessageDialog(this,
//                    "No cuenta con los privilegios suficientes para realizar esta acción.");
//        }
    }

    private void buscarCliente() {
        Cliente res = Buscador.buscarCliente(false);
        if (res != null) {
            comprador = res;
            txtCliente.setText(comprador.getIdentidad());
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
        modeloTabla.setValueAt(new CantidadStockTableWrapper(cal, 1f), nroFila, COLUMNA_CANTIDAD);
        modeloTabla.setValueAt(cal.calcularPrecioVenta(), nroFila,
                COLUMNA_PRECIOUNITARIO);
        calcularTotales();
    }

    private void calcularTotales() {
        actualizandoTabla = true;
        float total = 0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (modeloTabla.getValueAt(i, COLUMNA_CODIGO) != null) {
                Float cant = null;
                CantidadStockTableWrapper cantWrapper =
                        (CantidadStockTableWrapper) modeloTabla.getValueAt(
                        i, COLUMNA_CANTIDAD);
                if (cantWrapper != null) {
                    cant = cantWrapper.getCantidadSolicitada();
                }
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
            txtSubtotal.setText("");
            txtRecargoDescuento.setText("");
            txtTotalVenta.setText("");
            importeSubtotal = 0;
            importeRecargoDescuento = 0;
            importeTotal = 0;
        } else {
            float porcRecDesc = (Float) txtFormaRecDesc.getValue();
            switch (formaSeleccionada) {
                case FORMA_CONTADO:
                case FORMA_CUENTACORRIENTE:
                    if (porcRecDesc != 0) {
                        porcRecDesc = (-1) * porcRecDesc;
                    }
                    break;
            }
            importeSubtotal = total;
            importeRecargoDescuento = (porcRecDesc / 100) * total;
            importeTotal = importeSubtotal + importeRecargoDescuento;
            txtSubtotal.setText(FormateadorEstandar.formatearDinero(importeSubtotal));
            txtRecargoDescuento.setText(FormateadorEstandar.formatearDinero(importeRecargoDescuento));
            txtTotalVenta.setText(FormateadorEstandar.formatearDinero(importeTotal));
        }
        actualizandoTabla = false;
    }

    private void tomarSeleccionFormaCobro() {
        TipoFormaCobro selected = new ComboUtil<TipoFormaCobro>(cboFormaCobro).getSelected();
        if (selected == null) {
            return;
        }
        Long oidContado = ParamSistema.getValorParametroLong(ConfiguracionGeneral.oidCobroContado);
        Long oidCuentaCorriente = ParamSistema.getValorParametroLong(ConfiguracionGeneral.oidCobroCuentaCorriente);
        Long oidTarjeta = ParamSistema.getValorParametroLong(ConfiguracionGeneral.oidCobroTarjeta);

        Float descuento = ParamSistema.getValorParametroFloat(ConfiguracionGeneral.descuentoContado);
        Float recargo = ParamSistema.getValorParametroFloat(ConfiguracionGeneral.recargoTarjeta);
        if (selected.getOid() == oidContado) {
            formaSeleccionada = FORMA_CONTADO;
            lblFormaRecDesc.setText("Descuento");
            txtFormaRecDesc.setValue(descuento);
        }
        if (selected.getOid() == oidTarjeta) {
            formaSeleccionada = FORMA_TARJETA;
            lblFormaRecDesc.setText("Recargo");
            txtFormaRecDesc.setValue(recargo);
        }
        if (selected.getOid() == oidCuentaCorriente) {
            formaSeleccionada = FORMA_CUENTACORRIENTE;
            lblFormaRecDesc.setText("Descuento");
            txtFormaRecDesc.setValue(0);
        }
        calcularTotales();
    }

    /**
     * Confirma la venta actual.
     */
    private void tomarConfirmacion() {
        //Verificar que haya datos cargados
        Set<DetalleVenta> detalles = new TreeSet<DetalleVenta>();
        int item = 0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (modeloTabla.getValueAt(i, COLUMNA_CODIGO) != null) {
                CantidadStockTableWrapper cantStock =
                        (CantidadStockTableWrapper) modeloTabla.getValueAt(i, COLUMNA_CANTIDAD);
                Float cant = cantStock.getCantidadSolicitada();
                Float precio = (Float) modeloTabla.getValueAt(i, COLUMNA_PRECIOUNITARIO);
                CalidadesTableWrapper prodYCalidad =
                        (CalidadesTableWrapper) modeloTabla.getValueAt(i, COLUMNA_CALIDAD);
                if (prodYCalidad != null && cant != null && precio != null) {
                    DetalleVenta dv = new DetalleVenta();
                    item++;
                    dv.setCantidad(cant);
                    dv.setItem(item);
                    dv.setProducto(prodYCalidad.getProducto());
                    dv.setCalidad(prodYCalidad.getCalidad());
                    dv.setImporteUnitario(precio);
                    detalles.add(dv);
                }
            }
        }
        if (item == 0 || importeSubtotal == 0) {
            JOptionPane.showMessageDialog(this,
                    "Para registrar una venta debe cargar al menos un detalle\n" +
                    "y asegurarse de que el importe de los productos no sean '0'.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Solicitar confirmación de usuario.
        int res = JOptionPane.showConfirmDialog(this,
                "Está a punto de confirmar la venta.\n" +
                "¿Desea continuar?", "Confirmación requerida",
                JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.NO_OPTION) {
            return;
        }

        //Crea la venta y setea los datos.
        Venta venta = new Venta();
        if (optConsumidorFinal.isSelected()) {
            venta.setCliente(null);
        } else {
            venta.setCliente(comprador);
        }
//TODO: Cambiar aquí cuando se agregue la posibilidad de imprimir facturas.
        String tipoFactura = ConfiguracionGeneral.ultimoNroRemito;
        Integer nroComprobante = ParamSistema.getValorParametroInteger(tipoFactura) + 1;
        venta.setFactura(nroComprobante);
        venta.setTipoFactura(TIPO_REMITO);
        venta.setFecha(new Date());
        TipoFormaCobro formaCobro = new ComboUtil<TipoFormaCobro>(cboFormaCobro).getSelected();
        venta.setFormaCobro(formaCobro);
        venta.setImporteTotal(importeTotal);
        venta.setImporteRecargoDescuento(importeRecargoDescuento);
        //Agregar los detalles de la venta (solo los completos)
        venta.setDetalles(detalles);

        //Persistir la venta.
        try {
            gestor.registrarVenta(venta);
            ParamSistema.actualizarValorParametro(tipoFactura, nroComprobante);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al registrar la venta en la base de datos:\n" +
                    ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (chkImprimirRemito.isSelected()) {
            ImpresorRemito.imprimirRemitoVenta(venta);
        }
        //Informar al usuario.
        JOptionPane.showMessageDialog(this,
                "La venta fue registrada con éxito.",
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

        opcionesCliente = new javax.swing.ButtonGroup();
        pnlCliente = new javax.swing.JPanel();
        optConsumidorFinal = new javax.swing.JRadioButton();
        optSeleccion = new javax.swing.JRadioButton();
        txtCliente = new javax.swing.JTextField();
        btnBuscarCliente = new javax.swing.JButton();
        btnNuevoCliente = new javax.swing.JButton();
        pnlTransaccion = new javax.swing.JPanel();
        pnlScrollTabla = new javax.swing.JScrollPane();
        tblTransaccion = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        txtTotalVenta = new javax.swing.JTextField();
        btnQuitarSeleccion = new javax.swing.JButton();
        txtSubtotal = new javax.swing.JTextField();
        txtRecargoDescuento = new javax.swing.JTextField();
        lblRecargoDescuento = new javax.swing.JLabel();
        lblSubtotal = new javax.swing.JLabel();
        lblAyuda = new javax.swing.JLabel();
        btnCancelar = new javax.swing.JButton();
        btnRegistrar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        pnlFormaCobro = new javax.swing.JPanel();
        cboFormaCobro = new javax.swing.JComboBox();
        lblFormaRecDesc = new javax.swing.JLabel();
        txtFormaRecDesc = new javax.swing.JSpinner();
        lblPorciento = new javax.swing.JLabel();
        chkImprimirRemito = new javax.swing.JCheckBox();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaRegistrarVenta.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setMinimumSize(new java.awt.Dimension(780, 500));
        setName("Form"); // NOI18N
        setNextFocusableComponent(tblTransaccion);

        pnlCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlCliente.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlCliente.setName("pnlCliente"); // NOI18N

        opcionesCliente.add(optConsumidorFinal);
        optConsumidorFinal.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        optConsumidorFinal.setSelected(true);
        optConsumidorFinal.setText(resourceMap.getString("optConsumidorFinal.text")); // NOI18N
        optConsumidorFinal.setName("optConsumidorFinal"); // NOI18N
        optConsumidorFinal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optConsumidorFinalActionPerformed(evt);
            }
        });

        opcionesCliente.add(optSeleccion);
        optSeleccion.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        optSeleccion.setText(resourceMap.getString("optSeleccion.text")); // NOI18N
        optSeleccion.setName("optSeleccion"); // NOI18N
        optSeleccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optSeleccionActionPerformed(evt);
            }
        });

        txtCliente.setEditable(false);
        txtCliente.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtCliente.setText(resourceMap.getString("txtCliente.text")); // NOI18N
        txtCliente.setName("txtCliente"); // NOI18N

        btnBuscarCliente.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnBuscarCliente.setIcon(resourceMap.getIcon("btnBuscarCliente.icon")); // NOI18N
        btnBuscarCliente.setMnemonic('b');
        btnBuscarCliente.setText(resourceMap.getString("btnBuscarCliente.text")); // NOI18N
        btnBuscarCliente.setEnabled(false);
        btnBuscarCliente.setName("btnBuscarCliente"); // NOI18N
        btnBuscarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarClienteActionPerformed(evt);
            }
        });

        btnNuevoCliente.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnNuevoCliente.setIcon(resourceMap.getIcon("btnNuevoCliente.icon")); // NOI18N
        btnNuevoCliente.setMnemonic('n');
        btnNuevoCliente.setText(resourceMap.getString("btnNuevoCliente.text")); // NOI18N
        btnNuevoCliente.setName("btnNuevoCliente"); // NOI18N
        btnNuevoCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoClienteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlClienteLayout = new javax.swing.GroupLayout(pnlCliente);
        pnlCliente.setLayout(pnlClienteLayout);
        pnlClienteLayout.setHorizontalGroup(
            pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlClienteLayout.createSequentialGroup()
                        .addComponent(optConsumidorFinal)
                        .addGap(18, 18, 18)
                        .addComponent(optSeleccion)
                        .addGap(4, 4, 4))
                    .addGroup(pnlClienteLayout.createSequentialGroup()
                        .addComponent(txtCliente, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnNuevoCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnBuscarCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlClienteLayout.setVerticalGroup(
            pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClienteLayout.createSequentialGroup()
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(optConsumidorFinal)
                    .addComponent(optSeleccion)
                    .addComponent(btnNuevoCliente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBuscarCliente)
                    .addComponent(txtCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                ProductoTableWrapper.class, CalidadesTableWrapper.class, java.lang.Object.class, CantidadStockTableWrapper.class, java.lang.Float.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, false, true, usuarioConPrivilegios, false, false
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

        txtTotalVenta.setEditable(false);
        txtTotalVenta.setFont(resourceMap.getFont("Application.font.big")); // NOI18N
        txtTotalVenta.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtTotalVenta.setText(resourceMap.getString("txtTotalVenta.text")); // NOI18N
        txtTotalVenta.setName("txtTotalVenta"); // NOI18N

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

        txtSubtotal.setEditable(false);
        txtSubtotal.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtSubtotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSubtotal.setText(resourceMap.getString("txtSubtotal.text")); // NOI18N
        txtSubtotal.setName("txtSubtotal"); // NOI18N

        txtRecargoDescuento.setEditable(false);
        txtRecargoDescuento.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtRecargoDescuento.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtRecargoDescuento.setText(resourceMap.getString("txtRecargoDescuento.text")); // NOI18N
        txtRecargoDescuento.setName("txtRecargoDescuento"); // NOI18N

        lblRecargoDescuento.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblRecargoDescuento.setText(resourceMap.getString("lblRecargoDescuento.text")); // NOI18N
        lblRecargoDescuento.setName("lblRecargoDescuento"); // NOI18N

        lblSubtotal.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblSubtotal.setText(resourceMap.getString("lblSubtotal.text")); // NOI18N
        lblSubtotal.setName("lblSubtotal"); // NOI18N

        lblAyuda.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblAyuda.setText(resourceMap.getString("lblAyuda.text")); // NOI18N
        lblAyuda.setName("lblAyuda"); // NOI18N

        javax.swing.GroupLayout pnlTransaccionLayout = new javax.swing.GroupLayout(pnlTransaccion);
        pnlTransaccion.setLayout(pnlTransaccionLayout);
        pnlTransaccionLayout.setHorizontalGroup(
            pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTransaccionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlScrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 762, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTransaccionLayout.createSequentialGroup()
                        .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAyuda)
                            .addComponent(btnQuitarSeleccion))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
                        .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRecargoDescuento)
                            .addComponent(lblTotal)
                            .addComponent(lblSubtotal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtRecargoDescuento, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtSubtotal, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtTotalVenta, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlTransaccionLayout.setVerticalGroup(
            pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTransaccionLayout.createSequentialGroup()
                .addComponent(pnlScrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlTransaccionLayout.createSequentialGroup()
                        .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSubtotal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtRecargoDescuento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblRecargoDescuento))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlTransaccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTotalVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTotal)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTransaccionLayout.createSequentialGroup()
                        .addComponent(btnQuitarSeleccion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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

        pnlFormaCobro.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlFormaCobro.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlFormaCobro.setName("pnlFormaCobro"); // NOI18N

        cboFormaCobro.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        cboFormaCobro.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Contado" }));
        cboFormaCobro.setName("cboFormaCobro"); // NOI18N
        cboFormaCobro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboFormaCobroActionPerformed(evt);
            }
        });

        lblFormaRecDesc.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblFormaRecDesc.setText(resourceMap.getString("lblFormaRecDesc.text")); // NOI18N
        lblFormaRecDesc.setName("lblFormaRecDesc"); // NOI18N

        txtFormaRecDesc.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtFormaRecDesc.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(100.0f), Float.valueOf(1.0f)));
        txtFormaRecDesc.setName("txtFormaRecDesc"); // NOI18N
        txtFormaRecDesc.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                txtFormaRecDescStateChanged(evt);
            }
        });

        lblPorciento.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblPorciento.setText(resourceMap.getString("lblPorciento.text")); // NOI18N
        lblPorciento.setName("lblPorciento"); // NOI18N

        javax.swing.GroupLayout pnlFormaCobroLayout = new javax.swing.GroupLayout(pnlFormaCobro);
        pnlFormaCobro.setLayout(pnlFormaCobroLayout);
        pnlFormaCobroLayout.setHorizontalGroup(
            pnlFormaCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormaCobroLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormaCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboFormaCobro, 0, 192, Short.MAX_VALUE)
                    .addGroup(pnlFormaCobroLayout.createSequentialGroup()
                        .addComponent(lblFormaRecDesc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFormaRecDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPorciento)))
                .addContainerGap())
        );
        pnlFormaCobroLayout.setVerticalGroup(
            pnlFormaCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormaCobroLayout.createSequentialGroup()
                .addComponent(cboFormaCobro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFormaCobroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFormaRecDesc)
                    .addComponent(lblPorciento)
                    .addComponent(txtFormaRecDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        chkImprimirRemito.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        chkImprimirRemito.setMnemonic('I');
        chkImprimirRemito.setText(resourceMap.getString("chkImprimirRemito.text")); // NOI18N
        chkImprimirRemito.setName("chkImprimirRemito"); // NOI18N

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 139, Short.MAX_VALUE)
                        .addComponent(chkImprimirRemito)
                        .addGap(18, 18, 18)
                        .addComponent(btnRegistrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlFormaCobro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlCliente, 0, 97, Short.MAX_VALUE)
                    .addComponent(pnlFormaCobro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTransaccion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnRegistrar)
                    .addComponent(btnCerrar)
                    .addComponent(chkImprimirRemito))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void optSeleccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optSeleccionActionPerformed
    if (optSeleccion.isSelected()) {
        btnNuevoCliente.setEnabled(true);
        btnBuscarCliente.setEnabled(true);
        txtCliente.setText("");
    }
}//GEN-LAST:event_optSeleccionActionPerformed

private void optConsumidorFinalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optConsumidorFinalActionPerformed
    if (optConsumidorFinal.isSelected()) {
        btnNuevoCliente.setEnabled(false);
        btnBuscarCliente.setEnabled(false);
        txtCliente.setText("");
    }
}//GEN-LAST:event_optConsumidorFinalActionPerformed

private void btnNuevoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoClienteActionPerformed
    opcionNuevoCliente();
}//GEN-LAST:event_btnNuevoClienteActionPerformed

private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
    inicializarVentana();
}//GEN-LAST:event_btnCancelarActionPerformed

private void btnQuitarSeleccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarSeleccionActionPerformed
    quitarSeleccion();
}//GEN-LAST:event_btnQuitarSeleccionActionPerformed

private void btnBuscarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarClienteActionPerformed
    buscarCliente();
}//GEN-LAST:event_btnBuscarClienteActionPerformed

private void tblTransaccionKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblTransaccionKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_F1) {
        //Opción Consumidor Final
        optConsumidorFinal.setSelected(true);
        return;
    }
    if (evt.getKeyCode() == KeyEvent.VK_F2) {
        //Opción Seleccionar Cliente
        optSeleccion.setSelected(true);
        return;
    }
    if (optSeleccion.isSelected()) {
        if (evt.getKeyCode() == KeyEvent.VK_F3) {
            //Registrar Nuevo Cliente
            opcionNuevoCliente();
            return;
        }
        if (evt.getKeyCode() == KeyEvent.VK_F4) {
            //Buscar Cliente
            buscarCliente();
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

private void cboFormaCobroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboFormaCobroActionPerformed
    tomarSeleccionFormaCobro();
}//GEN-LAST:event_cboFormaCobroActionPerformed

private void txtFormaRecDescStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_txtFormaRecDescStateChanged
    calcularTotales();
}//GEN-LAST:event_txtFormaRecDescStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarCliente;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnNuevoCliente;
    private javax.swing.JButton btnQuitarSeleccion;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JComboBox cboFormaCobro;
    private javax.swing.JCheckBox chkImprimirRemito;
    private javax.swing.JLabel lblAyuda;
    private javax.swing.JLabel lblFormaRecDesc;
    private javax.swing.JLabel lblPorciento;
    private javax.swing.JLabel lblRecargoDescuento;
    private javax.swing.JLabel lblSubtotal;
    private javax.swing.JLabel lblTotal;
    private javax.swing.ButtonGroup opcionesCliente;
    private javax.swing.JRadioButton optConsumidorFinal;
    private javax.swing.JRadioButton optSeleccion;
    private javax.swing.JPanel pnlCliente;
    private javax.swing.JPanel pnlFormaCobro;
    private javax.swing.JScrollPane pnlScrollTabla;
    private javax.swing.JPanel pnlTransaccion;
    private javax.swing.JTable tblTransaccion;
    private javax.swing.JTextField txtCliente;
    private javax.swing.JSpinner txtFormaRecDesc;
    private javax.swing.JTextField txtRecargoDescuento;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTotalVenta;
    // End of variables declaration//GEN-END:variables
}
