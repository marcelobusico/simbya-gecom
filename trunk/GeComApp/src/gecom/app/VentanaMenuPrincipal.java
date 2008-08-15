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
package gecom.app;

import gecom.app.clientes.VentanaActualizarCliente;
import gecom.app.compras.VentanaRegistrarCompra;
import gecom.app.configuracion.VentanaActualizarParametrosGenerales;
import gecom.app.configuracion.VentanaActualizarProvinciasYLocalidades;
import gecom.app.productos.VentanaActualizarPreciosProductos;
import gecom.app.productos.VentanaActualizarProducto;
import gecom.app.productos.VentanaActualizarStockProductos;
import gecom.app.productos.VentanaGenerarInformeProductosVendidos;
import gecom.app.proveedores.VentanaActualizarProveedor;
import gecom.app.proveedores.VentanaConsultarCuentaCorrienteProveedor;
import gecom.app.tipos.VentanaActualizarTipos;
import gecom.app.usuarios.VentanaActualizarTiposUsuarioSistema;
import gecom.app.usuarios.VentanaActualizarUsuarioSistema;
import gecom.app.ventas.VentanaGenerarInformeVentas;
import gecom.app.ventas.VentanaRegistrarVenta;
import javax.naming.NamingException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import simbya.framework.appserver.GestorConexion;
import simbya.framework.decoradores.Fecha;
import simbya.gecom.entidades.TipoCalidad;
import simbya.gecom.entidades.TipoCilindradaMoto;
import simbya.gecom.entidades.TipoFormaCobro;
import simbya.gecom.entidades.TipoFormaPago;
import simbya.gecom.entidades.TipoMarca;
import simbya.gecom.entidades.TipoModelo;
import simbya.gecom.entidades.TipoRubro;
import simbya.gecom.entidades.TipoUnidadMedida;
import simbya.gecom.entidades.seguridad.CasoDeUso;
import simbya.gecom.entidades.seguridad.UsuarioSistema;
import simbya.gecom.gestores.seguridad.GestorCasosUsoRemote;

/**
 * La ventana principal de la aplicación.
 * @author Marcelo Busico.
 */
public class VentanaMenuPrincipal extends FrameView {

    private static final Logger log = Logger.getLogger(VentanaMenuPrincipal.class);
    private static VentanaMenuPrincipal instancia = null;
    private GestorMenuPrincipal gestor;
    private Timer reloj;
    private String mensajeUsuario = "";

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    private VentanaMenuPrincipal(SingleFrameApplication app) {
        super(app);
        gestor = new GestorMenuPrincipal();
        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                lblEstadoMensaje.setText(mensajeUsuario);
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                lblEstadoAnimacion.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        lblEstadoAnimacion.setIcon(idleIcon);
        barraProgreso.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        lblEstadoAnimacion.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    barraProgreso.setVisible(true);
                    barraProgreso.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    lblEstadoAnimacion.setIcon(idleIcon);
                    barraProgreso.setVisible(false);
                    barraProgreso.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    lblEstadoMensaje.setText((text == null) ? mensajeUsuario : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    barraProgreso.setVisible(true);
                    barraProgreso.setIndeterminate(false);
                    barraProgreso.setValue(value);
                }
            }
        });

        //Inicia el reloj del sistema.
        reloj = new Timer(1000, new TemporizadorReloj());
        reloj.start();
    }
    // </editor-fold>
    /**
     * Devuelve la única instancia del menú principal.
     * @return Referencia a la ventana del menú principal.
     */
    public static VentanaMenuPrincipal getInstancia() {
        if (instancia == null) {
            instancia = new VentanaMenuPrincipal(Main.getApplication());
        }
        return instancia;
    }

    public GestorMenuPrincipal getGestor() {
        return gestor;
    }

    /**
     * Devuelve el panel principal del menú para agregar las ventanas allí.
     * @return Referencia al panel principal.
     */
    public JDesktopPane getPanelPrincipal() {
        return this.panelPrincipal;
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = Main.getApplication().getMainFrame();
            aboutBox = new VentanaAcercaDe(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Main.getApplication().show(aboutBox);
    }

    @Action
    public void accionRegistrarVenta() {
        gestor.iniciarCU(VentanaRegistrarVenta.class);
    }

    @Action
    public void accionActualizarTipoUsuarioSistema() {
        gestor.iniciarCU(VentanaActualizarTiposUsuarioSistema.class);
    }

    private void funcionIncompleta() {
        JOptionPane.showMessageDialog(this.getFrame(),
                "Disculpe, esta función no se encuentra disponible por el momento.",
                "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Establece los permisos del usuario en los distintos item de menú,
     * desactivando aquellos a los que no se debe tener acceso.
     * @param usuario Usuario del sistema que inició sesión.
     */
    public void cargarPrivilegiosDelUsuario(UsuarioSistema usuario) {
        gestor.setUsuarioSistema(usuario);

        //Carga los CU del sistema.
        try {
            GestorCasosUsoRemote gestorCU =
                    (GestorCasosUsoRemote) GestorConexion.getInstancia().
                    getObjetoRemoto(GestorCasosUsoRemote.class);
            CasoDeUso.setCasosDeUso(gestorCU.cargarCasosDeUso());
        } catch (NamingException ex) {
            log.warn("No se han cargado los privilegios porque no se" +
                    " pudo conectar con el gestor de casos de uso.");
            return;
        }

        if (usuario == null) {
            log.warn("No se han cargado los privilegios porque no hay " +
                    "usuario seleccionado en el Menú Principal");
            return;
        }
        if (usuario.getOid() == 0) {
            log.warn("Se ha iniciado sesión con la cuenta del Administrador" +
                    " del Sistema.");
            return;
        }

        mensajeUsuario = "Sesión del Usuario: " + usuario.getNombreUsuario();

//------------------------------------------
// INICIO DE DECLARACION DE ITEMS DE MENU.        
//------------------------------------------

        //Menú Archivo -> Configuración:
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Archivo_Configuracion_ActualizarParametrosGenerales,
                itmActualizarParametrosGenerales);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Archivo_Configuracion_ActualizarProvinciasYLocalidades,
                itmActualizarProvinciasYLocalidades);

        //Menú Archivo -> Usuarios:
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Archivo_Usuarios_ActualizarTipoUsuarioSistema,
                itmActualizarTipoUsuario);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Archivo_Usuarios_ActualizarUsuarioSistema,
                itmActualizarUsuario);

        //Clientes:
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Clientes_ActualizarFormaCobro,
                itmActualizarFormaCobro);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Clientes_ActualizarCliente,
                itmActualizarCliente);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Clientes_RegistrarCobroCliente,
                itmRegistrarCobroCliente);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Clientes_ConsultarCuentaCorrienteCliente,
                itmConsultarCuentaCorrienteCliente);

        //Proveedores:
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Proveedores_ActualizarFormaPago,
                itmActualizarFormaPago);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Proveedores_ActualizarProveedor,
                itmActualizarProveedor);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Proveedores_RegistrarPagoProveedor,
                itmRegistrarPagoProveedor);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Proveedores_ConsultarCuentaCorrienteProveedor,
                itmConsultarCuentaCorrienteProveedor);

        //Productos -> Tablas y Tipos:
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Productos_TablasYTipos_ActualizarCalidad,
                itmActualizarCalidad);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Productos_TablasYTipos_ActualizarCilindradaMoto,
                itmActualizarCilindradaMoto);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Productos_TablasYTipos_ActualizarMarca,
                itmActualizarMarca);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Productos_TablasYTipos_ActualizarModelo,
                itmActualizarModelo);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Productos_TablasYTipos_ActualizarRubro,
                itmActualizarRubro);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Productos_TablasYTipos_ActualizarUnidadMedida,
                itmActualizarUnidadMedida);

        //Productos:
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Productos_ActualizarProducto,
                itmActualizarProducto);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Productos_ActualizarPreciosProductos,
                itmActualizarPreciosProductos);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Productos_ActualizarStockProductos,
                itmActualizarStockProductos);
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Productos_GenerarInformeProductosVendidos,
                itmGenerarInformeProductosVendidos);

        //Compras:
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Compras_RegistrarCompra,
                itmRegistrarCompra);

        //Ventas:
        CasoDeUso.validarMenu(usuario,
                CasoDeUso.Ventas_RegistrarVenta,
                itmRegistrarVenta);


        CasoDeUso.validarMenu(
                usuario,
                CasoDeUso.Ventas_GenerarInformeVentas,
                itmGenerarInformeVentas);

