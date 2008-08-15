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

import gecom.app.configuracion.ParamSistema;
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
import simbya.gecom.entidades.Venta;
import simbya.gecom.entidades.parametros.ConfiguracionGeneral;
import simbya.gecom.gestores.ventas.GestorGenerarInformeVentasRemote;

/**
 * Ventana para generar informes de ventas.
 * @author Marcelo Busico.
 */
public class VentanaGenerarInformeVentas extends VentanaInterna {

    private static final Logger log = Logger.getLogger(
            VentanaGenerarInformeVentas.class);
    private GestorGenerarInformeVentasRemote gestor;
    private DefaultTableModel modeloTabla;
    private static final int COLUMNA_FECHA = 0;
    private static final int COLUMNA_TIPOCPTE = 1;
    private static final int COLUMNA_NROCPTE = 2;
    private static final int COLUMNA_CLIENTE = 3;
    private static final int COLUMNA_IMPORTE = 4;
    private static final int COLUMNA_FORMACOBRO = 5;

    /** Creates new form VentanaReporteProductosVendidos */
    public VentanaGenerarInformeVentas() {
        initComponents();
        modeloTabla = (DefaultTableModel) tblVentas.getModel();
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorGenerarInformeVentasRemote) gc.getObjetoRemoto(
                GestorGenerarInformeVentasRemote.class);
    }

    /**
     * Inicializa el CU luego de enlazar con el gestor remoto con éxito.
     */
    public void inicializarVentana() {
        dateFechaDesde.setDate(new Date());
        dateFechaHasta.setDate(new Date());
        lblProductosVendidos.setText(" ");
        modeloTabla.setRowCount(0);
        txtTotalVentas.setText("");
        txtTotalVentasContado.setText("");
        txtTotalVentasTarjeta.setText("");
        txtTotalVentasCtaCte.setText("");
    }

    private void opcionGenerarInforme() {
        //Tomar la fecha desde y hasta
        Date fechaDesde = dateFechaDesde.getDate();
        Date fechaHasta = dateFechaHasta.getDate();

        if (fechaDesde == null || fechaHasta == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar las fechas desde y hasta para el informe.",
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
        if(fechaDesde.before(fechaHasta)) {
            JOptionPane.showMessageDialog(this,
                    "La fecha hasta no puede ser menor a la fecha desde.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;            
        }

        //Pasar los datos al gestor y generar el informe.
        List<Venta> ventasDelPeriodo = gestor.getVentasDelPeriodo(fechaDesde, fechaHasta);

        modeloTabla.setRowCount(0);

        if (ventasDelPeriodo.size() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No se encontraron ventas en el periodo seleccionado.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Mostrar el titulo del informe.
        lblProductosVendidos.setText("Ventas desde el " +
                FormateadorEstandar.formatearFecha(fechaDesde) +
                " hasta el " + FormateadorEstandar.formatearFecha(fechaHasta));

        float totalVentas = 0;
        float totalVentasContado = 0;
        float totalVentasTarjeta = 0;
        float totalVentasCtaCte = 0;

        for (Venta venta : ventasDelPeriodo) {
            //Llenar la tabla
            Object[] fila = new Object[6];
            fila[COLUMNA_FECHA] = FormateadorEstandar.formatearFecha(venta.getFecha());
            fila[COLUMNA_TIPOCPTE] = venta.getTipoFactura();
            fila[COLUMNA_NROCPTE] = venta.getFactura();
            if (venta.getCliente() != null) {
                fila[COLUMNA_CLIENTE] = venta.getCliente().getIdentidad();
            } else {
                fila[COLUMNA_CLIENTE] = "Consumidor Final";
            }
            fila[COLUMNA_IMPORTE] = FormateadorEstandar.formatearDinero(venta.getImporteTotal());
            fila[COLUMNA_FORMACOBRO] = venta.getFormaCobro().getNombre();
            modeloTabla.addRow(fila);

            Long oidContado = ParamSistema.getValorParametroLong(ConfiguracionGeneral.oidCobroContado);
            Long oidTarjeta = ParamSistema.getValorParametroLong(ConfiguracionGeneral.oidCobroTarjeta);
            Long oidCuentaCorriente = ParamSistema.getValorParametroLong(ConfiguracionGeneral.oidCobroCuentaCorriente);

            //Acumula
            totalVentas += venta.getImporteTotal();
            if (venta.getFormaCobro().getOid() == oidContado) {
                totalVentasContado += venta.getImporteTotal();
            }
            if (venta.getFormaCobro().getOid() == oidTarjeta) {
                totalVentasTarjeta += venta.getImporteTotal();
            }
            if (venta.getFormaCobro().getOid() == oidCuentaCorriente) {
                totalVentasCtaCte += venta.getImporteTotal();
            }
        }

        //Mostrar Totales
        txtTotalVentas.setText(
                FormateadorEstandar.formatearDinero(totalVentas));
        txtTotalVentasContado.setText(
                FormateadorEstandar.formatearDinero(totalVentasContado));
        txtTotalVentasTarjeta.setText(
                FormateadorEstandar.formatearDinero(totalVentasTarjeta));
        txtTotalVentasCtaCte.setText(
                FormateadorEstandar.formatearDinero(totalVentasCtaCte));

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlOpciones = new javax.swing.JPanel();
        pnlFechas = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        dateFechaHasta = new com.toedter.calendar.JDateChooser();
        dateFechaDesde = new com.toedter.calendar.JDateChooser();
        btnGenerarInforme = new javax.swing.JButton();
        pnlVentas = new javax.swing.JPanel();
        lblProductosVendidos = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblVentas = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        txtTotalVentas = new javax.swing.JTextField();
        txtTotalVentasContado = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtTotalVentasTarjeta = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtTotalVentasCtaCte = new javax.swing.JTextField();
        btnCancelar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaGenerarInformeVentas.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setMinimumSize(new java.awt.Dimension(750, 500));
        setName("Form"); // NOI18N

        pnlOpciones.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder()));
        pnlOpciones.setName("pnlOpciones"); // NOI18N

        pnlFechas.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("pnlFechas.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlFechas.setName("pnlFechas"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

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
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
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
                    .addComponent(jLabel1)
                    .addComponent(dateFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFechasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(dateFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnGenerarInforme.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        btnGenerarInforme.setIcon(resourceMap.getIcon("btnGenerarInforme.icon")); // NOI18N
        btnGenerarInforme.setMnemonic('g');
        btnGenerarInforme.setText(resourceMap.getString("btnGenerarInforme.text")); // NOI18N
        btnGenerarInforme.setName("btnGenerarInforme"); // NOI18N
        btnGenerarInforme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarInformeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlOpcionesLayout = new javax.swing.GroupLayout(pnlOpciones);
        pnlOpciones.setLayout(pnlOpcionesLayout);
        pnlOpcionesLayout.setHorizontalGroup(
            pnlOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOpcionesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlFechas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 297, Short.MAX_VALUE)
                .addComponent(btnGenerarInforme)
                .addContainerGap())
        );
        pnlOpcionesLayout.setVerticalGroup(
            pnlOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOpcionesLayout.createSequentialGroup()
                .addGroup(pnlOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnGenerarInforme)
                    .addComponent(pnlFechas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlVentas.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlVentas.setName("pnlVentas"); // NOI18N

        lblProductosVendidos.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblProductosVendidos.setText(resourceMap.getString("lblProductosVendidos.text")); // NOI18N
        lblProductosVendidos.setName("lblProductosVendidos"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tblVentas.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        tblVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Fecha", "Tipo Cpte", "Nº Cpte", "Cliente", "Importe", "F. Cobro"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblVentas.setName("tblVentas"); // NOI18N
        jScrollPane1.setViewportView(tblVentas);
        tblVentas.getColumnModel().getColumn(0).setMinWidth(80);
        tblVentas.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblVentas.getColumnModel().getColumn(0).setMaxWidth(120);
        tblVentas.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tblVentas.columnModel.title0")); // NOI18N
        tblVentas.getColumnModel().getColumn(1).setMinWidth(80);
        tblVentas.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblVentas.getColumnModel().getColumn(1).setMaxWidth(80);
        tblVentas.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tblVentas.columnModel.title1")); // NOI18N
        tblVentas.getColumnModel().getColumn(2).setMinWidth(80);
        tblVentas.getColumnModel().getColumn(2).setPreferredWidth(80);
        tblVentas.getColumnModel().getColumn(2).setMaxWidth(100);
        tblVentas.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tblVentas.columnModel.title2")); // NOI18N
        tblVentas.getColumnModel().getColumn(3).setMinWidth(120);
        tblVentas.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblVentas.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("tblVentas.columnModel.title3")); // NOI18N
        tblVentas.getColumnModel().getColumn(4).setMinWidth(100);
        tblVentas.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblVentas.getColumnModel().getColumn(4).setMaxWidth(140);
        tblVentas.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("tblVentas.columnModel.title4")); // NOI18N
        tblVentas.getColumnModel().getColumn(5).setMinWidth(100);
        tblVentas.getColumnModel().getColumn(5).setPreferredWidth(100);
        tblVentas.getColumnModel().getColumn(5).setMaxWidth(140);
        tblVentas.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("tblVentas.columnModel.title5")); // NOI18N

        jLabel4.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        txtTotalVentas.setEditable(false);
        txtTotalVentas.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtTotalVentas.setName("txtTotalVentas"); // NOI18N

        txtTotalVentasContado.setEditable(false);
        txtTotalVentasContado.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtTotalVentasContado.setText(resourceMap.getString("txtTotalVentasContado.text")); // NOI18N
        txtTotalVentasContado.setName("txtTotalVentasContado"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        txtTotalVentasTarjeta.setEditable(false);
        txtTotalVentasTarjeta.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtTotalVentasTarjeta.setText(resourceMap.getString("txtTotalVentasTarjeta.text")); // NOI18N
        txtTotalVentasTarjeta.setName("txtTotalVentasTarjeta"); // NOI18N

        jLabel6.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        txtTotalVentasCtaCte.setEditable(false);
        txtTotalVentasCtaCte.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        txtTotalVentasCtaCte.setName("txtTotalVentasCtaCte"); // NOI18N

        javax.swing.GroupLayout pnlVentasLayout = new javax.swing.GroupLayout(pnlVentas);
        pnlVentas.setLayout(pnlVentasLayout);
        pnlVentasLayout.setHorizontalGroup(
            pnlVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlVentasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
                    .addComponent(lblProductosVendidos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlVentasLayout.createSequentialGroup()
                        .addGroup(pnlVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtTotalVentasContado)
                            .addComponent(txtTotalVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlVentasLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtTotalVentasTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlVentasLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                                .addComponent(txtTotalVentasCtaCte, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        pnlVentasLayout.setVerticalGroup(
            pnlVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVentasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblProductosVendidos)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(txtTotalVentas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalVentasTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtTotalVentasContado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtTotalVentasCtaCte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlVentas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlOpciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnCerrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 503, Short.MAX_VALUE)
                        .addComponent(btnCancelar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlOpciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlVentas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnCerrar))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void btnGenerarInformeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarInformeActionPerformed
    opcionGenerarInforme();
}//GEN-LAST:event_btnGenerarInformeActionPerformed

private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
    inicializarVentana();
}//GEN-LAST:event_btnCancelarActionPerformed

private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
    dispose();
}//GEN-LAST:event_btnCerrarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnGenerarInforme;
    private com.toedter.calendar.JDateChooser dateFechaDesde;
    private com.toedter.calendar.JDateChooser dateFechaHasta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblProductosVendidos;
    private javax.swing.JPanel pnlFechas;
    private javax.swing.JPanel pnlOpciones;
    private javax.swing.JPanel pnlVentas;
    private javax.swing.JTable tblVentas;
    private javax.swing.JTextField txtTotalVentas;
    private javax.swing.JTextField txtTotalVentasContado;
    private javax.swing.JTextField txtTotalVentasCtaCte;
    private javax.swing.JTextField txtTotalVentasTarjeta;
    // End of variables declaration//GEN-END:variables
}
