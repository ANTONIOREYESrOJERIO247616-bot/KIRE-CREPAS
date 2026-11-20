
package VISTA;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import Conexion.Conexion;
import DETALLES.tablaHisVe.WrapEstadoRenderer;
import java.awt.BorderLayout;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
public class Historial_Ventas extends javax.swing.JPanel {
     private boolean ajusteAltosPendiente = false;
public Historial_Ventas() {
    
    initComponents();
  tablaHistorial.setShowHorizontalLines(false);
tablaHistorial.setShowVerticalLines(true);  
tablaHistorial.setGridColor(java.awt.Color.BLACK);
tablaHistorial.setShowGrid(true);
tablaHistorial.setShowHorizontalLines(true);
tablaHistorial.setShowVerticalLines(true);
    tablaHistorial.getTableHeader().setReorderingAllowed(false);

    jScrollPane1.getViewport().setBackground(
        java.awt.Color.WHITE
    );

    // Ocultar Observaciones, conservandola en el modelo.
    tablaHistorial.removeColumn(
        tablaHistorial.getColumnModel().getColumn(10)
    );
    // Mover visualmente ENVIO $ junto a Subtotal.
int columnaEnvio = tablaHistorial.convertColumnIndexToView(11);

tablaHistorial.moveColumn(columnaEnvio, 7);


    // Registrar una sola vez el doble clic.
    tablaHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getClickCount() != 2
                    || !SwingUtilities.isLeftMouseButton(evt)) {
                return;
            }

            int filaVista = tablaHistorial.rowAtPoint(evt.getPoint());

            if (filaVista == -1) {
                return;
            }

            tablaHistorial.setRowSelectionInterval(
                filaVista, filaVista
            );

            int filaModelo =
                tablaHistorial.convertRowIndexToModel(filaVista);

            String estado = String.valueOf(
                tablaHistorial.getModel().getValueAt(filaModelo, 9)
            ).trim();

            if ("CANCELADO".equalsIgnoreCase(estado)) {
                mostrarMotivoCancelacion(filaVista);
            } else if ("ACTIVO".equalsIgnoreCase(estado)) {
                verDetallePedido();
            }
        }
    });

    for (int i = 0; i < tablaHistorial.getColumnCount(); i++) {
        tablaHistorial.getColumnModel().getColumn(i)
            .setCellRenderer(new WrapEstadoRenderer(9));
    }

// Recalcular cuando cambie el ancho de las columnas.
tablaHistorial.getColumnModel().addColumnModelListener(
    new javax.swing.event.TableColumnModelListener() {

        @Override
        public void columnMarginChanged(
                javax.swing.event.ChangeEvent e) {
            recalcularTodosLosAltos();
        }

        @Override
        public void columnAdded(
                javax.swing.event.TableColumnModelEvent e) {
        }

        @Override
        public void columnRemoved(
                javax.swing.event.TableColumnModelEvent e) {
        }

        @Override
        public void columnMoved(
                javax.swing.event.TableColumnModelEvent e) {
        }

        @Override
        public void columnSelectionChanged(
                javax.swing.event.ListSelectionEvent e) {
        }
    }
);

// Recalcular cuando cambie el tamaño de la tabla.
tablaHistorial.addComponentListener(
    new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(
                java.awt.event.ComponentEvent e) {
            recalcularTodosLosAltos();
        }
    }
);

// Tambien responder si cambia la fuente durante la ejecucion.
tablaHistorial.addPropertyChangeListener(
    "font",
    e -> recalcularTodosLosAltos()
);
   configurarFechas();
}
private static final DateTimeFormatter FORMATO_FECHA =
        DateTimeFormatter.ofPattern("dd/MM/uuuu")
                .withResolverStyle(ResolverStyle.STRICT);