//------------------------------------------
// FIN DE DECLARACION DE ITEMS DE MENU.        
//------------------------------------------

        //Botones de acceso rápido

        CasoDeUso.validarMenu(usuario, CasoDeUso.Ventas_RegistrarVenta,
                btnRegistrarVenta);

        CasoDeUso.validarMenu(usuario, CasoDeUso.Compras_RegistrarCompra,
                btnRegistrarCompra);

        CasoDeUso.validarMenu(usuario, CasoDeUso.Productos_ActualizarProducto,
                btnActualizarProducto);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barraMenu = new javax.swing.JMenuBar();
        javax.swing.JMenu mnuArchivo = new javax.swing.JMenu();
        mnuConfiguracion = new javax.swing.JMenu();
        itmActualizarParametrosGenerales = new javax.swing.JMenuItem();
        itmActualizarProvinciasYLocalidades = new javax.swing.JMenuItem();
        mnuUsuarios = new javax.swing.JMenu();
        itmActualizarTipoUsuario = new javax.swing.JMenuItem();
        itmActualizarUsuario = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem itmSalir = new javax.swing.JMenuItem();
        mnuVer = new javax.swing.JMenu();
        itmVerBarraAccesosRapido = new javax.swing.JCheckBoxMenuItem();
        mnuClientes = new javax.swing.JMenu();
        itmActualizarFormaCobro = new javax.swing.JMenuItem();
        itmActualizarCliente = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        itmRegistrarCobroCliente = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        itmConsultarCuentaCorrienteCliente = new javax.swing.JMenuItem();
        mnuProveedores = new javax.swing.JMenu();
        itmActualizarFormaPago = new javax.swing.JMenuItem();
        itmActualizarProveedor = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        itmRegistrarPagoProveedor = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        itmConsultarCuentaCorrienteProveedor = new javax.swing.JMenuItem();
        mnuProductos = new javax.swing.JMenu();
        mnuProductosTablasYTipos = new javax.swing.JMenu();
        itmActualizarCalidad = new javax.swing.JMenuItem();
        itmActualizarCilindradaMoto = new javax.swing.JMenuItem();
        itmActualizarMarca = new javax.swing.JMenuItem();
        itmActualizarModelo = new javax.swing.JMenuItem();
        itmActualizarRubro = new javax.swing.JMenuItem();
        itmActualizarUnidadMedida = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        itmActualizarProducto = new javax.swing.JMenuItem();
        itmActualizarPreciosProductos = new javax.swing.JMenuItem();
        itmActualizarStockProductos = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        itmGenerarInformeProductosVendidos = new javax.swing.JMenuItem();
        mnuCompras = new javax.swing.JMenu();
        itmRegistrarCompra = new javax.swing.JMenuItem();
        mnuVentas = new javax.swing.JMenu();
        itmRegistrarVenta = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        itmGenerarInformeVentas = new javax.swing.JMenuItem();
        javax.swing.JMenu mnuAyuda = new javax.swing.JMenu();
        javax.swing.JMenuItem itmAcercaDe = new javax.swing.JMenuItem();
        panelEstado = new javax.swing.JPanel();
        javax.swing.JSeparator sepPanelEstado = new javax.swing.JSeparator();
        lblEstadoMensaje = new javax.swing.JLabel();
        lblEstadoAnimacion = new javax.swing.JLabel();
        barraProgreso = new javax.swing.JProgressBar();
        barraAccesosRapido = new javax.swing.JToolBar();
        btnRegistrarVenta = new javax.swing.JButton();
        btnRegistrarCompra = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        btnActualizarProducto = new javax.swing.JButton();
        panelSeparador = new javax.swing.JPanel();
        lblFechaHora = new javax.swing.JLabel();
        panelDesplazamiento = new javax.swing.JScrollPane();
        panelPrincipal = new javax.swing.JDesktopPane();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getResourceMap(VentanaMenuPrincipal.class);
        barraMenu.setFont(resourceMap.getFont("barraMenu.font")); // NOI18N
        barraMenu.setName("barraMenu"); // NOI18N

        mnuArchivo.setMnemonic('a');
        mnuArchivo.setText(resourceMap.getString("mnuArchivo.text")); // NOI18N
        mnuArchivo.setFont(resourceMap.getFont("barraMenu.font")); // NOI18N
        mnuArchivo.setName("mnuArchivo"); // NOI18N

        mnuConfiguracion.setIcon(resourceMap.getIcon("mnuConfiguracion.icon")); // NOI18N
        mnuConfiguracion.setMnemonic('f');
        mnuConfiguracion.setText(resourceMap.getString("mnuConfiguracion.text")); // NOI18N
        mnuConfiguracion.setFont(resourceMap.getFont("mnuConfiguracion.font")); // NOI18N
        mnuConfiguracion.setName("mnuConfiguracion"); // NOI18N

        itmActualizarParametrosGenerales.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmActualizarParametrosGenerales.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/wrench_orange.png"))); // NOI18N
        itmActualizarParametrosGenerales.setText(resourceMap.getString("itmActualizarParametrosGenerales.text")); // NOI18N
        itmActualizarParametrosGenerales.setName("itmActualizarParametrosGenerales"); // NOI18N
        itmActualizarParametrosGenerales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarParametrosGeneralesActionPerformed(evt);
            }
        });
        mnuConfiguracion.add(itmActualizarParametrosGenerales);

        itmActualizarProvinciasYLocalidades.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmActualizarProvinciasYLocalidades.setIcon(resourceMap.getIcon("itmActualizarProvinciasYLocalidades.icon")); // NOI18N
        itmActualizarProvinciasYLocalidades.setText(resourceMap.getString("itmActualizarProvinciasYLocalidades.text")); // NOI18N
        itmActualizarProvinciasYLocalidades.setName("itmActualizarProvinciasYLocalidades"); // NOI18N
        itmActualizarProvinciasYLocalidades.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarProvinciasYLocalidadesActionPerformed(evt);
            }
        });
        mnuConfiguracion.add(itmActualizarProvinciasYLocalidades);

        mnuArchivo.add(mnuConfiguracion);

        mnuUsuarios.setIcon(resourceMap.getIcon("mnuUsuarios.icon")); // NOI18N
        mnuUsuarios.setMnemonic('u');
        mnuUsuarios.setText(resourceMap.getString("mnuUsuarios.text")); // NOI18N
        mnuUsuarios.setFont(resourceMap.getFont("mnuConfiguracion.font")); // NOI18N
        mnuUsuarios.setName("mnuUsuarios"); // NOI18N

        itmActualizarTipoUsuario.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmActualizarTipoUsuario.setIcon(resourceMap.getIcon("itmActualizarTipoUsuario.icon")); // NOI18N
        itmActualizarTipoUsuario.setMnemonic('t');
        itmActualizarTipoUsuario.setText(resourceMap.getString("itmActualizarTipoUsuario.text")); // NOI18N
        itmActualizarTipoUsuario.setName("itmActualizarTipoUsuario"); // NOI18N
        itmActualizarTipoUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarTipoUsuarioActionPerformed(evt);
            }
        });
        mnuUsuarios.add(itmActualizarTipoUsuario);

        itmActualizarUsuario.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmActualizarUsuario.setIcon(resourceMap.getIcon("itmActualizarUsuario.icon")); // NOI18N
        itmActualizarUsuario.setMnemonic('u');
        itmActualizarUsuario.setText(resourceMap.getString("itmActualizarUsuario.text")); // NOI18N
        itmActualizarUsuario.setName("itmActualizarUsuario"); // NOI18N
        itmActualizarUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarUsuarioActionPerformed(evt);
            }
        });
        mnuUsuarios.add(itmActualizarUsuario);

        mnuArchivo.add(mnuUsuarios);

        jSeparator1.setName("jSeparator1"); // NOI18N
        mnuArchivo.add(jSeparator1);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(gecom.app.Main.class).getContext().getActionMap(VentanaMenuPrincipal.class, this);
        itmSalir.setAction(actionMap.get("quit")); // NOI18N
        itmSalir.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmSalir.setName("itmSalir"); // NOI18N
        mnuArchivo.add(itmSalir);

        barraMenu.add(mnuArchivo);

        mnuVer.setMnemonic('i');
        mnuVer.setText(resourceMap.getString("mnuVer.text")); // NOI18N
        mnuVer.setFont(resourceMap.getFont("barraMenu.font")); // NOI18N
        mnuVer.setName("mnuVer"); // NOI18N

        itmVerBarraAccesosRapido.setAction(actionMap.get("verBarraAccesosRapido")); // NOI18N
        itmVerBarraAccesosRapido.setFont(resourceMap.getFont("itmVerBarraAccesosRapido.font")); // NOI18N
        itmVerBarraAccesosRapido.setSelected(true);
        itmVerBarraAccesosRapido.setName("itmVerBarraAccesosRapido"); // NOI18N
        mnuVer.add(itmVerBarraAccesosRapido);

        barraMenu.add(mnuVer);

        mnuClientes.setMnemonic('l');
        mnuClientes.setText(resourceMap.getString("mnuClientes.text")); // NOI18N
        mnuClientes.setFont(resourceMap.getFont("barraMenu.font")); // NOI18N
        mnuClientes.setName("mnuClientes"); // NOI18N

        itmActualizarFormaCobro.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmActualizarFormaCobro.setIcon(resourceMap.getIcon("itmActualizarFormaCobro.icon")); // NOI18N
        itmActualizarFormaCobro.setMnemonic('f');
        itmActualizarFormaCobro.setText(resourceMap.getString("itmActualizarFormaCobro.text")); // NOI18N
        itmActualizarFormaCobro.setName("itmActualizarFormaCobro"); // NOI18N
        itmActualizarFormaCobro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarFormaCobroActionPerformed(evt);
            }
        });
        mnuClientes.add(itmActualizarFormaCobro);

        itmActualizarCliente.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmActualizarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/user-plain-yellow.png"))); // NOI18N
        itmActualizarCliente.setMnemonic('t');
        itmActualizarCliente.setText(resourceMap.getString("itmActualizarCliente.text")); // NOI18N
        itmActualizarCliente.setName("itmActualizarCliente"); // NOI18N
        itmActualizarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarClienteActionPerformed(evt);
            }
        });
        mnuClientes.add(itmActualizarCliente);

        jSeparator5.setName("jSeparator5"); // NOI18N
        mnuClientes.add(jSeparator5);

        itmRegistrarCobroCliente.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmRegistrarCobroCliente.setIcon(resourceMap.getIcon("itmRegistrarCobroCliente.icon")); // NOI18N
        itmRegistrarCobroCliente.setMnemonic('o');
        itmRegistrarCobroCliente.setText(resourceMap.getString("itmRegistrarCobroCliente.text")); // NOI18N
        itmRegistrarCobroCliente.setName("itmRegistrarCobroCliente"); // NOI18N
        itmRegistrarCobroCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmRegistrarCobroClienteActionPerformed(evt);
            }
        });
        mnuClientes.add(itmRegistrarCobroCliente);

        jSeparator7.setName("jSeparator7"); // NOI18N
        mnuClientes.add(jSeparator7);

        itmConsultarCuentaCorrienteCliente.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmConsultarCuentaCorrienteCliente.setIcon(resourceMap.getIcon("itmConsultarCuentaCorrienteCliente.icon")); // NOI18N
        itmConsultarCuentaCorrienteCliente.setMnemonic('u');
        itmConsultarCuentaCorrienteCliente.setText(resourceMap.getString("itmConsultarCuentaCorrienteCliente.text")); // NOI18N
        itmConsultarCuentaCorrienteCliente.setName("itmConsultarCuentaCorrienteCliente"); // NOI18N
        itmConsultarCuentaCorrienteCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmConsultarCuentaCorrienteClienteActionPerformed(evt);
            }
        });
        mnuClientes.add(itmConsultarCuentaCorrienteCliente);

        barraMenu.add(mnuClientes);

        mnuProveedores.setMnemonic('r');
        mnuProveedores.setText(resourceMap.getString("mnuProveedores.text")); // NOI18N
        mnuProveedores.setFont(resourceMap.getFont("barraMenu.font")); // NOI18N
        mnuProveedores.setName("mnuProveedores"); // NOI18N

        itmActualizarFormaPago.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmActualizarFormaPago.setIcon(resourceMap.getIcon("itmActualizarFormaPago.icon")); // NOI18N
        itmActualizarFormaPago.setMnemonic('f');
        itmActualizarFormaPago.setText(resourceMap.getString("itmActualizarFormaPago.text")); // NOI18N
        itmActualizarFormaPago.setName("itmActualizarFormaPago"); // NOI18N
        itmActualizarFormaPago.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarFormaPagoActionPerformed(evt);
            }
        });
        mnuProveedores.add(itmActualizarFormaPago);

        itmActualizarProveedor.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmActualizarProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/user-plain-yellow.png"))); // NOI18N
        itmActualizarProveedor.setMnemonic('t');
        itmActualizarProveedor.setText(resourceMap.getString("itmActualizarProveedor.text")); // NOI18N
        itmActualizarProveedor.setName("itmActualizarProveedor"); // NOI18N
        itmActualizarProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarProveedorActionPerformed(evt);
            }
        });
        mnuProveedores.add(itmActualizarProveedor);

        jSeparator3.setName("jSeparator3"); // NOI18N
        mnuProveedores.add(jSeparator3);

        itmRegistrarPagoProveedor.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmRegistrarPagoProveedor.setIcon(resourceMap.getIcon("itmRegistrarPagoProveedor.icon")); // NOI18N
        itmRegistrarPagoProveedor.setMnemonic('o');
        itmRegistrarPagoProveedor.setText(resourceMap.getString("itmRegistrarPagoProveedor.text")); // NOI18N
        itmRegistrarPagoProveedor.setName("itmRegistrarPagoProveedor"); // NOI18N
        itmRegistrarPagoProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmRegistrarPagoProveedorActionPerformed(evt);
            }
        });
        mnuProveedores.add(itmRegistrarPagoProveedor);

        jSeparator8.setName("jSeparator8"); // NOI18N
        mnuProveedores.add(jSeparator8);

        itmConsultarCuentaCorrienteProveedor.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmConsultarCuentaCorrienteProveedor.setIcon(resourceMap.getIcon("itmConsultarCuentaCorrienteProveedor.icon")); // NOI18N
        itmConsultarCuentaCorrienteProveedor.setMnemonic('u');
        itmConsultarCuentaCorrienteProveedor.setText(resourceMap.getString("itmConsultarCuentaCorrienteProveedor.text")); // NOI18N
        itmConsultarCuentaCorrienteProveedor.setName("itmConsultarCuentaCorrienteProveedor"); // NOI18N
        itmConsultarCuentaCorrienteProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmConsultarCuentaCorrienteProveedorActionPerformed(evt);
            }
        });
        mnuProveedores.add(itmConsultarCuentaCorrienteProveedor);

        barraMenu.add(mnuProveedores);

        mnuProductos.setMnemonic('p');
        mnuProductos.setText(resourceMap.getString("mnuProductos.text")); // NOI18N
        mnuProductos.setFont(resourceMap.getFont("barraMenu.font")); // NOI18N
        mnuProductos.setName("mnuProductos"); // NOI18N

        mnuProductosTablasYTipos.setIcon(resourceMap.getIcon("mnuProductosTablasYTipos.icon")); // NOI18N
        mnuProductosTablasYTipos.setMnemonic('y');
        mnuProductosTablasYTipos.setText(resourceMap.getString("mnuProductosTablasYTipos.text")); // NOI18N
        mnuProductosTablasYTipos.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        mnuProductosTablasYTipos.setName("mnuProductosTablasYTipos"); // NOI18N

        itmActualizarCalidad.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmActualizarCalidad.setIcon(resourceMap.getIcon("itmActualizarCalidad.icon")); // NOI18N
        itmActualizarCalidad.setText(resourceMap.getString("itmActualizarCalidad.text")); // NOI18N
        itmActualizarCalidad.setName("itmActualizarCalidad"); // NOI18N
        itmActualizarCalidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarCalidadActionPerformed(evt);
            }
        });
        mnuProductosTablasYTipos.add(itmActualizarCalidad);

        itmActualizarCilindradaMoto.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmActualizarCilindradaMoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/database.png"))); // NOI18N
        itmActualizarCilindradaMoto.setText(resourceMap.getString("itmActualizarCilindradaMoto.text")); // NOI18N
        itmActualizarCilindradaMoto.setName("itmActualizarCilindradaMoto"); // NOI18N
        itmActualizarCilindradaMoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarCilindradaMotoActionPerformed(evt);
            }
        });
        mnuProductosTablasYTipos.add(itmActualizarCilindradaMoto);

        itmActualizarMarca.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmActualizarMarca.setIcon(resourceMap.getIcon("itmActualizarMarca.icon")); // NOI18N
        itmActualizarMarca.setText(resourceMap.getString("itmActualizarMarca.text")); // NOI18N
        itmActualizarMarca.setName("itmActualizarMarca"); // NOI18N
        itmActualizarMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarMarcaActionPerformed(evt);
            }
        });
        mnuProductosTablasYTipos.add(itmActualizarMarca);

        itmActualizarModelo.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmActualizarModelo.setIcon(resourceMap.getIcon("itmActualizarModelo.icon")); // NOI18N
        itmActualizarModelo.setText(resourceMap.getString("itmActualizarModelo.text")); // NOI18N
        itmActualizarModelo.setName("itmActualizarModelo"); // NOI18N
        itmActualizarModelo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarModeloActionPerformed(evt);
            }
        });
        mnuProductosTablasYTipos.add(itmActualizarModelo);

        itmActualizarRubro.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmActualizarRubro.setIcon(resourceMap.getIcon("itmActualizarRubro.icon")); // NOI18N
        itmActualizarRubro.setText(resourceMap.getString("itmActualizarRubro.text")); // NOI18N
        itmActualizarRubro.setName("itmActualizarRubro"); // NOI18N
        itmActualizarRubro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarRubroActionPerformed(evt);
            }
        });
        mnuProductosTablasYTipos.add(itmActualizarRubro);

        itmActualizarUnidadMedida.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmActualizarUnidadMedida.setIcon(resourceMap.getIcon("itmActualizarUnidadMedida.icon")); // NOI18N
        itmActualizarUnidadMedida.setText(resourceMap.getString("itmActualizarUnidadMedida.text")); // NOI18N
        itmActualizarUnidadMedida.setName("itmActualizarUnidadMedida"); // NOI18N
        itmActualizarUnidadMedida.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarUnidadMedidaActionPerformed(evt);
            }
        });
        mnuProductosTablasYTipos.add(itmActualizarUnidadMedida);

        mnuProductos.add(mnuProductosTablasYTipos);

        jSeparator9.setName("jSeparator9"); // NOI18N
        mnuProductos.add(jSeparator9);

        itmActualizarProducto.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmActualizarProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/package.png"))); // NOI18N
        itmActualizarProducto.setMnemonic('t');
        itmActualizarProducto.setText(resourceMap.getString("itmActualizarProducto.text")); // NOI18N
        itmActualizarProducto.setToolTipText(resourceMap.getString("itmActualizarProducto.toolTipText")); // NOI18N
        itmActualizarProducto.setName("itmActualizarProducto"); // NOI18N
        itmActualizarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarProductoActionPerformed(evt);
            }
        });
        mnuProductos.add(itmActualizarProducto);

        itmActualizarPreciosProductos.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmActualizarPreciosProductos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/money_dollar.png"))); // NOI18N
        itmActualizarPreciosProductos.setMnemonic('r');
        itmActualizarPreciosProductos.setText(resourceMap.getString("itmActualizarPreciosProductos.text")); // NOI18N
        itmActualizarPreciosProductos.setName("itmActualizarPreciosProductos"); // NOI18N
        itmActualizarPreciosProductos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarPreciosProductosActionPerformed(evt);
            }
        });
        mnuProductos.add(itmActualizarPreciosProductos);

        itmActualizarStockProductos.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmActualizarStockProductos.setIcon(resourceMap.getIcon("itmActualizarStockProductos.icon")); // NOI18N
        itmActualizarStockProductos.setMnemonic('k');
        itmActualizarStockProductos.setText(resourceMap.getString("itmActualizarStockProductos.text")); // NOI18N
        itmActualizarStockProductos.setName("itmActualizarStockProductos"); // NOI18N
        itmActualizarStockProductos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmActualizarStockProductosActionPerformed(evt);
            }
        });
        mnuProductos.add(itmActualizarStockProductos);

        jSeparator11.setName("jSeparator11"); // NOI18N
        mnuProductos.add(jSeparator11);

        itmGenerarInformeProductosVendidos.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmGenerarInformeProductosVendidos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gecom/app/resources/icons/chart_curve.png"))); // NOI18N
        itmGenerarInformeProductosVendidos.setMnemonic('i');
        itmGenerarInformeProductosVendidos.setText(resourceMap.getString("itmGenerarInformeProductosVendidos.text")); // NOI18N
        itmGenerarInformeProductosVendidos.setName("itmGenerarInformeProductosVendidos"); // NOI18N
        itmGenerarInformeProductosVendidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmGenerarInformeProductosVendidosActionPerformed(evt);
            }
        });
        mnuProductos.add(itmGenerarInformeProductosVendidos);

        barraMenu.add(mnuProductos);

        mnuCompras.setMnemonic('c');
        mnuCompras.setText(resourceMap.getString("mnuCompras.text")); // NOI18N
        mnuCompras.setFont(resourceMap.getFont("barraMenu.font")); // NOI18N
        mnuCompras.setName("mnuCompras"); // NOI18N

        itmRegistrarCompra.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        itmRegistrarCompra.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmRegistrarCompra.setIcon(resourceMap.getIcon("itmRegistrarCompra.icon")); // NOI18N
        itmRegistrarCompra.setMnemonic('r');
        itmRegistrarCompra.setText(resourceMap.getString("itmRegistrarCompra.text")); // NOI18N
        itmRegistrarCompra.setName("itmRegistrarCompra"); // NOI18N
        itmRegistrarCompra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmRegistrarCompraActionPerformed(evt);
            }
        });
        mnuCompras.add(itmRegistrarCompra);

        barraMenu.add(mnuCompras);

        mnuVentas.setMnemonic('v');
        mnuVentas.setText(resourceMap.getString("mnuVentas.text")); // NOI18N
        mnuVentas.setFont(resourceMap.getFont("barraMenu.font")); // NOI18N
        mnuVentas.setName("mnuVentas"); // NOI18N

        itmRegistrarVenta.setAction(actionMap.get("accionRegistrarVenta")); // NOI18N
        itmRegistrarVenta.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmRegistrarVenta.setName("itmRegistrarVenta"); // NOI18N
        mnuVentas.add(itmRegistrarVenta);

        jSeparator6.setName("jSeparator6"); // NOI18N
        mnuVentas.add(jSeparator6);

        itmGenerarInformeVentas.setFont(resourceMap.getFont("Application.font.label")); // NOI18N
        itmGenerarInformeVentas.setIcon(resourceMap.getIcon("itmGenerarInformeVentas.icon")); // NOI18N
        itmGenerarInformeVentas.setMnemonic('i');
        itmGenerarInformeVentas.setText(resourceMap.getString("itmGenerarInformeVentas.text")); // NOI18N
        itmGenerarInformeVentas.setName("itmGenerarInformeVentas"); // NOI18N
        itmGenerarInformeVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmGenerarInformeVentasActionPerformed(evt);
            }
        });
        mnuVentas.add(itmGenerarInformeVentas);

        barraMenu.add(mnuVentas);

        mnuAyuda.setText(resourceMap.getString("mnuAyuda.text")); // NOI18N
        mnuAyuda.setFont(resourceMap.getFont("barraMenu.font")); // NOI18N
        mnuAyuda.setName("mnuAyuda"); // NOI18N

        itmAcercaDe.setAction(actionMap.get("showAboutBox")); // NOI18N
        itmAcercaDe.setFont(resourceMap.getFont("itmActualizarParametrosGenerales.font")); // NOI18N
        itmAcercaDe.setName("itmAcercaDe"); // NOI18N
        mnuAyuda.add(itmAcercaDe);

        barraMenu.add(mnuAyuda);

        panelEstado.setName("panelEstado"); // NOI18N

        sepPanelEstado.setName("sepPanelEstado"); // NOI18N

        lblEstadoMensaje.setName("lblEstadoMensaje"); // NOI18N

        lblEstadoAnimacion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblEstadoAnimacion.setName("lblEstadoAnimacion"); // NOI18N

        barraProgreso.setName("barraProgreso"); // NOI18N

        javax.swing.GroupLayout panelEstadoLayout = new javax.swing.GroupLayout(panelEstado);
        panelEstado.setLayout(panelEstadoLayout);
        panelEstadoLayout.setHorizontalGroup(
            panelEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sepPanelEstado, javax.swing.GroupLayout.DEFAULT_SIZE, 883, Short.MAX_VALUE)
            .addGroup(panelEstadoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblEstadoMensaje)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 699, Short.MAX_VALUE)
                .addComponent(barraProgreso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblEstadoAnimacion)
                .addContainerGap())
        );
        panelEstadoLayout.setVerticalGroup(
            panelEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEstadoLayout.createSequentialGroup()
                .addComponent(sepPanelEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEstadoMensaje)
                    .addComponent(lblEstadoAnimacion)
                    .addComponent(barraProgreso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        barraAccesosRapido.setRollover(true);
        barraAccesosRapido.setName("barraAccesosRapido"); // NOI18N

        btnRegistrarVenta.setFont(new java.awt.Font("Dialog", 1, 10));
        btnRegistrarVenta.setIcon(resourceMap.getIcon("btnRegistrarVenta.icon")); // NOI18N
        btnRegistrarVenta.setToolTipText(resourceMap.getString("btnRegistrarVenta.toolTipText")); // NOI18N
        btnRegistrarVenta.setFocusable(false);
        btnRegistrarVenta.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRegistrarVenta.setName("btnRegistrarVenta"); // NOI18N
        btnRegistrarVenta.setOpaque(false);
        btnRegistrarVenta.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRegistrarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarVentaActionPerformed(evt);
            }
        });
        barraAccesosRapido.add(btnRegistrarVenta);

        btnRegistrarCompra.setFont(resourceMap.getFont("btnRegistrarCompra.font")); // NOI18N
        btnRegistrarCompra.setIcon(resourceMap.getIcon("btnRegistrarCompra.icon")); // NOI18N
        btnRegistrarCompra.setText(resourceMap.getString("btnRegistrarCompra.text")); // NOI18N
        btnRegistrarCompra.setFocusable(false);
        btnRegistrarCompra.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRegistrarCompra.setName("btnRegistrarCompra"); // NOI18N
        btnRegistrarCompra.setOpaque(false);
        btnRegistrarCompra.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRegistrarCompra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarCompraActionPerformed(evt);
            }
        });
        barraAccesosRapido.add(btnRegistrarCompra);

        jSeparator4.setName("jSeparator4"); // NOI18N
        barraAccesosRapido.add(jSeparator4);

        btnActualizarProducto.setIcon(resourceMap.getIcon("btnActualizarProducto.icon")); // NOI18N
        btnActualizarProducto.setText(resourceMap.getString("btnActualizarProducto.text")); // NOI18N
        btnActualizarProducto.setToolTipText(resourceMap.getString("btnActualizarProducto.toolTipText")); // NOI18N
        btnActualizarProducto.setFocusable(false);
        btnActualizarProducto.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnActualizarProducto.setName("btnActualizarProducto"); // NOI18N
        btnActualizarProducto.setOpaque(false);
        btnActualizarProducto.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnActualizarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarProductoActionPerformed(evt);
            }
        });
        barraAccesosRapido.add(btnActualizarProducto);

        panelSeparador.setName("panelSeparador"); // NOI18N
        panelSeparador.setOpaque(false);
        panelSeparador.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout panelSeparadorLayout = new javax.swing.GroupLayout(panelSeparador);
        panelSeparador.setLayout(panelSeparadorLayout);
        panelSeparadorLayout.setHorizontalGroup(
            panelSeparadorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 636, Short.MAX_VALUE)
        );
        panelSeparadorLayout.setVerticalGroup(
            panelSeparadorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        barraAccesosRapido.add(panelSeparador);

        lblFechaHora.setFont(resourceMap.getFont("Application.font.big")); // NOI18N
        lblFechaHora.setText(resourceMap.getString("lblFechaHora.text")); // NOI18N
        lblFechaHora.setName("lblFechaHora"); // NOI18N
        barraAccesosRapido.add(lblFechaHora);

        panelDesplazamiento.setName("panelDesplazamiento"); // NOI18N

        panelPrincipal.setDragMode(javax.swing.JDesktopPane.OUTLINE_DRAG_MODE);
        panelPrincipal.setMinimumSize(new java.awt.Dimension(640, 480));
        panelPrincipal.setName("panelPrincipal"); // NOI18N
        panelPrincipal.setPreferredSize(new java.awt.Dimension(800, 500));
        panelDesplazamiento.setViewportView(panelPrincipal);

        setComponent(panelDesplazamiento);
        setMenuBar(barraMenu);
        setStatusBar(panelEstado);
        setToolBar(barraAccesosRapido);
    }// </editor-fold>//GEN-END:initComponents

