
package VISTA;

import Conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;


public class Venta extends javax.swing.JPanel {
 private Ventas_Categorias padre;
   private final int maxBases = 3;
   private final int maxFrutas = 3;

private final ButtonGroup grpTipoCrepa = new ButtonGroup();
    private ToggleSelectionModel modeloSeleccionBases;
    private ToggleSelectionModel modeloSeleccionFrutas;

    public Venta(Ventas_Categorias padre) {
        this.padre = padre;
        initComponents();

        // 1. Agrupar los radios
        
        grpTipoCrepa.add(rbBase1Fruta);
        grpTipoCrepa.add(rbBase2Frutas);
        grpTipoCrepa.add(rbBase3Frutas);
        grpTipoCrepa.add(rb2Bases1Fruta);
        grpTipoCrepa.add(rb2Bases2Frutas);
        grpTipoCrepa.add(rb2Bases3Frutas);
        grpTipoCrepa.add(rb3Bases1Fruta);
        grpTipoCrepa.add(rb3Bases2Frutas);
        grpTipoCrepa.add(rb3Bases3Frutas);
        grpTipoCrepa.add(rbBase);
        grpTipoCrepa.add(rbFruta);
        
        // Los radios indicaran automaticamente el tipo de crepa.
java.util.Enumeration<javax.swing.AbstractButton> radios =
    grpTipoCrepa.getElements();

while (radios.hasMoreElements()) {
    javax.swing.AbstractButton radio = radios.nextElement();
    radio.setFocusable(false);            // no recibe foco con Tab
    radio.setToolTipText(
        "El tipo se determina al seleccionar bases y frutas."
    );
    // Si el usuario hace clic, recalculamos para que vuelva al estado correcto
    radio.addActionListener(e -> actualizarTipoAutomatico());
}

      

        // 2. Configurar los JList con el modelo de selección personalizado
        modeloSeleccionBases = new ToggleSelectionModel();
        modeloSeleccionFrutas = new ToggleSelectionModel();
        listaBases.setSelectionModel(modeloSeleccionBases);
        listaFrutas.setSelectionModel(modeloSeleccionFrutas);

        

        // 4. Cargar datos
        cargarDesplegables();
        cargarPreciosCrepas();

        // Permitir hasta tres ingredientes de cada lista.
modeloSeleccionBases.setMaxSeleccion(maxBases);
modeloSeleccionFrutas.setMaxSeleccion(maxFrutas);

listaBases.addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        actualizarTipoAutomatico();
    }
});

listaFrutas.addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        actualizarTipoAutomatico();
    }
});

actualizarTipoAutomatico();
mostrarIngredientesComoBotones(
    listaBases,
    jScrollPane2,
    jLabel6,
    "Bases",
    maxBases
);