private void configurarFechas() {
    prepararCampoFecha(txtFechaInicio);
    prepararCampoFecha(txtFechaFin);

    btnHoy.addActionListener(e -> {
        LocalDate hoy = LocalDate.now();
        establecerPeriodo(hoy, hoy);
    });

    btnAyer.addActionListener(e -> {
        LocalDate ayer = LocalDate.now().minusDays(1);
        establecerPeriodo(ayer, ayer);
    });

    btnEsteMes.addActionListener(e -> {
        LocalDate hoy = LocalDate.now();
        establecerPeriodo(hoy.withDayOfMonth(1), hoy);
    });

    LocalDate hoy = LocalDate.now();
    establecerPeriodo(hoy, hoy);
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

private void establecerPeriodo(LocalDate inicio, LocalDate fin) {
    txtFechaInicio.setText(inicio.format(FORMATO_FECHA));
    txtFechaFin.setText(fin.format(FORMATO_FECHA));

    aplicarFiltroFechas();
}

private void aplicarFiltroFechas() {
    try {
        LocalDate inicio = LocalDate.parse(
            txtFechaInicio.getText().trim(), FORMATO_FECHA
        );

        LocalDate fin = LocalDate.parse(
            txtFechaFin.getText().trim(), FORMATO_FECHA
        );

        if (inicio.isAfter(fin)) {
            JOptionPane.showMessageDialog(
                this,
                "La fecha de inicio no puede ser mayor que la fecha final.",
                "Revisa las fechas",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // LocalDate.toString() produce yyyy-MM-dd.
        cargarVentas(inicio.toString(), fin.toString());

    } catch (DateTimeParseException e) {
        JOptionPane.showMessageDialog(
            this,
            "Selecciona una fecha de inicio y una fecha de fin validas."
        );
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
    private void cargarVentas() {
    cargarVentas(null, null);
}
    private void verDetallePedido() {
  int fila = tablaHistorial.getSelectedRow();

if (fila == -1) {
    JOptionPane.showMessageDialog(
        this,
        "Selecciona un pedido para ver el detalle."
    );
    return;
}

int filaModelo = tablaHistorial.convertRowIndexToModel(fila);

int idPedido = ((Number) tablaHistorial.getModel()
    .getValueAt(filaModelo, 0)).intValue();


try {
    StringBuilder texto = new StringBuilder();
    texto.append("DETALLE DEL PEDIDO #").append(idPedido).append("\n\n");

    String sql =
        "SELECT dp.id_detalle, dp.categoria, dp.producto, "
        + "dp.cantidad, dp.subtotal, p.fecha, p.estado, p.total, "
        + "p.tipo_entrega, p.costo_envio, "
        + "COALESCE(u.nombre_completo, 'N/A') AS vendedor, "
        + "COALESCE(p.observaciones, '') AS observaciones "
        + "FROM detalles_pedido dp "
        + "INNER JOIN pedidos p ON p.id_pedido = dp.id_pedido "
        + "LEFT JOIN usuarios u ON u.id_usuario = p.id_usuario "
        + "WHERE dp.id_pedido = ? ORDER BY dp.id_detalle";

    java.math.BigDecimal total = java.math.BigDecimal.ZERO;
    java.math.BigDecimal costoEnvio = java.math.BigDecimal.ZERO;
    int numero = 0;

    // Cerrar la conexion antes de mostrar la ventana.
    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, idPedido);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (numero == 0) {
                    Date fecha = rs.getTimestamp("fecha");

                    texto.append("Fecha: ")
                        .append(fecha == null ? "" :
                            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                                .format(fecha))
                        .append("\nCajero: ")
                        .append(rs.getString("vendedor"))
                        .append("\nEstado: ")
                        .append(rs.getString("estado"))
                        .append("\n");

                    String motivo = rs.getString("observaciones");

                    if ("CANCELADO".equalsIgnoreCase(rs.getString("estado"))
                            && !motivo.trim().isEmpty()) {
                        texto.append("Motivo: ").append(motivo).append("\n");
                    }

                    total = rs.getBigDecimal("total");
                    costoEnvio = rs.getBigDecimal("costo_envio");

                    texto.append("Tipo de entrega: ")
                    .append(rs.getString("tipo_entrega"))
                    .append("\n");
                    texto.append("\nPRODUCTOS:\n\n");
                }

                numero++;

                texto.append(numero).append(". Categoria: ")
                    .append(rs.getString("categoria"))
                    .append("\n   Producto: ")
                    .append(rs.getString("producto")
                        .replaceAll("(?i)<br\\s*/?>", "\n   "))
                    .append("\n   Cantidad: ")
                    .append(rs.getInt("cantidad"))
                    .append("\n   Extras:\n      ")
                    .append(consultarExtras(conn, rs.getInt("id_detalle"))
                        .replace("\n", "\n      "))
                    .append("\n   Subtotal con extras: $")
                    .append(rs.getBigDecimal("subtotal").toPlainString())
                    .append("\n\n");
            }
        }
    }

    if (numero == 0) {
        JOptionPane.showMessageDialog(
            this,
            "No se encontraron productos para este pedido."
        );
        return;
    }

    texto.append("PRODUCTOS Y EXTRAS: $")
    .append(total.subtract(costoEnvio).toPlainString())
    .append("\nENVIO: $")
    .append(costoEnvio.toPlainString())
    .append("\nTOTAL DEL PEDIDO: $")
    .append(total.toPlainString());

    JTextArea area = new JTextArea(texto.toString());
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
    area.setCaretPosition(0);

    JScrollPane scroll = new JScrollPane(area);
    scroll.setPreferredSize(new java.awt.Dimension(570, 450));

    JOptionPane.showMessageDialog(
        this,
        scroll,
        "Detalle del Pedido #" + idPedido,
        JOptionPane.PLAIN_MESSAGE
    );

} catch (SQLException e) {
    JOptionPane.showMessageDialog(
        this,
        "Error al cargar detalle:\n" + e.getMessage()
    );
} 
}
    private String consultarExtras(Connection conn, int idDetalle)
        throws SQLException {

    String sql =
        "SELECT nombre, precio_unitario "
        + "FROM detalles_pedido_extras "
        + "WHERE id_detalle = ? ORDER BY id_detalle_extra";

    StringBuilder texto = new StringBuilder();

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, idDetalle);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (texto.length() > 0) {
                    texto.append("\n");
                }

                java.math.BigDecimal precio =
                        rs.getBigDecimal("precio_unitario");

                texto.append(rs.getString("nombre"))
                    .append(" ($")
                    .append(precio.toPlainString())
                    .append(")");
            }
        }
    }

    return texto.length() == 0
            ? "Sin extras registrados"
            : texto.toString();
}