private void itmActualizarRubroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarRubroActionPerformed
    gestor.iniciarCUGenerico(VentanaActualizarTipos.class, TipoRubro.class, "Rubro");
}//GEN-LAST:event_itmActualizarRubroActionPerformed

private void itmActualizarCilindradaMotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarCilindradaMotoActionPerformed
    gestor.iniciarCUGenerico(VentanaActualizarTipos.class, TipoCilindradaMoto.class, "Cilindrada de Moto");
}//GEN-LAST:event_itmActualizarCilindradaMotoActionPerformed

private void itmActualizarCalidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarCalidadActionPerformed
    gestor.iniciarCUGenerico(VentanaActualizarTipos.class, TipoCalidad.class, "Calidad");
}//GEN-LAST:event_itmActualizarCalidadActionPerformed

private void itmActualizarMarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarMarcaActionPerformed
    gestor.iniciarCUGenerico(VentanaActualizarTipos.class, TipoMarca.class, "Marca");
}//GEN-LAST:event_itmActualizarMarcaActionPerformed

private void itmActualizarModeloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarModeloActionPerformed
    gestor.iniciarCUGenerico(VentanaActualizarTipos.class, TipoModelo.class, "Modelo");
}//GEN-LAST:event_itmActualizarModeloActionPerformed

