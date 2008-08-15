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

import gecom.app.table.ProductoVendidoTableRender;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import javax.naming.NamingException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.formateadores.FormateadorEstandar;
import simbya.framework.interfaces.VentanaInterna;
import simbya.gecom.decoradores.ProductoVendido;
import simbya.gecom.gestores.productos.GestorGenerarInformeProductosVendidosRemote;

/**
 * Ventana con informe de productos vendidos.
 * @author Marcelo Busico.
 */
public class VentanaGenerarInformeProductosVendidos extends VentanaInterna {

    private static final Logger log = Logger.getLogger(
            VentanaGenerarInformeProductosVendidos.class);
    private GestorGenerarInformeProductosVendidosRemote gestor;
    private DefaultTableModel modeloTabla;
    private static final int COLUMNA_CODIGO = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_CANTIDAD = 2;
    private static final int COLUMNA_UNIDADES = 3;
    private static final int COLUMNA_IMPORTE = 4;
    private static final int COLUMNA_SUBTOTAL = 5;

    /** Creates new form VentanaReporteProductosVendidos */
    public VentanaGenerarInformeProductosVendidos() {
        initComponents();
        modeloTabla = (DefaultTableModel) tblProductosVendidos.getModel();
        tblProductosVendidos.setDefaultRenderer(ProductoVendido.class,
                new ProductoVendidoTableRender(tblProductosVendidos.getSelectionBackground()));
    }

    /**
     * Asocia la ventana con su gestor correspondiente.
     */
    public void enlazarGestorRemoto(GestorConexion gc) throws NamingException {
        gestor = (GestorGenerarInformeProductosVendidosRemote) gc.getObjetoRemoto(
                GestorGenerarInformeProductosVendidosRemote.class);
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
        Set<ProductoVendido> ventasDelPeriodo = gestor.getProductosDelPeriodo(fechaDesde, fechaHasta);

        modeloTabla.setRowCount(0);

        if (ventasDelPeriodo.size() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No se encontraron ventas en el periodo seleccionado.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Mostrar el titulo del informe.
        lblProductosVendidos.setText("Productos vendidos desde el " +
                FormateadorEstandar.formatearFecha(fechaDesde) +
                " hasta el " + FormateadorEstandar.formatearFecha(fechaHasta));

        float totalVentas = 0;
        for (ProductoVendido pv : ventasDelPeriodo) {
            //Llenar la tabla
            Object[] fila = new Object[6];

            fila[COLUMNA_CODIGO] = pv.getProducto().getCodigo();
            fila[COLUMNA_DESCRIPCION] = pv;
            fila[COLUMNA_CANTIDAD] = pv.getCantidadVendida();
            fila[COLUMNA_UNIDADES] = pv.getProducto().getProductoUM().getNombre();
            fila[COLUMNA_IMPORTE] = pv.getImportePromedioVenta();
            fila[COLUMNA_SUBTOTAL] = pv.getSubtotalVenta();

            modeloTabla.addRow(fila);

            //Acumula
            totalVentas += pv.getSubtotalVenta();
        }

        //Mostrar Totales
        txtTotalVentas.setText(
                FormateadorEstandar.formatearDinero(totalVentas));

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
        btnGenerarInforme = new javax.swing.JButton();
        pnlFechas = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        dateFechaHasta = new com.toedter.calendar.JDateChooser();
        dateFechaDesde = new com.toedter.calendar.JDateChooser();
        pnlProductosVendidos = new javax.swing.JPanel();
        lblProductosVendidos = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductosVendidos = new javax.swing.JTable();
        txtTotalVentas = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        pnlReferencias = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        btnCancelar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaGenerarInformeProductosVendidos.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setFrameIcon(resourceMap.getIcon("Form.frameIcon")); // NOI18N
        setMinimumSize(new java.awt.Dimension(700, 500));
        setName("Form"); // NOI18N

        pnlOpciones.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder()));
        pnlOpciones.setName("pnlOpciones"); // NOI18N

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

        javax.swing.GroupLayout pnlOpcionesLayout = new javax.swing.GroupLayout(pnlOpciones);
        pnlOpciones.setLayout(pnlOpcionesLayout);
        pnlOpcionesLayout.setHorizontalGroup(
            pnlOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOpcionesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlFechas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 125, Short.MAX_VALUE)
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

        pnlProductosVendidos.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlProductosVendidos.setName("pnlProductosVendidos"); // NOI18N

