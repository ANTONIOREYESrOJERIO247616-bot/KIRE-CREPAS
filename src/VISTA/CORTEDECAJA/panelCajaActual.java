
package VISTA.CORTEDECAJA;

import Conexion.Conexion;
import VISTA.Login;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.print.PrinterException;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
public class panelCajaActual extends javax.swing.JPanel {
private boolean corteAbierto = false;
   public panelCajaActual() {
    initComponents();

    jScrollPane1.getViewport().setBackground(Color.WHITE);

    configurarLabelsCorte();

    btnExportarExcel.addActionListener(e -> exportarExcel());
    btnImprimirReport.addActionListener(e -> generarReportePDF());
    
    tablaDetalleVentas.addMouseListener(
        new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                if (evt.getClickCount() != 2
                        || !SwingUtilities.isLeftMouseButton(evt)) {
                    return;
                }

                int fila = tablaDetalleVentas.rowAtPoint(
                    evt.getPoint()
                );

                if (fila == -1) {
                    return;
                }

                tablaDetalleVentas.setRowSelectionInterval(
                    fila, fila
                );

                verDetallePedido();
            }
        }
    );

    actualizarDatos();
}
   public void actualizarDatos() {
    actualizarEstadoCorte();
}
   // ============================================================
// CONFIGURAR TODOS LOS LABELS DEL CORTE (SE LLAMA UNA SOLA VEZ)
// ============================================================
private void configurarLabelsCorte() {
    // Fuentes
    Font fontTitulo = new Font("Roboto", Font.BOLD, 12);
    Font fontDatos = new Font("Roboto", Font.PLAIN, 11);
    Font fontTotales = new Font("Roboto", Font.BOLD, 14);
    
    // ============================================================
    // 1. LABELS DE ESTADO (los 4 de la derecha)
    // ============================================================
    // lblEstadoCorte
    lblEstadoCorte.setHorizontalAlignment(JLabel.CENTER);
    lblEstadoCorte.setVerticalAlignment(JLabel.CENTER);
    lblEstadoCorte.setFont(fontTitulo);
    lblEstadoCorte.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
    
    // lblFechaCorte
    lblFechaCorte.setHorizontalAlignment(JLabel.CENTER);
    lblFechaCorte.setVerticalAlignment(JLabel.CENTER);
    lblFechaCorte.setFont(fontDatos);
    lblFechaCorte.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
    
    // lblHoraInicio
    lblHoraInicio.setHorizontalAlignment(JLabel.CENTER);
    lblHoraInicio.setVerticalAlignment(JLabel.CENTER);
    lblHoraInicio.setFont(fontDatos);
    lblHoraInicio.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
    
    // lblHoraCierre
    lblHoraCierre.setHorizontalAlignment(JLabel.CENTER);
    lblHoraCierre.setVerticalAlignment(JLabel.CENTER);
    lblHoraCierre.setFont(fontDatos);
    lblHoraCierre.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
    
    // ============================================================
    // 2. LABELS DE TOTALES (los 5 de la parte superior)
    // ============================================================
    // lblTotalVentas
    lblTotalVentas.setHorizontalAlignment(JLabel.CENTER);
    lblTotalVentas.setVerticalAlignment(JLabel.CENTER);
    lblTotalVentas.setFont(fontTotales);
    lblTotalVentas.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
    
    // lblTotalEfectivo
    lblTotalEfectivo.setHorizontalAlignment(JLabel.CENTER);
    lblTotalEfectivo.setVerticalAlignment(JLabel.CENTER);
    lblTotalEfectivo.setFont(fontTotales);
    lblTotalEfectivo.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
    
    // lblTotalCambio
    lblTotalCambio.setHorizontalAlignment(JLabel.CENTER);
    lblTotalCambio.setVerticalAlignment(JLabel.CENTER);
    lblTotalCambio.setFont(fontTotales);
    lblTotalCambio.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
    
    // lblTotalPedidos
    lblTotalPedidos.setHorizontalAlignment(JLabel.CENTER);
    lblTotalPedidos.setVerticalAlignment(JLabel.CENTER);
    lblTotalPedidos.setFont(fontTotales);
    lblTotalPedidos.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
    
    // lblTotalProductos
    lblTotalProductos.setHorizontalAlignment(JLabel.CENTER);
    lblTotalProductos.setVerticalAlignment(JLabel.CENTER);
    lblTotalProductos.setFont(fontTotales);
    lblTotalProductos.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK, 1));
}
// ============================================================
// CARGAR DATOS DEL CORTE
// ============================================================
private void cargarCorte() {

    DefaultTableModel modelo =
            (DefaultTableModel) tablaDetalleVentas.getModel();

    modelo.setRowCount(0);

    corteAbierto = false;
    btnCerrarCorte.setEnabled(false);

    lblEstadoCorte.setText("SIN CORTE ABIERTO");
    lblFechaCorte.setText("FECHA: ---");
    lblHoraInicio.setText("INICIO: ---");
    lblHoraCierre.setText("CIERRE: ---");

    mostrarTotalesCorte(
        java.math.BigDecimal.ZERO,
        java.math.BigDecimal.ZERO,
        java.math.BigDecimal.ZERO,
        0, 0
    );

    try (Connection conn = Conexion.getConexion()) {

        int idCorte;
        Timestamp apertura;
        boolean esHoy;

        // Buscar el unico corte abierto, aunque sea de otra fecha
        String sqlCorte =
                "SELECT id_corte, fecha_apertura, "
                + "DATE(fecha_apertura) = CURDATE() AS es_hoy "
                + "FROM cortes_caja "
                + "WHERE estado = 'ABIERTO' "
                + "ORDER BY id_corte";

        try (PreparedStatement ps = conn.prepareStatement(sqlCorte);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return;
            }

            idCorte = rs.getInt("id_corte");
            apertura = rs.getTimestamp("fecha_apertura");
            esHoy = rs.getBoolean("es_hoy");

            if (rs.next()) {
                throw new SQLException(
                    "Hay varios cortes abiertos. Deben revisarse."
                );
            }
        }

        java.math.BigDecimal ventas = java.math.BigDecimal.ZERO;
        java.math.BigDecimal efectivo = java.math.BigDecimal.ZERO;
        java.math.BigDecimal cambio = java.math.BigDecimal.ZERO;

        int pedidos = 0;
        int productos = 0;

        // Cargar solamente las ventas activas de este corte
        try (PreparedStatement ps =
                conn.prepareStatement(sqlVentasDelCorte(true))) {

            ps.setInt(1, idCorte);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    java.math.BigDecimal total =
                            rs.getBigDecimal("total");

                    java.math.BigDecimal recibido =
                            rs.getBigDecimal("efectivo");

                    java.math.BigDecimal devuelto =
                            rs.getBigDecimal("cambio");

                    ventas = ventas.add(total);
                    efectivo = efectivo.add(recibido);
                    cambio = cambio.add(devuelto);

                    pedidos++;
                    productos += rs.getInt("productos");

                    modelo.addRow(new Object[]{
                        rs.getInt("id_pedido"),
                        formatearFechaHora(rs.getTimestamp("fecha")),
                        rs.getString("cajero"),
                        rs.getString("categorias"),
                        "$" + total.toPlainString(),
                        "$" + recibido.toPlainString(),
                        "$" + devuelto.toPlainString(),
                        rs.getString("estado")
                    });
                }
            }
        }

        corteAbierto = true;

        // El cierre de cortes atrasados se resolvera por separado
        btnCerrarCorte.setEnabled(esHoy);

        lblEstadoCorte.setText(
            "<html><center>CORTE #" + idCorte
            + "<br><b>"
            + (esHoy ? "ABIERTO" : "ABIERTO - FECHA ANTERIOR")
            + "</b></center></html>"
        );

        lblFechaCorte.setText(
            "FECHA: "
            + new SimpleDateFormat("dd/MM/yyyy").format(apertura)
        );

        lblHoraInicio.setText(
            "INICIO: "
            + new SimpleDateFormat("HH:mm:ss").format(apertura)
        );

        mostrarTotalesCorte(
            ventas, efectivo, cambio, pedidos, productos
        );

    } catch (SQLException e) {
        modelo.setRowCount(0);
        lblEstadoCorte.setText("ERROR AL CARGAR CORTE");

        JOptionPane.showMessageDialog(
            this,
            "Error al cargar el corte:\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        e.printStackTrace();
    }
}
// ============================================================
// ACTUALIZAR ESTADO DEL CORTE (SIN HTML - RECOMENDADO)
// ============================================================
private void actualizarEstadoCorte() {
    cargarCorte();
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


private void mostrarTotalesCorte(
        java.math.BigDecimal ventas,
        java.math.BigDecimal efectivo,
        java.math.BigDecimal cambio,
        int pedidos,
        int productos) {

    lblTotalVentas.setText(
        "<html><center>TOTAL VENTAS<br><b>$"
        + ventas.setScale(2).toPlainString()
        + "</b></center></html>"
    );

    lblTotalEfectivo.setText(
        "<html><center>EFECTIVO RECIBIDO<br><b>$"
        + efectivo.setScale(2).toPlainString()
        + "</b></center></html>"
    );

    lblTotalCambio.setText(
        "<html><center>CAMBIO ENTREGADO<br><b>$"
        + cambio.setScale(2).toPlainString()
        + "</b></center></html>"
    );

    lblTotalPedidos.setText(
        "<html><center>PEDIDOS<br><b>"
        + pedidos + "</b></center></html>"
    );

    lblTotalProductos.setText(
        "<html><center>PRODUCTOS<br><b>"
        + productos + "</b></center></html>"
    );
}
private String formatearFechaHora(java.sql.Timestamp fecha) {
    if (fecha == null) return "";
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    return sdf.format(fecha);
}
// ============================================================
// ABRIR CORTE MANUALMENTE
// ============================================================
public void abrirCorteManual() {
    // Verificar si ya hay un corte abierto
    if (corteAbierto) {
        JOptionPane.showMessageDialog(this,
            "⚠️ Ya hay un corte abierto para hoy.\n" +
            "No es necesario abrir otro.",
            "Corte ya abierto",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Solicitar confirmación
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "¿Estás seguro de que quieres ABRIR el corte de caja?\n" +
        "Fecha: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + "\n" +
        "Cajero: " + Login.usuarioLogueado.nombreCompleto,
        "Confirmar apertura de corte",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );
    
    if (confirm != JOptionPane.YES_OPTION) return;
    
    // Crear el corte
    java.sql.Connection conn = null;
    java.sql.PreparedStatement ps = null;
    
    try {
        conn = Conexion.getConexion();
        String sql = "INSERT INTO cortes_caja (id_usuario_apertura, fecha_apertura, estado) " +
                     "VALUES (?, NOW(), 'ABIERTO')";
        ps = conn.prepareStatement(sql);
        ps.setInt(1, Login.usuarioLogueado.idUsuario);
        ps.executeUpdate();
        ps.close();
        
        JOptionPane.showMessageDialog(this,
            "✅ Corte abierto exitosamente\n" +
            "Hora: " + new SimpleDateFormat("HH:mm:ss").format(new Date()),
            "Corte abierto",
            JOptionPane.INFORMATION_MESSAGE);
        
        // Actualizar estado
        actualizarEstadoCorte();
        
        // Habilitar el botón de cerrar corte
        btnCerrarCorte.setEnabled(true);
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "❌ Error al abrir corte:\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } finally {
        try {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
// ============================================================
// CERRAR CORTE
// ============================================================
private void cerrarCorte() {

    if (Login.usuarioLogueado == null) {
        JOptionPane.showMessageDialog(
            this, "Debes iniciar sesion para cerrar el corte."
        );
        return;
    }

    // Confirmar antes de iniciar la transaccion
    int opcion = JOptionPane.showConfirmDialog(
        this,
        "Se cerrara el corte abierto de hoy.\n"
        + "Se calcularan todas sus ventas activas,\n"
        + "sin usar los filtros de fecha o cajero de la pantalla.\n\n"
        + "Tambien se permite cerrar sin ventas.\n"
        + "Deseas continuar?",
        "Confirmar cierre",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    if (opcion != JOptionPane.YES_OPTION) {
        return;
    }

    int idCorte;
    int totalPedidos = 0;
    int totalProductos = 0;

    java.math.BigDecimal totalVentas =
            java.math.BigDecimal.ZERO;

    java.math.BigDecimal totalEfectivo =
            java.math.BigDecimal.ZERO;

    java.math.BigDecimal totalCambio =
            java.math.BigDecimal.ZERO;

    java.sql.Timestamp fechaCierre;

    try (Connection conn = Conexion.getConexion()) {

        conn.setAutoCommit(false);

        try {
            // Bloquear el corte mientras se calcula y se cierra
            String sqlCorte =
                    "SELECT id_corte, "
                    + "DATE(fecha_apertura) = CURDATE() AS es_hoy "
                    + "FROM cortes_caja "
                    + "WHERE estado = 'ABIERTO' "
                    + "ORDER BY id_corte "
                    + "FOR UPDATE";

            try (PreparedStatement ps = conn.prepareStatement(sqlCorte);
                 ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                        "No existe un corte abierto para cerrar."
                    );
                }

                idCorte = rs.getInt("id_corte");
                boolean esHoy = rs.getBoolean("es_hoy");

                if (rs.next()) {
                    throw new SQLException(
                        "Hay varios cortes abiertos.\n"
                        + "Primero deben revisarse para evitar "
                        + "cerrar el corte equivocado."
                    );
                }

                if (!esHoy) {
                    throw new SQLException(
                        "El corte abierto corresponde a otra fecha.\n"
                        + "Su regularizacion se hara por separado."
                    );
                }
            }

            // Leer los pedidos del corte y bloquearlos durante el cierre.
            // Cada pedido aparece una sola vez.
            String sqlPedidos =
                    "SELECT p.id_pedido, p.total, p.efectivo, "
                    + "p.cambio, p.estado, "
                    + "(SELECT COALESCE(SUM(dp.cantidad), 0) "
                    + " FROM detalles_pedido dp "
                    + " WHERE dp.id_pedido = p.id_pedido) AS productos "
                    + "FROM pedidos p "
                    + "WHERE p.id_corte = ? "
                    + "ORDER BY p.id_pedido "
                    + "FOR UPDATE";

            try (PreparedStatement ps =
                    conn.prepareStatement(sqlPedidos)) {

                ps.setInt(1, idCorte);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        String estado = rs.getString("estado");

                        if (estado == null
                                || !"ACTIVO".equalsIgnoreCase(
                                        estado.trim())) {
                            continue;
                        }

                        java.math.BigDecimal venta =
                                rs.getBigDecimal("total");

                        java.math.BigDecimal efectivo =
                                rs.getBigDecimal("efectivo");

                        java.math.BigDecimal cambio =
                                rs.getBigDecimal("cambio");

                        // Para registros cuyo cambio no fue almacenado
                        if (cambio == null) {
                            cambio = efectivo.subtract(venta);
                        }

                        totalVentas = totalVentas.add(venta);
                        totalEfectivo = totalEfectivo.add(efectivo);
                        totalCambio = totalCambio.add(cambio);

                        totalPedidos++;
                        totalProductos += rs.getInt("productos");
                    }
                }
            }

            // Usar la hora del servidor de base de datos
            try (PreparedStatement ps =
                        conn.prepareStatement("SELECT NOW() AS cierre");
                 ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                        "No se pudo obtener la fecha de cierre."
                    );
                }

                fechaCierre = rs.getTimestamp("cierre");
            }

            // Guardar los totales calculados y cerrar el corte
            String sqlCerrar =
                    "UPDATE cortes_caja SET "
                    + "fecha_cierre = ?, "
                    + "id_usuario_cierre = ?, "
                    + "total_ventas = ?, "
                    + "total_efectivo = ?, "
                    + "total_cambio = ?, "
                    + "total_pedidos = ?, "
                    + "total_productos = ?, "
                    + "estado = 'CERRADO' "
                    + "WHERE id_corte = ? AND estado = 'ABIERTO'";

            try (PreparedStatement ps =
                    conn.prepareStatement(sqlCerrar)) {

                ps.setTimestamp(1, fechaCierre);
                ps.setInt(2, Login.usuarioLogueado.idUsuario);
                ps.setBigDecimal(3, totalVentas);
                ps.setBigDecimal(4, totalEfectivo);
                ps.setBigDecimal(5, totalCambio);
                ps.setInt(6, totalPedidos);
                ps.setInt(7, totalProductos);
                ps.setInt(8, idCorte);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                        "No se pudo actualizar el corte."
                    );
                }
            }

            conn.commit();

        } catch (SQLException | RuntimeException e) {

            try {
                conn.rollback();
            } catch (SQLException errorRollback) {
                e.addSuppressed(errorRollback);
            }

            throw e;
        }

    } catch (SQLException | RuntimeException e) {
        JOptionPane.showMessageDialog(
            this,
            "No se pudo completar el cierre:\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        e.printStackTrace();
        return;
    }

    // Actualizar el estado visual
    corteAbierto = false;
    btnCerrarCorte.setEnabled(false);
    

    lblEstadoCorte.setText(
        "<html><center>ESTADO<br><b>CERRADO</b></center></html>"
    );

    lblHoraCierre.setText(
        "<html><center>CIERRE<br><b>"
        + formatearFechaHora(fechaCierre)
        + "</b></center></html>"
    );

    // Mostrar los importes realmente guardados
    JOptionPane.showMessageDialog(
        this,
        "Corte #" + idCorte + " cerrado correctamente.\n\n"
        + "Ventas activas: $"
        + totalVentas.setScale(2).toPlainString() + "\n"
        + "Efectivo recibido: $"
        + totalEfectivo.setScale(2).toPlainString() + "\n"
        + "Cambio entregado: $"
        + totalCambio.setScale(2).toPlainString() + "\n"
        + "Pedidos activos: " + totalPedidos + "\n"
        + "Productos vendidos: " + totalProductos + "\n"
        + "Fecha de cierre: " + formatearFechaHora(fechaCierre),
        "Corte cerrado",
        JOptionPane.INFORMATION_MESSAGE
);

// Limpiar las ventas y los totales al terminar el cierre
actualizarEstadoCorte();
}
// ============================================================
// EXPORTAR A EXCEL
// ============================================================
private void exportarExcel() {
    javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tablaDetalleVentas.getModel();
    if (modelo.getRowCount() == 0) {
        javax.swing.JOptionPane.showMessageDialog(this, "No hay datos para exportar");
        return;
    }
    
    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
    fileChooser.setDialogTitle("Guardar archivo Excel");
    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));
    fileChooser.setSelectedFile(new java.io.File("Corte_Caja.xlsx"));
    
    int resultado = fileChooser.showSaveDialog(this);
    if (resultado != javax.swing.JFileChooser.APPROVE_OPTION) return;
    
    String rutaArchivo = fileChooser.getSelectedFile().getAbsolutePath();
    if (!rutaArchivo.endsWith(".xlsx")) {
        rutaArchivo += ".xlsx";
    }
    
    try {
        exportarDatosExcel(rutaArchivo, modelo);
        javax.swing.JOptionPane.showMessageDialog(this, "✅ Archivo exportado correctamente:\n" + rutaArchivo);
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error al exportar: " + e.getMessage());
        e.printStackTrace();
    }
}