private void itmActualizarFormaPagoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarFormaPagoActionPerformed
    gestor.iniciarCUGenerico(VentanaActualizarTipos.class, TipoFormaPago.class, "Forma de Pago");
}//GEN-LAST:event_itmActualizarFormaPagoActionPerformed

private void itmActualizarFormaCobroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarFormaCobroActionPerformed
    gestor.iniciarCUGenerico(VentanaActualizarTipos.class, TipoFormaCobro.class, "Forma de Cobro");
}//GEN-LAST:event_itmActualizarFormaCobroActionPerformed

private void itmActualizarTipoUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarTipoUsuarioActionPerformed
    gestor.iniciarCU(VentanaActualizarTiposUsuarioSistema.class);
}//GEN-LAST:event_itmActualizarTipoUsuarioActionPerformed

private void itmActualizarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarUsuarioActionPerformed
    gestor.iniciarCU(VentanaActualizarUsuarioSistema.class);
}//GEN-LAST:event_itmActualizarUsuarioActionPerformed

private void itmActualizarUnidadMedidaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarUnidadMedidaActionPerformed
    gestor.iniciarCUGenerico(VentanaActualizarTipos.class, TipoUnidadMedida.class, "Unidad de Medida");
}//GEN-LAST:event_itmActualizarUnidadMedidaActionPerformed