private String extrasParaTabla(String texto) {
    return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
}
  
   private void mostrarMotivoCancelacion(int filaVista) {
    int filaModelo = tablaHistorial.convertRowIndexToModel(filaVista);

    javax.swing.table.TableModel modelo = tablaHistorial.getModel();

    String estado = String.valueOf(
        modelo.getValueAt(filaModelo, 9)
    );

    if (!"CANCELADO".equalsIgnoreCase(estado.trim())) {
        return;
    }

    Object idPedido = modelo.getValueAt(filaModelo, 0);
    Object valorMotivo = modelo.getValueAt(filaModelo, 10);

    String motivo = valorMotivo == null
            ? ""
            : valorMotivo.toString().trim();

    if (motivo.isEmpty()) {
        motivo = "Este pedido fue cancelado sin un motivo registrado.";
    }

    javax.swing.JTextArea texto = new javax.swing.JTextArea(motivo);
    texto.setEditable(false);
    texto.setLineWrap(true);
    texto.setWrapStyleWord(true);
    texto.setFont(tablaHistorial.getFont());
    texto.setCaretPosition(0);

    javax.swing.JScrollPane scroll =
            new javax.swing.JScrollPane(texto);

    scroll.setPreferredSize(new java.awt.Dimension(400, 180));

    javax.swing.JOptionPane.showMessageDialog(
        this,
        scroll,
        "Motivo de cancelacion - Pedido #" + idPedido,
        javax.swing.JOptionPane.INFORMATION_MESSAGE
    );
}
 private void cargarVentas(String fechaInicio, String fechaFin) {
    DefaultTableModel modelo =
        (DefaultTableModel) tablaHistorial.getModel();

    modelo.setRowCount(0);

    StringBuilder sql = new StringBuilder(
        "SELECT p.id_pedido, p.fecha, dp.id_detalle, "
        + "dp.categoria, dp.producto, dp.cantidad, dp.subtotal, "
        + "p.total, p.estado, p.costo_envio, "
        + "COALESCE(u.nombre_completo, 'N/A') AS vendedor, "
        + "COALESCE(p.observaciones, '') AS observaciones "
        + "FROM pedidos p "
        + "INNER JOIN detalles_pedido dp "
        + "ON p.id_pedido = dp.id_pedido "
        + "LEFT JOIN usuarios u ON p.id_usuario = u.id_usuario "
        + "WHERE 1 = 1 "
    );

    boolean filtrarInicio =
        fechaInicio != null && !fechaInicio.isEmpty();

    boolean filtrarFin =
        fechaFin != null && !fechaFin.isEmpty();

    if (filtrarInicio) {
        sql.append("AND DATE(p.fecha) >= ? ");
    }

    if (filtrarFin) {
        sql.append("AND DATE(p.fecha) <= ? ");
    }

    sql.append(
        "ORDER BY p.fecha DESC, p.id_pedido DESC, dp.id_detalle ASC"
    );

    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql.toString())) {

        int parametro = 1;

        if (filtrarInicio) {
            ps.setString(parametro++, fechaInicio);
        }

        if (filtrarFin) {
            ps.setString(parametro++, fechaFin);
        }

        SimpleDateFormat formato =
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        java.util.Set<Integer> pedidosMostrados =
            new java.util.HashSet<>();

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idPedido = rs.getInt("id_pedido");

                Date fecha = rs.getTimestamp("fecha");
                boolean primerProducto = pedidosMostrados.add(idPedido);

String totalMostrar = primerProducto
        ? "$" + rs.getBigDecimal("total").toPlainString()
        : "";

String envioMostrar = primerProducto
        ? "$" + rs.getBigDecimal("costo_envio").toPlainString()
        : "";
                        

                String extras = consultarExtras(
                    conn,
                    rs.getInt("id_detalle")
                );

                String categoria = rs.getString("categoria");

                modelo.addRow(new Object[]{
                    idPedido,
                    fecha == null ? "" : formato.format(fecha),
                    categoria == null ? "Sin categoria" : categoria,
                    rs.getString("producto"),
                    rs.getInt("cantidad"),
                    extrasParaTabla(extras),
                    "$" + rs.getBigDecimal("subtotal").toPlainString(),
                    totalMostrar,
                    rs.getString("vendedor"),
                    rs.getString("estado"),
                    rs.getString("observaciones"),
                    envioMostrar
                });
            }
        }

    } catch (SQLException e) {
        modelo.setRowCount(0);

        JOptionPane.showMessageDialog(
            this,
            "Error al cargar ventas:\n" + e.getMessage()
        );
    }

    actualizarResumen();
    recalcularTodosLosAltos();
} 
   
    /**
     * Actualiza el resumen estadístico de las ventas
     */
 private void actualizarResumen() {
    DefaultTableModel modelo =
            (DefaultTableModel) tablaHistorial.getModel();

    java.math.BigDecimal totalVentas =
            new java.math.BigDecimal("0.00");

    java.util.Set<Integer> pedidos = new java.util.HashSet<>();
    int totalProductos = 0;

    for (int i = 0; i < modelo.getRowCount(); i++) {
        String estado = String.valueOf(modelo.getValueAt(i, 9));

        if (!"ACTIVO".equalsIgnoreCase(estado)) {
            continue;
        }

        totalProductos +=
                ((Number) modelo.getValueAt(i, 4)).intValue();

        int idPedido =
                ((Number) modelo.getValueAt(i, 0)).intValue();

        // Total Pedido aparece solamente en una fila del pedido.
        Object valorTotal = modelo.getValueAt(i, 7);

        if (valorTotal == null) {
            continue;
        }

        String importe = valorTotal.toString()
                .replace("$", "")
                .trim();

        if (!importe.isEmpty() && pedidos.add(idPedido)) {
            totalVentas = totalVentas.add(
                new java.math.BigDecimal(importe)
            );
        }
    }

    rVenta.setText(
        "Total: $" + totalVentas.toPlainString()
        + "  |  Pedidos: " + pedidos.size()
        + "  |  Productos: " + totalProductos
    );
}
private void recalcularTodosLosAltos() {
    if (ajusteAltosPendiente) {
        return;
    }

    ajusteAltosPendiente = true;

    SwingUtilities.invokeLater(() -> {
        try {
            // Alto minimo configurado en el diseñador.
            int altoMinimo = tablaHistorial.getRowHeight();

            for (int fila = 0;
                    fila < tablaHistorial.getRowCount();
                    fila++) {

                int altoNecesario = altoMinimo;

                for (int columna = 0;
                        columna < tablaHistorial.getColumnCount();
                        columna++) {

                    int anchoCelda = tablaHistorial.getCellRect(
                        fila, columna, false
                    ).width;

                    if (anchoCelda <= 0) {
                        continue;
                    }

                    javax.swing.table.TableCellRenderer renderer =
                        tablaHistorial.getCellRenderer(fila, columna);

                    java.awt.Component componente =
                        tablaHistorial.prepareRenderer(
                            renderer, fila, columna
                        );

                    componente.setSize(
                        anchoCelda, Short.MAX_VALUE
                    );

                    int altoCelda =
                        componente.getPreferredSize().height
                        + tablaHistorial.getRowMargin();

                    altoNecesario = Math.max(
                        altoNecesario, altoCelda
                    );
                }

                if (tablaHistorial.getRowHeight(fila)
                        != altoNecesario) {

                    tablaHistorial.setRowHeight(
                        fila, altoNecesario
                    );
                }
            }
        } finally {
            ajusteAltosPendiente = false;
        }
    });
} 


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaHistorial = new javax.swing.JTable();
        btnExportarExcel = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rVenta = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtFechaInicio = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtFechaFin = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        btnBuscar = new javax.swing.JButton();
        btnHoy = new javax.swing.JButton();
        btnAyer = new javax.swing.JButton();
        btnEsteMes = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 204, 255));
        setMaximumSize(new java.awt.Dimension(1280, 800));
        setMinimumSize(new java.awt.Dimension(1280, 800));
        setPreferredSize(new java.awt.Dimension(1280, 800));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tablaHistorial.setFont(new java.awt.Font("Roboto Light", 1, 12)); // NOI18N
        tablaHistorial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Fecha/Hora", "Categoria", "Producto", "Cantidad ", "Extras", "Subtotal", "Total Pedido", "Vendedor", "Estado", "Observaciones ", "Envio $"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaHistorial.setRowHeight(25);
        jScrollPane1.setViewportView(tablaHistorial);
        if (tablaHistorial.getColumnModel().getColumnCount() > 0) {
            tablaHistorial.getColumnModel().getColumn(0).setMinWidth(50);
            tablaHistorial.getColumnModel().getColumn(0).setPreferredWidth(50);
            tablaHistorial.getColumnModel().getColumn(0).setMaxWidth(50);
            tablaHistorial.getColumnModel().getColumn(1).setMinWidth(150);
            tablaHistorial.getColumnModel().getColumn(1).setPreferredWidth(150);
            tablaHistorial.getColumnModel().getColumn(1).setMaxWidth(150);
            tablaHistorial.getColumnModel().getColumn(2).setMinWidth(60);
            tablaHistorial.getColumnModel().getColumn(2).setPreferredWidth(60);
            tablaHistorial.getColumnModel().getColumn(2).setMaxWidth(60);
            tablaHistorial.getColumnModel().getColumn(4).setMinWidth(60);
            tablaHistorial.getColumnModel().getColumn(4).setPreferredWidth(60);
            tablaHistorial.getColumnModel().getColumn(4).setMaxWidth(60);
            tablaHistorial.getColumnModel().getColumn(5).setMinWidth(100);
            tablaHistorial.getColumnModel().getColumn(5).setPreferredWidth(100);
            tablaHistorial.getColumnModel().getColumn(5).setMaxWidth(100);
            tablaHistorial.getColumnModel().getColumn(6).setMinWidth(60);
            tablaHistorial.getColumnModel().getColumn(6).setPreferredWidth(60);
            tablaHistorial.getColumnModel().getColumn(6).setMaxWidth(60);
            tablaHistorial.getColumnModel().getColumn(7).setMinWidth(120);
            tablaHistorial.getColumnModel().getColumn(7).setPreferredWidth(120);
            tablaHistorial.getColumnModel().getColumn(7).setMaxWidth(120);
            tablaHistorial.getColumnModel().getColumn(8).setMinWidth(150);
            tablaHistorial.getColumnModel().getColumn(8).setPreferredWidth(150);
            tablaHistorial.getColumnModel().getColumn(8).setMaxWidth(150);
            tablaHistorial.getColumnModel().getColumn(9).setMinWidth(80);
            tablaHistorial.getColumnModel().getColumn(9).setPreferredWidth(80);
            tablaHistorial.getColumnModel().getColumn(9).setMaxWidth(80);
            tablaHistorial.getColumnModel().getColumn(11).setMinWidth(50);
            tablaHistorial.getColumnModel().getColumn(11).setPreferredWidth(50);
            tablaHistorial.getColumnModel().getColumn(11).setMaxWidth(50);
        }

        btnExportarExcel.setText("EXPORTAR EXCEL");
        btnExportarExcel.addActionListener(this::btnExportarExcelActionPerformed);

        btnCancelar.setText("CANCELAR VENTA ");
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(btnExportarExcel)
                .addGap(45, 45, 45)
                .addComponent(btnCancelar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1252, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExportarExcel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 220, 1260, 560));

        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(this::btnActualizarActionPerformed);
        add(btnActualizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 150, 90, -1));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Roboto Black", 1, 48)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/VISTA/IMAGENES/ESTTT.jpg"))); // NOI18N
        jLabel1.setText("HISTORIAL DE VENTAS");

        rVenta.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rVenta.setText("Total:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(400, 400, 400)
                        .addComponent(rVenta)))
                .addContainerGap(579, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rVenta))
        );

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1280, 140));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setMaximumSize(new java.awt.Dimension(200, 100));
        jPanel4.setMinimumSize(new java.awt.Dimension(200, 100));

        jLabel2.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel2.setText("INICIO:");

        txtFechaInicio.setFont(new java.awt.Font("Roboto Light", 1, 12)); // NOI18N
        txtFechaInicio.addActionListener(this::txtFechaInicioActionPerformed);

        jLabel3.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel3.setText("FIN:");

        txtFechaFin.setFont(new java.awt.Font("Roboto Light", 1, 12)); // NOI18N
        txtFechaFin.addActionListener(this::txtFechaFinActionPerformed);

        jLabel4.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel4.setText("FECHA:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFechaInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFechaFin, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtFechaInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(txtFechaFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(56, Short.MAX_VALUE))
        );

        add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 150, 300, 60));

        btnBuscar.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 150, 90, 60));

        btnHoy.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnHoy.setText("HOY");
        add(btnHoy, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 140, -1, -1));

        btnAyer.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnAyer.setText("AYER");
        add(btnAyer, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 140, -1, -1));

        btnEsteMes.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnEsteMes.setText("ESTE MES");
        add(btnEsteMes, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 140, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void txtFechaInicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFechaInicioActionPerformed
         // Este evento se dispara cuando presionas Enter en el campo
    btnBuscar.doClick(); // Simula clic en el botón Buscar
    }//GEN-LAST:event_txtFechaInicioActionPerformed

    private void txtFechaFinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFechaFinActionPerformed
         // Este evento se dispara cuando presionas Enter en el campo
        btnBuscar.doClick(); // Simula clic en el botón Buscar
    }//GEN-LAST:event_txtFechaFinActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
    aplicarFiltroFechas();

    
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
      aplicarFiltroFechas();
    }//GEN-LAST:event_btnActualizarActionPerformed