private void exportarDatosExcel(String rutaArchivo, javax.swing.table.DefaultTableModel modelo) throws Exception {
    org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
    org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Corte de Caja");
    
    // Estilos
    org.apache.poi.ss.usermodel.CellStyle styleHeader = workbook.createCellStyle();
    org.apache.poi.ss.usermodel.Font fontHeader = workbook.createFont();
    fontHeader.setBold(true);
    styleHeader.setFont(fontHeader);
    styleHeader.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
    styleHeader.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
    styleHeader.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
    styleHeader.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
    styleHeader.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
    styleHeader.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
    styleHeader.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
    
    org.apache.poi.ss.usermodel.CellStyle styleDatos = workbook.createCellStyle();
    styleDatos.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
    styleDatos.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
    styleDatos.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
    styleDatos.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
    styleDatos.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
    
    // Título
    org.apache.poi.ss.usermodel.Row rowTitulo = sheet.createRow(0);
    org.apache.poi.ss.usermodel.Cell cellTitulo = rowTitulo.createCell(0);
    cellTitulo.setCellValue("📊 CORTE DE CAJA");
    org.apache.poi.ss.usermodel.CellStyle styleTitulo = workbook.createCellStyle();
    org.apache.poi.ss.usermodel.Font fontTitulo = workbook.createFont();
    fontTitulo.setBold(true);
    fontTitulo.setFontHeightInPoints((short) 16);
    styleTitulo.setFont(fontTitulo);
    styleTitulo.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
    cellTitulo.setCellStyle(styleTitulo);
    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 7));
    
    // Fecha
    org.apache.poi.ss.usermodel.Row rowFecha = sheet.createRow(1);
    org.apache.poi.ss.usermodel.Cell cellFecha = rowFecha.createCell(0);
    cellFecha.setCellValue("Generado: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 7));
    
    // Encabezados
    org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(3);
    String[] columnas = {
    "ID", "Fecha/Hora", "Cajero", "Categoria",
    "Total", "Efectivo", "Cambio", "Estado"
};
    for (int i = 0; i < columnas.length; i++) {
        org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
        cell.setCellValue(columnas[i]);
        cell.setCellStyle(styleHeader);
    }
    
    // Datos
    int rowIndex = 4;
    for (int i = 0; i < modelo.getRowCount(); i++) {
        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);
        for (int j = 0; j < modelo.getColumnCount(); j++) {
            Object valor = modelo.getValueAt(i, j);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
            if (valor instanceof Integer) {
                cell.setCellValue((Integer) valor);
            } else if (valor instanceof Double) {
                cell.setCellValue((Double) valor);
            } else {
                cell.setCellValue(valor != null ? valor.toString() : "");
            }
            cell.setCellStyle(styleDatos);
        }
    }
    // Sumar solamente las ventas con estado ACTIVO
