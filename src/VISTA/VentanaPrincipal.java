
package VISTA;

import Conexion.Conexion;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author USER
 */
public class VentanaPrincipal extends javax.swing.JFrame {
       // Variable para guardar el usuario logueado
            private Login.Usuario usuarioLogueado;
            private Color colorNormal = new Color(242, 230, 255);
            private Color colorHover = new Color(255, 230, 240);
            private Color colorTextoNormal = new Color(0, 0, 0);
            private Color colorTextoHover = new Color(80, 0, 80);
            private JLabel menuSeleccionado = null;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName());

    /**
     * Creates new form VentanaPrincipal
     */
   public VentanaPrincipal(Login.Usuario usuario) {
    this.usuarioLogueado = usuario;
    initComponents();
    this.setLocationRelativeTo(null);
    Conexion.cerrarCortesViejosAlIniciar();
    // ============================================================
    // 1. MOSTRAR NOMBRE DEL USUARIO LOGUEADO
    // ============================================================
    // Modificar el lblBienvenida para que se vea bien
    lblBienvenida.setText(" " + usuarioLogueado.nombreCompleto);
    lblBienvenida.setFont(new java.awt.Font("Roboto", 1, 14));
    lblBienvenida.setForeground(new Color(80, 0, 80));
    lblBienvenida.setBounds(260, 10, 400, 30);  // Ajustar posición
    
    // ============================================================
    // 2. OCULTAR BOTONES SEGÚN ROL
    // ============================================================
    if (usuarioLogueado.rol.equals("CAJERO")) {
        lblAgregarProducto.setVisible(false);
        lblAdminUsuario.setVisible(false);
    }
    
    // ============================================================
    // 3. AGREGAR EFECTOS HOVER
    // ============================================================
    agregarEfectoHover(lblVentas);
    agregarEfectoHover(lblAgregarProducto);
    agregarEfectoHover(lblHistorial);
    agregarEfectoHover(lblAdminUsuario);
    agregarEfectoHover(lblCorteCaja);
    
    // ============================================================
    // 4. CARGAR VENTANA DE VENTAS POR DEFECTO
    // ============================================================
    lblVentasMouseClicked(null);
    // ============================================================
// 5. CONFIGURAR BOTÓN DE CERRAR SESIÓN
// ============================================================
btnCerrarSesion.setText("Cerrar Sesión");
btnCerrarSesion.setFont(new java.awt.Font("Roboto", 1, 12));
btnCerrarSesion.setForeground(Color.RED);
btnCerrarSesion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
btnCerrarSesion.addActionListener(e -> cerrarSesion());
// Actualizar el estado del botón "Abrir Corte"
actualizarEstadoBotonAbrirCorte();
}
// ============================================================
// CERRAR SESIÓN
// ============================================================
private void cerrarSesion() {
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "¿Estás seguro de que quieres cerrar sesión?",
        "Cerrar Sesión",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );
    
    if (confirm == JOptionPane.YES_OPTION) {
        // 1. Limpiar el usuario logueado
        Login.usuarioLogueado = null;
        
        // 2. Cerrar la ventana principal
        this.dispose();
        
        // 3. Abrir el Login nuevamente
        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }
}
private void agregarEfectoHover(JLabel label) {
    // Conservar el borde del diseñador.
    label.putClientProperty(
        "bordeOriginalMenu",
        label.getBorder()
    );

    label.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            pintarOpcionMenu(label, true);
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent evt) {
            // Mantener el color si esta seccion sigue seleccionada.
            pintarOpcionMenu(label, label == menuSeleccionado);
        }
    });
}

private void pintarOpcionMenu(JLabel label, boolean resaltado) {
    javax.swing.border.Border bordeOriginal =
        (javax.swing.border.Border) label.getClientProperty(
            "bordeOriginalMenu"
        );

    if (resaltado) {
        label.setBackground(colorHover);
        label.setForeground(colorTextoHover);
        label.setOpaque(true);

        label.setBorder(
            javax.swing.BorderFactory.createCompoundBorder(
                bordeOriginal,
                javax.swing.BorderFactory.createMatteBorder(
                    0, 0, 3, 0,
                    new Color(180, 80, 180)
                )
            )
        );
    } else {
        label.setBackground(colorNormal);
        label.setForeground(colorTextoNormal);
        label.setOpaque(false);
        label.setBorder(bordeOriginal);
    }

    label.repaint();
}

private void seleccionarOpcionMenu(JLabel label) {
    if (menuSeleccionado != null) {
        pintarOpcionMenu(menuSeleccionado, false);
    }

    menuSeleccionado = label;
    pintarOpcionMenu(menuSeleccionado, true);
}
    // ============================================================