mostrarIngredientesComoBotones(
    listaFrutas,
    jScrollPane3,
    jLabel7,
    "Frutas",
    maxFrutas
);
    }
    private void mostrarIngredientesComoBotones(
        javax.swing.JList<String> lista,
        javax.swing.JScrollPane scroll,
        javax.swing.JLabel etiqueta,
        String titulo,
        int maximo) {

    java.awt.Color fondoNormal = java.awt.Color.WHITE;
    java.awt.Color fondoSeleccionado =
        new java.awt.Color(255, 214, 234);

    java.awt.Color bordeNormal =
        new java.awt.Color(215, 215, 215);

    java.awt.Color bordeSeleccionado =
        new java.awt.Color(190, 80, 130);

    javax.swing.JPanel panelBotones = new javax.swing.JPanel(
        new java.awt.GridLayout(0, 1, 0, 8)
    );

    panelBotones.setBackground(java.awt.Color.WHITE);
    panelBotones.setBorder(
        javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6)
    );

    java.util.List<javax.swing.JToggleButton> botones =
        new java.util.ArrayList<>();

    // Actualizar botones y contador desde la seleccion real.
    Runnable actualizarAspecto = () -> {
        for (int i = 0; i < botones.size(); i++) {
            javax.swing.JToggleButton boton = botones.get(i);
            boolean seleccionado = lista.isSelectedIndex(i);

            boton.setSelected(seleccionado);
            boton.setBackground(
                seleccionado ? fondoSeleccionado : fondoNormal
            );
            boton.setForeground(java.awt.Color.BLACK);

            boton.setBorder(
                javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(
                        seleccionado
                            ? bordeSeleccionado
                            : bordeNormal,
                        2
                    ),
                    javax.swing.BorderFactory.createEmptyBorder(
                        4, 4, 4, 4
                    )
                )
            );
        }

        etiqueta.setText(
            titulo + ": "
            + lista.getSelectedIndices().length
            + "/" + maximo
        );
    };

    // Conservar el clic derecho para limpiar esta seleccion.
    java.awt.event.MouseAdapter limpiarConClicDerecho =
        new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    lista.clearSelection();
                }
            }
        };

    for (int i = 0; i < lista.getModel().getSize(); i++) {
        final int indice = i;
        String nombre = lista.getModel().getElementAt(i);

        javax.swing.JToggleButton boton =
            new javax.swing.JToggleButton(
                "<html><div style='width:100px; text-align:center;'>"
                + escaparTextoIngrediente(nombre)
                + "</div></html>"
            );

        // Permite mostrar los colores definidos para cada estado.
        boton.setUI(
            new javax.swing.plaf.basic.BasicToggleButtonUI()
        );

        boton.setFont(
            new java.awt.Font("Roboto Light", java.awt.Font.BOLD, 14)
        );

        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new java.awt.Dimension(140, 50));
        boton.setToolTipText(nombre);

        boton.setCursor(
            java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.HAND_CURSOR
            )
        );

        boton.addActionListener(e -> {
            if (lista.isSelectedIndex(indice)) {
                lista.removeSelectionInterval(indice, indice);

            } else if (lista.getSelectedIndices().length < maximo) {
                lista.addSelectionInterval(indice, indice);

            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }

            // Si se rechazo el cuarto ingrediente,
            // devolver su boton al estado correcto.
            actualizarAspecto.run();
        });

        boton.addMouseListener(limpiarConClicDerecho);

        botones.add(boton);
        panelBotones.add(boton);
    }

    if (botones.isEmpty()) {
        javax.swing.JLabel vacio = new javax.swing.JLabel(
            "Sin ingredientes disponibles",
            javax.swing.SwingConstants.CENTER
        );
        panelBotones.add(vacio);
    }

    // Mantener los botones arriba sin estirarlos verticalmente.
    javax.swing.JPanel contenido = new javax.swing.JPanel(
        new java.awt.BorderLayout()
    ) {
        @Override
        public java.awt.Dimension getPreferredSize() {
            java.awt.Dimension dimension = super.getPreferredSize();

            // El ancho lo determina el JScrollPane del diseñador.
            return new java.awt.Dimension(0, dimension.height);
        }
    };

    contenido.setBackground(java.awt.Color.WHITE);
    contenido.add(panelBotones, java.awt.BorderLayout.NORTH);

    panelBotones.addMouseListener(limpiarConClicDerecho);
    contenido.addMouseListener(limpiarConClicDerecho);

    scroll.setViewportView(contenido);
    scroll.getViewport().setBackground(java.awt.Color.WHITE);

    scroll.setHorizontalScrollBarPolicy(
        javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    );

    scroll.getVerticalScrollBar().setUnitIncrement(18);

    // Tambien actualizar cuando Agregar limpia las selecciones.
    lista.addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            actualizarAspecto.run();
        }
    });

    actualizarAspecto.run();
}

private String escaparTextoIngrediente(String texto) {
    return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
}

