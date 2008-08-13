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
package simbya.framework.buscador;

import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import simbya.framework.decoradores.Fecha;
import simbya.framework.formateadores.FormateadorEstandar;

/**
 * Diálogo de selección genérico con filtrado de elementos.
 * @author Marcelo Busico.
 */
public class DialogoFiltroGenerico extends javax.swing.JDialog {

    private static final Logger log = Logger.getLogger(DialogoFiltroGenerico.class);
    private DefaultListModel modeloLista;
    private DefaultListModel modeloFiltro;
    private Object seleccion;
    private List<ParametroFiltrado> camposFiltrantes;
    private String nombreClase;
    private String restriccionHQL;
    private GestorBuscador gestor;

    /** 
     * Crea un nuevo formulario DialogoFiltroGenerico.
     */
    protected DialogoFiltroGenerico(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        seleccion = null;
        initComponents();
        modeloLista = new DefaultListModel();
        modeloFiltro = new DefaultListModel();
        lstElementos.setModel(modeloLista);
        lstFiltros.setModel(modeloFiltro);
    }

    private void setCamposFiltrantes(List<ParametroFiltrado> camposFiltrantes) {
        this.camposFiltrantes = camposFiltrantes;
    }

    private void setNombreClase(String nombreClase) {
        this.nombreClase = nombreClase;
    }

    private Object getSeleccion() {
        return seleccion;
    }

    private void setGestor(GestorBuscador gestor) {
        this.gestor = gestor;
    }

    private String getRestriccionHQL() {
        return restriccionHQL;
    }

    private void setRestriccionHQL(String restriccionHQL) {
        this.restriccionHQL = restriccionHQL;
    }

    /**
     * Muestra un cuadro de diálogo genérico que permite filtrar y seleccionar 
     * un elemento entre varios disponibles.
     * @param padre Ventana padre que llama al diálogo, que será desactivada
     * mientras el diálogo permanezca visible.
     * @param nombreClase Nombre de la Clase del elemento a buscar. Debería utilizar
     * el método getName() de la clase para ésta variable y debe ser una clase
     * mapeada por Hibernate.
     * @param camposFiltrantes Una lista de pares clave-valor que contendrán en la
     * clave el valor del atributo a filtrar, y el valor es el nombre del atributo clave
     * a mostrar en el combo de selección.
     * @param sesion Sesion de Hibernate que tenga una transacción abierta para poder
     * realizar las consultas sobre los objetos que llenarán la lista de selección.
     * @param restriccionHQL Valor optativo (podría ser null) que representa una
     * restricción extra para los elementos mostrados en la lista.
     * @return Objeto seleccionado por el usuario una vez confirmado el diálogo, o bien
     * null si el usuario no seleccionó un objecto o si cerró el diálogo.
     * @throws java.lang.Exception Lanza una excepción en caso de que alguno de los
     * datos proporcionados al método sea inválido por no corresponder al mapeo o bien
     * por alguna otra causa.
     */
    public static Object mostrarDialogo(java.awt.Frame padre,
            String nombreClase, List<ParametroFiltrado> camposFiltrantes,
            GestorBuscador gestor, String restriccionHQL) throws
            Exception {

        DialogoFiltroGenerico dialogo = new DialogoFiltroGenerico(padre, true);
        dialogo.setCamposFiltrantes(camposFiltrantes);
        dialogo.setNombreClase(nombreClase);
        dialogo.setGestor(gestor);
        dialogo.setRestriccionHQL(restriccionHQL);

        dialogo.setVisible(true);

        return dialogo.getSeleccion();
    }

    /**
     * Toma la selección de la lista y sale del diálogo.
     */
    private void tomarSeleccion() {
        seleccion = lstElementos.getSelectedValue();
        if (seleccion == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un elemento de la lista.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
        } else {
            dispose();
        }
    }

