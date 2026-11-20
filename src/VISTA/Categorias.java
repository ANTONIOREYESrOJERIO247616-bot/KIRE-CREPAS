
package VISTA;
import Conexion.Conexion;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author USER
 */
public class Categorias extends javax.swing.JPanel {
private boolean restaurandoDisponibilidad = false;

private final java.util.Map<Integer, Boolean> disponibilidadGuardada =
        new java.util.HashMap<>();
    public Categorias() {
          initComponents();
        configurarEventos();
        configurarDisponibilidad();
        cargarCategorias();
         jScrollPane1.getViewport().setBackground(new Color(255, 255, 255));
    }
   // ============================================================
    // CONFIGURAR EVENTOS
    // ============================================================
    private void configurarEventos() {
        btnAgregar.addActionListener(e -> agregarCategoria());
        btnEditar.addActionListener(e -> editarCategoria());
        btnEliminar.addActionListener(e -> eliminarCategoria());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        // Al hacer clic en una fila, copiar el nombre a txtNombre
        tablaCategorias.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int fila = tablaCategorias.getSelectedRow();
                if (fila >= 0) {
                    String nombre = tablaCategorias.getValueAt(fila, 1).toString();
                    txtNombre.setText(nombre);
                }
            }
        });
    }
  private void configurarDisponibilidad() {
    DefaultTableModel modelo =
            (DefaultTableModel) tablaCategorias.getModel();

    tablaCategorias.getTableHeader().setReorderingAllowed(false);

    modelo.addTableModelListener(e -> {
        if (restaurandoDisponibilidad) {
            return;
        }

        // Disponible corresponde a la columna 3.
        if (e.getType() != javax.swing.event.TableModelEvent.UPDATE
                || e.getColumn() != 3) {
            return;
        }

        int fila = e.getFirstRow();

        if (fila < 0 || fila >= modelo.getRowCount()) {
            return;
        }

        int idCategoria =
                ((Number) modelo.getValueAt(fila, 0)).intValue();

        Boolean anterior =
                disponibilidadGuardada.get(idCategoria);

        if (anterior == null) {
            return;
        }

        boolean nuevo =
                Boolean.TRUE.equals(modelo.getValueAt(fila, 3));

        if (nuevo == anterior.booleanValue()) {
            return;
        }

        try {
            if (Login.usuarioLogueado == null) {
                throw new SQLException(
                    "Debes iniciar sesion para cambiar la disponibilidad."
                );
            }

            String sql =
                    "UPDATE categorias "
                    + "SET disponible = ? "
                    + "WHERE id_categoria = ? AND activo = TRUE "
                    + "AND EXISTS ("
                    + "SELECT 1 FROM usuarios "
                    + "WHERE id_usuario = ? "
                    + "AND rol = 'ADMIN' AND activo = TRUE"
                    + ")";

            try (Connection conn = Conexion.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setBoolean(1, nuevo);
                ps.setInt(2, idCategoria);
                ps.setInt(3, Login.usuarioLogueado.idUsuario);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                        "No se pudo cambiar la disponibilidad.\n"
                        + "Verifica que seas administrador "
                        + "y que la categoria siga activa."
                    );
                }
            }

            disponibilidadGuardada.put(idCategoria, nuevo);

        } catch (SQLException ex) {
            restaurandoDisponibilidad = true;

            try {
                modelo.setValueAt(anterior, fila, 3);
            } finally {
                restaurandoDisponibilidad = false;
            }

            JOptionPane.showMessageDialog(
                this,
                "Error al guardar disponibilidad:\n" + ex.getMessage(),
                "Disponibilidad",
                JOptionPane.ERROR_MESSAGE
            );
        }
    });
}
    // ============================================================
    // CARGAR CATEGORÍAS EN LA TABLA
    // ============================================================
    private void cargarCategorias() {
        DefaultTableModel modelo = (DefaultTableModel) tablaCategorias.getModel();
        disponibilidadGuardada.clear();
        modelo.setRowCount(0);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Conexion.getConexion();
            String sql =
    "SELECT id_categoria, nombre, es_especial, disponible "
    + "FROM categorias "
    + "WHERE activo = TRUE "
    + "ORDER BY id_categoria";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_categoria");
                String nombre = rs.getString("nombre");
                boolean especial = rs.getBoolean("es_especial");
                String textoEspecial = especial ? "Sí" : "No";

                boolean disponible = rs.getBoolean("disponible");

disponibilidadGuardada.put(id, disponible);

modelo.addRow(new Object[]{
    id,
    nombre,
    textoEspecial,
    disponible
});
            }

            lblTotal.setText("Total: " + modelo.getRowCount() + " categorías");

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

    // ============================================================
    // AGREGAR CATEGORÍA
    // ============================================================
    private void agregarCategoria() {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el nombre de la categoría");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Conexion.getConexion();
            String sql = "INSERT INTO categorias (nombre, es_especial) VALUES (?, FALSE)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Categoría agregada exitosamente");
            limpiarCampos();
            cargarCategorias();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, "❌ Ya existe una categoría con ese nombre");
            } else {
                JOptionPane.showMessageDialog(this, "Error al agregar: " + e.getMessage());
            }
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
    // EDITAR CATEGORÍA
    // ============================================================
    private void editarCategoria() {
        int fila = tablaCategorias.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una categoría para editar");
            return;
        }

        int id = (int) tablaCategorias.getValueAt(fila, 0);
        String nombreActual = tablaCategorias.getValueAt(fila, 1).toString();
        String especialStr = tablaCategorias.getValueAt(fila, 2).toString();

        // Proteger la categoría especial
        if (especialStr.equals("Sí")) {
            JOptionPane.showMessageDialog(this,
                "⚠️ Esta categoría es especial y no se puede modificar",
                "Categoría protegida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nuevoNombre = txtNombre.getText().trim();
        if (nuevoNombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el nuevo nombre");
            return;
        }

        if (nuevoNombre.equals(nombreActual)) {
            JOptionPane.showMessageDialog(this, "No se detectaron cambios");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Modificar la categoría '" + nombreActual + "' a '" + nuevoNombre + "'?",
            "Confirmar modificación",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Conexion.getConexion();
            String sql = "UPDATE categorias SET nombre = ? WHERE id_categoria = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, nuevoNombre);
            ps.setInt(2, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Categoría modificada exitosamente");
            limpiarCampos();
            cargarCategorias();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, "❌ Ya existe una categoría con ese nombre");
            } else {
                JOptionPane.showMessageDialog(this, "Error al modificar: " + e.getMessage());
            }
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
    // ELIMINAR CATEGORÍA
    // ============================================================
    private void eliminarCategoria() {
    int filaVista = tablaCategorias.getSelectedRow();

    if (filaVista == -1) {
        JOptionPane.showMessageDialog(
            this,
            "Selecciona una categoria para eliminar."
        );
        return;
    }

    int filaModelo =
        tablaCategorias.convertRowIndexToModel(filaVista);

    javax.swing.table.TableModel modelo =
        tablaCategorias.getModel();

    int id = ((Number) modelo.getValueAt(filaModelo, 0))
        .intValue();

    String nombre =
        String.valueOf(modelo.getValueAt(filaModelo, 1));

    String especial =
        String.valueOf(modelo.getValueAt(filaModelo, 2));

    if ("Sí".equalsIgnoreCase(especial)) {
        JOptionPane.showMessageDialog(
            this,
            "Esta categoria es especial y no se puede eliminar.",
            "Categoria protegida",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int confirmacion = JOptionPane.showConfirmDialog(
        this,
        "¿Eliminar la categoria '" + nombre + "'?",
        "Confirmar eliminacion",
        JOptionPane.YES_NO_OPTION
    );

    if (confirmacion != JOptionPane.YES_OPTION) {
        return;
    }

    try (Connection conn = Conexion.getConexion()) {
        if (conn == null) {
            throw new SQLException(
                "No se pudo conectar con la base de datos."
            );
        }

        String sqlConteo =
            "SELECT "
            + "(SELECT COUNT(*) FROM productos "
            + "WHERE id_categoria = ? AND activo = TRUE) "
            + "AS productos_activos, "
            + "(SELECT COUNT(*) FROM extras "
            + "WHERE id_categoria = ? AND activo = TRUE) "
            + "AS extras_activos";

        int productos;
        int extras;

        try (PreparedStatement ps =
                conn.prepareStatement(sqlConteo)) {

            ps.setInt(1, id);
            ps.setInt(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                        "No se pudo comprobar el contenido de la categoria."
                    );
                }

                productos = rs.getInt("productos_activos");
                extras = rs.getInt("extras_activos");
            }
        }

        if (productos > 0 || extras > 0) {
            JOptionPane.showMessageDialog(
                this,
                "No puedes eliminar la categoria '" + nombre + "'.\n\n"
                + "Productos activos: " + productos + "\n"
                + "Extras activos: " + extras + "\n\n"
                + "Primero eliminalos o muevelos a otra categoria.\n"
                + "Quitar la palomita de Disponible no los elimina.",
                "Categoria con productos o extras",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Volver a comprobar las condiciones al desactivar.
        String sqlEliminar =
            "UPDATE categorias SET activo = FALSE "
            + "WHERE id_categoria = ? "
            + "AND activo = TRUE "
            + "AND es_especial = FALSE "
            + "AND NOT EXISTS ("
            + "SELECT 1 FROM productos "
            + "WHERE id_categoria = ? AND activo = TRUE"
            + ") "
            + "AND NOT EXISTS ("
            + "SELECT 1 FROM extras "
            + "WHERE id_categoria = ? AND activo = TRUE"
            + ")";

        try (PreparedStatement ps =
                conn.prepareStatement(sqlEliminar)) {

            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.setInt(3, id);

            if (ps.executeUpdate() != 1) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se elimino la categoria.\n"
                    + "Sus datos o sus productos y extras cambiaron.\n"
                    + "Actualiza el listado y vuelve a revisar."
                );
                return;
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al eliminar la categoria:\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    limpiarCampos();
    cargarCategorias();

    JOptionPane.showMessageDialog(
        this,
        "Categoria eliminada correctamente."
    );
}

    // ============================================================
    // LIMPIAR CAMPOS
    // ============================================================
    private void limpiarCampos() {
        txtNombre.setText("");
        tablaCategorias.clearSelection();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        txtNombre = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaCategorias = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        lblTotal = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 204, 255));
        setMaximumSize(new java.awt.Dimension(980, 620));
        setMinimumSize(new java.awt.Dimension(980, 620));
        setPreferredSize(new java.awt.Dimension(980, 620));

        jLabel1.setFont(new java.awt.Font("Roboto Black", 1, 48)); // NOI18N
        jLabel1.setText("CATEGORIAS");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtNombre.setBorder(null);
        txtNombre.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jPanel1.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 20, 178, -1));

        btnAgregar.setText("AGREGAR");
        jPanel1.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 130, -1));

        btnEditar.setText("MODIFICAR");
        jPanel1.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 80, -1, -1));

        btnLimpiar.setText("LIMPIAR");
        jPanel1.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 500, -1, -1));

        btnEliminar.setText("ELIMINAR");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        jPanel1.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 80, -1, -1));

        jLabel2.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel2.setText("Nombre de Categoria:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        tablaCategorias.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tablaCategorias.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        tablaCategorias.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Especial", "Disponible"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaCategorias.setRowHeight(30);
        jScrollPane1.setViewportView(tablaCategorias);
        if (tablaCategorias.getColumnModel().getColumnCount() > 0) {
            tablaCategorias.getColumnModel().getColumn(0).setMinWidth(100);
            tablaCategorias.getColumnModel().getColumn(0).setPreferredWidth(100);
            tablaCategorias.getColumnModel().getColumn(0).setMaxWidth(100);
            tablaCategorias.getColumnModel().getColumn(2).setMinWidth(50);
            tablaCategorias.getColumnModel().getColumn(2).setPreferredWidth(50);
            tablaCategorias.getColumnModel().getColumn(2).setMaxWidth(50);
            tablaCategorias.getColumnModel().getColumn(3).setMinWidth(70);
            tablaCategorias.getColumnModel().getColumn(3).setPreferredWidth(70);
            tablaCategorias.getColumnModel().getColumn(3).setMaxWidth(70);
        }

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 0, 590, 520));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 40, 190, -1));

        lblTotal.setText("jLabel3");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(lblTotal)
                .addGap(85, 85, 85))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 813, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 529, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEliminarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable tablaCategorias;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables
}