private void actualizarTipoAutomatico() {
    int bases = listaBases.getSelectedIndices().length;
    int frutas = listaFrutas.getSelectedIndices().length;

    javax.swing.JRadioButton opcion = null;

    if (bases == 1 && frutas == 0) {
        opcion = rbBase;

    } else if (bases == 0 && frutas == 1) {
        opcion = rbFruta;

    } else if (bases >= 1 && bases <= 3
            && frutas >= 1 && frutas <= 3) {

        javax.swing.JRadioButton[][] combinaciones = {
            {
                rbBase1Fruta,
                rbBase2Frutas,
                rbBase3Frutas
            },
            {
                rb2Bases1Fruta,
                rb2Bases2Frutas,
                rb2Bases3Frutas
            },
            {
                rb3Bases1Fruta,
                rb3Bases2Frutas,
                rb3Bases3Frutas
            }
        };

        opcion = combinaciones[bases - 1][frutas - 1];
    }

    if (opcion != null) {
        opcion.setSelected(true);
        btnAgregarCrepa.setEnabled(true);
        btnAgregarCrepa.setToolTipText(null);

    } else {
        grpTipoCrepa.clearSelection();
        btnAgregarCrepa.setEnabled(false);

        btnAgregarCrepa.setToolTipText(
            bases == 0 && frutas == 0
                ? "Selecciona los ingredientes."
                : "Esta combinacion no tiene una tarifa. "
                  + "Completa la seleccion con bases y frutas."
        );
    }
}
private void cargarPreciosCrepas() {
    try {
        Connection conn = Conexion.getConexion();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT tipo, precio FROM precios_crepas");
        
        while (rs.next()) {
            String tipo = rs.getString("tipo");
            String precio = "$" + rs.getInt("precio");  // <-- CON $ Y SIN .00
            
            switch (tipo) {
                case "Base + 1 Fruta":
                    preBase1Fruta.setText(precio);
                    break;
                case "Base + 2 Frutas":
                    preBase2Frutas.setText(precio);
                    break;
                case "Base + 3 Frutas":
                    preBase3Frutas.setText(precio);
                    break;
                case "2 Bases + 1 Fruta":
                    pre2Bases1Fruta.setText(precio);
                    break;
                case "2 Bases + 2 Frutas":
                    pre2Bases2Frutas.setText(precio);
                    break;
                case "2 Bases + 3 Frutas":
                    pre2Bases3Frutas.setText(precio);
                    break;
                case "3 Bases + 1 Fruta":
                    pre3Bases1Fruta.setText(precio);
                    break;
                case "3 Bases + 2 Frutas":
                    pre3Bases2Frutas.setText(precio);
                    break;
                case "3 Bases + 3 Frutas":
                    pre3Bases3Frutas.setText(precio);
                    break;
                case "Solo Base":
                    preSoloBase.setText(precio);
                    break;
                case "Solo Fruta":
                    preSoloFruta.setText(precio);
                    break;
            }
        }
        
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar precios: " + e.getMessage());
    }
}
 private double obtenerPrecioCrepa(String tipo) throws SQLException {
    String sql =
        "SELECT precio FROM precios_crepas WHERE tipo = ?";

    try (Connection conn = Conexion.getConexion()) {
        if (conn == null) {
            throw new SQLException(
                "No se pudo conectar con la base de datos."
            );
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                        "No hay un precio registrado para: " + tipo
                    );
                }

                double precio = rs.getDouble("precio");

                if (rs.wasNull() || precio <= 0) {
                    throw new SQLException(
                        "El precio de '" + tipo
                        + "' debe ser mayor que cero."
                    );
                }

                return precio;
            }
        }
    }
}
private void cargarDesplegables() {
    try {
        Connection conn = Conexion.getConexion();
        
        // Limpiar los JList
        DefaultListModel<String> modelBases = new DefaultListModel<>();
        DefaultListModel<String> modelFrutas = new DefaultListModel<>();
        
        Statement stmt = conn.createStatement();
        
        // Cargar bases
        ResultSet rs = stmt.executeQuery(
    "SELECT nombre FROM bases "
    + "WHERE activo = TRUE AND disponible = TRUE "
    + "ORDER BY nombre"
);
        while (rs.next()) {
            modelBases.addElement(rs.getString("nombre"));
        }
        listaBases.setModel(modelBases);
        
        // Cargar frutas
        rs = stmt.executeQuery(
    "SELECT nombre FROM frutas "
    + "WHERE activo = TRUE AND disponible = TRUE "
    + "ORDER BY nombre"
);
        while (rs.next()) {
            modelFrutas.addElement(rs.getString("nombre"));
        }
        listaFrutas.setModel(modelFrutas);
        
        // ⚠️ ELIMINADO: Ya no se cargan frappés aquí, ahora se cargan en cargarFrappesEnTabla()
        
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar los datos: " + e.getMessage());
    }
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        rbBase1Fruta = new javax.swing.JRadioButton();
        rbBase2Frutas = new javax.swing.JRadioButton();
        rb2Bases1Fruta = new javax.swing.JRadioButton();
        rb2Bases2Frutas = new javax.swing.JRadioButton();
        lblPrecioBase4 = new javax.swing.JLabel();
        rbBase3Frutas = new javax.swing.JRadioButton();
        rb2Bases3Frutas = new javax.swing.JRadioButton();
        rbBase = new javax.swing.JRadioButton();
        rbFruta = new javax.swing.JRadioButton();
        rb3Bases1Fruta = new javax.swing.JRadioButton();
        rb3Bases2Frutas = new javax.swing.JRadioButton();
        rb3Bases3Frutas = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        preBase1Fruta = new javax.swing.JTextField();
        preBase2Frutas = new javax.swing.JTextField();
        preBase3Frutas = new javax.swing.JTextField();
        pre2Bases1Fruta = new javax.swing.JTextField();
        pre2Bases2Frutas = new javax.swing.JTextField();
        pre2Bases3Frutas = new javax.swing.JTextField();
        pre3Bases1Fruta = new javax.swing.JTextField();
        pre3Bases2Frutas = new javax.swing.JTextField();
        pre3Bases3Frutas = new javax.swing.JTextField();
        preSoloBase = new javax.swing.JTextField();
        preSoloFruta = new javax.swing.JTextField();
        btnAgregarCrepa = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listaBases = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        listaFrutas = new javax.swing.JList<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 214, 234));
        setMaximumSize(new java.awt.Dimension(600, 800));
        setMinimumSize(new java.awt.Dimension(600, 800));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(600, 800));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(new java.awt.Dimension(1260, 140));
        jPanel1.setMinimumSize(new java.awt.Dimension(1260, 140));
        jPanel1.setPreferredSize(new java.awt.Dimension(1260, 140));
        jPanel1.setRequestFocusEnabled(false);
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Roboto Black", 1, 48)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/VISTA/IMAGENES/decoracion caja registro1.jpg"))); // NOI18N
        jLabel1.setText("CREPAS");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 590, 110));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 610, 120));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        rbBase1Fruta.setBackground(new java.awt.Color(255, 204, 255));
        rbBase1Fruta.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        rbBase1Fruta.setText("1 Base +1  Fruta ");
        rbBase1Fruta.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        rbBase1Fruta.setMaximumSize(new java.awt.Dimension(132, 29));
        rbBase1Fruta.setMinimumSize(new java.awt.Dimension(132, 29));
        rbBase1Fruta.setPreferredSize(new java.awt.Dimension(132, 29));
        jPanel2.add(rbBase1Fruta, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 140, -1));

        rbBase2Frutas.setBackground(new java.awt.Color(255, 204, 255));
        rbBase2Frutas.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        rbBase2Frutas.setText("1 Base + 2 Frutas ");
        rbBase2Frutas.setMaximumSize(new java.awt.Dimension(140, 29));
        rbBase2Frutas.setMinimumSize(new java.awt.Dimension(140, 29));
        rbBase2Frutas.setPreferredSize(new java.awt.Dimension(140, 29));
        jPanel2.add(rbBase2Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        rb2Bases1Fruta.setBackground(new java.awt.Color(255, 204, 255));
        rb2Bases1Fruta.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        rb2Bases1Fruta.setText("2 Bases +1 Fruta");
        rb2Bases1Fruta.setMaximumSize(new java.awt.Dimension(134, 29));
        rb2Bases1Fruta.setMinimumSize(new java.awt.Dimension(134, 29));
        rb2Bases1Fruta.setPreferredSize(new java.awt.Dimension(134, 29));
        jPanel2.add(rb2Bases1Fruta, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, -1, -1));

        rb2Bases2Frutas.setBackground(new java.awt.Color(255, 204, 255));
        rb2Bases2Frutas.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        rb2Bases2Frutas.setText("2 Bases + 2 Frutas ");
        rb2Bases2Frutas.setMaximumSize(new java.awt.Dimension(147, 29));
        rb2Bases2Frutas.setMinimumSize(new java.awt.Dimension(147, 29));
        rb2Bases2Frutas.setPreferredSize(new java.awt.Dimension(147, 29));
        rb2Bases2Frutas.addActionListener(this::rb2Bases2FrutasActionPerformed);
        jPanel2.add(rb2Bases2Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, -1, -1));
        jPanel2.add(lblPrecioBase4, new org.netbeans.lib.awtextra.AbsoluteConstraints(479, 23, -1, 33));

        rbBase3Frutas.setBackground(new java.awt.Color(255, 204, 255));
        rbBase3Frutas.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        rbBase3Frutas.setText("1 Base + 3 Frutas ");
        rbBase3Frutas.setMaximumSize(new java.awt.Dimension(140, 29));
        rbBase3Frutas.setMinimumSize(new java.awt.Dimension(140, 29));
        rbBase3Frutas.setPreferredSize(new java.awt.Dimension(140, 29));
        jPanel2.add(rbBase3Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, -1, -1));

        rb2Bases3Frutas.setBackground(new java.awt.Color(255, 204, 255));
        rb2Bases3Frutas.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        rb2Bases3Frutas.setText("2 Bases + 3 Frutas ");
        rb2Bases3Frutas.setMaximumSize(new java.awt.Dimension(147, 29));
        rb2Bases3Frutas.setMinimumSize(new java.awt.Dimension(147, 29));
        rb2Bases3Frutas.setPreferredSize(new java.awt.Dimension(147, 29));
        rb2Bases3Frutas.addActionListener(this::rb2Bases3FrutasActionPerformed);
        jPanel2.add(rb2Bases3Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, -1, -1));

        rbBase.setBackground(new java.awt.Color(255, 204, 255));
        rbBase.setText("Base");
        rbBase.setMaximumSize(new java.awt.Dimension(47, 29));
        rbBase.setMinimumSize(new java.awt.Dimension(47, 29));
        rbBase.setPreferredSize(new java.awt.Dimension(47, 29));
        jPanel2.add(rbBase, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, 60, -1));

        rbFruta.setBackground(new java.awt.Color(255, 204, 255));
        rbFruta.setText("Fruta");
        rbFruta.setMaximumSize(new java.awt.Dimension(50, 29));
        rbFruta.setMinimumSize(new java.awt.Dimension(50, 29));
        rbFruta.setPreferredSize(new java.awt.Dimension(50, 29));
        jPanel2.add(rbFruta, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, -1, -1));

        rb3Bases1Fruta.setBackground(new java.awt.Color(255, 204, 255));
        rb3Bases1Fruta.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        rb3Bases1Fruta.setText("3 Bases +1 Fruta");
        rb3Bases1Fruta.setMaximumSize(new java.awt.Dimension(134, 29));
        rb3Bases1Fruta.setMinimumSize(new java.awt.Dimension(134, 29));
        rb3Bases1Fruta.setPreferredSize(new java.awt.Dimension(134, 29));
        jPanel2.add(rb3Bases1Fruta, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, -1, -1));

        rb3Bases2Frutas.setBackground(new java.awt.Color(255, 204, 255));
        rb3Bases2Frutas.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        rb3Bases2Frutas.setText("3 Bases + 2 Frutas");
        rb3Bases2Frutas.setMaximumSize(new java.awt.Dimension(144, 29));
        rb3Bases2Frutas.setMinimumSize(new java.awt.Dimension(144, 29));
        rb3Bases2Frutas.setPreferredSize(new java.awt.Dimension(144, 29));
        jPanel2.add(rb3Bases2Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, -1, -1));

        rb3Bases3Frutas.setBackground(new java.awt.Color(255, 204, 255));
        rb3Bases3Frutas.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        rb3Bases3Frutas.setText("3 Bases + 3 Frutas");
        rb3Bases3Frutas.setMaximumSize(new java.awt.Dimension(144, 29));
        rb3Bases3Frutas.setMinimumSize(new java.awt.Dimension(144, 29));
        rb3Bases3Frutas.setPreferredSize(new java.awt.Dimension(144, 29));
        rb3Bases3Frutas.addActionListener(this::rb3Bases3FrutasActionPerformed);
        jPanel2.add(rb3Bases3Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 330, -1, -1));

        jLabel3.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel3.setText("SENCILLAS");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, -1, -1));

        preBase1Fruta.setEditable(false);
        preBase1Fruta.setBackground(new java.awt.Color(255, 255, 255));
        preBase1Fruta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        preBase1Fruta.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        preBase1Fruta.setMaximumSize(new java.awt.Dimension(70, 29));
        preBase1Fruta.setMinimumSize(new java.awt.Dimension(70, 29));
        preBase1Fruta.setName(""); // NOI18N
        preBase1Fruta.setOpaque(true);
        preBase1Fruta.setPreferredSize(new java.awt.Dimension(70, 29));
        preBase1Fruta.addActionListener(this::preBase1FrutaActionPerformed);
        jPanel2.add(preBase1Fruta, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 10, -1, -1));

        preBase2Frutas.setEditable(false);
        preBase2Frutas.setBackground(new java.awt.Color(255, 255, 255));
        preBase2Frutas.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        preBase2Frutas.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        preBase2Frutas.setMaximumSize(new java.awt.Dimension(70, 29));
        preBase2Frutas.setMinimumSize(new java.awt.Dimension(70, 22));
        preBase2Frutas.setName(""); // NOI18N
        preBase2Frutas.setOpaque(true);
        preBase2Frutas.setPreferredSize(new java.awt.Dimension(70, 29));
        preBase2Frutas.addActionListener(this::preBase2FrutasActionPerformed);
        jPanel2.add(preBase2Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 50, -1, -1));

        preBase3Frutas.setEditable(false);
        preBase3Frutas.setBackground(new java.awt.Color(255, 255, 255));
        preBase3Frutas.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        preBase3Frutas.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        preBase3Frutas.setMaximumSize(new java.awt.Dimension(70, 29));
        preBase3Frutas.setMinimumSize(new java.awt.Dimension(70, 29));
        preBase3Frutas.setName(""); // NOI18N
        preBase3Frutas.setOpaque(true);
        preBase3Frutas.setPreferredSize(new java.awt.Dimension(70, 29));
        preBase3Frutas.addActionListener(this::preBase3FrutasActionPerformed);
        jPanel2.add(preBase3Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 90, -1, -1));

        pre2Bases1Fruta.setEditable(false);
        pre2Bases1Fruta.setBackground(new java.awt.Color(255, 255, 255));
        pre2Bases1Fruta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        pre2Bases1Fruta.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pre2Bases1Fruta.setMaximumSize(new java.awt.Dimension(70, 29));
        pre2Bases1Fruta.setMinimumSize(new java.awt.Dimension(70, 29));
        pre2Bases1Fruta.setName(""); // NOI18N
        pre2Bases1Fruta.setOpaque(true);
        pre2Bases1Fruta.setPreferredSize(new java.awt.Dimension(70, 29));
        pre2Bases1Fruta.addActionListener(this::pre2Bases1FrutaActionPerformed);
        jPanel2.add(pre2Bases1Fruta, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 130, -1, -1));

        pre2Bases2Frutas.setEditable(false);
        pre2Bases2Frutas.setBackground(new java.awt.Color(255, 255, 255));
        pre2Bases2Frutas.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        pre2Bases2Frutas.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pre2Bases2Frutas.setMaximumSize(new java.awt.Dimension(70, 29));
        pre2Bases2Frutas.setMinimumSize(new java.awt.Dimension(70, 29));
        pre2Bases2Frutas.setName(""); // NOI18N
        pre2Bases2Frutas.setOpaque(true);
        pre2Bases2Frutas.setPreferredSize(new java.awt.Dimension(70, 29));
        pre2Bases2Frutas.addActionListener(this::pre2Bases2FrutasActionPerformed);
        jPanel2.add(pre2Bases2Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 170, -1, -1));

        pre2Bases3Frutas.setEditable(false);
        pre2Bases3Frutas.setBackground(new java.awt.Color(255, 255, 255));
        pre2Bases3Frutas.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        pre2Bases3Frutas.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pre2Bases3Frutas.setMaximumSize(new java.awt.Dimension(70, 29));
        pre2Bases3Frutas.setMinimumSize(new java.awt.Dimension(70, 29));
        pre2Bases3Frutas.setName(""); // NOI18N
        pre2Bases3Frutas.setOpaque(true);
        pre2Bases3Frutas.setPreferredSize(new java.awt.Dimension(70, 29));
        pre2Bases3Frutas.addActionListener(this::pre2Bases3FrutasActionPerformed);
        jPanel2.add(pre2Bases3Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 210, -1, -1));

        pre3Bases1Fruta.setEditable(false);
        pre3Bases1Fruta.setBackground(new java.awt.Color(255, 255, 255));
        pre3Bases1Fruta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        pre3Bases1Fruta.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pre3Bases1Fruta.setMaximumSize(new java.awt.Dimension(70, 29));
        pre3Bases1Fruta.setMinimumSize(new java.awt.Dimension(70, 29));
        pre3Bases1Fruta.setName(""); // NOI18N
        pre3Bases1Fruta.setOpaque(true);
        pre3Bases1Fruta.setPreferredSize(new java.awt.Dimension(70, 29));
        pre3Bases1Fruta.addActionListener(this::pre3Bases1FrutaActionPerformed);
        jPanel2.add(pre3Bases1Fruta, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 250, -1, -1));

        pre3Bases2Frutas.setEditable(false);
        pre3Bases2Frutas.setBackground(new java.awt.Color(255, 255, 255));
        pre3Bases2Frutas.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        pre3Bases2Frutas.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pre3Bases2Frutas.setMaximumSize(new java.awt.Dimension(70, 29));
        pre3Bases2Frutas.setMinimumSize(new java.awt.Dimension(70, 29));
        pre3Bases2Frutas.setName(""); // NOI18N
        pre3Bases2Frutas.setOpaque(true);
        pre3Bases2Frutas.setPreferredSize(new java.awt.Dimension(70, 29));
        pre3Bases2Frutas.setRequestFocusEnabled(false);
        pre3Bases2Frutas.addActionListener(this::pre3Bases2FrutasActionPerformed);
        jPanel2.add(pre3Bases2Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 290, -1, -1));

        pre3Bases3Frutas.setEditable(false);
        pre3Bases3Frutas.setBackground(new java.awt.Color(255, 255, 255));
        pre3Bases3Frutas.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        pre3Bases3Frutas.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pre3Bases3Frutas.setMaximumSize(new java.awt.Dimension(70, 29));
        pre3Bases3Frutas.setMinimumSize(new java.awt.Dimension(70, 29));
        pre3Bases3Frutas.setName(""); // NOI18N
        pre3Bases3Frutas.setOpaque(true);
        pre3Bases3Frutas.setPreferredSize(new java.awt.Dimension(70, 29));
        pre3Bases3Frutas.addActionListener(this::pre3Bases3FrutasActionPerformed);
        jPanel2.add(pre3Bases3Frutas, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 330, -1, -1));

        preSoloBase.setEditable(false);
        preSoloBase.setBackground(new java.awt.Color(255, 255, 255));
        preSoloBase.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        preSoloBase.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        preSoloBase.setMaximumSize(new java.awt.Dimension(70, 29));
        preSoloBase.setMinimumSize(new java.awt.Dimension(70, 29));
        preSoloBase.setName(""); // NOI18N
        preSoloBase.setOpaque(true);
        preSoloBase.setPreferredSize(new java.awt.Dimension(70, 29));
        preSoloBase.addActionListener(this::preSoloBaseActionPerformed);
        jPanel2.add(preSoloBase, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 390, -1, -1));

        preSoloFruta.setEditable(false);
        preSoloFruta.setBackground(new java.awt.Color(255, 255, 255));
        preSoloFruta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        preSoloFruta.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        preSoloFruta.setMaximumSize(new java.awt.Dimension(70, 29));
        preSoloFruta.setMinimumSize(new java.awt.Dimension(70, 29));
        preSoloFruta.setName(""); // NOI18N
        preSoloFruta.setOpaque(true);
        preSoloFruta.setPreferredSize(new java.awt.Dimension(70, 29));
        preSoloFruta.addActionListener(this::preSoloFrutaActionPerformed);
        jPanel2.add(preSoloFruta, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 430, -1, -1));

        btnAgregarCrepa.setBackground(new java.awt.Color(255, 204, 255));
        btnAgregarCrepa.setFont(new java.awt.Font("Roboto", 1, 12)); // NOI18N
        btnAgregarCrepa.setText("AGREGAR");
        btnAgregarCrepa.addActionListener(this::btnAgregarCrepaActionPerformed);
        jPanel2.add(btnAgregarCrepa, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 590, 90, 30));

        jLabel9.setText("X");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 600, -1, -1));

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 170, 260, 630));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setFont(new java.awt.Font("Roboto Black", 1, 18)); // NOI18N
        jLabel6.setText("Base:");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, -1, -1));

        jLabel7.setFont(new java.awt.Font("Roboto Black", 1, 18)); // NOI18N
        jLabel7.setText("Fruta:");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 0, -1, -1));

        listaBases.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        listaBases.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(listaBases);

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 160, 610));

        listaFrutas.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        listaFrutas.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(listaFrutas);

        jPanel3.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 20, 160, 610));

        add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 170, 350, 630));

        jLabel4.setFont(new java.awt.Font("Roboto ExtraBold", 1, 24)); // NOI18N
        jLabel4.setText("\"ARMA TU CREPA\"");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 140, -1, -1));

        jLabel10.setFont(new java.awt.Font("Roboto ExtraBold", 1, 28)); // NOI18N
        jLabel10.setText("CREPAS");
        jLabel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 130, -1, 30));
    }// </editor-fold>//GEN-END:initComponents

    private void rb2Bases2FrutasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rb2Bases2FrutasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rb2Bases2FrutasActionPerformed

    private void btnAgregarCrepaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarCrepaActionPerformed
  java.util.List<String> basesSeleccionadas =
        listaBases.getSelectedValuesList();

    java.util.List<String> frutasSeleccionadas =
        listaFrutas.getSelectedValuesList();

    int numBases = basesSeleccionadas.size();
    int numFrutas = frutasSeleccionadas.size();

    // Mantener los radios sincronizados con los ingredientes.
    actualizarTipoAutomatico();

    String tipo;

    if (numBases == 1 && numFrutas == 0) {
        tipo = "Solo Base";

    } else if (numBases == 0 && numFrutas == 1) {
        tipo = "Solo Fruta";

    } else if (numBases >= 1 && numBases <= 3
            && numFrutas >= 1 && numFrutas <= 3) {

        String textoBases = numBases == 1
            ? "Base"
            : numBases + " Bases";

        String textoFrutas = numFrutas == 1
            ? "1 Fruta"
            : numFrutas + " Frutas";

        tipo = textoBases + " + " + textoFrutas;

    } else {
        // Combinacion vacia o sin tarifa:
        // actualizarTipoAutomatico() deshabilita Agregar.
        return;
    }

    // Construir la descripcion para el carrito.
    String nombreProducto = tipo;

    if (!basesSeleccionadas.isEmpty()) {
        nombreProducto += "<br>"
            + String.join(", ", basesSeleccionadas);
    }

    if (!frutasSeleccionadas.isEmpty()) {
        nombreProducto += "<br>"
            + String.join(", ", frutasSeleccionadas);
    }

    double precio;

    try {
        precio = obtenerPrecioCrepa(tipo);

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "No se pudo agregar la crepa:\n" + e.getMessage(),
            "Error al consultar el precio",
            JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    // Conservar el manejo de extras y del carrito en el padre.
    boolean agregado = padre.agregarAlCarrito(
        "Crepas",
        nombreProducto,
        1,
        precio
    );

    // Limpiar solamente cuando realmente se agrego.
    if (agregado) {
        listaBases.clearSelection();
        listaFrutas.clearSelection();
        actualizarTipoAutomatico();
    }
       
    
    
    }//GEN-LAST:event_btnAgregarCrepaActionPerformed
