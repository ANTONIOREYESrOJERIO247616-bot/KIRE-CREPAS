
package VISTA;

import Conexion.Conexion;
import DETALLES.tablaVcat.WrapRenderer;
import VISTA.DETALLESDEVENTAS.SelectorExtras;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.TableColumn;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

public class Ventas_Categorias extends javax.swing.JPanel {

public Ventas_Categorias() {
    initComponents();
    VISTA.AGREGARPRODUCTOS.FiltroPrecio.aplicar(txtCostoEnvio);
    configurarCarrito();
    tablaCarrito.setFont(
    new java.awt.Font("Roboto Light", java.awt.Font.BOLD, 13)
);
    configurarTablaCategorias();
    cargarCategorias();
    // ✅ Cambiar fondo del JScrollPane
   
    jScrollPane1.getViewport().setBackground(new Color(255, 255, 255));
    jScrollPane2.getViewport().setBackground(new Color(255, 255, 255));
    
     // ✅ LIMPIAR EL CARRITO AL INICIAR
    DefaultTableModel modeloCarrito = (DefaultTableModel) tablaCarrito.getModel();
    modeloCarrito.setRowCount(0);
    // ✅ Aplicar renderer con wrap a TODAS las columnas del carrito
for (int i = 0; i < tablaCarrito.getColumnCount(); i++) {
    tablaCarrito.getColumnModel().getColumn(i).setCellRenderer(new WrapRenderer());
}

// ✅ Listener para recalcular altos si el usuario cambia el ancho de columnas
tablaCarrito.getColumnModel().addColumnModelListener(new javax.swing.event.TableColumnModelListener() {
    @Override public void columnAdded(javax.swing.event.TableColumnModelEvent e) {}
    @Override public void columnRemoved(javax.swing.event.TableColumnModelEvent e) {}
    @Override public void columnMoved(javax.swing.event.TableColumnModelEvent e) {}
    @Override public void columnMarginChanged(javax.swing.event.ChangeEvent e) {
        recalcularTodosLosAltos();
    }
    @Override public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) {}
});
    
      // ✅ SELECCIONAR CREPAS POR DEFECTO
    seleccionarCategoriaEspecial();
    configurarEntrega();
}
private void configurarEntrega() {
    cmbTipoEntrega.setModel(
        new javax.swing.DefaultComboBoxModel<>(
            new String[]{"LOCAL", "DOMICILIO"}
        )
    );

    cmbTipoEntrega.setSelectedItem("LOCAL");
    txtCostoEnvio.setText("0.00");
    txtCostoEnvio.setEnabled(false);
    txtCostoEnvio.addFocusListener(
    new java.awt.event.FocusAdapter() {
        @Override
        public void focusGained(java.awt.event.FocusEvent e) {
            String texto = txtCostoEnvio.getText().trim()
                    .replace(",", ".");

            try {
                if (!texto.isEmpty()
                        && new BigDecimal(texto).signum() == 0) {
                    txtCostoEnvio.setText("");
                }
            } catch (NumberFormatException ex) {
                // Conservar el texto para que el usuario lo corrija.
            }
        }

        @Override
        public void focusLost(java.awt.event.FocusEvent e) {
            if (txtCostoEnvio.getText().trim().isEmpty()) {
                txtCostoEnvio.setText("0.00");
            }
        }
    }
);

    cmbTipoEntrega.addActionListener(e -> {
        boolean domicilio =
                "DOMICILIO".equals(cmbTipoEntrega.getSelectedItem());

        txtCostoEnvio.setEnabled(domicilio);
        
        if (!domicilio) {
    txtCostoEnvio.setText("0.00");
}

        

        calcularTotal();
    });

    txtCostoEnvio.getDocument().addDocumentListener(
        new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(
                    javax.swing.event.DocumentEvent e) {
                calcularTotal();
            }

            @Override
            public void removeUpdate(
                    javax.swing.event.DocumentEvent e) {
                calcularTotal();
            }

            @Override
            public void changedUpdate(
                    javax.swing.event.DocumentEvent e) {
                calcularTotal();
            }
        }
    );

    calcularTotal();
}