    /**
     * Muestra los elementos en la lista de selección.
     * @param elementos Lista de objetos que llenarán la lista utilizando su
     * método toString().
     */
    private void mostrarElementos(List elementos) {
        modeloLista.removeAllElements();
        if (elementos == null) {
            lblCantidad.setText("0");
            return;
        }
        for (Object object : elementos) {
            modeloLista.addElement(object);
        }
        lblCantidad.setText(String.valueOf(modeloLista.size()));
    }

    /**
     * Inicializa el cuadro de diálogo con la información obtenida de la BD.
     */
    private void inicializarDialogo() {
        //Llena la lista
        String consulta = "from " + nombreClase;
        if (getRestriccionHQL() != null) {
            consulta += " where " + restriccionHQL;
        }
        List res = gestor.buscar(consulta);
        mostrarElementos(res);
        //Llena el combo
        cboFiltroSimple.removeAllItems();
        cboFiltroAvanzado.removeAllItems();
        if (camposFiltrantes == null) {
            return;
        }

        for (ParametroFiltrado parametro : camposFiltrantes) {
            cboFiltroSimple.addItem(parametro);
            cboFiltroAvanzado.addItem(parametro);
        }

        //Seleccionar el primer elemento si el combo no esta vacío.
        if (cboFiltroAvanzado.getItemCount() > 0) {
            cboFiltroAvanzado.setSelectedIndex(0);
            setOpciones();
        }
        if (cboFiltroSimple.getItemCount() > 0) {
            cboFiltroSimple.setSelectedIndex(0);
        }
    }

    /**
     * Filtra los elementos de la lista a partir del valor ingresado en el
     * cuadro de texto y el tipo de dato elegido en el combo.
     */
    private void filtrarResultadosSimples() {
        String valorIngresado = txtValorSimple.getText();
        String consulta = "from " + nombreClase;
        if (getRestriccionHQL() != null) {
            consulta += " where " + restriccionHQL;
        }
        List res = null;
        if (cboFiltroSimple.getSelectedItem() != null) {
            ParametroFiltrado param =
                    (ParametroFiltrado) cboFiltroSimple.getSelectedItem();

            if (valorIngresado != null && valorIngresado.isEmpty() == false) {
                //Verificar el tipo de dato elegido.
                int tipo = param.getTipo();
                switch (tipo) {
                    case ParametroFiltrado.cadena:
                        //Busca el valor al principio de la cadena
                        consulta = "from " + nombreClase + " where " +
                                param.getAtributo() + " like '" + valorIngresado + "%'";
                        if (getRestriccionHQL() != null) {
                            consulta += " and " + restriccionHQL;
                        }
                        res = gestor.buscar(consulta);
                        break;

                    case ParametroFiltrado.entero:
                        Long entero = null;
                        try {
                            entero = Long.parseLong(valorIngresado);
                            //Busca el valor exacto
                            consulta = "from " + nombreClase + " where " +
                                    param.getAtributo() + " = " + String.valueOf(
                                    entero.longValue());
                            if (getRestriccionHQL() != null) {
                                consulta += " and " + restriccionHQL;
                            }
                        } catch (NumberFormatException e) {
                            mostrarElementos(null);
                            return;
                        }
                        res = gestor.buscar(consulta);
                        break;

                    case ParametroFiltrado.decimal:
                        Float decimal = null;
                        try {
                            decimal = Float.parseFloat(valorIngresado);
                            //Busca el valor exacto
                            consulta = "from " + nombreClase + " where " +
                                    param.getAtributo() + " = " + String.valueOf(
                                    decimal.floatValue());
                            if (getRestriccionHQL() != null) {
                                consulta += " and " + restriccionHQL;
                            }
                        } catch (NumberFormatException e) {
                            mostrarElementos(null);
                            return;
                        }
                        res = gestor.buscar(consulta);
                        break;

                    case ParametroFiltrado.fecha:
                        Date fecha = FormateadorEstandar.desformatearFecha(valorIngresado);
                        if (fecha == null) {
                            res = null;
                        } else {
                            //Busca la fecha exacta
                            consulta = "from " + nombreClase + " where " +
                                    param.getAtributo() + " = ?";
                            if (getRestriccionHQL() != null) {
                                consulta += " and " + restriccionHQL;
                            }
                            res = gestor.buscar(consulta, fecha);
                        }
                        break;
                }
            } else {
                consulta = "from " + nombreClase;
                if (getRestriccionHQL() != null) {
                    consulta += " where " + restriccionHQL;
                }
                res = gestor.buscar(consulta);
            }
            mostrarElementos(res);
        }
    }

