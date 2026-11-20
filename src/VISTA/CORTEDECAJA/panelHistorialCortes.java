
package VISTA.CORTEDECAJA;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import Conexion.Conexion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
public class panelHistorialCortes extends javax.swing.JPanel {
    private static final DateTimeFormatter FORMATO_FECHA =
    DateTimeFormatter.ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT);
public panelHistorialCortes() {
    initComponents();

    configurarHistorialCortes();

    SimpleDateFormat formato =
        new SimpleDateFormat("dd/MM/yyyy");

    String hoy = formato.format(new Date());

    txtFechaInicio.setText(hoy);
    txtFechaFin.setText(hoy);

    txtFechaInicio.setToolTipText("Formato: dd/MM/yyyy");
    txtFechaFin.setToolTipText("Formato: dd/MM/yyyy");

    // Estos filtros estaban desactivados en el codigo original.
    txtFechaInicio.setEnabled(true);
    txtFechaFin.setEnabled(true);
    cmbCajero.setEnabled(true);
    btnBuscar.setEnabled(true);
    cargarCajeros();

    tablaHistorialCortes.addMouseListener(
        new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                if (evt.getClickCount() != 2
                        || !SwingUtilities.isLeftMouseButton(evt)) {
                    return;
                }

                int fila = tablaHistorialCortes.rowAtPoint(
                    evt.getPoint()
                );

                if (fila == -1) {
                    return;
                }

                verVentasCorteSeleccionado(fila);
            }
        }
    );

        configurarPeriodoCortes();
        actualizarDatos();

}
private void configurarPeriodoCortes() {
    prepararCampoFecha(txtFechaInicio);
    prepararCampoFecha(txtFechaFin);

    cmbCajero.setEnabled(true);
    btnBuscar.setEnabled(true);

    cmbPeriodoCortes.setModel(
        new javax.swing.DefaultComboBoxModel<>(
            new String[]{
                "Todas las fechas",
                "Hoy",
                "Ayer",
                "Este mes",
                "Personalizado"
            }
        )
    );

    // Al entrar, mostrar todas las fechas.
    cmbPeriodoCortes.setSelectedIndex(0);
    aplicarPeriodoCortes();

    cmbPeriodoCortes.addActionListener(e -> {
        aplicarPeriodoCortes();
    });
}