/**
     * Convierte una fecha de formato dd/MM/yyyy a yyyy-MM-dd para SQL
     * @param fecha Fecha en formato dd/MM/yyyy
     * @return Fecha en formato yyyy-MM-dd o null si hay error
     */
   private String convertirFechaSQL(String fecha) {
    if (fecha == null || fecha.isEmpty()) return null;
    
    try {
        // Limpiar formato
        fecha = fecha.replace("-", "/").replace(".", "/");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        Date date = sdf.parse(fecha);
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    } catch (ParseException e) {
        return null;
    }
}
    
    /**
     * Valida el formato de una fecha
     * @param fecha Fecha a validar en formato dd/MM/yyyy
     * @return true si el formato es válido
     */
    private boolean validarFormatoFecha(String fecha) {
         if (fecha == null || fecha.isEmpty()) return true;
    
    try {
        // Limpiar formato
        fecha = fecha.replace("-", "/").replace(".", "/");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        sdf.parse(fecha);
        return true;
    } catch (ParseException e) {
        return false;
    }
}
    
    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
   
    int filaVista = tablaHistorial.getSelectedRow();

if (filaVista == -1) {
    JOptionPane.showMessageDialog(
        this, "Selecciona un pedido para cancelar"
    );
    return;
}

int filaModelo = tablaHistorial.convertRowIndexToModel(filaVista);