        lblProductosVendidos.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        lblProductosVendidos.setText(resourceMap.getString("lblProductosVendidos.text")); // NOI18N
        lblProductosVendidos.setName("lblProductosVendidos"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tblProductosVendidos.setAutoCreateRowSorter(true);
        tblProductosVendidos.setFont(resourceMap.getFont("Application.font.text")); // NOI18N
        tblProductosVendidos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Código", "Descripción", "Cantidad", "U. Medida", "$ Promedio", "Subtotal"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, ProductoVendido.class, java.lang.Float.class, java.lang.Object.class, java.lang.Float.class, java.lang.Float.class
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
        tblProductosVendidos.setName("tblProductosVendidos"); // NOI18N
        jScrollPane1.setViewportView(tblProductosVendidos);
        tblProductosVendidos.getColumnModel().getColumn(0).setMinWidth(60);
        tblProductosVendidos.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblProductosVendidos.getColumnModel().getColumn(0).setMaxWidth(100);
        tblProductosVendidos.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tblProductosVendidos.columnModel.title0")); // NOI18N
        tblProductosVendidos.getColumnModel().getColumn(1).setMinWidth(250);
        tblProductosVendidos.getColumnModel().getColumn(1).setPreferredWidth(250);
        tblProductosVendidos.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tblProductosVendidos.columnModel.title1")); // NOI18N
        tblProductosVendidos.getColumnModel().getColumn(2).setMinWidth(60);
        tblProductosVendidos.getColumnModel().getColumn(2).setPreferredWidth(60);
        tblProductosVendidos.getColumnModel().getColumn(2).setMaxWidth(100);
        tblProductosVendidos.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tblProductosVendidos.columnModel.title2")); // NOI18N
        tblProductosVendidos.getColumnModel().getColumn(3).setMinWidth(80);
        tblProductosVendidos.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblProductosVendidos.getColumnModel().getColumn(3).setMaxWidth(120);
        tblProductosVendidos.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("tblProductosVendidos.columnModel.title5")); // NOI18N
        tblProductosVendidos.getColumnModel().getColumn(4).setMinWidth(80);
        tblProductosVendidos.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblProductosVendidos.getColumnModel().getColumn(4).setMaxWidth(120);
        tblProductosVendidos.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("tblProductosVendidos.columnModel.title3")); // NOI18N
        tblProductosVendidos.getColumnModel().getColumn(5).setMinWidth(100);
        tblProductosVendidos.getColumnModel().getColumn(5).setPreferredWidth(100);
        tblProductosVendidos.getColumnModel().getColumn(5).setMaxWidth(120);
        tblProductosVendidos.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("tblProductosVendidos.columnModel.title4")); // NOI18N

        txtTotalVentas.setEditable(false);
        txtTotalVentas.setFont(resourceMap.getFont("Application.font.big")); // NOI18N
        txtTotalVentas.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtTotalVentas.setText(resourceMap.getString("txtTotalVentas.text")); // NOI18N
        txtTotalVentas.setName("txtTotalVentas"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("Application.font.big")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        javax.swing.GroupLayout pnlProductosVendidosLayout = new javax.swing.GroupLayout(pnlProductosVendidos);
        pnlProductosVendidos.setLayout(pnlProductosVendidosLayout);
        pnlProductosVendidosLayout.setHorizontalGroup(
            pnlProductosVendidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductosVendidosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProductosVendidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
                    .addComponent(lblProductosVendidos, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProductosVendidosLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTotalVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlProductosVendidosLayout.setVerticalGroup(
            pnlProductosVendidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductosVendidosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblProductosVendidos)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProductosVendidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtTotalVentas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pnlReferencias.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("pnlReferencias.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("Application.font.label"))); // NOI18N
        pnlReferencias.setName("pnlReferencias"); // NOI18N

        jLabel5.setBackground(resourceMap.getColor("jLabel5.background")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(resourceMap.getString("toolTipText.claseA")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        jLabel5.setOpaque(true);

        jLabel6.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(resourceMap.getString("toolTipText.claseA")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setToolTipText(resourceMap.getString("toolTipText.claseB")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setBackground(resourceMap.getColor("jLabel8.background")); // NOI18N
        jLabel8.setToolTipText(resourceMap.getString("toolTipText.claseB")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        jLabel8.setOpaque(true);

        jLabel9.setBackground(resourceMap.getColor("jLabel9.background")); // NOI18N
        jLabel9.setToolTipText(resourceMap.getString("toolTipText.claseC")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        jLabel9.setOpaque(true);

        jLabel10.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setToolTipText(resourceMap.getString("toolTipText.claseC")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        javax.swing.GroupLayout pnlReferenciasLayout = new javax.swing.GroupLayout(pnlReferencias);
        pnlReferencias.setLayout(pnlReferenciasLayout);
        pnlReferenciasLayout.setHorizontalGroup(
            pnlReferenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlReferenciasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlReferenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlReferenciasLayout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6))
                    .addGroup(pnlReferenciasLayout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7))
                    .addGroup(pnlReferenciasLayout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlReferenciasLayout.setVerticalGroup(
            pnlReferenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlReferenciasLayout.createSequentialGroup()
                .addGroup(pnlReferenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlReferenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlReferenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(pnlProductosVendidos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnCerrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 453, Short.MAX_VALUE)
                        .addComponent(btnCancelar))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(pnlOpciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlReferencias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlReferencias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlOpciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProductosVendidos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnCerrar))
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

private void btnGenerarInformeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarInformeActionPerformed
    opcionGenerarInforme();
}//GEN-LAST:event_btnGenerarInformeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnGenerarInforme;
    private com.toedter.calendar.JDateChooser dateFechaDesde;
    private com.toedter.calendar.JDateChooser dateFechaHasta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblProductosVendidos;
    private javax.swing.JPanel pnlFechas;
    private javax.swing.JPanel pnlOpciones;
    private javax.swing.JPanel pnlProductosVendidos;
    private javax.swing.JPanel pnlReferencias;
    private javax.swing.JTable tblProductosVendidos;
    private javax.swing.JTextField txtTotalVentas;
    // End of variables declaration//GEN-END:variables
}