private void aplicarPeriodoCortes() {
    int opcion = cmbPeriodoCortes.getSelectedIndex();
    LocalDate hoy = LocalDate.now();

    boolean personalizado = opcion == 4;

    txtFechaInicio.setEnabled(personalizado);
    txtFechaFin.setEnabled(personalizado);

    // Sin limite de fechas.
    if (opcion == 0) {
        txtFechaInicio.setText("");
        txtFechaFin.setText("");
        return;
    }

    // Permitir elegir las fechas con el calendario.
    if (personalizado) {
        if (txtFechaInicio.getText().trim().isEmpty()) {
            txtFechaInicio.setText(
                hoy.format(FORMATO_FECHA)
            );
        }

        if (txtFechaFin.getText().trim().isEmpty()) {
            txtFechaFin.setText(
                hoy.format(FORMATO_FECHA)
            );
        }

        return;
    }

    LocalDate inicio = hoy;
    LocalDate fin = hoy;

    if (opcion == 2) {
        // Ayer.
        inicio = hoy.minusDays(1);
        fin = inicio;

    } else if (opcion == 3) {
        // Desde el primer dia del mes hasta hoy.
        inicio = hoy.withDayOfMonth(1);
    }

    txtFechaInicio.setText(
        inicio.format(FORMATO_FECHA)
    );

    txtFechaFin.setText(
        fin.format(FORMATO_FECHA)
    );
}
public void actualizarDatos() {
    cargarHistorialCortes();
}
 private void configurarHistorialCortes() {

    javax.swing.table.TableCellRenderer dosLineas =
            new javax.swing.table.TableCellRenderer() {

        private final javax.swing.JTextArea texto =
                new javax.swing.JTextArea();

        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable tabla,
                Object valor,
                boolean seleccionado,
                boolean tieneFoco,
                int fila,
                int columna) {

            texto.setText(valor == null ? "" : valor.toString());
            texto.setFont(tabla.getFont());
            texto.setOpaque(true);
            texto.setLineWrap(false);
            texto.setBorder(
                javax.swing.BorderFactory.createEmptyBorder(4, 6, 4, 6)
            );

            texto.setBackground(
                seleccionado
                    ? tabla.getSelectionBackground()
                    : tabla.getBackground()
            );

            texto.setForeground(
                seleccionado
                    ? tabla.getSelectionForeground()
                    : tabla.getForeground()
            );

            return texto;
        }
    };

    tablaHistorialCortes.getColumnModel()
            .getColumn(1).setCellRenderer(dosLineas);

    tablaHistorialCortes.getColumnModel()
            .getColumn(2).setCellRenderer(dosLineas);

    int altoLinea = tablaHistorialCortes
            .getFontMetrics(tablaHistorialCortes.getFont())
            .getHeight();

    tablaHistorialCortes.setRowHeight(altoLinea * 2 + 10);

    // Permitir desplazamiento horizontal si falta espacio
    tablaHistorialCortes.setAutoResizeMode(
    javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS
);

    int[] anchos = {60, 250, 300, 150, 170, 110};

    for (int i = 0; i < anchos.length; i++) {
        tablaHistorialCortes.getColumnModel()
                .getColumn(i).setPreferredWidth(anchos[i]);
    }

    jScrollPane2.getViewport().setBackground(Color.WHITE);
}
    private void verVentasCorteSeleccionado(int filaVista) {

    int filaModelo =
            tablaHistorialCortes.convertRowIndexToModel(filaVista);

    javax.swing.table.TableModel historial =
            tablaHistorialCortes.getModel();

    int idCorte = ((Number)
            historial.getValueAt(filaModelo, 0)).intValue();

    String totalGuardado =
            String.valueOf(historial.getValueAt(filaModelo, 3));

    DefaultTableModel modelo = new DefaultTableModel(
        new Object[]{
            "ID", "FECHA/HORA", "CAJERO", "CATEGORIA",
            "TOTAL", "EFECTIVO", "CAMBIO", "ESTADO"
        }, 0
    ) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    // En el historial se muestran activos y cancelados
    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps =
                conn.prepareStatement(sqlVentasDelCorte(false))) {

        ps.setInt(1, idCorte);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id_pedido"),
                    formatearFechaHora(rs.getTimestamp("fecha")),
                    rs.getString("cajero"),
                    rs.getString("categorias"),
                    "$" + rs.getBigDecimal("total").toPlainString(),
                    "$" + rs.getBigDecimal("efectivo").toPlainString(),
                    "$" + rs.getBigDecimal("cambio").toPlainString(),
                    rs.getString("estado")
                });
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al consultar las ventas del corte:\n"
            + e.getMessage()
        );
        e.printStackTrace();
        return;
    }

    javax.swing.JTable tabla = new javax.swing.JTable(modelo);
    tabla.setRowHeight(26);
    tabla.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

    int[] anchos = {60, 170, 190, 180, 100, 100, 100, 110};

    for (int i = 0; i < anchos.length; i++) {
        tabla.getColumnModel().getColumn(i)
                .setPreferredWidth(anchos[i]);
    }

    JScrollPane scroll = new JScrollPane(tabla);
    scroll.setPreferredSize(new Dimension(850, 350));
    scroll.getViewport().setBackground(Color.WHITE);

    JLabel resumen = new JLabel(
        "Corte #" + idCorte
        + " | Total guardado al cerrar: " + totalGuardado
    );

    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.add(resumen, BorderLayout.NORTH);
    panel.add(scroll, BorderLayout.CENTER);

    if (modelo.getRowCount() == 0) {
        panel.add(
            new JLabel(
                "No hay ventas vinculadas. Puede ser un corte sin ventas "
                + "o anterior al uso de id_corte."
            ),
            BorderLayout.SOUTH
        );
    }

    JOptionPane.showMessageDialog(
        this,
        panel,
        "Ventas del corte #" + idCorte,
        JOptionPane.PLAIN_MESSAGE
    );
    }
  private void cargarHistorialCortes() {

    LocalDate inicio;
    LocalDate fin;

    try {
        inicio = leerFechaFiltro(
            txtFechaInicio.getText(), "inicio"
        );

        fin = leerFechaFiltro(
            txtFechaFin.getText(), "fin"
        );

        if (inicio != null && fin != null
                && inicio.isAfter(fin)) {

            throw new IllegalArgumentException(
                "La fecha de inicio no puede ser posterior a la de fin."
            );
        }

    } catch (IllegalArgumentException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
        return;
    }

    Integer idUsuario = null;

    // El combo contiene: Todos, 1 - Nombre, 2 - Nombre...
    if (cmbCajero.getSelectedIndex() > 0) {

        String seleccionado = String.valueOf(
            cmbCajero.getSelectedItem()
        );

        try {
            idUsuario = Integer.valueOf(
                seleccionado.split(" - ", 2)[0].trim()
            );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this, "Selecciona un usuario valido."
            );
            return;
        }
    }

    String sql =
        "SELECT c.id_corte, c.fecha_apertura, c.fecha_cierre, "
        + "c.total_ventas, c.total_pedidos, c.estado, "
        + "ua.nombre_completo AS usuario_apertura, "
        + "uc.nombre_completo AS usuario_cierre "
        + "FROM cortes_caja c "
        + "LEFT JOIN usuarios ua "
        + "ON c.id_usuario_apertura = ua.id_usuario "
        + "LEFT JOIN usuarios uc "
        + "ON c.id_usuario_cierre = uc.id_usuario "
        + "WHERE c.estado = 'CERRADO' ";

    // Filtrar por la fecha de cierre.
    if (inicio != null) {
        sql += "AND c.fecha_cierre >= ? ";
    }

    if (fin != null) {
        sql += "AND c.fecha_cierre < ? ";
    }

    // Buscar al usuario que abrio o cerro el corte.
    if (idUsuario != null) {
        sql +=
            "AND (c.id_usuario_apertura = ? "
            + "OR c.id_usuario_cierre = ?) ";
    }

    sql += "ORDER BY c.fecha_cierre DESC, c.id_corte DESC";

    SimpleDateFormat formato =
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    java.util.List<Object[]> filas =
        new java.util.ArrayList<>();

    try (Connection conn = Conexion.getConexion()) {

        if (conn == null) {
            throw new SQLException(
                "No se pudo conectar con la base de datos."
            );
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            int parametro = 1;

            if (inicio != null) {
                ps.setTimestamp(
                    parametro++,
                    Timestamp.valueOf(inicio.atStartOfDay())
                );
            }

            if (fin != null) {
                // Incluye completo el dia de la fecha final.
                ps.setTimestamp(
                    parametro++,
                    Timestamp.valueOf(
                        fin.plusDays(1).atStartOfDay()
                    )
                );
            }

            if (idUsuario != null) {
                ps.setInt(parametro++, idUsuario);
                ps.setInt(parametro++, idUsuario);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Timestamp apertura =
                        rs.getTimestamp("fecha_apertura");

                    Timestamp cierre =
                        rs.getTimestamp("fecha_cierre");

                    String usuarioApertura =
                        rs.getString("usuario_apertura");

                    String usuarioCierre =
                        rs.getString("usuario_cierre");

                    java.math.BigDecimal total =
                        rs.getBigDecimal("total_ventas");

                    Object pedidos =
                        rs.getObject("total_pedidos");

                    String fechaApertura = apertura == null
                        ? "Sin registro"
                        : formato.format(apertura);

                    String fechaCierre = cierre == null
                        ? "Sin registro"
                        : formato.format(cierre);

                    if (usuarioApertura == null) {
                        usuarioApertura = "Sin registro";
                    }

                    if (usuarioCierre == null) {
                        usuarioCierre = "Sin registro";
                    }

                    filas.add(new Object[]{
                        rs.getInt("id_corte"),

                        "Apertura: " + fechaApertura
                            + "\nCierre: " + fechaCierre,

                        "Abrio: " + usuarioApertura
                            + "\nCerro: " + usuarioCierre,

                        total == null
                            ? "Sin registro"
                            : "$" + total.toPlainString(),

                        pedidos == null
                            ? "Sin registro"
                            : pedidos,

                        rs.getString("estado")
                    });
                }
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al buscar cortes:\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    DefaultTableModel modelo =
        (DefaultTableModel) tablaHistorialCortes.getModel();

    modelo.setRowCount(0);

    for (Object[] fila : filas) {
        modelo.addRow(fila);
    }
}
  private LocalDate leerFechaFiltro(String texto, String campo) {

    texto = texto.trim();

    // Un campo vacio significa que no hay limite en esa fecha.
    if (texto.isEmpty()) {
        return null;
    }

    DateTimeFormatter formato = DateTimeFormatter
        .ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT);

    try {
        return LocalDate.parse(texto, formato);

    } catch (DateTimeParseException e) {
        throw new IllegalArgumentException(
            "La fecha de " + campo + " no es valida.\n"
            + "Usa dd/MM/yyyy. Ejemplo: 21/09/2026."
        );
    }
}
   