DefaultTableModel modelo =
        (DefaultTableModel) tablaHistorial.getModel();

int idPedido = ((Number) modelo.getValueAt(filaModelo, 0)).intValue();

String estado = String.valueOf(
    modelo.getValueAt(filaModelo, 9)
);

if (!"ACTIVO".equalsIgnoreCase(estado.trim())) {
    JOptionPane.showMessageDialog(
        this, "Solo puedes cancelar pedidos activos"
    );
    return;
}

// Pedir el motivo antes de modificar la venta
javax.swing.JTextArea campoMotivo =
        new javax.swing.JTextArea(5, 30);

campoMotivo.setLineWrap(true);
campoMotivo.setWrapStyleWord(true);

javax.swing.JScrollPane scroll =
        new javax.swing.JScrollPane(campoMotivo);

String motivo;

while (true) {
    int opcion = JOptionPane.showConfirmDialog(
        this,
        new Object[]{
            "Pedido #" + idPedido,
            "Escribe el motivo de cancelacion:",
            scroll,
            "Al aceptar se cancelara todo el pedido."
        },
        "Cancelar venta",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    if (opcion != JOptionPane.OK_OPTION) {
        return;
    }

    motivo = campoMotivo.getText().trim();

    if (motivo.isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "Debes escribir un motivo para cancelar la venta."
        );
        continue;
    }

    if (motivo.length() > 500) {
        JOptionPane.showMessageDialog(
            this, "El motivo debe tener como maximo 500 caracteres."
        );
        continue;
    }

    break;
}