private BigDecimal obtenerCostoEnvio() {
    if (!"DOMICILIO".equals(cmbTipoEntrega.getSelectedItem())) {
        return new BigDecimal("0.00");
    }

    String texto = txtCostoEnvio.getText().trim()
            .replace(",", ".");
    if (texto.isEmpty()) {
    return new BigDecimal("0.00");
}

    // Acepta cero o un importe positivo con hasta dos decimales.
    if (!texto.matches("\\d+(\\.\\d{1,2})?")) {
        throw new IllegalArgumentException(
            "Escribe un costo de envio valido, por ejemplo: 25.00"
        );
    }

    BigDecimal costo = new BigDecimal(texto)
            .setScale(2, RoundingMode.UNNECESSARY);

    if (costo.compareTo(new BigDecimal("99999999.99")) > 0) {
        throw new IllegalArgumentException(
            "El costo de envio es demasiado grande"
        );
    }

    return costo;
}

private BigDecimal obtenerTotalPedido() {
    DefaultTableModel modelo =
            (DefaultTableModel) tablaCarrito.getModel();

    // Sin productos no hay un pedido que cobrar.
    if (modelo.getRowCount() == 0) {
        return new BigDecimal("0.00");
    }

    BigDecimal totalProductos = BigDecimal.ZERO;

    for (int i = 0; i < modelo.getRowCount(); i++) {
        BigDecimal subtotal =
                (BigDecimal) modelo.getValueAt(i, 4);

        totalProductos = totalProductos.add(subtotal);
    }

    BigDecimal total = totalProductos.add(obtenerCostoEnvio())
            .setScale(2, RoundingMode.UNNECESSARY);

    if (total.compareTo(new BigDecimal("99999999.99")) > 0) {
        throw new IllegalArgumentException(
            "El total del pedido es demasiado grande"
        );
    }

    return total;
}
// ============================================================
// SELECCIONAR AUTOMÁTICAMENTE LA CATEGORÍA ESPECIAL (CREPAS)
// ============================================================
private void seleccionarCategoriaEspecial() {
    DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();

    for (int i = 0; i < modelo.getRowCount(); i++) {
        String fila = modelo.getValueAt(i, 0).toString();
        // Formato: "id - nombre - esEspecial"
        String[] partes = fila.split(" - ");
        boolean esEspecial = Boolean.parseBoolean(partes[2]);

        if (esEspecial) {
            // Seleccionar la fila en la tabla
            jTable1.setRowSelectionInterval(i, i);

            // Cargar su panel
            int idCategoria = Integer.parseInt(partes[0]);
            String nombre = partes[1];
            cargarPanelCategoria(idCategoria, nombre, esEspecial);

            break;
        }
    }
}

 // ============================================================
    // CONFIGURAR LA TABLA DE CATEGORÍAS
    // ============================================================
    private void configurarTablaCategorias() {
        // Al hacer clic en una fila, cargar el panel correspondiente
        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = jTable1.getSelectedRow();
                if (fila == -1) return;

                int idCategoria = Integer.parseInt(
                    jTable1.getValueAt(fila, 0).toString().split(" - ")[0]);
                String nombre = jTable1.getValueAt(fila, 0).toString().split(" - ")[1];
                boolean esEspecial = Boolean.parseBoolean(
                    jTable1.getValueAt(fila, 0).toString().split(" - ")[2]);

                cargarPanelCategoria(idCategoria, nombre, esEspecial);
            }
        });
    }
      // ============================================================
    // CARGAR CATEGORÍAS EN jTable1
    // ============================================================
    private void cargarCategorias() {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        modelo.setRowCount(0);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Conexion.getConexion();
           String sql =
    "SELECT id_categoria, nombre, es_especial FROM categorias "
    + "WHERE activo = TRUE AND disponible = TRUE "
    + "ORDER BY es_especial DESC, nombre";
                         
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_categoria");
                String nombre = rs.getString("nombre");
                boolean esEspecial = rs.getBoolean("es_especial");

                String fila = id + " - " + nombre + " - " + esEspecial;
                modelo.addRow(new Object[]{fila});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar categorías: " + e.getMessage());
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
 private void cargarPanelCategoria(int idCategoria, String nombre, boolean esEspecial) {
    jPanel1.removeAll();

    if (esEspecial) {
        Venta panelCrepas = new Venta(this);
        jPanel1.add(panelCrepas, BorderLayout.CENTER);
    } else {
       PanelProductosPorCategoria panelProductos = 
    new PanelProductosPorCategoria(idCategoria, nombre, this);
        jPanel1.add(panelProductos, BorderLayout.CENTER);
    }

    jPanel1.revalidate();
    jPanel1.repaint();
}
            private void configurarCarrito() {
        txtTotal.setEditable(false);
        txtTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtTotal.setText("$0.00");

        txtCambio.setEditable(false);
        txtCambio.setFocusable(false);

        txtEfectivo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != '.' && c != '\b') {
                    evt.consume();
                }
                if (c == '.' && txtEfectivo.getText().contains(".")) {
                    evt.consume();
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularCambio();
            }
        });
    }
 public boolean agregarAlCarrito(
        String categoria,
        String producto,
        int cantidad,
        double precioUnitario) {

    if (cantidad <= 0
            || !Double.isFinite(precioUnitario)
            || precioUnitario < 0) {
        JOptionPane.showMessageDialog(
            this,
            "Cantidad o precio del producto invalido."
        );
        return false;
    }

    int idCategoria;

    String sql =
        "SELECT id_categoria FROM categorias "
+ "WHERE nombre = ? AND activo = TRUE "
+ "AND disponible = TRUE";

    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, categoria);

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                JOptionPane.showMessageDialog(
                    this,
                    "La categoria ya no esta disponible."
                );
                return false;
            }

            idCategoria = rs.getInt("id_categoria");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al consultar la categoria:\n" + e.getMessage()
        );
        return false;
    }

    SelectorExtras.Seleccion extras = SelectorExtras.mostrar(
        this,
        idCategoria,
        producto.replace("<br>", " / ")
    );

    // Cancelar el selector no agrega el producto.
    if (extras == null) {
        return false;
    }

    BigDecimal precioBase = BigDecimal.valueOf(precioUnitario)
        .setScale(2, RoundingMode.HALF_UP);

    BigDecimal subtotal = precioBase
        .add(extras.getTotalPorUnidad())
        .multiply(BigDecimal.valueOf(cantidad));

    if (subtotal.compareTo(new BigDecimal("99999999.99")) > 0) {
        JOptionPane.showMessageDialog(
            this,
            "El subtotal supera el importe permitido."
        );
        return false;
    }

    DefaultTableModel modelo =
        (DefaultTableModel) tablaCarrito.getModel();

    modelo.addRow(new Object[]{
        categoria,
        producto,
        cantidad,
        extras,
        subtotal
    });

    calcularTotal();
    recalcularTodosLosAltos();

    return true;
}
      // ============================================================