java.math.BigDecimal totalActivas = java.math.BigDecimal.ZERO;

for (int i = 0; i < modelo.getRowCount(); i++) {
    // Columna 7: ESTADO
    Object estado = modelo.getValueAt(i, 7);

    if (estado != null
            && "ACTIVO".equalsIgnoreCase(estado.toString().trim())) {

        // Columna 4: TOTAL
        Object valorTotal = modelo.getValueAt(i, 4);

        if (valorTotal == null) {
            throw new IllegalArgumentException(
                "Falta el total de la venta en la fila " + (i + 1)
            );
        }

        String textoTotal = valorTotal.toString()
                .replace("$", "")
                .trim()
                .replace(",", ".");

        totalActivas = totalActivas.add(
            new java.math.BigDecimal(textoTotal)
        );
    }
}

// Dejar una fila vacia y agregar el resumen
org.apache.poi.ss.usermodel.Row filaResumen =
        sheet.createRow(rowIndex + 1);

// Texto en la columna Categoria
org.apache.poi.ss.usermodel.Cell celdaEtiqueta =
        filaResumen.createCell(6);

celdaEtiqueta.setCellValue("TOTAL VENTAS ACTIVAS");
celdaEtiqueta.setCellStyle(styleHeader);

// Importe en la columna Total
org.apache.poi.ss.usermodel.Cell celdaTotal =
        filaResumen.createCell(7);

