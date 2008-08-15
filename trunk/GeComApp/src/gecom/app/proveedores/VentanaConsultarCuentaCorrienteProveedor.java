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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.naming.NamingException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.formateadores.FormateadorEstandar;
import simbya.framework.interfaces.VentanaInterna;
import simbya.gecom.entidades.MovimientoProveedor;
import simbya.gecom.entidades.Proveedor;
import simbya.gecom.gestores.proveedores.GestorConsultarCuentaCorrienteProveedorRemote;

/**
 * Ventana para consultar la cuenta corriente de proveedores.
 * @author Marcelo Busico.
 */
public class VentanaConsultarCuentaCorrienteProveedor extends VentanaInterna {

    private static final Logger log = Logger.getLogger(
            VentanaConsultarCuentaCorrienteProveedor.class);
    private GestorConsultarCuentaCorrienteProveedorRemote gestor;
    private DefaultTableModel modeloTabla;
    private Proveedor persona;
    private static final int COLUMNA_FECHA = 0;
    private static final int COLUMNA_TIPOMOVIMIENTO = 1;
    private static final int COLUMNA_OBSERVACIONES = 2;
    private static final int COLUMNA_IMPORTE = 3;
    private static final int COLUMNA_SALDO = 4;

    /** Creates new form VentanaConsultarCuentaCorrienteProveedor */
    public VentanaConsultarCuentaCorrienteProveedor() {
        initComponents();
        modeloTabla = (DefaultTableModel) tblMovimientoDesdeHasta.getModel();
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorConsultarCuentaCorrienteProveedorRemote) gc.getObjetoRemoto(
                GestorConsultarCuentaCorrienteProveedorRemote.class);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    public void inicializarVentana() {
        dateFechaDesde.setDate(new Date());
        dateFechaHasta.setDate(new Date());
        lblMovimientoDesdeHasta.setText(" ");
        modeloTabla.setRowCount(0);
        txtSaldoActual.setText("");
        txtSaldoAnterior.setText("");
        txtSaldoPeriodo.setText("");
        persona = null;
        txtPersona.setText("");
        pnlSolapas.setSelectedIndex(0);
    }