    /**
     * Establece la opciones de filtrado del cuadro de búsqueda según el tipo
     * de dato elegido en el cuadro de texto y establece sus ToolTipText.
     */
    private void setOpciones() {
        Object res = cboFiltroAvanzado.getSelectedItem();
        if (res == null) {
            panelDondeBuscar.setVisible(false);
        }
        ParametroFiltrado pf = (ParametroFiltrado) res;
        int tipo = pf.getTipo();
        switch (tipo) {
            case ParametroFiltrado.cadena:
                opcion1.setText("Valor Exacto");
                opcion1.setToolTipText(
                        "El texto buscado coincide totalmente con el ingresado");
                opcion2.setText("Contiene");
                opcion2.setToolTipText(
                        "El texto buscado contiene al texto ingresado");
                opcion3.setText("Comienzo");
                opcion3.setToolTipText(
                        "El texto buscado comienza con el texto ingresado");
                opcion4.setText("Final");
                opcion4.setToolTipText(
                        "El texto buscado finaliza con el texto ingresado");
                break;
            case ParametroFiltrado.decimal:
            case ParametroFiltrado.entero:
                opcion1.setText("Igual");
                opcion1.setToolTipText(
                        "El número buscado es exactamente igual al ingresado");
                opcion2.setText("Distinto");
                opcion2.setToolTipText(
                        "El número buscado es distinto al ingresado");
                opcion3.setText("Menor");
                opcion3.setToolTipText(
                        "El número buscado es menor al ingresado");
                opcion4.setText("Mayor");
                opcion4.setToolTipText(
                        "El número buscado es mayor al ingresado");
                break;
            case ParametroFiltrado.fecha:
                opcion1.setText("Igual");
                opcion1.setToolTipText(
                        "La fecha buscada es igual a la ingresada");
                opcion2.setText("Distinta");
                opcion2.setToolTipText(
                        "La fecha buscada es distinta a la ingresada");
                opcion3.setText("Antes");
                opcion3.setToolTipText(
                        "La fecha buscada es anterior a la ingresada");
                opcion4.setText("Después");
                opcion4.setToolTipText(
                        "La fecha buscada es posterior a la ingresada");
                break;
        }

    }