private String sqlVentasDelCorte(boolean soloActivos) {

    return "SELECT p.id_pedido, p.fecha, p.total, p.efectivo, "
        + "COALESCE(p.cambio, p.efectivo - p.total) AS cambio, "
        + "p.estado, "
        + "COALESCE(u.nombre_completo, 'Sin registro') AS cajero, "

        + "COALESCE(("
        + "SELECT GROUP_CONCAT("
        + "DISTINCT NULLIF(TRIM(dp.categoria), '') "
        + "ORDER BY dp.categoria SEPARATOR ', ') "
        + "FROM detalles_pedido dp "
        + "WHERE dp.id_pedido = p.id_pedido"
        + "), 'Sin categoria') AS categorias, "

        + "(SELECT COALESCE(SUM(dp.cantidad), 0) "
        + "FROM detalles_pedido dp "
        + "WHERE dp.id_pedido = p.id_pedido) AS productos "

        + "FROM pedidos p "
        + "LEFT JOIN usuarios u ON p.id_usuario = u.id_usuario "
        + "WHERE p.id_corte = ? "
        + (soloActivos ? "AND p.estado = 'ACTIVO' " : "")
        + "ORDER BY p.fecha DESC, p.id_pedido DESC";
}
private void prepararCampoFecha(javax.swing.JTextField campo) {
    campo.setEditable(false);
    campo.setBackground(java.awt.Color.WHITE);
    campo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
    campo.setToolTipText("Haz clic para elegir una fecha");
    campo.setCursor(
        java.awt.Cursor.getPredefinedCursor(
            java.awt.Cursor.HAND_CURSOR
        )
    );

    campo.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            if (campo.isEnabled()
                    && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                mostrarCalendario(campo);
            }
        }
    });

    // Tambien permite abrir el calendario con Enter.
    campo.getInputMap().put(
        javax.swing.KeyStroke.getKeyStroke("ENTER"),
        "abrirCalendario"
    );

    campo.getActionMap().put(
        "abrirCalendario",
        new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                mostrarCalendario(campo);
            }
        }
    );
}
private String formatearFechaHora(java.sql.Timestamp fecha) {
    if (fecha == null) return "";
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    return sdf.format(fecha);
}
// ============================================================
// CARGAR CAJEROS EN EL COMBOBOX
// ============================================================
private void cargarCajeros() {
    cmbCajero.removeAllItems();
    cmbCajero.addItem("Todos");
    
    java.sql.Connection conn = null;
    java.sql.PreparedStatement ps = null;
    java.sql.ResultSet rs = null;
    
    try {
        conn = Conexion.getConexion();
        String sql = "SELECT id_usuario, nombre_completo FROM usuarios WHERE activo = 1 AND rol IN ('ADMIN', 'CAJERO') ORDER BY nombre_completo";
        ps = conn.prepareStatement(sql);
        rs = ps.executeQuery();
        
        while (rs.next()) {
            int id = rs.getInt("id_usuario");
            String nombre = rs.getString("nombre_completo");
            cmbCajero.addItem(id + " - " + nombre);
        }
        
    } catch (java.sql.SQLException e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error al cargar cajeros: " + e.getMessage());
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
private void mostrarCalendario(javax.swing.JTextField campo) {
    LocalDate fechaInicial;

    try {
        fechaInicial = LocalDate.parse(
            campo.getText().trim(), FORMATO_FECHA
        );
    } catch (DateTimeParseException e) {
        fechaInicial = LocalDate.now();
    }

    final LocalDate fechaSeleccionada = fechaInicial;
    final YearMonth[] mesActual = {
        YearMonth.from(fechaInicial)
    };

    javax.swing.JDialog ventana = new javax.swing.JDialog(
        javax.swing.SwingUtilities.getWindowAncestor(this),
        "Seleccionar fecha",
        java.awt.Dialog.ModalityType.APPLICATION_MODAL
    );

    javax.swing.JPanel contenido = new javax.swing.JPanel(
        new java.awt.BorderLayout(8, 8)
    );

    contenido.setBorder(
        javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12)
    );

    javax.swing.JPanel encabezado = new javax.swing.JPanel(
        new java.awt.BorderLayout(8, 0)
    );

    javax.swing.JButton anterior = new javax.swing.JButton("<");
    javax.swing.JButton siguiente = new javax.swing.JButton(">");

    anterior.setToolTipText("Mes anterior");
    siguiente.setToolTipText("Mes siguiente");

    javax.swing.JLabel titulo = new javax.swing.JLabel(
        "", javax.swing.SwingConstants.CENTER
    );

    encabezado.add(anterior, java.awt.BorderLayout.WEST);
    encabezado.add(titulo, java.awt.BorderLayout.CENTER);
    encabezado.add(siguiente, java.awt.BorderLayout.EAST);

    javax.swing.JPanel dias = new javax.swing.JPanel(
        new java.awt.GridLayout(7, 7, 4, 4)
    );

    dias.setPreferredSize(new java.awt.Dimension(350, 250));

    DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern(
        "MMMM uuuu", new java.util.Locale("es", "MX")
    );

    Runnable dibujarMes = () -> {
        dias.removeAll();

        titulo.setText(
            mesActual[0].atDay(1).format(formatoMes)
        );

        String[] nombres = {
            "Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"
        };

        for (String nombre : nombres) {
            dias.add(new javax.swing.JLabel(
                nombre, javax.swing.SwingConstants.CENTER
            ));
        }

        int espacios = mesActual[0].atDay(1)
                .getDayOfWeek().getValue() - 1;

        for (int i = 0; i < espacios; i++) {
            dias.add(new javax.swing.JLabel(""));
        }

        for (int dia = 1;
                dia <= mesActual[0].lengthOfMonth();
                dia++) {

            final LocalDate fecha = mesActual[0].atDay(dia);

            javax.swing.JButton botonDia =
                    new javax.swing.JButton(String.valueOf(dia));

            botonDia.setMargin(new java.awt.Insets(2, 2, 2, 2));

            if (fecha.equals(fechaSeleccionada)) {
                botonDia.setBorder(
                    javax.swing.BorderFactory.createLineBorder(
                        new java.awt.Color(33, 150, 243), 2
                    )
                );
            }

            if (fecha.equals(LocalDate.now())) {
                botonDia.setFont(
                    botonDia.getFont().deriveFont(java.awt.Font.BOLD)
                );
                botonDia.setToolTipText("Hoy");
            }

            botonDia.addActionListener(e -> {
                campo.setText(fecha.format(FORMATO_FECHA));
                ventana.dispose();
            });

            dias.add(botonDia);
        }

        // Siete encabezados y seis semanas.
        while (dias.getComponentCount() < 49) {
            dias.add(new javax.swing.JLabel(""));
        }

        dias.revalidate();
        dias.repaint();
    };

    anterior.addActionListener(e -> {
        mesActual[0] = mesActual[0].minusMonths(1);
        dibujarMes.run();
    });

    siguiente.addActionListener(e -> {
        mesActual[0] = mesActual[0].plusMonths(1);
        dibujarMes.run();
    });

    javax.swing.JButton cerrar = new javax.swing.JButton("Cerrar");
    cerrar.addActionListener(e -> ventana.dispose());

    contenido.add(encabezado, java.awt.BorderLayout.NORTH);
    contenido.add(dias, java.awt.BorderLayout.CENTER);
    contenido.add(cerrar, java.awt.BorderLayout.SOUTH);

    ventana.setContentPane(contenido);
    ventana.setDefaultCloseOperation(
        javax.swing.WindowConstants.DISPOSE_ON_CLOSE
    );

    dibujarMes.run();

    ventana.pack();
    ventana.setResizable(false);
    ventana.setLocationRelativeTo(this);
    ventana.setVisible(true);
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtFechaInicio = new javax.swing.JTextField();
        txtFechaFin = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        cmbCajero = new javax.swing.JComboBox<>();
        btnActualizar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaHistorialCortes = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cmbPeriodoCortes = new javax.swing.JComboBox<>();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(1100, 720));
        setMinimumSize(new java.awt.Dimension(1100, 720));
        setPreferredSize(new java.awt.Dimension(1100, 720));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtFechaInicio.setMaximumSize(new java.awt.Dimension(110, 22));
        txtFechaInicio.setMinimumSize(new java.awt.Dimension(110, 22));
        txtFechaInicio.setPreferredSize(new java.awt.Dimension(110, 22));
        txtFechaInicio.setVerifyInputWhenFocusTarget(false);
        add(txtFechaInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 50, 110, -1));

        txtFechaFin.setMaximumSize(new java.awt.Dimension(110, 22));
        txtFechaFin.setMinimumSize(new java.awt.Dimension(110, 22));
        txtFechaFin.setPreferredSize(new java.awt.Dimension(110, 22));
        txtFechaFin.addActionListener(this::txtFechaFinActionPerformed);
        add(txtFechaFin, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 50, -1, -1));

        btnBuscar.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 30, -1, 50));

        cmbCajero.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos" }));
        cmbCajero.addActionListener(this::cmbCajeroActionPerformed);
        add(cmbCajero, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 50, -1, -1));

        btnActualizar.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(this::btnActualizarActionPerformed);
        add(btnActualizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 40, -1, -1));

        tablaHistorialCortes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "FECHA Y HORA", "USUARIO QUE ABRIO Y CERRO", "TOTAL DE VENTAS ", "CANTIDAD DE PEDIDOS", "ESTADO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tablaHistorialCortes);

        add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 98, 1066, 600));

        jLabel1.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel1.setText("INICIO:");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 50, -1, -1));

        jLabel2.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel2.setText("FIN:");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 50, -1, -1));

        jLabel3.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel3.setText("Usuarios:");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 30, -1, -1));

        jLabel4.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel4.setText("Periodo:");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 30, -1, -1));

        cmbPeriodoCortes.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cmbPeriodoCortes, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 50, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void txtFechaFinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFechaFinActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFechaFinActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed

       cargarHistorialCortes();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void cmbCajeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCajeroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCajeroActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
      actualizarDatos();
    }//GEN-LAST:event_btnActualizarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JComboBox<String> cmbCajero;
    private javax.swing.JComboBox<String> cmbPeriodoCortes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tablaHistorialCortes;
    private javax.swing.JTextField txtFechaFin;
    private javax.swing.JTextField txtFechaInicio;
    // End of variables declaration//GEN-END:variables
}