private void itmActualizarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarProductoActionPerformed
    gestor.iniciarCU(VentanaActualizarProducto.class);
}//GEN-LAST:event_itmActualizarProductoActionPerformed

private void btnRegistrarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarVentaActionPerformed
    gestor.iniciarCU(VentanaRegistrarVenta.class);
}//GEN-LAST:event_btnRegistrarVentaActionPerformed

private void btnActualizarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarProductoActionPerformed
    gestor.iniciarCU(VentanaActualizarProducto.class);
}//GEN-LAST:event_btnActualizarProductoActionPerformed

private void itmActualizarProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarProveedorActionPerformed
    gestor.iniciarCU(VentanaActualizarProveedor.class);
}//GEN-LAST:event_itmActualizarProveedorActionPerformed

private void itmActualizarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarClienteActionPerformed
    gestor.iniciarCU(VentanaActualizarCliente.class);
}//GEN-LAST:event_itmActualizarClienteActionPerformed

private void itmRegistrarCompraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmRegistrarCompraActionPerformed
    gestor.iniciarCU(VentanaRegistrarCompra.class);
}//GEN-LAST:event_itmRegistrarCompraActionPerformed

private void btnRegistrarCompraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarCompraActionPerformed
    gestor.iniciarCU(VentanaRegistrarCompra.class);
}//GEN-LAST:event_btnRegistrarCompraActionPerformed