// AJUSTAR ALTO DE UNA FILA SEGÚN EL TEXTO DE LA COLUMNA "PRODUCTO"
// ============================================================
private void ajustarAltoFila(int fila) {
    if (fila < 0 || fila >= tablaCarrito.getRowCount()) {
        return;
    }

    int altoNecesario = 25;

    for (int columna = 0;
            columna < tablaCarrito.getColumnCount();
            columna++) {

        int anchoCelda = tablaCarrito.getCellRect(
            fila, columna, false
        ).width;

        // Esperar a que la columna tenga un ancho valido.
        if (anchoCelda <= 0) {
            continue;
        }

        javax.swing.table.TableCellRenderer renderer =
            tablaCarrito.getCellRenderer(fila, columna);

        java.awt.Component componente =
            tablaCarrito.prepareRenderer(
                renderer, fila, columna
            );

        componente.setSize(anchoCelda, Short.MAX_VALUE);

        int altoCelda =
            componente.getPreferredSize().height
            + tablaCarrito.getRowMargin();

        altoNecesario = Math.max(altoNecesario, altoCelda);
    }

    if (tablaCarrito.getRowHeight(fila) != altoNecesario) {
        tablaCarrito.setRowHeight(fila, altoNecesario);
    }
}
// ============================================================
// RECALCULAR TODOS LOS ALTOS (por si cambia el ancho de columna)
// ============================================================
private void recalcularTodosLosAltos() {
    javax.swing.SwingUtilities.invokeLater(() -> {
        for (int i = 0; i < tablaCarrito.getRowCount(); i++) {
            ajustarAltoFila(i);
        }
    });
}
  private void calcularTotal() {
    try {
        BigDecimal total = obtenerTotalPedido();

        txtTotal.setText("$" + total.toPlainString());
        txtCambio.setText("");
        calcularCambio();

    } catch (IllegalArgumentException e) {
        // Mientras se escribe un importe incompleto,
        // no mostrar un total ni un cambio incorrectos.
        txtTotal.setText("");
        txtCambio.setText("");
    }
}
        private void calcularCambio() {
        try {
            String efectivoStr = txtEfectivo.getText().trim();
            if (!efectivoStr.isEmpty()) {
                double efectivo = Double.parseDouble(efectivoStr);
                String totalStr = txtTotal.getText().replace("$", "");
                double total = Double.parseDouble(totalStr);
                double cambio = efectivo - total;

                if (cambio >= 0) {
                    txtCambio.setText("$" + String.format("%.2f", cambio));
                } else {
                    txtCambio.setText("Falta: $" + String.format("%.2f", -cambio));
                }
            } else {
                txtCambio.setText("");
            }
        } catch (NumberFormatException e) {
            // Ignorar
        }
    }  
            private boolean verificarCorteAbierto() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Conexion.getConexion();
            String sql = "SELECT id_corte FROM cortes_caja " +
                         "WHERE estado = 'ABIERTO' AND DATE(fecha_apertura) = CURDATE()";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jpListCategorias = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        txtEfectivo = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtCambio = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaCarrito = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtCostoEnvio = new javax.swing.JTextField();
        cmbTipoEntrega = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        btnGuardarPedido = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnCancelarPedido = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(1260, 800));
        setMinimumSize(new java.awt.Dimension(1260, 800));
        setName(""); // NOI18N
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setMaximumSize(new java.awt.Dimension(610, 800));
        jPanel1.setMinimumSize(new java.awt.Dimension(610, 800));
        jPanel1.setPreferredSize(new java.awt.Dimension(610, 800));
        jPanel1.setLayout(new java.awt.BorderLayout());
        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 0, 610, 800));

        jpListCategorias.setBackground(new java.awt.Color(204, 255, 255));
        jpListCategorias.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jTable1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTable1.setFont(new java.awt.Font("Roboto Black", 1, 12)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Categorias"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setGridColor(new java.awt.Color(204, 204, 204));
        jTable1.setRowHeight(60);
        jTable1.setSelectionBackground(new java.awt.Color(102, 153, 255));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jpListCategoriasLayout = new javax.swing.GroupLayout(jpListCategorias);
        jpListCategorias.setLayout(jpListCategoriasLayout);
        jpListCategoriasLayout.setHorizontalGroup(
            jpListCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
        );
        jpListCategoriasLayout.setVerticalGroup(
            jpListCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpListCategoriasLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        add(jpListCategorias, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 100, 800));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel13.setFont(new java.awt.Font("Roboto Black", 1, 18)); // NOI18N
        jLabel13.setText("Efectivo");
        jPanel6.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, -1));

        txtEfectivo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtEfectivo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel6.add(txtEfectivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 137, 40));

        jLabel12.setFont(new java.awt.Font("Roboto Black", 1, 18)); // NOI18N
        jLabel12.setText("Cambio");
        jPanel6.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 40, -1, -1));

        txtCambio.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCambio.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCambio.addActionListener(this::txtCambioActionPerformed);
        txtCambio.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCambioKeyReleased(evt);
            }
        });
        jPanel6.add(txtCambio, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 60, 135, 41));

        tablaCarrito.setFont(new java.awt.Font("Roboto Light", 1, 13)); // NOI18N
        tablaCarrito.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Categoria", "Producto", "Cantidad ", "Extras", "Subtotal"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaCarrito.setRowHeight(25);
        tablaCarrito.setSelectionBackground(new java.awt.Color(102, 153, 255));
        jScrollPane2.setViewportView(tablaCarrito);
        if (tablaCarrito.getColumnModel().getColumnCount() > 0) {
            tablaCarrito.getColumnModel().getColumn(0).setMinWidth(70);
            tablaCarrito.getColumnModel().getColumn(0).setPreferredWidth(70);
            tablaCarrito.getColumnModel().getColumn(0).setMaxWidth(70);
            tablaCarrito.getColumnModel().getColumn(2).setMinWidth(50);
            tablaCarrito.getColumnModel().getColumn(2).setPreferredWidth(50);
            tablaCarrito.getColumnModel().getColumn(2).setMaxWidth(50);
            tablaCarrito.getColumnModel().getColumn(3).setMinWidth(110);
            tablaCarrito.getColumnModel().getColumn(3).setPreferredWidth(110);
            tablaCarrito.getColumnModel().getColumn(3).setMaxWidth(110);
            tablaCarrito.getColumnModel().getColumn(4).setMinWidth(60);
            tablaCarrito.getColumnModel().getColumn(4).setPreferredWidth(60);
            tablaCarrito.getColumnModel().getColumn(4).setMaxWidth(60);
        }

        jPanel6.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 570, 580));

        jLabel14.setFont(new java.awt.Font("Roboto ExtraBold", 1, 24)); // NOI18N
        jLabel14.setText("CARRITO");
        jPanel6.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 0, -1, -1));

        jLabel5.setFont(new java.awt.Font("Roboto Medium", 0, 18)); // NOI18N
        jLabel5.setText("Total:");
        jPanel6.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 690, -1, -1));

        txtTotal.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel6.add(txtTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 690, 131, -1));

        jLabel1.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel1.setText("Entrega:");
        jPanel6.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 10, -1, -1));

        txtCostoEnvio.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCostoEnvio.setEnabled(false);
        txtCostoEnvio.addActionListener(this::txtCostoEnvioActionPerformed);
        jPanel6.add(txtCostoEnvio, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 50, 130, 40));

        cmbTipoEntrega.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel6.add(cmbTipoEntrega, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 10, -1, -1));

        jLabel2.setText("Envio: $");
        jPanel6.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 60, -1, -1));

        add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 0, 570, 730));

        btnGuardarPedido.setText("Guardar");
        btnGuardarPedido.addActionListener(this::btnGuardarPedidoActionPerformed);
        add(btnGuardarPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 750, 110, 40));

        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(1000, 750, 90, 40));

        btnCancelarPedido.setText("Cancelar");
        btnCancelarPedido.addActionListener(this::btnCancelarPedidoActionPerformed);
        add(btnCancelarPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 750, -1, 40));
    }// </editor-fold>//GEN-END:initComponents

    private void txtCambioKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCambioKeyReleased

        try {
            String efectivoStr = txtEfectivo.getText().trim();
            if (!efectivoStr.isEmpty()) {
                double efectivo = Double.parseDouble(efectivoStr);
                String totalStr = txtTotal.getText().replace("$", "");
                double total = Double.parseDouble(totalStr);
                double cambio = efectivo - total;

                if (cambio >= 0) {
                    txtCambio.setText("$" + String.format("%.2f", cambio));
                } else {
                    txtCambio.setText("Falta: $" + String.format("%.2f", -cambio));
                }
            }
        } catch (NumberFormatException e) {
            // No hacer nada
        }
    }//GEN-LAST:event_txtCambioKeyReleased

    private void txtCambioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCambioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCambioActionPerformed

    private void btnCancelarPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarPedidoActionPerformed
       
        DefaultTableModel modelo = (DefaultTableModel) tablaCarrito.getModel();
        modelo.setRowCount(0);
        txtEfectivo.setText("");
        txtCambio.setText("");
        txtTotal.setText("$0.00");
        // Resetear altos (aunque ya no hay filas, por si acaso)
      tablaCarrito.setRowHeight(25);
      cmbTipoEntrega.setSelectedItem("LOCAL");
      txtCostoEnvio.setText("0.00");
     txtCostoEnvio.setEnabled(false);
    }//GEN-LAST:event_btnCancelarPedidoActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed

        // Obtener la fila seleccionada
        int filaSeleccionada = tablaCarrito.getSelectedRow();

        // Verificar que haya una fila seleccionada
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para eliminar");
            return;
        }

        // Confirmar eliminación
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Seguro que quieres eliminar este producto?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Eliminar la fila
            DefaultTableModel modelo = (DefaultTableModel) tablaCarrito.getModel();
            modelo.removeRow(filaSeleccionada);

            // Recalcular el total
            calcularTotal();
            // Recalcular altos por si cambió el ancho disponible
            recalcularTodosLosAltos();

            // Limpiar el campo de efectivo y cambio
            txtEfectivo.setText("");
            txtCambio.setText("");
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnGuardarPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarPedidoActionPerformed
 

    DefaultTableModel modelo =
            (DefaultTableModel) tablaCarrito.getModel();

    // 1. Verificar que haya productos
    if (modelo.getRowCount() == 0) {
        JOptionPane.showMessageDialog(
            this, "No hay productos en el carrito"
        );
        return;
    }

    // 2. Verificar la sesion
    if (Login.usuarioLogueado == null) {
        JOptionPane.showMessageDialog(
            this, "Debes iniciar sesion para registrar una venta"
        );
        return;
    }
  // 3. Calcular el total incluyendo el envio