    /**
     * Agrega a la lista avanzada el filtro seleccionado a partir de la selección 
     * del usuario y del valor buscado.
     */
    private void agregarFiltro() {
        String valorIngresado = txtValorAvanzado.getText();
        if (cboFiltroAvanzado.getSelectedItem() == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Debe seleccionar el tipo de filtro a utilizar.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (valorIngresado == null || valorIngresado.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Debe ingresar el valor a buscar.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ParametroFiltrado param =
                (ParametroFiltrado) cboFiltroAvanzado.getSelectedItem();

        //Verificar el tipo de dato elegido
        int tipo = param.getTipo();

        Object objeto = null;
        Filtro filtro = new Filtro();
        filtro.setParametro(
                (ParametroFiltrado) cboFiltroAvanzado.getSelectedItem());
        int opcionElegida = 0;
        switch (tipo) {
            case ParametroFiltrado.cadena:
                if (opcion1.isSelected()) {
                    opcionElegida = Filtro.TIPO_CADENA_EXACTO;
                } else {
                    if (opcion2.isSelected()) {
                        opcionElegida = Filtro.TIPO_CADENA_CONTIENE;
                    } else {
                        if (opcion3.isSelected()) {
                            opcionElegida = Filtro.TIPO_CADENA_COMIENZO;
                        } else {
                            opcionElegida = Filtro.TIPO_CADENA_FINAL;
                        }
                    }
                }
                objeto = valorIngresado;
                break;

            case ParametroFiltrado.entero:
                if (opcion1.isSelected()) {
                    opcionElegida = Filtro.TIPO_ENTERO_IGUAL;
                } else {
                    if (opcion2.isSelected()) {
                        opcionElegida = Filtro.TIPO_ENTERO_DISTINTO;
                    } else {
                        if (opcion3.isSelected()) {
                            opcionElegida = Filtro.TIPO_ENTERO_MENOR;
                        } else {
                            opcionElegida = Filtro.TIPO_ENTERO_MAYOR;
                        }
                    }
                }
                Long entero = null;
                try {
                    entero = Long.parseLong(valorIngresado);
                    objeto = entero;
                } catch (NumberFormatException e) {
                    objeto = null;
                }
                break;

            case ParametroFiltrado.decimal:
                if (opcion1.isSelected()) {
                    opcionElegida = Filtro.TIPO_DECIMAL_IGUAL;
                } else {
                    if (opcion2.isSelected()) {
                        opcionElegida = Filtro.TIPO_DECIMAL_DISTINTO;
                    } else {
                        if (opcion3.isSelected()) {
                            opcionElegida = Filtro.TIPO_DECIMAL_MENOR;
                        } else {
                            opcionElegida = Filtro.TIPO_DECIMAL_MAYOR;
                        }
                    }
                }
                Float decimal = null;
                try {
                    decimal = Float.parseFloat(valorIngresado);
                    objeto = decimal;
                } catch (NumberFormatException e) {
                    objeto = null;
                }
                break;

            case ParametroFiltrado.fecha:
                if (opcion1.isSelected()) {
                    opcionElegida = Filtro.TIPO_FECHA_IGUAL;
                } else {
                    if (opcion2.isSelected()) {
                        opcionElegida = Filtro.TIPO_FECHA_DISTINTA;
                    } else {
                        if (opcion3.isSelected()) {
                            opcionElegida = Filtro.TIPO_FECHA_ANTES;
                        } else {
                            opcionElegida = Filtro.TIPO_FECHA_DESPUES;
                        }
                    }
                }

                Date fecha = FormateadorEstandar.desformatearFecha(valorIngresado);
                objeto = fecha;
                break;
        }
        if (objeto == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Debe ingresar un valor válido para el filtro.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        filtro.setValor(objeto);
        filtro.setTipo(opcionElegida);
        modeloFiltro.addElement(filtro);
        filtrarResultadosAvanzado();
    }

    /**
     * Quita de la lista el filtro seleccionado.
     */
    private void quitarFiltro() {
        int indice = lstFiltros.getSelectedIndex();
        if (indice >= 0) {
            modeloFiltro.remove(lstFiltros.getSelectedIndex());
            filtrarResultadosAvanzado();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No hay filtro seleccionado de la lista para quitar.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
        }

    }

    /**
     * Quita de la lista de filtros a todos los filtros previamente agregados.
     */
    private void limpiarFiltros() {
        modeloFiltro.removeAllElements();
        filtrarResultadosAvanzado();
    }

    /**
     * Filtra los elementos de la lista a partir del valor de los filtros
     * definidos por el usuario en la búsqueda avanzada.
     */
    private void filtrarResultadosAvanzado() {
        List res = null;
        String consulta = "from " + nombreClase;
        if (getRestriccionHQL() != null) {
            consulta += " where " + restriccionHQL;
        }
        //Verificar que la lista de filtros contenga elementos
        if (modeloFiltro.getSize() == 0) {
            //Ejecutar la consulta
            res = gestor.buscar(consulta);
            //Mostrar los elementos
            mostrarElementos(res);
            return;
        }
        //La lista contiene filtros
        if (getRestriccionHQL() != null) {
            consulta += " and ";
        } else {
            consulta += " where ";
        }
        //Recorrer toda la lista de filtros agregados
        for (int i = 0; i < modeloFiltro.getSize(); i++) {
            log.trace("Dentro del for con i = " + i);
            //Si hay mas de 1 filtro agregar el 'and'
            if (i > 0) {
                consulta += " and ";
            }
            Filtro factual = (Filtro) modeloFiltro.get(i);
            ParametroFiltrado param = factual.getParametro();

            //Verificar el tipo de dato elegido.
            Object valor = factual.getValor();
            log.trace("Ahora va el switch con el tipo = " + factual.getTipo());
            switch (factual.getTipo()) {
                case Filtro.TIPO_CADENA_EXACTO:
                    consulta += "lower(" + param.getAtributo() +
                            ") like lower('" + valor.toString() + "')";
                    break;
                case Filtro.TIPO_CADENA_CONTIENE:
                    consulta += "lower(" + param.getAtributo() +
                            ") like lower('%" + valor.toString() + "%')";
                    break;
                case Filtro.TIPO_CADENA_COMIENZO:
                    consulta += "lower(" + param.getAtributo() +
                            ") like lower('" + valor.toString() + "%')";
                    break;
                case Filtro.TIPO_CADENA_FINAL:
                    consulta += "lower(" + param.getAtributo() +
                            ") like lower('%" + valor.toString() + "')";
                    break;


                case Filtro.TIPO_ENTERO_IGUAL:
                    consulta += param.getAtributo() + " = " + valor.toString();
                    break;
                case Filtro.TIPO_ENTERO_DISTINTO:
                    consulta += param.getAtributo() + " != " + valor.toString();
                    break;
                case Filtro.TIPO_ENTERO_MENOR:
                    consulta += param.getAtributo() + " < " + valor.toString();
                    break;
                case Filtro.TIPO_ENTERO_MAYOR:
                    consulta += param.getAtributo() + " > " + valor.toString();
                    break;


                case Filtro.TIPO_DECIMAL_IGUAL:
                    consulta += param.getAtributo() + " = " + valor.toString();
                    break;
                case Filtro.TIPO_DECIMAL_DISTINTO:
                    consulta += param.getAtributo() + " != " + valor.toString();
                    break;
                case Filtro.TIPO_DECIMAL_MENOR:
                    consulta += param.getAtributo() + " < " + valor.toString();
                    break;
                case Filtro.TIPO_DECIMAL_MAYOR:
                    consulta += param.getAtributo() + " > " + valor.toString();
                    break;

                case Filtro.TIPO_FECHA_IGUAL:
                    consulta += param.getAtributo() + " = '" +
                            new Fecha((Date) valor).getFechaSQL() + "'";
                    break;
                case Filtro.TIPO_FECHA_DISTINTA:
                    consulta += param.getAtributo() + " != '" +
                            new Fecha((Date) valor).getFechaSQL() + "'";
                    break;
                case Filtro.TIPO_FECHA_ANTES:
                    consulta += param.getAtributo() + " < '" +
                            new Fecha((Date) valor).getFechaSQL() + "'";
                    break;
                case Filtro.TIPO_FECHA_DESPUES:
                    consulta += param.getAtributo() + " > '" +
                            new Fecha((Date) valor).getFechaSQL() + "'";
                    break;
            }
        }

        //Ejecutar la consulta
        log.trace(consulta);
        res = gestor.buscar(consulta);

        //Mostrar los elementos
        mostrarElementos(res);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grupoOcionesFiltrado = new javax.swing.ButtonGroup();
        btnSeleccionar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        panelElementos = new javax.swing.JPanel();
        panelDesplazamiento = new javax.swing.JScrollPane();
        lstElementos = new javax.swing.JList();
        lblTotal = new javax.swing.JLabel();
        lblCantidad = new javax.swing.JLabel();
        panelConSolapas = new javax.swing.JTabbedPane();
        panelBusquedaSimple = new javax.swing.JPanel();
        panelCuadroFiltroSimple = new javax.swing.JPanel();
        txtValorSimple = new javax.swing.JTextField();
        cboFiltroSimple = new javax.swing.JComboBox();
        lblFiltroSimple = new javax.swing.JLabel();
        lblValorSimple = new javax.swing.JLabel();
        btnBuscar = new javax.swing.JButton();
        panelBusquedaAvanzada = new javax.swing.JPanel();
        panelDondeBuscar = new javax.swing.JPanel();
        opcion1 = new javax.swing.JRadioButton();
        opcion3 = new javax.swing.JRadioButton();
        opcion4 = new javax.swing.JRadioButton();
        opcion2 = new javax.swing.JRadioButton();
        txtValorAvanzado = new javax.swing.JTextField();
        cboFiltroAvanzado = new javax.swing.JComboBox();
        lblFiltroAvanzado = new javax.swing.JLabel();
        lblValorAvanzado = new javax.swing.JLabel();
        btnQuitarTodos = new javax.swing.JButton();
        btnQuitar = new javax.swing.JButton();
        btnAgregar = new javax.swing.JButton();
        panelDesplazamientoFiltros = new javax.swing.JScrollPane();
        lstFiltros = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Búsqueda...");
        setMinimumSize(new java.awt.Dimension(629, 505));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        btnSeleccionar.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        btnSeleccionar.setText("Seleccionar");
        btnSeleccionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSeleccionarActionPerformed(evt);
            }
        });

        btnCancelar.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        panelElementos.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lstElementos.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        lstElementos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstElementos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstElementosMouseClicked(evt);
            }
        });
        lstElementos.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lstElementosKeyPressed(evt);
            }
        });
        panelDesplazamiento.setViewportView(lstElementos);

        lblTotal.setText("Resultados encontrados:");

        lblCantidad.setText(" ");

        javax.swing.GroupLayout panelElementosLayout = new javax.swing.GroupLayout(panelElementos);
        panelElementos.setLayout(panelElementosLayout);
        panelElementosLayout.setHorizontalGroup(
            panelElementosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelElementosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelElementosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelDesplazamiento, javax.swing.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE)
                    .addGroup(panelElementosLayout.createSequentialGroup()
                        .addComponent(lblTotal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCantidad, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelElementosLayout.setVerticalGroup(
            panelElementosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelElementosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelDesplazamiento, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelElementosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotal)
                    .addComponent(lblCantidad))
                .addContainerGap())
        );

        panelConSolapas.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        panelConSolapas.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                panelConSolapasStateChanged(evt);
            }
        });

        panelCuadroFiltroSimple.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtValorSimple.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        txtValorSimple.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtValorSimpleKeyPressed(evt);
            }
        });

        cboFiltroSimple.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N

        lblFiltroSimple.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        lblFiltroSimple.setText("Filtrar Por El Campo:");

        lblValorSimple.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        lblValorSimple.setText("Valor Buscado:");

        btnBuscar.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCuadroFiltroSimpleLayout = new javax.swing.GroupLayout(panelCuadroFiltroSimple);
        panelCuadroFiltroSimple.setLayout(panelCuadroFiltroSimpleLayout);
        panelCuadroFiltroSimpleLayout.setHorizontalGroup(
            panelCuadroFiltroSimpleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCuadroFiltroSimpleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCuadroFiltroSimpleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblFiltroSimple)
                    .addComponent(lblValorSimple))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCuadroFiltroSimpleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboFiltroSimple, 0, 206, Short.MAX_VALUE)
                    .addComponent(txtValorSimple, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBuscar)
                .addGap(124, 124, 124))
        );
        panelCuadroFiltroSimpleLayout.setVerticalGroup(
            panelCuadroFiltroSimpleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCuadroFiltroSimpleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCuadroFiltroSimpleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFiltroSimple)
                    .addComponent(cboFiltroSimple, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCuadroFiltroSimpleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValorSimple)
                    .addComponent(txtValorSimple, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelBusquedaSimpleLayout = new javax.swing.GroupLayout(panelBusquedaSimple);
        panelBusquedaSimple.setLayout(panelBusquedaSimpleLayout);
        panelBusquedaSimpleLayout.setHorizontalGroup(
            panelBusquedaSimpleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBusquedaSimpleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelCuadroFiltroSimple, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelBusquedaSimpleLayout.setVerticalGroup(
            panelBusquedaSimpleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBusquedaSimpleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelCuadroFiltroSimple, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(97, Short.MAX_VALUE))
        );

        panelConSolapas.addTab("Búsqueda Simple", panelBusquedaSimple);

        panelDondeBuscar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        grupoOcionesFiltrado.add(opcion1);
        opcion1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        opcion1.setSelected(true);
        opcion1.setText("Valor exacto");

        grupoOcionesFiltrado.add(opcion3);
        opcion3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        opcion3.setText("Comienzo");

        grupoOcionesFiltrado.add(opcion4);
        opcion4.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        opcion4.setText("Final");

        grupoOcionesFiltrado.add(opcion2);
        opcion2.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        opcion2.setText("Contiene");

        txtValorAvanzado.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        txtValorAvanzado.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtValorAvanzadoKeyPressed(evt);
            }
        });

        cboFiltroAvanzado.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        cboFiltroAvanzado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboFiltroAvanzadoActionPerformed(evt);
            }
        });

        lblFiltroAvanzado.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        lblFiltroAvanzado.setText("Filtrar Por El Campo:");

        lblValorAvanzado.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        lblValorAvanzado.setText("Valor Buscado:");

        javax.swing.GroupLayout panelDondeBuscarLayout = new javax.swing.GroupLayout(panelDondeBuscar);
        panelDondeBuscar.setLayout(panelDondeBuscarLayout);
        panelDondeBuscarLayout.setHorizontalGroup(
            panelDondeBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDondeBuscarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDondeBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblFiltroAvanzado)
                    .addComponent(lblValorAvanzado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDondeBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtValorAvanzado, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                    .addComponent(cboFiltroAvanzado, 0, 152, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(panelDondeBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(opcion1)
                    .addComponent(opcion2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDondeBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(opcion4)
                    .addComponent(opcion3))
                .addContainerGap())
        );
        panelDondeBuscarLayout.setVerticalGroup(
            panelDondeBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDondeBuscarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDondeBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDondeBuscarLayout.createSequentialGroup()
                        .addGroup(panelDondeBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFiltroAvanzado)
                            .addComponent(cboFiltroAvanzado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelDondeBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblValorAvanzado)
                            .addComponent(txtValorAvanzado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelDondeBuscarLayout.createSequentialGroup()
                        .addComponent(opcion3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(opcion4))
                    .addGroup(panelDondeBuscarLayout.createSequentialGroup()
                        .addComponent(opcion1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(opcion2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnQuitarTodos.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        btnQuitarTodos.setText("Quitar todos");
        btnQuitarTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarTodosActionPerformed(evt);
            }
        });

        btnQuitar.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        btnQuitar.setText("Quitar Filtro");
        btnQuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarActionPerformed(evt);
            }
        });

        btnAgregar.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        btnAgregar.setText("Agregar Filtro");
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });

        panelDesplazamientoFiltros.setViewportView(lstFiltros);

        javax.swing.GroupLayout panelBusquedaAvanzadaLayout = new javax.swing.GroupLayout(panelBusquedaAvanzada);
        panelBusquedaAvanzada.setLayout(panelBusquedaAvanzadaLayout);
        panelBusquedaAvanzadaLayout.setHorizontalGroup(
            panelBusquedaAvanzadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBusquedaAvanzadaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBusquedaAvanzadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelDesplazamientoFiltros, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                    .addComponent(panelDondeBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelBusquedaAvanzadaLayout.createSequentialGroup()
                        .addComponent(btnAgregar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnQuitar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnQuitarTodos)))
                .addContainerGap())
        );
        panelBusquedaAvanzadaLayout.setVerticalGroup(
            panelBusquedaAvanzadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBusquedaAvanzadaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelDondeBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBusquedaAvanzadaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregar)
                    .addComponent(btnQuitar)
                    .addComponent(btnQuitarTodos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelDesplazamientoFiltros, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelConSolapas.addTab("Búsqueda Avanzada", panelBusquedaAvanzada);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelConSolapas, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                    .addComponent(panelElementos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSeleccionar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelElementos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelConSolapas, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnSeleccionar))
                .addContainerGap())
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-629)/2, (screenSize.height-505)/2, 629, 505);
    }// </editor-fold>//GEN-END:initComponents
    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        inicializarDialogo();
    }//GEN-LAST:event_formWindowOpened

    private void btnSeleccionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSeleccionarActionPerformed
        tomarSeleccion();
    }//GEN-LAST:event_btnSeleccionarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        filtrarResultadosSimples();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void txtValorSimpleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtValorSimpleKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            filtrarResultadosSimples();
        }
    }//GEN-LAST:event_txtValorSimpleKeyPressed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        agregarFiltro();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void cboFiltroAvanzadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboFiltroAvanzadoActionPerformed
        setOpciones();
    }//GEN-LAST:event_cboFiltroAvanzadoActionPerformed

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        quitarFiltro();
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void btnQuitarTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarTodosActionPerformed
        limpiarFiltros();
    }//GEN-LAST:event_btnQuitarTodosActionPerformed

    private void txtValorAvanzadoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtValorAvanzadoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            agregarFiltro();
        }
    }//GEN-LAST:event_txtValorAvanzadoKeyPressed

    private void panelConSolapasStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_panelConSolapasStateChanged
        if (panelBusquedaSimple.isVisible()) {
            filtrarResultadosSimples();
            return;
        }
        if (panelBusquedaAvanzada.isVisible()) {
            filtrarResultadosAvanzado();
            return;
        }
    }//GEN-LAST:event_panelConSolapasStateChanged