// Guardar el motivo y el estado juntos
String sql = "UPDATE pedidos "
        + "SET estado = 'CANCELADO', observaciones = ? "
        + "WHERE id_pedido = ? AND estado = 'ACTIVO'";

try (Connection conn = Conexion.getConexion();
     PreparedStatement ps = conn.prepareStatement(sql)) {

    ps.setString(1, motivo);
    ps.setInt(2, idPedido);

    int actualizados = ps.executeUpdate();

    if (actualizados > 0) {
        JOptionPane.showMessageDialog(
            this,
            "Pedido #" + idPedido + " cancelado correctamente."
        );
    } else {
        JOptionPane.showMessageDialog(
            this,
            "El pedido ya no esta activo o no existe."
        );
    }

} catch (SQLException e) {
    JOptionPane.showMessageDialog(
        this, "Error al cancelar el pedido:\n" + e.getMessage()
    );
    e.printStackTrace();
    return;
}

// Recargar tambien las observaciones
String fechaInicioSQL =
        convertirFechaSQL(txtFechaInicio.getText().trim());

String fechaFinSQL =
        convertirFechaSQL(txtFechaFin.getText().trim());

cargarVentas(fechaInicioSQL, fechaFinSQL);

    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnExportarExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportarExcelActionPerformed
    // 1. Verificar que hay datos para exportar
    DefaultTableModel modelo = (DefaultTableModel) tablaHistorial.getModel();
    if (modelo.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, 
            "No hay datos para exportar", 
            "Exportar Excel", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // 2. Mostrar diálogo para elegir ubicación y nombre del archivo
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Guardar archivo Excel");
    fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
    fileChooser.setSelectedFile(new java.io.File("Historial_Ventas.xlsx"));
    
    int resultado = fileChooser.showSaveDialog(this);
    if (resultado != JFileChooser.APPROVE_OPTION) {
        return; // Usuario canceló
    }
    
    String rutaArchivo = fileChooser.getSelectedFile().getAbsolutePath();
    if (!rutaArchivo.endsWith(".xlsx")) {
        rutaArchivo += ".xlsx";
    }
    
    // 3. Crear el archivo Excel
    try {
        exportarDatosExcel(rutaArchivo, modelo);
        JOptionPane.showMessageDialog(this, 
            "✅ Archivo exportado correctamente:\n" + rutaArchivo,
            "Exportar Excel", 
            JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, 
            "Error al exportar: " + e.getMessage(),
            "Exportar Excel", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }


    }//GEN-LAST:event_btnExportarExcelActionPerformed
private void exportarDatosExcel(
        String rutaArchivo,
        DefaultTableModel modelo) throws IOException {

    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Historial de Ventas");

        String[] columnas = {
            "ID", "Fecha/Hora", "Categoria", "Producto", "Cantidad",
            "Extras", "Subtotal", "Total Pedido", "Vendedor",
            "Estado", "Observaciones"
        };

        // Anchos aproximados en caracteres.
        int[] anchos = {
            9, 22, 20, 42, 12, 48, 16, 18, 26, 16, 48
        };

        for (int i = 0; i < anchos.length; i++) {
            sheet.setColumnWidth(i, anchos[i] * 256);
        }

        org.apache.poi.ss.usermodel.Font fuenteDatos =
            workbook.createFont();

        fuenteDatos.setFontName("Arial");
        fuenteDatos.setFontHeightInPoints((short) 11);
        fuenteDatos.setColor(IndexedColors.BLACK.getIndex());

        org.apache.poi.ss.usermodel.Font fuenteNegrita =
            workbook.createFont();

        fuenteNegrita.setFontName("Arial");
        fuenteNegrita.setFontHeightInPoints((short) 11);
        fuenteNegrita.setColor(IndexedColors.BLACK.getIndex());
        fuenteNegrita.setBold(true);

        // Datos: fondo blanco y letras negras.
        CellStyle estiloDatos = workbook.createCellStyle();
        estiloDatos.setFont(fuenteDatos);
        estiloDatos.setFillForegroundColor(
            IndexedColors.WHITE.getIndex()
        );
        estiloDatos.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloDatos.setVerticalAlignment(VerticalAlignment.CENTER);
        estiloDatos.setWrapText(true);

        estiloDatos.setBorderTop(BorderStyle.THIN);
        estiloDatos.setBorderBottom(BorderStyle.THIN);
        estiloDatos.setBorderLeft(BorderStyle.THIN);
        estiloDatos.setBorderRight(BorderStyle.THIN);

        estiloDatos.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloDatos.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloDatos.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloDatos.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

        CellStyle estiloMoneda = workbook.createCellStyle();
        estiloMoneda.cloneStyleFrom(estiloDatos);
        estiloMoneda.setAlignment(HorizontalAlignment.RIGHT);
        estiloMoneda.setDataFormat(
            workbook.createDataFormat().getFormat("$#,##0.00")
        );

        CellStyle estiloEncabezado = workbook.createCellStyle();
        estiloEncabezado.cloneStyleFrom(estiloDatos);
        estiloEncabezado.setFont(fuenteNegrita);
        estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);
        estiloEncabezado.setFillForegroundColor(
            IndexedColors.GREY_25_PERCENT.getIndex()
        );

        CellStyle estiloTitulo = workbook.createCellStyle();
        estiloTitulo.setFont(fuenteNegrita);
        estiloTitulo.setAlignment(HorizontalAlignment.CENTER);

        Row titulo = sheet.createRow(0);
        titulo.setHeightInPoints(28);

        Cell celdaTitulo = titulo.createCell(0);
        celdaTitulo.setCellValue("HISTORIAL DE VENTAS");
        celdaTitulo.setCellStyle(estiloTitulo);

        sheet.addMergedRegion(
            new org.apache.poi.ss.util.CellRangeAddress(
                0, 0, 0, columnas.length - 1
            )
        );

        sheet.createRow(1).createCell(0).setCellValue(
            "Generado: "
            + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new Date())
        );

        sheet.addMergedRegion(
            new org.apache.poi.ss.util.CellRangeAddress(
                1, 1, 0, columnas.length - 1
            )
        );

        int filaExcel = 3;

        java.math.BigDecimal totalActivo = java.math.BigDecimal.ZERO;

        java.util.Set<Integer> pedidosActivos =
            new java.util.HashSet<>();

        java.util.Set<Integer> pedidosCancelados =
            new java.util.HashSet<>();

        // Primera seccion: activos. Segunda seccion: cancelados.
        String[] estados = {"ACTIVO", "CANCELADO"};

        for (String estadoSeccion : estados) {
            int columnasSeccion = "ACTIVO".equals(estadoSeccion)
    ? columnas.length - 1
    : columnas.length;
            Row seccion = sheet.createRow(filaExcel++);
            seccion.setHeightInPoints(25);

            Cell nombreSeccion = seccion.createCell(0);
            nombreSeccion.setCellValue(
                "ACTIVO".equals(estadoSeccion)
                    ? "PEDIDOS ACTIVOS"
                    : "PEDIDOS CANCELADOS"
            );
            nombreSeccion.setCellStyle(estiloTitulo);

           sheet.addMergedRegion(
    new org.apache.poi.ss.util.CellRangeAddress(
        seccion.getRowNum(),
        seccion.getRowNum(),
        0,
        columnasSeccion - 1
    )
);
           Row encabezado = sheet.createRow(filaExcel++);
encabezado.setHeightInPoints(30);

for (int j = 0; j < columnasSeccion; j++) {
    Cell celda = encabezado.createCell(j);
    celda.setCellValue(columnas[j]);
    celda.setCellStyle(estiloEncabezado);
}

            int filasSeccion = 0;

            for (int i = 0; i < modelo.getRowCount(); i++) {
                String estado = String.valueOf(
                    modelo.getValueAt(i, 9)
                ).trim();

                if (!estadoSeccion.equalsIgnoreCase(estado)) {
                    continue;
                }

                Row fila = sheet.createRow(filaExcel++);
                filasSeccion++;

                int lineasFila = 1;

                for (int j = 0; j < columnasSeccion; j++) {
                    Object valor = modelo.getValueAt(i, j);

                    String texto = valor == null
                        ? ""
                        : valor.toString();

                    // Producto y Extras pueden contener saltos HTML.
                    if (j == 3 || j == 5) {
                        texto = texto
                            .replaceAll("(?i)<br\\s*/?>", "\n")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("&amp;", "&");
                    }

                    Cell celda = fila.createCell(j);
                    celda.setCellStyle(estiloDatos);

                    if (j == 0 || j == 4) {
                        if (valor instanceof Number) {
                            celda.setCellValue(
                                ((Number) valor).doubleValue()
                            );
                        } else {
                            celda.setCellValue(texto);
                        }

                    } else if ((j == 6 || j == 7)
                            && !texto.trim().isEmpty()) {

                        java.math.BigDecimal importe =
                            new java.math.BigDecimal(
                                texto.replace("$", "")
                                    .trim()
                                    .replace(",", ".")
                            );

                        celda.setCellValue(importe.doubleValue());
                        celda.setCellStyle(estiloMoneda);

                    } else {
                        celda.setCellValue(texto);
                    }

                    // Estimar altura para mostrar textos de varias lineas.
                    int lineasCelda = 0;

                    for (String linea : texto.split("\\R", -1)) {
                        int capacidad = Math.max(1, anchos[j] - 3);

                        lineasCelda += Math.max(
                            1,
                            (int) Math.ceil(
                                (double) linea.length() / capacidad
                            )
                        );
                    }

                    lineasFila = Math.max(lineasFila, lineasCelda);
                }

                fila.setHeightInPoints(
                    Math.min(409f, Math.max(24f, lineasFila * 15f + 6f))
                );

                int idPedido =
                    ((Number) modelo.getValueAt(i, 0)).intValue();
                if ("ACTIVO".equals(estadoSeccion)) {
    Object valorTotal = modelo.getValueAt(i, 7);

    String importe = valorTotal == null
            ? ""
            : valorTotal.toString()
                    .replace("$", "")
                    .trim();

    // Sumar el total completo una sola vez por pedido.
    if (!importe.isEmpty() && pedidosActivos.add(idPedido)) {
        totalActivo = totalActivo.add(
            new java.math.BigDecimal(importe)
        );
    }

} else {
    pedidosCancelados.add(idPedido);
}

               
            }

            if (filasSeccion == 0) {
                Row vacia = sheet.createRow(filaExcel++);
                Cell mensaje = vacia.createCell(0);
                mensaje.setCellValue("Sin registros");
                mensaje.setCellStyle(estiloDatos);
            }

            filaExcel += 2;
        }

        Row filaTotal = sheet.createRow(filaExcel++);
        filaTotal.setHeightInPoints(30);

        Cell etiquetaTotal = filaTotal.createCell(5);
        etiquetaTotal.setCellValue("TOTAL VENTAS ACTIVAS");
        etiquetaTotal.setCellStyle(estiloEncabezado);

        Cell importeTotal = filaTotal.createCell(7);
        importeTotal.setCellValue(totalActivo.doubleValue());
        importeTotal.setCellStyle(estiloMoneda);

        Row cantidadActivos = sheet.createRow(filaExcel++);
        cantidadActivos.createCell(5).setCellValue("Pedidos activos");
        cantidadActivos.createCell(7).setCellValue(pedidosActivos.size());

        Row cantidadCancelados = sheet.createRow(filaExcel);
        cantidadCancelados.createCell(5).setCellValue("Pedidos cancelados");
        cantidadCancelados.createCell(7).setCellValue(pedidosCancelados.size());

        try (FileOutputStream salida = new FileOutputStream(rutaArchivo)) {
            workbook.write(salida);
        }
    }
    
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnAyer;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEsteMes;
    private javax.swing.JButton btnExportarExcel;
    private javax.swing.JButton btnHoy;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel rVenta;
    private javax.swing.JTable tablaHistorial;
    private javax.swing.JTextField txtFechaFin;
    private javax.swing.JTextField txtFechaInicio;
    // End of variables declaration//GEN-END:variables

}