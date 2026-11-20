package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;

public class Conexion {
    
    // ============================================================
    // ✅ TODAS LAS VARIABLES ESTÁTICAS
    // ============================================================
    private static Connection con = null;
    private static final String URL = "jdbc:mysql://localhost:3306/kire_crepas?useSSL=false&serverTimezone=America/Mexico_City";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    // ============================================================
    // ✅ MÉTODO ESTÁTICO (SIN CAMBIOS EN TUS MÓDULOS)
    // ============================================================
    public static Connection getConexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Conexion Establecida");
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Error de Conexion: " + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    // ============================================================
    // 📌 INICIAR CIERRE AUTOMÁTICO A LAS 23:59
    // ============================================================
    public static void iniciarCierreAutomatico() {
        Timer timer = new Timer();
        
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calendar.set(java.util.Calendar.MINUTE, 59);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        java.util.Date primeraEjecucion = calendar.getTime();
        
        if (primeraEjecucion.before(new java.util.Date())) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
            primeraEjecucion = calendar.getTime();
        }
        
        System.out.println("⏰ Cierre automático programado para: " + primeraEjecucion);
        
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cerrarCortesAutomaticamente();
            }
        }, primeraEjecucion, 24 * 60 * 60 * 1000);
    }

    // ============================================================
    // 📌 CERRAR CORTES AUTOMÁTICAMENTE (23:59) - CON TOTALES
    // ============================================================
    private static void cerrarCortesAutomaticamente() {
        Connection conn = null;
        PreparedStatement psBuscar = null;
        PreparedStatement psUpdate = null;
        PreparedStatement psTotales = null;
        PreparedStatement psProductos = null;
        ResultSet rs = null;
        ResultSet rsTotales = null;
        ResultSet rsProductos = null;
        
        try {
            conn = getConexion();
            
            // 1. Buscar cortes ABIERTOS
            String sqlBuscar = "SELECT id_corte, fecha_apertura, id_usuario_apertura " +
                               "FROM cortes_caja WHERE estado = 'ABIERTO'";
            psBuscar = conn.prepareStatement(sqlBuscar);
            rs = psBuscar.executeQuery();
            
            // 2. Preparar update
            String sqlUpdate = "UPDATE cortes_caja SET " +
                               "estado = 'CERRADO', " +
                               "fecha_cierre = NOW(), " +
                               "id_usuario_cierre = ?, " +
                               "total_ventas = ?, " +
                               "total_efectivo = ?, " +
                               "total_cambio = ?, " +
                               "total_pedidos = ?, " +
                               "total_productos = ?, " +
                               "observaciones = 'Cerrado automáticamente a las 23:59' " +
                               "WHERE id_corte = ?";
            psUpdate = conn.prepareStatement(sqlUpdate);
            
            int afectados = 0;
            
            while (rs.next()) {
                int idCorte = rs.getInt("id_corte");
                Timestamp fechaApertura = rs.getTimestamp("fecha_apertura");
                int idUsuarioApertura = rs.getInt("id_usuario_apertura");
                
                double totalVentas = 0, totalEfectivo = 0, totalCambio = 0;
                int totalPedidos = 0, totalProductos = 0;
                
                // Totales de pedidos ACTIVOS del día del corte
                String sqlTotales = "SELECT COALESCE(SUM(total),0) AS s_total, " +
                                    "COALESCE(SUM(efectivo),0) AS s_efectivo, " +
                                    "COALESCE(SUM(cambio),0) AS s_cambio, " +
                                    "COUNT(*) AS n_pedidos " +
                                    "FROM pedidos WHERE estado='ACTIVO' " +
                                    "AND DATE(fecha) = DATE(?)";
                psTotales = conn.prepareStatement(sqlTotales);
                psTotales.setTimestamp(1, fechaApertura);
                rsTotales = psTotales.executeQuery();
                if (rsTotales.next()) {
                    totalVentas = rsTotales.getDouble("s_total");
                    totalEfectivo = rsTotales.getDouble("s_efectivo");
                    totalCambio = rsTotales.getDouble("s_cambio");
                    totalPedidos = rsTotales.getInt("n_pedidos");
                }
                rsTotales.close();
                psTotales.close();
                
                // Total de productos
                String sqlProductos = "SELECT COALESCE(SUM(dp.cantidad),0) AS s_prod " +
                                      "FROM detalles_pedido dp " +
                                      "INNER JOIN pedidos p ON dp.id_pedido = p.id_pedido " +
                                      "WHERE p.estado='ACTIVO' AND DATE(p.fecha) = DATE(?)";
                psProductos = conn.prepareStatement(sqlProductos);
                psProductos.setTimestamp(1, fechaApertura);
                rsProductos = psProductos.executeQuery();
                if (rsProductos.next()) {
                    totalProductos = rsProductos.getInt("s_prod");
                }
                rsProductos.close();
                psProductos.close();
                
                // Actualizar corte
                psUpdate.setInt(1, idUsuarioApertura);
                psUpdate.setDouble(2, totalVentas);
                psUpdate.setDouble(3, totalEfectivo);
                psUpdate.setDouble(4, totalCambio);
                psUpdate.setInt(5, totalPedidos);
                psUpdate.setInt(6, totalProductos);
                psUpdate.setInt(7, idCorte);
                psUpdate.executeUpdate();
                afectados++;
            }
            
            if (afectados > 0) {
                System.out.println("✅ CIERRE AUTOMÁTICO: " + afectados + " corte(s) cerrado(s)");
            } else {
                System.out.println("ℹ️ No había cortes abiertos");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error en cierre automático: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (rsTotales != null) rsTotales.close();
                if (rsProductos != null) rsProductos.close();
                if (psBuscar != null) psBuscar.close();
                if (psUpdate != null) psUpdate.close();
                if (psTotales != null) psTotales.close();
                if (psProductos != null) psProductos.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ============================================================
    // 📌 CERRAR CORTES VIEJOS AL INICIAR - CON TOTALES Y FECHA 23:59
    // ============================================================
    public static void cerrarCortesViejosAlIniciar() {
        Connection conn = null;
        PreparedStatement psBuscar = null;
        PreparedStatement psUpdate = null;
        PreparedStatement psTotales = null;
        PreparedStatement psProductos = null;
        ResultSet rs = null;
        ResultSet rsTotales = null;
        ResultSet rsProductos = null;
        
        try {
            conn = getConexion();
            
            // 1. Buscar cortes ABIERTOS de días anteriores
            String sqlBuscar = "SELECT id_corte, fecha_apertura, id_usuario_apertura " +
                               "FROM cortes_caja WHERE estado = 'ABIERTO' " +
                               "AND DATE(fecha_apertura) < CURDATE()";
            psBuscar = conn.prepareStatement(sqlBuscar);
            rs = psBuscar.executeQuery();
            
            // 2. Preparar update (fecha_cierre ahora es parámetro)
            String sqlUpdate = "UPDATE cortes_caja SET " +
                               "estado = 'CERRADO', " +
                               "fecha_cierre = ?, " +
                               "id_usuario_cierre = ?, " +
                               "total_ventas = ?, " +
                               "total_efectivo = ?, " +
                               "total_cambio = ?, " +
                               "total_pedidos = ?, " +
                               "total_productos = ?, " +
                               "observaciones = 'Cerrado automáticamente al iniciar sistema' " +
                               "WHERE id_corte = ?";
            psUpdate = conn.prepareStatement(sqlUpdate);
            
            int afectados = 0;
            
            while (rs.next()) {
                int idCorte = rs.getInt("id_corte");
                Timestamp fechaApertura = rs.getTimestamp("fecha_apertura");
                int idUsuarioApertura = rs.getInt("id_usuario_apertura");
                
                // 🔥 CALCULAR FECHA DE CIERRE = MISMO DÍA DE APERTURA A LAS 23:59:59
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(fechaApertura);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                Timestamp fechaCierreForzada = new Timestamp(cal.getTimeInMillis());
                
                double totalVentas = 0, totalEfectivo = 0, totalCambio = 0;
                int totalPedidos = 0, totalProductos = 0;
                
                // Totales de pedidos ACTIVOS del día del corte
                String sqlTotales = "SELECT COALESCE(SUM(total),0) AS s_total, " +
                                    "COALESCE(SUM(efectivo),0) AS s_efectivo, " +
                                    "COALESCE(SUM(cambio),0) AS s_cambio, " +
                                    "COUNT(*) AS n_pedidos " +
                                    "FROM pedidos WHERE estado='ACTIVO' " +
                                    "AND DATE(fecha) = DATE(?)";
                psTotales = conn.prepareStatement(sqlTotales);
                psTotales.setTimestamp(1, fechaApertura);
                rsTotales = psTotales.executeQuery();
                if (rsTotales.next()) {
                    totalVentas = rsTotales.getDouble("s_total");
                    totalEfectivo = rsTotales.getDouble("s_efectivo");
                    totalCambio = rsTotales.getDouble("s_cambio");
                    totalPedidos = rsTotales.getInt("n_pedidos");
                }
                rsTotales.close();
                psTotales.close();
                
                // Total de productos
                String sqlProductos = "SELECT COALESCE(SUM(dp.cantidad),0) AS s_prod " +
                                      "FROM detalles_pedido dp " +
                                      "INNER JOIN pedidos p ON dp.id_pedido = p.id_pedido " +
                                      "WHERE p.estado='ACTIVO' AND DATE(p.fecha) = DATE(?)";
                psProductos = conn.prepareStatement(sqlProductos);
                psProductos.setTimestamp(1, fechaApertura);
                rsProductos = psProductos.executeQuery();
                if (rsProductos.next()) {
                    totalProductos = rsProductos.getInt("s_prod");
                }
                rsProductos.close();
                psProductos.close();
                
                // Actualizar corte
                psUpdate.setTimestamp(1, fechaCierreForzada);  // ← 23:59:59 del día de apertura
                psUpdate.setInt(2, idUsuarioApertura);
                psUpdate.setDouble(3, totalVentas);
                psUpdate.setDouble(4, totalEfectivo);
                psUpdate.setDouble(5, totalCambio);
                psUpdate.setInt(6, totalPedidos);
                psUpdate.setInt(7, totalProductos);
                psUpdate.setInt(8, idCorte);
                psUpdate.executeUpdate();
                afectados++;
            }
            
            if (afectados > 0) {
                System.out.println("✅ Se cerraron " + afectados + " corte(s) viejo(s) al iniciar");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al cerrar cortes viejos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (rsTotales != null) rsTotales.close();
                if (rsProductos != null) rsProductos.close();
                if (psBuscar != null) psBuscar.close();
                if (psUpdate != null) psUpdate.close();
                if (psTotales != null) psTotales.close();
                if (psProductos != null) psProductos.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}