private void lstElementosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstElementosMouseClicked
    if (evt.getClickCount() == 2 && lstElementos.getModel().getSize() != 0) {
        tomarSeleccion();
    }
}//GEN-LAST:event_lstElementosMouseClicked

private void lstElementosKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lstElementosKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ENTER && lstElementos.getModel().getSize() != 0) {
        tomarSeleccion();
    }
}//GEN-LAST:event_lstElementosKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JButton btnQuitarTodos;
    private javax.swing.JButton btnSeleccionar;
    private javax.swing.JComboBox cboFiltroAvanzado;
    private javax.swing.JComboBox cboFiltroSimple;
    private javax.swing.ButtonGroup grupoOcionesFiltrado;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblFiltroAvanzado;
    private javax.swing.JLabel lblFiltroSimple;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblValorAvanzado;
    private javax.swing.JLabel lblValorSimple;
    private javax.swing.JList lstElementos;
    private javax.swing.JList lstFiltros;
    private javax.swing.JRadioButton opcion1;
    private javax.swing.JRadioButton opcion2;
    private javax.swing.JRadioButton opcion3;
    private javax.swing.JRadioButton opcion4;
    private javax.swing.JPanel panelBusquedaAvanzada;
    private javax.swing.JPanel panelBusquedaSimple;
    private javax.swing.JTabbedPane panelConSolapas;
    private javax.swing.JPanel panelCuadroFiltroSimple;
    private javax.swing.JScrollPane panelDesplazamiento;
    private javax.swing.JScrollPane panelDesplazamientoFiltros;
    private javax.swing.JPanel panelDondeBuscar;
    private javax.swing.JPanel panelElementos;
    private javax.swing.JTextField txtValorAvanzado;
    private javax.swing.JTextField txtValorSimple;
    // End of variables declaration//GEN-END:variables
}