String tipoEntrega =
        String.valueOf(cmbTipoEntrega.getSelectedItem());

BigDecimal costoEnvio;
BigDecimal totalPedido;
double total;

try {
    costoEnvio = obtenerCostoEnvio();
    totalPedido = obtenerTotalPedido();

    if (totalPedido.signum() <= 0) {
        throw new IllegalArgumentException(
            "El total debe ser mayor que cero"
        );
    }

    total = totalPedido.doubleValue();
    txtTotal.setText("$" + totalPedido.toPlainString());

} catch (IllegalArgumentException e) {
    JOptionPane.showMessageDialog(this, e.getMessage());

    if (txtCostoEnvio.isEnabled()) {
        txtCostoEnvio.requestFocusInWindow();
    }

    return;
}
 

    // 4. Leer y validar el efectivo
    String efectivoStr = txtEfectivo.getText().trim();

    if (efectivoStr.isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "Ingresa el efectivo para completar la venta"
        );
        txtEfectivo.requestFocusInWindow();
        return;
    }

    double efectivo;

    try {
        efectivo = Double.parseDouble(
            efectivoStr.replace(",", ".")
        );

        if (!Double.isFinite(efectivo) || efectivo < 0) {
            throw new NumberFormatException();
        }

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
            this, "Ingresa un monto de efectivo valido"
        );
        txtEfectivo.requestFocusInWindow();
        return;
    }

    // 5. Verificar que alcance el efectivo
    if (efectivo < total) {
        JOptionPane.showMessageDialog(
            this,
            "El efectivo no alcanza para cubrir el total.\n"
            + "Total: $" + String.format("%.2f", total) + "\n"
            + "Efectivo: $" + String.format("%.2f", efectivo) + "\n"
            + "Faltan: $" + String.format("%.2f", total - efectivo),
            "Efectivo insuficiente",
            JOptionPane.WARNING_MESSAGE
        );
        txtEfectivo.requestFocusInWindow();
        return;
    }

    double cambio = efectivo - total;
    int idPedido;
    int idCorte;

    // 6. Guardar todo en una misma transaccion
    try (java.sql.Connection conn = Conexion.getConexion()) {

        conn.setAutoCommit(false);

        try {
            // Buscar el corte abierto y bloquearlo mientras se guarda
            String sqlCorte =
                    "SELECT id_corte, "
                    + "DATE(fecha_apertura) = CURDATE() AS es_hoy "
                    + "FROM cortes_caja "
                    + "WHERE estado = 'ABIERTO' "
                    + "ORDER BY id_corte "
                    + "FOR UPDATE";

            try (java.sql.PreparedStatement psCorte =
                        conn.prepareStatement(sqlCorte);
                 java.sql.ResultSet rsCorte = psCorte.executeQuery()) {

                if (!rsCorte.next()) {
                    throw new java.sql.SQLException(
                        "No hay un corte abierto.\n"
                        + "Abre el corte de caja antes de vender."
                    );
                }

                idCorte = rsCorte.getInt("id_corte");
                boolean esHoy = rsCorte.getBoolean("es_hoy");

                if (rsCorte.next()) {
                    throw new java.sql.SQLException(
                        "Hay varios cortes abiertos.\n"
                        + "Es necesario revisarlos antes de vender."
                    );
                }

                if (!esHoy) {
                    throw new java.sql.SQLException(
                        "El corte abierto no corresponde al dia de hoy.\n"
                        + "Regulariza ese corte antes de registrar ventas."
                    );
                }
            }

            // 7. Guardar el pedido incluyendo su corte
            String sqlPedido =
    "INSERT INTO pedidos "
    + "(total, efectivo, cambio, id_usuario, id_corte, "
    + "tipo_entrega, costo_envio) "
    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (java.sql.PreparedStatement psPedido =
                    conn.prepareStatement(
                        sqlPedido,
                        java.sql.Statement.RETURN_GENERATED_KEYS
                    )) {
                    psPedido.setBigDecimal(1, totalPedido);
psPedido.setDouble(2, efectivo);
psPedido.setDouble(3, cambio);
psPedido.setInt(4, Login.usuarioLogueado.idUsuario);
psPedido.setInt(5, idCorte);
psPedido.setString(6, tipoEntrega);
psPedido.setBigDecimal(7, costoEnvio);
               

                if (psPedido.executeUpdate() != 1) {
                    throw new java.sql.SQLException(
                        "No se pudo registrar el pedido."
                    );
                }

                try (java.sql.ResultSet rs =
                        psPedido.getGeneratedKeys()) {

                    if (!rs.next()) {
                        throw new java.sql.SQLException(
                            "No se pudo obtener el numero del pedido."
                        );
                    }

                    idPedido = rs.getInt(1);
                }
            }

        // 8. Guardar productos y extras en la misma transaccion.
String sqlDetalle =
    "INSERT INTO detalles_pedido "
    + "(id_pedido, categoria, producto, cantidad, subtotal) "
    + "VALUES (?, ?, ?, ?, ?)";

String sqlExtra =
    "INSERT INTO detalles_pedido_extras "
    + "(id_detalle, id_extra, nombre, cantidad, precio_unitario) "
    + "VALUES (?, ?, ?, ?, ?)";

try (PreparedStatement psDetalle = conn.prepareStatement(
            sqlDetalle,
            java.sql.Statement.RETURN_GENERATED_KEYS
        );
     PreparedStatement psExtra = conn.prepareStatement(sqlExtra)) {

    for (int i = 0; i < modelo.getRowCount(); i++) {

        String categoria = modelo.getValueAt(i, 0).toString();
        String producto = modelo.getValueAt(i, 1).toString();

        int cantidad =
            ((Number) modelo.getValueAt(i, 2)).intValue();

        Object valorExtras = modelo.getValueAt(i, 3);
        Object valorSubtotal = modelo.getValueAt(i, 4);

        if (!(valorExtras instanceof SelectorExtras.Seleccion)
                || !(valorSubtotal instanceof BigDecimal)) {
            throw new SQLException(
                "Datos invalidos en la fila " + (i + 1)
            );
        }

        SelectorExtras.Seleccion seleccion =
            (SelectorExtras.Seleccion) valorExtras;

        BigDecimal subtotal = (BigDecimal) valorSubtotal;

        if (cantidad <= 0 || subtotal.signum() < 0) {
            throw new SQLException(
                "Cantidad o subtotal invalido en la fila " + (i + 1)
            );
        }

        psDetalle.setInt(1, idPedido);
        psDetalle.setString(2, categoria);
        psDetalle.setString(3, producto);
        psDetalle.setInt(4, cantidad);
        psDetalle.setBigDecimal(5, subtotal);

        if (psDetalle.executeUpdate() != 1) {
            throw new SQLException(
                "No se pudo guardar el producto de la fila " + (i + 1)
            );
        }

        int idDetalle;

        try (ResultSet claves = psDetalle.getGeneratedKeys()) {
            if (!claves.next()) {
                throw new SQLException(
                    "No se pudo obtener el ID del detalle."
                );
            }

            idDetalle = claves.getInt(1);
        }

        for (SelectorExtras.Extra extra : seleccion.getExtras()) {
            psExtra.setInt(1, idDetalle);
            psExtra.setInt(2, extra.id);
            psExtra.setString(3, extra.nombre);

            // Cada extra se selecciona una vez por unidad de producto.
            psExtra.setInt(4, cantidad);
            psExtra.setBigDecimal(5, extra.precio);

            psExtra.addBatch();
        }
    }

    psExtra.executeBatch();
}   
            // Confirmar solo cuando se guardo todo
            conn.commit();

        } catch (java.sql.SQLException | RuntimeException e) {

            // Deshacer el pedido y sus detalles si ocurre un error
            try {
                conn.rollback();
            } catch (java.sql.SQLException errorRollback) {
                e.addSuppressed(errorRollback);
            }

            throw e;
        }

    } catch (java.sql.SQLException | RuntimeException e) {
        JOptionPane.showMessageDialog(
            this,
            "No se pudo completar el guardado del pedido:\n"
            + e.getMessage(),
            "Error al guardar",
            JOptionPane.ERROR_MESSAGE
        );
        e.printStackTrace();
        return;
    }
     // Armar el ticket antes de limpiar el carrito.