private void itmActualizarStockProductosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarStockProductosActionPerformed
    gestor.iniciarCU(VentanaActualizarStockProductos.class);
}//GEN-LAST:event_itmActualizarStockProductosActionPerformed

private void itmActualizarPreciosProductosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarPreciosProductosActionPerformed
    gestor.iniciarCU(VentanaActualizarPreciosProductos.class);
}//GEN-LAST:event_itmActualizarPreciosProductosActionPerformed

private void itmActualizarParametrosGeneralesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarParametrosGeneralesActionPerformed
    gestor.iniciarCU(VentanaActualizarParametrosGenerales.class);
}//GEN-LAST:event_itmActualizarParametrosGeneralesActionPerformed

private void itmActualizarProvinciasYLocalidadesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmActualizarProvinciasYLocalidadesActionPerformed
    gestor.iniciarCU(VentanaActualizarProvinciasYLocalidades.class);
}//GEN-LAST:event_itmActualizarProvinciasYLocalidadesActionPerformed

private void itmRegistrarCobroClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmRegistrarCobroClienteActionPerformed
    funcionIncompleta();
}//GEN-LAST:event_itmRegistrarCobroClienteActionPerformed

private void itmConsultarCuentaCorrienteClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmConsultarCuentaCorrienteClienteActionPerformed
    funcionIncompleta();
}//GEN-LAST:event_itmConsultarCuentaCorrienteClienteActionPerformed