// Guardar como numero para que Excel pueda utilizarlo
celdaTotal.setCellValue(totalActivas.doubleValue());

org.apache.poi.ss.usermodel.CellStyle estiloTotal =
        workbook.createCellStyle();

estiloTotal.cloneStyleFrom(styleHeader);
estiloTotal.setDataFormat(
    workbook.createDataFormat().getFormat("\"$\"#,##0.00")
);

celdaTotal.setCellStyle(estiloTotal);
    // Ajustar columnas
    for (int i = 0; i < columnas.length; i++) {
        sheet.autoSizeColumn(i);
        if (sheet.getColumnWidth(i) < 4000) {
            sheet.setColumnWidth(i, 4000);
        }
    }
    
    // Guardar
    try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(rutaArchivo)) {
        workbook.write(fileOut);
    }
    workbook.close();
}
 // ============================================================
// VER DETALLE DEL PEDIDO (DOBLE CLIC)
// ============================================================
private void verDetallePedido() {
    int fila = tablaDetalleVentas.getSelectedRow();
    if (fila == -1) {
        JOptionPane.showMessageDialog(this, "Selecciona un pedido para ver el detalle");
        return;
    }
    
    // Obtener el ID del pedido de la tabla
    int idPedido = (int) tablaDetalleVentas.getValueAt(fila, 0);
    
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    
    try {
        conn = Conexion.getConexion();
        
        // Consultar los detalles del pedido
         String sql =
    "SELECT dp.id_detalle, dp.categoria, dp.producto, "
    + "dp.cantidad, dp.subtotal, "
    + "p.total, p.fecha, p.estado, u.nombre_completo AS cajero "
    + "FROM detalles_pedido dp "
    + "INNER JOIN pedidos p ON dp.id_pedido = p.id_pedido "
    + "LEFT JOIN usuarios u ON p.id_usuario = u.id_usuario "
    + "WHERE dp.id_pedido = ? "
    + "ORDER BY dp.id_detalle ASC";
        
        ps = conn.prepareStatement(sql);
        ps.setInt(1, idPedido);
        rs = ps.executeQuery();
        
        // Construir el detalle
        StringBuilder detalle = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        double totalGeneral = 0;
        int contador = 1;
        String fechaPedido = "";
        String estado = "";
        String cajero = "";
        
        detalle.append("📋 DETALLE DEL PEDIDO #").append(idPedido).append("\n");
        detalle.append("══════════════════════════════════════════\n\n");
        
        while (rs.next()) {
            // Obtener datos del pedido (solo en la primera fila)
            if (contador == 1) {
                fechaPedido = sdf.format(rs.getTimestamp("fecha"));
                estado = rs.getString("estado");
                cajero = rs.getString("cajero");
                if (cajero == null) cajero = "N/A";
                totalGeneral = rs.getDouble("total");
                
                detalle.append("📅 Fecha: ").append(fechaPedido).append("\n");
                detalle.append("👤 Cajero: ").append(cajero).append("\n");
                detalle.append("📊 Estado: ").append(estado).append("\n");
                detalle.append("──────────────────────────────────────\n");
                detalle.append("🛒 PRODUCTOS:\n\n");
            }
            
            // Datos del producto
            String categoria = rs.getString("categoria");

if (categoria == null || categoria.trim().isEmpty()) {
    categoria = "Sin categoria";
}

String producto = rs.getString("producto");

if (producto == null) {
    producto = "";
}

// Convertir los saltos HTML en saltos de linea para el JTextArea
producto = producto.replaceAll("(?i)<br\\s*/?>", "\n   ");
            int cantidad = rs.getInt("cantidad");
            double subtotal = rs.getDouble("subtotal");
            
            
           detalle.append(contador).append(". Categoria: ").append(categoria).append("\n");
           detalle.append("   Producto: ").append(producto).append("\n");
           detalle.append("   Cantidad: ").append(cantidad).append("\n");
            
            // Mostrar extras solo si aplican
            String extras = consultarExtrasPedido(
    conn,
    rs.getInt("id_detalle")
);

detalle.append("   Extras:\n      ")
    .append(extras.replace("\n", "\n      "))
    .append("\n");
            
            detalle.append("   Subtotal: $").append(String.format("%.2f", subtotal)).append("\n\n");
            
            contador++;
        }
        
        detalle.append("──────────────────────────────────────\n");
        detalle.append("💰 TOTAL DEL PEDIDO: $").append(String.format("%.2f", totalGeneral)).append("\n");
        detalle.append("══════════════════════════════════════════");
        
        // Mostrar el detalle en una ventana emergente
        mostrarDetalleEnVentana(idPedido, detalle.toString());
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar detalle: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
 // ============================================================
// MOSTRAR DETALLE EN VENTANA EMERGENTE
// ============================================================
private void mostrarDetalleEnVentana(int idPedido, String detalle) {
    // Crear el diálogo
    JDialog dialog = new JDialog(
        (JFrame) SwingUtilities.getWindowAncestor(this), 
        "Detalle del Pedido #" + idPedido, 
        true
    );
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setLayout(new BorderLayout(10, 10));
    
    // Área de texto con el detalle
    JTextArea textArea = new JTextArea(detalle);
    textArea.setEditable(false);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
    textArea.setBackground(new Color(250, 250, 250));
    textArea.setMargin(new java.awt.Insets(10, 15, 10, 15));
    
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(500, 400));
    dialog.add(scrollPane, BorderLayout.CENTER);
    
    // Panel de botones
    JPanel panelBotones = new JPanel();
    panelBotones.setBackground(new Color(240, 240, 240));
    
    JButton btnCerrar = new JButton("Cerrar");
    btnCerrar.setFont(new Font("Roboto", Font.BOLD, 14));
    btnCerrar.setBackground(new Color(200, 200, 200));
    btnCerrar.addActionListener(e -> dialog.dispose());
    
    panelBotones.add(btnCerrar);
    dialog.add(panelBotones, BorderLayout.SOUTH);
    
    // Centrar y mostrar
    dialog.pack();
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}
private String consultarExtrasPedido(Connection conn, int idDetalle)
        throws SQLException {

    String sql =
        "SELECT nombre, cantidad, precio_unitario "
        + "FROM detalles_pedido_extras "
        + "WHERE id_detalle = ? "
        + "ORDER BY id_detalle_extra";

    StringBuilder texto = new StringBuilder();

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, idDetalle);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (texto.length() > 0) {
                    texto.append("\n");
                }

                int cantidad = rs.getInt("cantidad");

                java.math.BigDecimal precio =
                    rs.getBigDecimal("precio_unitario");

                java.math.BigDecimal importe = precio.multiply(
                    java.math.BigDecimal.valueOf(cantidad)
                );

                texto.append(rs.getString("nombre"))
                    .append(" x").append(cantidad)
                    .append(" | $").append(precio.toPlainString())
                    .append(" c/u | $").append(importe.toPlainString());
            }
        }
    }

    return texto.length() == 0
        ? "Sin extras registrados"
        : texto.toString();
}

   
    // ============================================================
    // GENERAR REPORTE DE CAJA CON VISTA PREVIA
    // ============================================================
    private void generarReportePDF() {

        // Verificar que exista un corte abierto
        if (!corteAbierto) {
            JOptionPane.showMessageDialog(
                this,
                "No hay un corte de caja abierto para generar el reporte.",
                "Reporte de caja",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            StringBuilder html = new StringBuilder();

            html.append("<html><head><style>");
            html.append("body{font-family:Arial,sans-serif;color:#263238;margin:25px;}");
            html.append("h1{text-align:center;color:#17365D;margin-bottom:4px;}");
            html.append("h2{color:#17365D;border-bottom:2px solid #17365D;padding-bottom:5px;}");
            html.append(".subtitulo{text-align:center;color:#666;font-size:11px;}");
            html.append(".datos{background:#F2F6FC;padding:12px;border-radius:6px;}");
            html.append(".resumen{width:100%;border-collapse:collapse;margin-top:12px;}");
            html.append(".resumen td{padding:10px;border:1px solid #CCD5E0;text-align:center;}");
            html.append(".numero{font-size:17px;font-weight:bold;color:#17365D;}");
            html.append("table.ventas{width:100%;border-collapse:collapse;font-size:10px;}");
            html.append(".ventas th{background:#17365D;color:white;padding:8px;}");
            html.append(".ventas td{border:1px solid #CCD5E0;padding:6px;text-align:center;}");
            html.append(".ventas tr:nth-child(even){background:#F2F6FC;}");
            html.append(".pie{text-align:center;color:#777;font-size:10px;margin-top:25px;}");
            html.append("</style></head><body>");

            // Encabezado
            html.append("<h1>KYRE CREPAS</h1>");
            html.append("<div class='subtitulo'>REPORTE DE CORTE DE CAJA</div>");

            html.append("<p class='subtitulo'>Generado el: ")
                .append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()))
                .append("</p>");

            // Informacion general del corte
            html.append("<h2>Informacion del corte</h2>");
            html.append("<div class='datos'>");

            html.append("<b>Numero y estado:</b> ")
                .append(escaparHTML(etiquetaAtexto(lblEstadoCorte.getText())))
                .append("<br>");

            html.append("<b>Fecha de apertura:</b> ")
                .append(escaparHTML(etiquetaAtexto(lblFechaCorte.getText())))
                .append("<br>");

            html.append("<b>Hora de inicio:</b> ")
                .append(escaparHTML(etiquetaAtexto(lblHoraInicio.getText())))
                .append("<br>");

            html.append("<b>Hora de cierre:</b> ")
                .append(escaparHTML(etiquetaAtexto(lblHoraCierre.getText())));

            html.append("</div>");

            
            // Resumen de caja: solamente total de ventas
            html.append("<h2>Resumen de caja</h2>");
            html.append("<table class='resumen'><tr>");

            html.append("<td style='width:100%;'>")
                .append("Total de ventas<br>")
                .append("<span class='numero'>")
                .append(escaparHTML(
                    etiquetaAtexto(lblTotalVentas.getText())
                        .replace("TOTAL VENTAS", "")
                        .trim()
                ))
                .append("</span></td>");

            html.append("</tr></table>");


            // Tabla de pedidos
            html.append("<h2>Detalle de ventas</h2>");
            html.append("<table class='ventas'><thead><tr>");

            String[] encabezados = {
                "ID", "Fecha y hora", "Cajero", "Categoria",
                "Total", "Efectivo", "Cambio", "Estado"
            };

            for (String encabezado : encabezados) {
                html.append("<th>")
                    .append(escaparHTML(encabezado))
                    .append("</th>");
            }

            html.append("</tr></thead><tbody>");

            DefaultTableModel modelo =
                (DefaultTableModel) tablaDetalleVentas.getModel();

            for (int fila = 0; fila < modelo.getRowCount(); fila++) {
                html.append("<tr>");

                for (int columna = 0;
                     columna < modelo.getColumnCount();
                     columna++) {

                    Object valor = modelo.getValueAt(fila, columna);

                    html.append("<td>")
                        .append(escaparHTML(
                            valor == null ? "" : valor.toString()
                        ))
                        .append("</td>");
                }

                html.append("</tr>");
            }

            if (modelo.getRowCount() == 0) {
                html.append("<tr><td colspan='8'>")
                    .append("No hay ventas registradas en este corte.")
                    .append("</td></tr>");
            }

            html.append("</tbody></table>");

            html.append("<div class='pie'>")
                .append("KYRE CREPAS - Reporte generado desde el sistema de caja")
                .append("<br>Firma del responsable: __________________________")
                .append("</div>");

            html.append("</body></html>");

            // Crear la vista previa
            JEditorPane vistaPrevia = new JEditorPane();
            vistaPrevia.setContentType("text/html");
            vistaPrevia.setEditable(false);
            vistaPrevia.setText(html.toString());
            vistaPrevia.setCaretPosition(0);

            JDialog dialogo = new JDialog(
                SwingUtilities.getWindowAncestor(this) instanceof JFrame
                    ? (JFrame) SwingUtilities.getWindowAncestor(this)
                    : null,
                "Vista previa - Reporte de caja",
                true
            );

            dialogo.setLayout(new BorderLayout(10, 10));

            JScrollPane scroll = new JScrollPane(vistaPrevia);
            scroll.setPreferredSize(new Dimension(950, 600));
            dialogo.add(scroll, BorderLayout.CENTER);

            JPanel panelBotones = new JPanel();

            JButton btnImprimirPDF = new JButton("Imprimir / Guardar PDF");
            JButton btnCerrarVista = new JButton("Cerrar");

            btnImprimirPDF.setBackground(new Color(30, 100, 180));
            btnImprimirPDF.setForeground(Color.WHITE);
            btnImprimirPDF.setFocusPainted(false);

         
btnImprimirPDF.addActionListener(e -> {
    try {
        java.awt.print.PrinterJob trabajo =
                java.awt.print.PrinterJob.getPrinterJob();

        // Configurar la hoja en horizontal
        java.awt.print.PageFormat formato =
                trabajo.defaultPage();

        formato.setOrientation(
                java.awt.print.PageFormat.LANDSCAPE
        );

        // Configurar margenes de la hoja
        java.awt.print.Paper papel = formato.getPaper();

        double anchoHoja = papel.getWidth();
        double altoHoja = papel.getHeight();
        double margen = 25;

        papel.setImageableArea(
                margen,
                margen,
                anchoHoja - margen * 2,
                altoHoja - margen * 2
        );

        formato.setPaper(papel);

        final java.awt.print.PageFormat formatoFinal = formato;

        // Ajustar el documento al ancho imprimible
        int anchoContenido =
                (int) formatoFinal.getImageableWidth();

        vistaPrevia.setSize(
                anchoContenido,
                Short.MAX_VALUE
        );

        vistaPrevia.setSize(
                anchoContenido,
                vistaPrevia.getPreferredSize().height
        );

        final int altoContenido =
                vistaPrevia.getPreferredSize().height;

        trabajo.setPrintable((grafico, pagina, numeroPagina) -> {

            double altoPagina =
                    formatoFinal.getImageableHeight();

            int totalPaginas = Math.max(
                    1,
                    (int) Math.ceil(altoContenido / altoPagina)
            );

            if (numeroPagina >= totalPaginas) {
                return java.awt.print.Printable.NO_SUCH_PAGE;
            }

            java.awt.Graphics2D g2 =
                    (java.awt.Graphics2D) grafico.create();

            try {
                g2.translate(
                        formatoFinal.getImageableX(),
                        formatoFinal.getImageableY()
                );

                g2.clipRect(
                        0,
                        0,
                        (int) formatoFinal.getImageableWidth(),
                        (int) altoPagina
                );

                g2.translate(0, -numeroPagina * altoPagina);

                vistaPrevia.printAll(g2);

            } finally {
                g2.dispose();
            }

            return java.awt.print.Printable.PAGE_EXISTS;
        }, formatoFinal);

        // Mostrar el dialogo de impresion
        if (trabajo.printDialog()) {
            trabajo.print();

            JOptionPane.showMessageDialog(
                    dialogo,
                    "El reporte se envio a impresion.",
                    "Reporte de caja",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

    } catch (java.awt.print.PrinterException ex) {
        JOptionPane.showMessageDialog(
                dialogo,
                "No se pudo imprimir el reporte:\n"
                + ex.getMessage(),
                "Error de impresion",
                JOptionPane.ERROR_MESSAGE
        );
    }
});


            btnCerrarVista.addActionListener(e -> dialogo.dispose());

            panelBotones.add(btnImprimirPDF);
            panelBotones.add(btnCerrarVista);

            dialogo.add(panelBotones, BorderLayout.SOUTH);
            dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialogo.setSize(1000, 700);
            dialogo.setLocationRelativeTo(this);
            dialogo.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Error al generar el reporte:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    // Quitar etiquetas HTML de los labels de resumen
    private String etiquetaAtexto(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
            .replaceAll("(?i)<br\\s*/?>", " ")
            .replaceAll("<[^>]*>", "")
            .replace("&nbsp;", " ")
            .trim();
    }

    // Evitar que los datos rompan el formato HTML
    private String escaparHTML(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblEstadoCorte = new javax.swing.JLabel();
        lblFechaCorte = new javax.swing.JLabel();
        lblHoraInicio = new javax.swing.JLabel();
        lblHoraCierre = new javax.swing.JLabel();
        lblTotalVentas = new javax.swing.JLabel();
        lblTotalEfectivo = new javax.swing.JLabel();
        lblTotalCambio = new javax.swing.JLabel();
        lblTotalPedidos = new javax.swing.JLabel();
        lblTotalProductos = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaDetalleVentas = new javax.swing.JTable();
        btnCerrarCorte = new javax.swing.JButton();
        btnExportarExcel = new javax.swing.JButton();
        btnImprimirReport = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(1100, 720));
        setMinimumSize(new java.awt.Dimension(1100, 720));
        setPreferredSize(new java.awt.Dimension(1100, 720));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblEstadoCorte.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblEstadoCorte.setMaximumSize(new java.awt.Dimension(200, 60));
        lblEstadoCorte.setMinimumSize(new java.awt.Dimension(200, 60));
        lblEstadoCorte.setPreferredSize(new java.awt.Dimension(200, 60));
        add(lblEstadoCorte, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        lblFechaCorte.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblFechaCorte.setMaximumSize(new java.awt.Dimension(200, 60));
        lblFechaCorte.setMinimumSize(new java.awt.Dimension(200, 60));
        lblFechaCorte.setPreferredSize(new java.awt.Dimension(200, 60));
        add(lblFechaCorte, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 10, -1, -1));

        lblHoraInicio.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblHoraInicio.setMaximumSize(new java.awt.Dimension(200, 60));
        lblHoraInicio.setMinimumSize(new java.awt.Dimension(200, 60));
        lblHoraInicio.setPreferredSize(new java.awt.Dimension(200, 60));
        add(lblHoraInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 10, -1, -1));

        lblHoraCierre.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblHoraCierre.setMaximumSize(new java.awt.Dimension(200, 60));
        lblHoraCierre.setMinimumSize(new java.awt.Dimension(200, 60));
        lblHoraCierre.setPreferredSize(new java.awt.Dimension(200, 60));
        add(lblHoraCierre, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 10, -1, -1));

        lblTotalVentas.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblTotalVentas.setMaximumSize(new java.awt.Dimension(200, 60));
        lblTotalVentas.setMinimumSize(new java.awt.Dimension(200, 60));
        lblTotalVentas.setPreferredSize(new java.awt.Dimension(200, 60));
        add(lblTotalVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 10, -1, -1));

        lblTotalEfectivo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblTotalEfectivo.setMaximumSize(new java.awt.Dimension(200, 60));
        lblTotalEfectivo.setMinimumSize(new java.awt.Dimension(200, 60));
        lblTotalEfectivo.setPreferredSize(new java.awt.Dimension(200, 60));
        add(lblTotalEfectivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 650, -1, -1));

        lblTotalCambio.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblTotalCambio.setMaximumSize(new java.awt.Dimension(200, 60));
        lblTotalCambio.setMinimumSize(new java.awt.Dimension(200, 60));
        lblTotalCambio.setPreferredSize(new java.awt.Dimension(200, 60));
        add(lblTotalCambio, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 650, -1, -1));

        lblTotalPedidos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblTotalPedidos.setMaximumSize(new java.awt.Dimension(200, 60));
        lblTotalPedidos.setMinimumSize(new java.awt.Dimension(200, 60));
        lblTotalPedidos.setPreferredSize(new java.awt.Dimension(200, 60));
        add(lblTotalPedidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 650, -1, -1));

        lblTotalProductos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblTotalProductos.setMaximumSize(new java.awt.Dimension(200, 60));
        lblTotalProductos.setMinimumSize(new java.awt.Dimension(200, 60));
        lblTotalProductos.setPreferredSize(new java.awt.Dimension(200, 60));
        add(lblTotalProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 650, -1, -1));

        tablaDetalleVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "FECHA/HORA", "CAJERO", "CATEGORIA", "TOTAL", "EFECTIVO", "CAMBIO", "ESTADO"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaDetalleVentas);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 104, 1028, 460));

        btnCerrarCorte.setText("CERRAR CORTE");
        btnCerrarCorte.addActionListener(this::btnCerrarCorteActionPerformed);
        add(btnCerrarCorte, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 580, -1, 40));

        btnExportarExcel.setText("EXPORTAR EXCEL");
        add(btnExportarExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 580, -1, 40));

        btnImprimirReport.setText("IMPRIMIR REPORTE ");
        add(btnImprimirReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 590, -1, 30));
    }// </editor-fold>//GEN-END:initComponents

    private void btnCerrarCorteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarCorteActionPerformed
        cerrarCorte();
    }//GEN-LAST:event_btnCerrarCorteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarCorte;
    private javax.swing.JButton btnExportarExcel;
    private javax.swing.JButton btnImprimirReport;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblEstadoCorte;
    private javax.swing.JLabel lblFechaCorte;
    private javax.swing.JLabel lblHoraCierre;
    private javax.swing.JLabel lblHoraInicio;
    private javax.swing.JLabel lblTotalCambio;
    private javax.swing.JLabel lblTotalEfectivo;
    private javax.swing.JLabel lblTotalPedidos;
    private javax.swing.JLabel lblTotalProductos;
    private javax.swing.JLabel lblTotalVentas;
    private javax.swing.JTable tablaDetalleVentas;
    // End of variables declaration//GEN-END:variables
}