String textoTicket = construirTextoTicket(
    idPedido,
    totalPedido,
    efectivo,
    cambio,
    tipoEntrega,
    costoEnvio
);

// Limpiar el carrito y después mostrar el ticket.
btnCancelarPedidoActionPerformed(evt);
mostrarTicketEnPantalla(textoTicket, idPedido);
    
    
    
    }
    
 private String construirTextoTicket(
        int idPedido,
        BigDecimal totalPedido,
        double efectivo,
        double cambio,
        String tipoEntrega,
        BigDecimal costoEnvio) {

    String fecha = java.time.LocalDateTime.now().format(
        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    );

    BigDecimal efectivoTicket = BigDecimal.valueOf(efectivo)
        .setScale(2, RoundingMode.HALF_UP);

    BigDecimal cambioTicket = BigDecimal.valueOf(cambio)
        .setScale(2, RoundingMode.HALF_UP);

    String nombreCajero = Login.usuarioLogueado == null
        ? "N/A"
        : Login.usuarioLogueado.nombreCompleto;

    StringBuilder ticket = new StringBuilder();

    ticket.append("          CAFETERIA KIRE\n");
    ticket.append("           TICKET DE VENTA\n");
    ticket.append("----------------------------------------\n");
    ticket.append("Pedido: #").append(idPedido).append("\n");
    ticket.append("Fecha: ").append(fecha).append("\n");
    ticket.append("Cajero: ").append(nombreCajero).append("\n");
    ticket.append("----------------------------------------\n");

    DefaultTableModel modelo =
        (DefaultTableModel) tablaCarrito.getModel();

    for (int fila = 0; fila < modelo.getRowCount(); fila++) {
        String categoria = String.valueOf(modelo.getValueAt(fila, 0));

        String producto = String.valueOf(modelo.getValueAt(fila, 1))
            .replaceAll("(?i)<br\\s*/?>", " / ")
            .replace('\n', ' ');

        int cantidad =
            ((Number) modelo.getValueAt(fila, 2)).intValue();

        BigDecimal subtotal =
            (BigDecimal) modelo.getValueAt(fila, 4);

        ticket.append(cantidad)
            .append(" x ")
            .append(producto)
            .append("\n");

        ticket.append("  Categoria: ")
            .append(categoria)
            .append("\n");

        Object valorExtras = modelo.getValueAt(fila, 3);

        if (valorExtras instanceof SelectorExtras.Seleccion) {
            SelectorExtras.Seleccion seleccion =
                (SelectorExtras.Seleccion) valorExtras;

            for (SelectorExtras.Extra extra : seleccion.getExtras()) {
                ticket.append("  + ")
                    .append(extra.nombre)
                    .append(" ($")
                    .append(extra.precio.toPlainString())
                    .append(" c/u)\n");
            }
        }

        ticket.append("  Subtotal: $")
            .append(subtotal.toPlainString())
            .append("\n\n");
    }

    ticket.append("----------------------------------------\n");
    ticket.append("Entrega: ").append(tipoEntrega).append("\n");

    if ("DOMICILIO".equals(tipoEntrega)) {
        ticket.append("Envio: $")
            .append(costoEnvio.toPlainString())
            .append("\n");
    }

    ticket.append("----------------------------------------\n");
    ticket.append("TOTAL: $")
        .append(totalPedido.toPlainString())
        .append("\n");

    ticket.append("EFECTIVO: $")
        .append(efectivoTicket.toPlainString())
        .append("\n");

    ticket.append("CAMBIO: $")
        .append(cambioTicket.toPlainString())
        .append("\n\n");

    ticket.append("       Gracias por tu compra");

    return ticket.toString();
}