// Clase personalizada para selección sin Ctrl (solo clic izquierdo)
class ToggleSelectionModel
        extends javax.swing.DefaultListSelectionModel {

    private int maxSeleccion = Integer.MAX_VALUE;

    public ToggleSelectionModel() {
        setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
    }

    public void setMaxSeleccion(int max) {
        this.maxSeleccion = max;
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        if (index0 < 0 || index1 < 0) {
            return;
        }

        // Volver a pulsar un ingrediente lo desmarca.
        if (isSelectedIndex(index0)) {
            removeSelectionInterval(index0, index1);
            return;
        }

        addSelectionInterval(index0, index1);
    }

    @Override
    public void addSelectionInterval(int index0, int index1) {
        if (index0 < 0 || index1 < 0) {
            return;
        }

        int inicio = Math.min(index0, index1);
        int fin = Math.max(index0, index1);
        int cantidad = getSelectedIndices().length;

        for (int i = inicio; i <= fin; i++) {
            if (!isSelectedIndex(i)) {
                cantidad++;
            }
        }

        if (cantidad > maxSeleccion) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }

        super.addSelectionInterval(index0, index1);
    }
}
    private void rb2Bases3FrutasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rb2Bases3FrutasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rb2Bases3FrutasActionPerformed

    private void rb3Bases3FrutasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rb3Bases3FrutasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rb3Bases3FrutasActionPerformed

    private void preBase1FrutaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preBase1FrutaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_preBase1FrutaActionPerformed

    private void preBase2FrutasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preBase2FrutasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_preBase2FrutasActionPerformed

    private void preBase3FrutasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preBase3FrutasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_preBase3FrutasActionPerformed

    private void pre2Bases1FrutaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pre2Bases1FrutaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pre2Bases1FrutaActionPerformed

    private void pre2Bases2FrutasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pre2Bases2FrutasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pre2Bases2FrutasActionPerformed

    private void pre2Bases3FrutasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pre2Bases3FrutasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pre2Bases3FrutasActionPerformed

    private void pre3Bases1FrutaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pre3Bases1FrutaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pre3Bases1FrutaActionPerformed

    private void pre3Bases2FrutasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pre3Bases2FrutasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pre3Bases2FrutasActionPerformed

    private void pre3Bases3FrutasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pre3Bases3FrutasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pre3Bases3FrutasActionPerformed

    private void preSoloBaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preSoloBaseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_preSoloBaseActionPerformed

    private void preSoloFrutaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preSoloFrutaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_preSoloFrutaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarCrepa;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblPrecioBase4;
    private javax.swing.JList<String> listaBases;
    private javax.swing.JList<String> listaFrutas;
    private javax.swing.JTextField pre2Bases1Fruta;
    private javax.swing.JTextField pre2Bases2Frutas;
    private javax.swing.JTextField pre2Bases3Frutas;
    private javax.swing.JTextField pre3Bases1Fruta;
    private javax.swing.JTextField pre3Bases2Frutas;
    private javax.swing.JTextField pre3Bases3Frutas;
    private javax.swing.JTextField preBase1Fruta;
    private javax.swing.JTextField preBase2Frutas;
    private javax.swing.JTextField preBase3Frutas;
    private javax.swing.JTextField preSoloBase;
    private javax.swing.JTextField preSoloFruta;
    private javax.swing.JRadioButton rb2Bases1Fruta;
    private javax.swing.JRadioButton rb2Bases2Frutas;
    private javax.swing.JRadioButton rb2Bases3Frutas;
    private javax.swing.JRadioButton rb3Bases1Fruta;
    private javax.swing.JRadioButton rb3Bases2Frutas;
    private javax.swing.JRadioButton rb3Bases3Frutas;
    private javax.swing.JRadioButton rbBase;
    private javax.swing.JRadioButton rbBase1Fruta;
    private javax.swing.JRadioButton rbBase2Frutas;
    private javax.swing.JRadioButton rbBase3Frutas;
    private javax.swing.JRadioButton rbFruta;
    // End of variables declaration//GEN-END:variables
// ============================================================
// VERIFICAR SI EL CORTE DE CAJA ESTÁ ABIERTO
// ============================================================
private boolean verificarCorteAbierto() {
    java.sql.Connection conn = null;
    java.sql.PreparedStatement ps = null;
    java.sql.ResultSet rs = null;
    
    try {
        conn = Conexion.getConexion();
        String sql = "SELECT id_corte FROM cortes_caja " +
                     "WHERE estado = 'ABIERTO' AND DATE(fecha_apertura) = CURDATE()";
        ps = conn.prepareStatement(sql);
        rs = ps.executeQuery();
        return rs.next();
    } catch (java.sql.SQLException e) {
        e.printStackTrace();
        return false;
    } finally {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}
}