private void itmRegistrarPagoProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmRegistrarPagoProveedorActionPerformed
    funcionIncompleta();
}//GEN-LAST:event_itmRegistrarPagoProveedorActionPerformed

private void itmConsultarCuentaCorrienteProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmConsultarCuentaCorrienteProveedorActionPerformed
    gestor.iniciarCU(VentanaConsultarCuentaCorrienteProveedor.class);
}//GEN-LAST:event_itmConsultarCuentaCorrienteProveedorActionPerformed

private void itmGenerarInformeProductosVendidosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmGenerarInformeProductosVendidosActionPerformed
    gestor.iniciarCU(VentanaGenerarInformeProductosVendidos.class);
}//GEN-LAST:event_itmGenerarInformeProductosVendidosActionPerformed

private void itmGenerarInformeVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmGenerarInformeVentasActionPerformed
    gestor.iniciarCU(VentanaGenerarInformeVentas.class);
}//GEN-LAST:event_itmGenerarInformeVentasActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barraAccesosRapido;
    private javax.swing.JMenuBar barraMenu;
    private javax.swing.JProgressBar barraProgreso;
    private javax.swing.JButton btnActualizarProducto;
    private javax.swing.JButton btnRegistrarCompra;
    private javax.swing.JButton btnRegistrarVenta;
    private javax.swing.JMenuItem itmActualizarCalidad;
    private javax.swing.JMenuItem itmActualizarCilindradaMoto;
    private javax.swing.JMenuItem itmActualizarCliente;
    private javax.swing.JMenuItem itmActualizarFormaCobro;
    private javax.swing.JMenuItem itmActualizarFormaPago;
    private javax.swing.JMenuItem itmActualizarMarca;
    private javax.swing.JMenuItem itmActualizarModelo;
    private javax.swing.JMenuItem itmActualizarParametrosGenerales;
    private javax.swing.JMenuItem itmActualizarPreciosProductos;
    private javax.swing.JMenuItem itmActualizarProducto;
    private javax.swing.JMenuItem itmActualizarProveedor;
    private javax.swing.JMenuItem itmActualizarProvinciasYLocalidades;
    private javax.swing.JMenuItem itmActualizarRubro;
    private javax.swing.JMenuItem itmActualizarStockProductos;
    private javax.swing.JMenuItem itmActualizarTipoUsuario;
    private javax.swing.JMenuItem itmActualizarUnidadMedida;
    private javax.swing.JMenuItem itmActualizarUsuario;
    private javax.swing.JMenuItem itmConsultarCuentaCorrienteCliente;
    private javax.swing.JMenuItem itmConsultarCuentaCorrienteProveedor;
    private javax.swing.JMenuItem itmGenerarInformeProductosVendidos;
    private javax.swing.JMenuItem itmGenerarInformeVentas;
    private javax.swing.JMenuItem itmRegistrarCobroCliente;
    private javax.swing.JMenuItem itmRegistrarCompra;
    private javax.swing.JMenuItem itmRegistrarPagoProveedor;
    private javax.swing.JMenuItem itmRegistrarVenta;
    private javax.swing.JCheckBoxMenuItem itmVerBarraAccesosRapido;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JLabel lblEstadoAnimacion;
    private javax.swing.JLabel lblEstadoMensaje;
    private javax.swing.JLabel lblFechaHora;
    private javax.swing.JMenu mnuClientes;
    private javax.swing.JMenu mnuCompras;
    private javax.swing.JMenu mnuConfiguracion;
    private javax.swing.JMenu mnuProductos;
    private javax.swing.JMenu mnuProductosTablasYTipos;
    private javax.swing.JMenu mnuProveedores;
    private javax.swing.JMenu mnuUsuarios;
    private javax.swing.JMenu mnuVentas;
    private javax.swing.JMenu mnuVer;
    private javax.swing.JScrollPane panelDesplazamiento;
    private javax.swing.JPanel panelEstado;
    private javax.swing.JDesktopPane panelPrincipal;
    private javax.swing.JPanel panelSeparador;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;

    /** 
     * SubClase TemporizadorReloj que hace las veces de temporizador.
     */
    class TemporizadorReloj implements ActionListener {

        private String thActual = null;
        private Calendar cal = Calendar.getInstance();

        @Override
        public void actionPerformed(ActionEvent evt) {
            //Aca va el codigo que se repite en cada intervalo del Timer
            cal.setTime(new Date());
            thActual = Fecha.getFechaActual() + " - " + Fecha.getHoraActual();
            lblFechaHora.setText(thActual);
        }
    }

    @Action
    public Task verBarraAccesosRapido() {
        return new VerBarraAccesosRapidoTask(getApplication());
    }

    private class VerBarraAccesosRapidoTask extends org.jdesktop.application.Task<Object, Void> {

        VerBarraAccesosRapidoTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to VerBarraAccesosRapidoTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            barraAccesosRapido.setVisible(itmVerBarraAccesosRapido.isSelected());
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }
}