private void mostrarTicketEnPantalla(String contenido, int idPedido) {
    javax.swing.JTextArea areaTicket =
        new javax.swing.JTextArea(contenido);

    areaTicket.setEditable(false);
    areaTicket.setLineWrap(true);
    areaTicket.setWrapStyleWord(true);
    areaTicket.setCaretPosition(0);

    areaTicket.setFont(
        new java.awt.Font(
            java.awt.Font.MONOSPACED,
            java.awt.Font.PLAIN,
            14
        )
    );

    javax.swing.JScrollPane desplazamiento =
        new javax.swing.JScrollPane(areaTicket);

    desplazamiento.setPreferredSize(
        new java.awt.Dimension(400, 520)
    );

    JOptionPane.showMessageDialog(
        this,
        desplazamiento,
        "Ticket de venta #" + idPedido,
        JOptionPane.INFORMATION_MESSAGE
    );



    }//GEN-LAST:event_btnGuardarPedidoActionPerformed

    private void txtCostoEnvioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCostoEnvioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCostoEnvioActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelarPedido;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardarPedido;
    private javax.swing.JComboBox<String> cmbTipoEntrega;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel jpListCategorias;
    private javax.swing.JTable tablaCarrito;
    private javax.swing.JTextField txtCambio;
    private javax.swing.JTextField txtCostoEnvio;
    private javax.swing.JTextField txtEfectivo;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