    private void opcionConsultar() {
        //Tomar la fecha desde y hasta
        Date fechaDesde = dateFechaDesde.getDate();
        Date fechaHasta = dateFechaHasta.getDate();

        if (fechaDesde == null || fechaHasta == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar las fechas desde y hasta para la consulta.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Quita las horas y minutos de las fechas.
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaDesde);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        fechaDesde = cal.getTime();
        log.debug("Fecha desde: " + fechaDesde);

        cal.setTime(fechaHasta);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        fechaHasta = cal.getTime();
        log.debug("Fecha hasta: " + fechaHasta);

        //Verifica que la fecha desde sea anterior a fecha hasta.
        if (fechaDesde.before(fechaHasta)) {
            JOptionPane.showMessageDialog(this,
                    "La fecha hasta no puede ser menor a la fecha desde.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        //Pasar los datos al gestor y generar el informe.
        List<MovimientoProveedor> movimientos = gestor.getMovimientosDelPeriodo(
                persona, fechaDesde, fechaHasta);

        modeloTabla.setRowCount(0);

        if (movimientos.size() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No se encontraron movimientos en el periodo seleccionado.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Mostrar el titulo del informe.
        lblMovimientoDesdeHasta.setText("Movimientos desde el " +
                FormateadorEstandar.formatearFecha(fechaDesde) +
                " hasta el " + FormateadorEstandar.formatearFecha(fechaHasta));
//TODO: Completar
//        float totalVentas = 0;
//        for (ProductoVendido pv : ventasDelPeriodo) {
//            //Llenar la tabla
//            Object[] fila = new Object[6];
//
//            fila[COLUMNA_CODIGO] = pv.getProducto().getCodigo();
//            fila[COLUMNA_DESCRIPCION] = pv;
//            fila[COLUMNA_CANTIDAD] = pv.getCantidadVendida();
//            fila[COLUMNA_UNIDADES] = pv.getProducto().getProductoUM().getNombre();
//            fila[COLUMNA_IMPORTE] = pv.getImportePromedioVenta();
//            fila[COLUMNA_SUBTOTAL] = pv.getSubtotalVenta();
//
//            modeloTabla.addRow(fila);
//
//            //Acumula
//            totalVentas += pv.getSubtotalVenta();
//        }
//
//        //Mostrar Totales
//        txtTotalVentas.setText(
//                FormateadorEstandar.formatearDinero(totalVentas));

    }

    private void buscarPersona() {
        Proveedor res = Buscador.buscarProveedor(false);
        if (res != null) {
            persona = res;
            txtPersona.setText(persona.getIdentidad());
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

        btnCancelar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        pnlSolapas = new javax.swing.JTabbedPane();
        pnlProveedor = new javax.swing.JPanel();
        lblProveedor = new javax.swing.JLabel();
        txtPersona = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnConsultar = new javax.swing.JButton();
        pnlFechas = new javax.swing.JPanel();
        lblFechaDesde = new javax.swing.JLabel();
        lblFechaHasta = new javax.swing.JLabel();
        dateFechaHasta = new com.toedter.calendar.JDateChooser();
        dateFechaDesde = new com.toedter.calendar.JDateChooser();
        lblTituloFiltros = new javax.swing.JLabel();
        pnlMovimientoDesdeHasta = new javax.swing.JPanel();
        lblMovimientoDesdeHasta = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMovimientoDesdeHasta = new javax.swing.JTable();
        pnlTotales = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtSaldoPeriodo = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtSaldoAnterior = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtSaldoActual = new javax.swing.JTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaConsultarCuentaCorrienteProveedor.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setMinimumSize(new java.awt.Dimension(750, 450));
        setName("Form"); // NOI18N

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

        btnCerrar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnCerrar.setIcon(resourceMap.getIcon("btnCerrar.icon")); // NOI18N
        btnCerrar.setText(resourceMap.getString("btnCerrar.text")); // NOI18N
        btnCerrar.setName("btnCerrar"); // NOI18N
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        pnlSolapas.setName("pnlSolapas"); // NOI18N

        pnlProveedor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlProveedor.setName("pnlProveedor"); // NOI18N

        lblProveedor.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblProveedor.setText(resourceMap.getString("lblProveedor.text")); // NOI18N
        lblProveedor.setName("lblProveedor"); // NOI18N

        txtPersona.setEditable(false);
        txtPersona.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtPersona.setText(resourceMap.getString("txtPersona.text")); // NOI18N
        txtPersona.setName("txtPersona"); // NOI18N

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

        btnConsultar.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnConsultar.setIcon(resourceMap.getIcon("btnConsultar.icon")); // NOI18N
        btnConsultar.setMnemonic('c');
        btnConsultar.setText(resourceMap.getString("btnConsultar.text")); // NOI18N
        btnConsultar.setName("btnConsultar"); // NOI18N
        btnConsultar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsultarActionPerformed(evt);
            }
        });

        pnlFechas.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Fechas", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14))); // NOI18N
        pnlFechas.setName("pnlFechas"); // NOI18N

        lblFechaDesde.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblFechaDesde.setText(resourceMap.getString("lblFechaDesde.text")); // NOI18N
        lblFechaDesde.setName("lblFechaDesde"); // NOI18N

        lblFechaHasta.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblFechaHasta.setText(resourceMap.getString("lblFechaHasta.text")); // NOI18N
        lblFechaHasta.setName("lblFechaHasta"); // NOI18N

        dateFechaHasta.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        dateFechaHasta.setName("dateFechaHasta"); // NOI18N

        dateFechaDesde.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        dateFechaDesde.setName("dateFechaDesde"); // NOI18N

        javax.swing.GroupLayout pnlFechasLayout = new javax.swing.GroupLayout(pnlFechas);
        pnlFechas.setLayout(pnlFechasLayout);
        pnlFechasLayout.setHorizontalGroup(
            pnlFechasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFechasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFechasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFechaHasta)
                    .addComponent(lblFechaDesde))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFechasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateFechaDesde, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                    .addComponent(dateFechaHasta, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlFechasLayout.setVerticalGroup(
            pnlFechasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFechasLayout.createSequentialGroup()
                .addGroup(pnlFechasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblFechaHasta)
                    .addComponent(dateFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFechasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFechaDesde)
                    .addComponent(dateFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblTituloFiltros.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblTituloFiltros.setText(resourceMap.getString("lblTituloFiltros.text")); // NOI18N
        lblTituloFiltros.setName("lblTituloFiltros"); // NOI18N

        javax.swing.GroupLayout pnlProveedorLayout = new javax.swing.GroupLayout(pnlProveedor);
        pnlProveedor.setLayout(pnlProveedorLayout);
        pnlProveedorLayout.setHorizontalGroup(
            pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProveedorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloFiltros)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProveedorLayout.createSequentialGroup()
                        .addComponent(lblProveedor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPersona, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBuscar))
                    .addComponent(pnlFechas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConsultar, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        pnlProveedorLayout.setVerticalGroup(
            pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProveedorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloFiltros)
                .addGap(18, 18, 18)
                .addGroup(pnlProveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProveedor)
                    .addComponent(txtPersona, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar))
                .addGap(18, 18, 18)
                .addComponent(pnlFechas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                .addComponent(btnConsultar)
                .addContainerGap())
        );

        pnlSolapas.addTab(resourceMap.getString("pnlProveedor.TabConstraints.tabTitle"), pnlProveedor); // NOI18N

        pnlMovimientoDesdeHasta.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlMovimientoDesdeHasta.setName("pnlMovimientoDesdeHasta"); // NOI18N

        lblMovimientoDesdeHasta.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblMovimientoDesdeHasta.setText(resourceMap.getString("lblMovimientoDesdeHasta.text")); // NOI18N
        lblMovimientoDesdeHasta.setName("lblMovimientoDesdeHasta"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tblMovimientoDesdeHasta.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        tblMovimientoDesdeHasta.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Fecha", "Tipo Movimiento", "Observaciones", "Importe", "Saldo"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblMovimientoDesdeHasta.setName("tblMovimientoDesdeHasta"); // NOI18N
        jScrollPane1.setViewportView(tblMovimientoDesdeHasta);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(0).setMinWidth(80);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(0).setMaxWidth(120);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tblMovimientoDesdeHasta.columnModel.title0")); // NOI18N
        tblMovimientoDesdeHasta.getColumnModel().getColumn(1).setMinWidth(100);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(1).setMaxWidth(170);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tblMovimientoDesdeHasta.columnModel.title1")); // NOI18N
        tblMovimientoDesdeHasta.getColumnModel().getColumn(2).setMinWidth(150);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tblMovimientoDesdeHasta.columnModel.title2")); // NOI18N
        tblMovimientoDesdeHasta.getColumnModel().getColumn(3).setMinWidth(80);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(3).setMaxWidth(100);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("tblMovimientoDesdeHasta.columnModel.title3")); // NOI18N
        tblMovimientoDesdeHasta.getColumnModel().getColumn(4).setMinWidth(80);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(4).setMaxWidth(100);
        tblMovimientoDesdeHasta.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("tblMovimientoDesdeHasta.columnModel.title4")); // NOI18N

        pnlTotales.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlTotales.setName("pnlTotales"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        txtSaldoPeriodo.setEditable(false);
        txtSaldoPeriodo.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtSaldoPeriodo.setText(resourceMap.getString("txtSaldoPeriodo.text")); // NOI18N
        txtSaldoPeriodo.setName("txtSaldoPeriodo"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        txtSaldoAnterior.setEditable(false);
        txtSaldoAnterior.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtSaldoAnterior.setText(resourceMap.getString("txtSaldoAnterior.text")); // NOI18N
        txtSaldoAnterior.setName("txtSaldoAnterior"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        txtSaldoActual.setEditable(false);
        txtSaldoActual.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        txtSaldoActual.setText(resourceMap.getString("txtSaldoActual.text")); // NOI18N
        txtSaldoActual.setName("txtSaldoActual"); // NOI18N

        javax.swing.GroupLayout pnlTotalesLayout = new javax.swing.GroupLayout(pnlTotales);
        pnlTotales.setLayout(pnlTotalesLayout);
        pnlTotalesLayout.setHorizontalGroup(
            pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtSaldoActual)
                    .addComponent(txtSaldoAnterior)
                    .addComponent(txtSaldoPeriodo, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlTotalesLayout.setVerticalGroup(
            pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSaldoAnterior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtSaldoPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtSaldoActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlMovimientoDesdeHastaLayout = new javax.swing.GroupLayout(pnlMovimientoDesdeHasta);
        pnlMovimientoDesdeHasta.setLayout(pnlMovimientoDesdeHastaLayout);
        pnlMovimientoDesdeHastaLayout.setHorizontalGroup(
            pnlMovimientoDesdeHastaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMovimientoDesdeHastaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMovimientoDesdeHastaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 683, Short.MAX_VALUE)
                    .addComponent(lblMovimientoDesdeHasta, javax.swing.GroupLayout.DEFAULT_SIZE, 683, Short.MAX_VALUE)
                    .addComponent(pnlTotales, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        pnlMovimientoDesdeHastaLayout.setVerticalGroup(
            pnlMovimientoDesdeHastaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMovimientoDesdeHastaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMovimientoDesdeHasta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTotales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlSolapas.addTab(resourceMap.getString("pnlMovimientoDesdeHasta.TabConstraints.tabTitle"), pnlMovimientoDesdeHasta); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSolapas, javax.swing.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCerrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 503, Short.MAX_VALUE)
                        .addComponent(btnCancelar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSolapas, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCerrar)
                    .addComponent(btnCancelar))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
    dispose();
}//GEN-LAST:event_btnCerrarActionPerformed

private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
    inicializarVentana();
}//GEN-LAST:event_btnCancelarActionPerformed

private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
    opcionConsultar();
}//GEN-LAST:event_btnConsultarActionPerformed

private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
    buscarPersona();
}//GEN-LAST:event_btnBuscarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnConsultar;
    private com.toedter.calendar.JDateChooser dateFechaDesde;
    private com.toedter.calendar.JDateChooser dateFechaHasta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFechaDesde;
    private javax.swing.JLabel lblFechaHasta;
    private javax.swing.JLabel lblMovimientoDesdeHasta;
    private javax.swing.JLabel lblProveedor;
    private javax.swing.JLabel lblTituloFiltros;
    private javax.swing.JPanel pnlFechas;
    private javax.swing.JPanel pnlMovimientoDesdeHasta;
    private javax.swing.JPanel pnlProveedor;
    private javax.swing.JTabbedPane pnlSolapas;
    private javax.swing.JPanel pnlTotales;
    private javax.swing.JTable tblMovimientoDesdeHasta;
    private javax.swing.JTextField txtPersona;
    private javax.swing.JTextField txtSaldoActual;
    private javax.swing.JTextField txtSaldoAnterior;
    private javax.swing.JTextField txtSaldoPeriodo;
    // End of variables declaration//GEN-END:variables
}