// ACTUALIZAR ESTADO DEL BOTÓN "ABRIR CORTE"
// ============================================================
private void actualizarEstadoBotonAbrirCorte() {
    // Verificar si hay un corte abierto
    boolean corteAbierto = verificarCorteAbierto();
    
    if (corteAbierto) {
        btnAbrirCorte.setText("✅ CORTE ABIERTO");
        btnAbrirCorte.setBackground(new Color(200, 255, 200));
        btnAbrirCorte.setForeground(new Color(0, 120, 0));
        btnAbrirCorte.setEnabled(false);
    } else {
        btnAbrirCorte.setText("🔑 ABRIR CORTE");
        btnAbrirCorte.setBackground(new Color(255, 200, 200));
        btnAbrirCorte.setForeground(Color.BLACK);
        btnAbrirCorte.setEnabled(true);
    }
}

// ============================================================
// VERIFICAR SI HAY CORTE ABIERTO
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
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblVentas = new javax.swing.JLabel();
        lblAgregarProducto = new javax.swing.JLabel();
        lblHistorial = new javax.swing.JLabel();
        lblAdminUsuario = new javax.swing.JLabel();
        btnCerrarSesion = new javax.swing.JButton();
        lblBienvenida = new javax.swing.JLabel();
        lblCorteCaja = new javax.swing.JLabel();
        btnAbrirCorte = new javax.swing.JButton();
        Contenedor = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(1500, 800));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 204, 204));
        jPanel1.setMaximumSize(new java.awt.Dimension(1500, 800));
        jPanel1.setMinimumSize(new java.awt.Dimension(1500, 800));
        jPanel1.setPreferredSize(new java.awt.Dimension(1500, 800));
        jPanel1.setLayout(null);

        jPanel2.setBackground(new java.awt.Color(242, 230, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        jPanel2.setMaximumSize(new java.awt.Dimension(240, 800));
        jPanel2.setMinimumSize(new java.awt.Dimension(240, 800));
        jPanel2.setPreferredSize(new java.awt.Dimension(240, 800));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Roboto Medium", 1, 18)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/VISTA/IMAGENES/logo bien1.png"))); // NOI18N
        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 220, 170));

        lblVentas.setBackground(new java.awt.Color(0, 0, 0));
        lblVentas.setFont(new java.awt.Font("Roboto Condensed", 1, 24)); // NOI18N
        lblVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/VISTA/IMAGENES/ICONO DE VENTAS 11.jpg"))); // NOI18N
        lblVentas.setText("VENTAS");
        lblVentas.setToolTipText("");
        lblVentas.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        lblVentas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblVentasMouseClicked(evt);
            }
        });
        jPanel2.add(lblVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 220, 200, 73));

        lblAgregarProducto.setBackground(new java.awt.Color(0, 0, 0));
        lblAgregarProducto.setFont(new java.awt.Font("Roboto Condensed", 1, 24)); // NOI18N
        lblAgregarProducto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAgregarProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/VISTA/IMAGENES/IAP2.jpg"))); // NOI18N
        lblAgregarProducto.setText("CATALOGO");
        lblAgregarProducto.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        lblAgregarProducto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblAgregarProducto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAgregarProductoMouseClicked(evt);
            }
        });
        jPanel2.add(lblAgregarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 200, 80));

        lblHistorial.setBackground(new java.awt.Color(0, 0, 0));
        lblHistorial.setFont(new java.awt.Font("Roboto Condensed", 1, 24)); // NOI18N
        lblHistorial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/VISTA/IMAGENES/icreve2.jpg"))); // NOI18N
        lblHistorial.setText("HISTORIAL");
        lblHistorial.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        lblHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblHistorialMouseClicked(evt);
            }
        });
        jPanel2.add(lblHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, 200, 80));

        lblAdminUsuario.setBackground(new java.awt.Color(0, 0, 0));
        lblAdminUsuario.setFont(new java.awt.Font("Roboto Condensed", 1, 15)); // NOI18N
        lblAdminUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/VISTA/IMAGENES/icreve2.jpg"))); // NOI18N
        lblAdminUsuario.setText("ADMIN USUARIO");
        lblAdminUsuario.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        lblAdminUsuario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblAdminUsuario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAdminUsuarioMouseClicked(evt);
            }
        });
        jPanel2.add(lblAdminUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 490, 200, 80));

        btnCerrarSesion.setText("Cerrar Sesión");
        jPanel2.add(btnCerrarSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 760, -1, -1));

        lblBienvenida.setText("Bienvenida:");
        jPanel2.add(lblBienvenida, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, -1, -1));

        lblCorteCaja.setBackground(new java.awt.Color(0, 0, 0));
        lblCorteCaja.setFont(new java.awt.Font("Roboto Condensed", 1, 15)); // NOI18N
        lblCorteCaja.setIcon(new javax.swing.ImageIcon(getClass().getResource("/VISTA/IMAGENES/icreve2.jpg"))); // NOI18N
        lblCorteCaja.setText("CORTE DE CAJA");
        lblCorteCaja.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        lblCorteCaja.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblCorteCaja.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCorteCajaMouseClicked(evt);
            }
        });
        jPanel2.add(lblCorteCaja, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 580, 200, 80));

        btnAbrirCorte.setText("ABRIR CORTE");
        btnAbrirCorte.addActionListener(this::btnAbrirCorteActionPerformed);
        jPanel2.add(btnAbrirCorte, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, -1, -1));

        jPanel1.add(jPanel2);
        jPanel2.setBounds(0, 0, 220, 800);

        Contenedor.setBackground(new java.awt.Color(255, 214, 234));
        Contenedor.setMaximumSize(new java.awt.Dimension(1280, 800));
        Contenedor.setMinimumSize(new java.awt.Dimension(1280, 800));
        Contenedor.setPreferredSize(new java.awt.Dimension(1280, 800));
        Contenedor.setLayout(new java.awt.BorderLayout());
        jPanel1.add(Contenedor);
        Contenedor.setBounds(220, 0, 1280, 800);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblVentasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblVentasMouseClicked
    Ventas_Categorias panel = new  Ventas_Categorias ();
       Contenedor.removeAll();
       Contenedor.add(panel);
       Contenedor.revalidate();
       Contenedor.repaint();
       
    seleccionarOpcionMenu(lblVentas);
    }//GEN-LAST:event_lblVentasMouseClicked

    private void lblAgregarProductoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAgregarProductoMouseClicked
    AgregarProducto panel2 = new AgregarProducto();
     Contenedor.removeAll();
     Contenedor.add(panel2);
     Contenedor.revalidate();
     Contenedor.repaint();
     
    seleccionarOpcionMenu(lblAgregarProducto);
    }//GEN-LAST:event_lblAgregarProductoMouseClicked

    private void lblHistorialMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblHistorialMouseClicked
     Historial_Ventas panel3 = new Historial_Ventas ();
       Contenedor.removeAll();
       Contenedor.add(panel3);
       Contenedor.revalidate();
       Contenedor.repaint();
       
    seleccionarOpcionMenu(lblHistorial);
    }//GEN-LAST:event_lblHistorialMouseClicked

    private void lblAdminUsuarioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAdminUsuarioMouseClicked
       AdminUsuario panel4 = new AdminUsuario ();
       Contenedor.removeAll();
       Contenedor.add(panel4);
       Contenedor.revalidate();
       Contenedor.repaint();
       
    seleccionarOpcionMenu(lblAdminUsuario);
    }//GEN-LAST:event_lblAdminUsuarioMouseClicked

    private void lblCorteCajaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCorteCajaMouseClicked
        CorteCaja panel5 = new CorteCaja ();
       Contenedor.removeAll();
       Contenedor.add(panel5);
       Contenedor.revalidate();
       Contenedor.repaint();
       
    seleccionarOpcionMenu(lblCorteCaja);
    
    }//GEN-LAST:event_lblCorteCajaMouseClicked

    private void btnAbrirCorteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirCorteActionPerformed
       // Verificar si el panel CorteCaja está cargado
    if (Contenedor.getComponentCount() > 0) {
        java.awt.Component comp = Contenedor.getComponent(0);
        if (comp instanceof CorteCaja) {
            // Llamar al método abrirCorteManual del panel CorteCaja
            ((CorteCaja) comp).abrirCorteManual();
            
            // Actualizar el estado del botón
            actualizarEstadoBotonAbrirCorte();
        } else {
            JOptionPane.showMessageDialog(this,
                "⚠️ Primero selecciona el módulo CORTE DE CAJA",
                "Abrir Corte",
                JOptionPane.INFORMATION_MESSAGE);
        }
    } else {
        JOptionPane.showMessageDialog(this,
            "⚠️ Primero selecciona el módulo CORTE DE CAJA",
            "Abrir Corte",
            JOptionPane.INFORMATION_MESSAGE);
    }
    }//GEN-LAST:event_btnAbrirCorteActionPerformed

    /**
     * @param args the command line arguments
     */
public static void main(String args[]) {
    /* Set the Nimbus look and feel */
    try {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
        logger.log(java.util.logging.Level.SEVERE, null, ex);
    }

    /* Create and display the form */
    // ✅ Ahora inicia desde Login, no desde VentanaPrincipal
    java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Contenedor;
    private javax.swing.JButton btnAbrirCorte;
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblAdminUsuario;
    private javax.swing.JLabel lblAgregarProducto;
    private javax.swing.JLabel lblBienvenida;
    private javax.swing.JLabel lblCorteCaja;
    private javax.swing.JLabel lblHistorial;
    private javax.swing.JLabel lblVentas;
    // End of variables declaration//GEN-END:variables
}
