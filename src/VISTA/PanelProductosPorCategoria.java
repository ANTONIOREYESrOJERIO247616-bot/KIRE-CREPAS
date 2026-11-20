
package VISTA;

import Conexion.Conexion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
public class PanelProductosPorCategoria extends javax.swing.JPanel {
       private int idCategoria;
       private String nombreCategoria; 
    private Ventas_Categorias padre;
    
    public PanelProductosPorCategoria(int idCategoria, String nombreCategoria, Ventas_Categorias padre) {
    this.idCategoria = idCategoria;
    this.nombreCategoria = nombreCategoria;
    this.padre = padre;
    initComponents();
    cargarProductos();
}

    private void cargarProductos() {
    // 1. Panel con 4 columnas, filas automáticas
    JPanel panelTarjetas = new JPanel();
    panelTarjetas.setLayout(new GridLayout(0, 4, 15, 15));
    panelTarjetas.setBackground(new Color(255, 214, 234));
    panelTarjetas.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // ❗️ CLAVE: forzar que el panel NO se estire, sino que use su tamaño preferido
    // Esto es lo que evita que las tarjetas se encojan al crecer el scroll
    panelTarjetas.setPreferredSize(null);

    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        conn = Conexion.getConexion();
        String sql =
    "SELECT p.nombre, p.precio, p.imagen "
    + "FROM productos p "
    + "INNER JOIN categorias c "
    + "ON c.id_categoria = p.id_categoria "
    + "WHERE p.id_categoria = ? "
    + "AND p.activo = TRUE "
    + "AND p.disponible = TRUE "
    + "AND c.activo = TRUE "
    + "AND c.disponible = TRUE "
    + "ORDER BY p.nombre";
        
        ps = conn.prepareStatement(sql);
        ps.setInt(1, idCategoria);
        rs = ps.executeQuery();

        boolean hayProductos = false;

      while (rs.next()) {
    hayProductos = true;
    String nombre = rs.getString("nombre");
    double precio = rs.getDouble("precio");

    String imagen = rs.getString("imagen");
    if (imagen == null || imagen.isEmpty()) imagen = "sin_imagen.png";

    JPanel tarjeta = crearTarjeta(nombre, precio, imagen);
    panelTarjetas.add(tarjeta);   // ← ESTA LÍNEA FALTABA
}

        if (!hayProductos) {
            JLabel lblVacio = new JLabel("No hay productos en esta categoría");
            lblVacio.setHorizontalAlignment(SwingConstants.CENTER);
            lblVacio.setFont(new Font("Roboto", Font.ITALIC, 18));
            panelTarjetas.setLayout(new BorderLayout());
            panelTarjetas.add(lblVacio, BorderLayout.CENTER);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Error al cargar productos: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2. Panel intermedio que respeta el tamaño preferido del grid
    JPanel panelContenedor = new JPanel(new BorderLayout());
    panelContenedor.setBackground(new Color(255, 214, 234));
    panelContenedor.add(panelTarjetas, BorderLayout.NORTH);

    // 3. Scroll vertical
    JScrollPane scroll = new JScrollPane(panelContenedor);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    scroll.getVerticalScrollBar().setPreferredSize(
    new Dimension(5, 0)
);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

  // 4. Título de la categoría
JLabel lblTitulo = new JLabel(nombreCategoria, SwingConstants.CENTER);
lblTitulo.setFont(new Font("Roboto", Font.BOLD, 35));
lblTitulo.setForeground(Color.BLACK);
lblTitulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
lblTitulo.setOpaque(true);
lblTitulo.setBackground(new Color(255, 214, 234));

// 5. Contenedor que agrupa el título + el scroll
JPanel panelPrincipal = new JPanel(new BorderLayout());
panelPrincipal.setBackground(new Color(255, 214, 234));
panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
panelPrincipal.add(scroll, BorderLayout.CENTER);

// 6. Agregar al panel
removeAll();
add(panelPrincipal, BorderLayout.CENTER);
revalidate();
repaint();
}
    // ============================================================
    // CREAR TARJETA DE PRODUCTO
    // ============================================================
   private JPanel crearTarjeta(String nombre, double precio, String nombreImagen) {
    JPanel tarjeta = new JPanel();
    tarjeta.setLayout(new BorderLayout());
    tarjeta.setBackground(Color.WHITE);
    tarjeta.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    tarjeta.setPreferredSize(new Dimension(135, 200));
    tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // 1. IMAGEN (arriba)
    JLabel lblImagen = new JLabel("", SwingConstants.CENTER);
    try {
        String ruta = "src/VISTA/IMAGENES/productos/" + nombreImagen;
        javax.swing.ImageIcon icono = new javax.swing.ImageIcon(ruta);
        java.awt.Image img = icono.getImage().getScaledInstance(130, 130, java.awt.Image.SCALE_SMOOTH);
        lblImagen.setIcon(new javax.swing.ImageIcon(img));
    } catch (Exception e) {
        // Si falla, intenta con la imagen por defecto
        try {
            String ruta = "src/VISTA/IMAGENES/productos/sin_imagen.png";
            javax.swing.ImageIcon icono = new javax.swing.ImageIcon(ruta);
            java.awt.Image img = icono.getImage().getScaledInstance(110, 120, java.awt.Image.SCALE_SMOOTH);
            lblImagen.setIcon(new javax.swing.ImageIcon(img));
        } catch (Exception ex) {
            lblImagen.setText("Sin imagen");
        }
    }
    lblImagen.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
    tarjeta.add(lblImagen, BorderLayout.NORTH);

    // 2. NOMBRE (centro)
    JLabel lblNombre = new JLabel(nombre, SwingConstants.CENTER);
    lblNombre.setFont(new Font("Roboto", Font.BOLD, 18));
    lblNombre.setForeground(Color.BLACK);
    lblNombre.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    tarjeta.add(lblNombre, BorderLayout.CENTER);

    // 3. PRECIO (abajo)
    JLabel lblPrecio = new JLabel("$" + String.format("%.2f", precio), SwingConstants.CENTER);
    lblPrecio.setFont(new Font("Roboto", Font.BOLD, 18));
    lblPrecio.setForeground(new Color(180, 0, 0));   // rojo oscuro
    lblPrecio.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
    tarjeta.add(lblPrecio, BorderLayout.SOUTH);

    // Hover
    tarjeta.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            tarjeta.setBackground(new Color(255, 230, 240));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            tarjeta.setBackground(Color.WHITE);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            agregarProductoAlCarrito(nombre, precio);
        }
    });

    return tarjeta;
}
   // ============================================================
    // AGREGAR AL CARRITO DEL PADRE
    // ============================================================
private void agregarProductoAlCarrito(String nombre, double precio) {
    padre.agregarAlCarrito(
        nombreCategoria,
        nombre,
        1,
        precio
    );
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(610, 800));
        setMinimumSize(new java.awt.Dimension(610, 800));
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

