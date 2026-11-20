package VISTA.AGREGARPRODUCTOS;
import Conexion.Conexion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PanelExtras extends javax.swing.JPanel {
private Integer idExtraSeleccionado = null;
private final List<Integer> idsCategorias = new ArrayList<>();
private javax.swing.table.TableRowSorter<DefaultTableModel>
        filtroExtras;

private boolean cargandoFiltroExtras = false;
private boolean restaurandoDisponibilidad = false;
private boolean recargandoTabla = false;

private final java.util.Map<Integer, Boolean> disponibilidadGuardada =
        new java.util.HashMap<>();
   
public PanelExtras() {
    initComponents();
    FiltroPrecio.aplicar(txtPrecioExtra);

    tablaExtras.setSelectionMode(
        javax.swing.ListSelectionModel.SINGLE_SELECTION
    );
    tablaExtras.getTableHeader().setReorderingAllowed(false);

    jScrollPane1.getViewport().setBackground(java.awt.Color.WHITE);

    btnAgregarExtra.addActionListener(e -> guardarExtra(false));
    btnModificarExtra.addActionListener(e -> guardarExtra(true));
    btnDesactivarExtra.addActionListener(e -> desactivarExtra());
    btnLimpiarExtra.addActionListener(e -> limpiarFormulario());

    tablaExtras.getSelectionModel().addListSelectionListener(e -> {
    if (e.getValueIsAdjusting()) {
        return;
    }

    javax.swing.SwingUtilities.invokeLater(() -> {
        if (tablaExtras.getSelectionModel()
                .getValueIsAdjusting()) {
            return;
        }

        cargarExtraSeleccionado();
    });
});

    configurarFiltroExtras();
    configurarDisponibilidadExtras();
    actualizarDatos();
}
public void actualizarDatos() {
    if (recargandoTabla) {
        return;
    }

    if (tablaExtras.isEditing()
            && !tablaExtras.getCellEditor().stopCellEditing()) {
        return;
    }

    recargandoTabla = true;

    try {
        limpiarFormulario();
        tablaExtras.setRowSorter(null);

        cargarDatosExtrasDesdeBD();

    } finally {
        try {
            if (filtroExtras != null) {
                filtroExtras.allRowsChanged();
                tablaExtras.setRowSorter(filtroExtras);
            }
        } finally {
            recargandoTabla = false;
        }
    }
}
private void configurarDisponibilidadExtras() {
    DefaultTableModel modelo =
            (DefaultTableModel) tablaExtras.getModel();

    modelo.addTableModelListener(e -> {
        if (restaurandoDisponibilidad) {
            return;
        }

        // Ignorar cargas de filas y cambios en otras columnas.
        if (e.getType() != javax.swing.event.TableModelEvent.UPDATE
                || e.getColumn() != 4) {
            return;
        }

        int fila = e.getFirstRow();

        if (fila < 0 || fila >= modelo.getRowCount()) {
            return;
        }

        // La fila del evento pertenece al modelo, incluso con filtro.
        int idExtra =
                ((Number) modelo.getValueAt(fila, 0)).intValue();

        Boolean anterior = disponibilidadGuardada.get(idExtra);

        if (anterior == null) {
            return;
        }

        boolean nuevo =
                Boolean.TRUE.equals(modelo.getValueAt(fila, 4));

        if (nuevo == anterior.booleanValue()) {
            return;
        }

        try {
            if (VISTA.Login.usuarioLogueado == null) {
                throw new SQLException(
                    "Debes iniciar sesion para cambiar la disponibilidad."
                );
            }

            String sql =
                    "UPDATE extras "
                    + "SET disponible = ? "
                    + "WHERE id_extra = ? AND activo = TRUE "
                    + "AND EXISTS ("
                    + "SELECT 1 FROM usuarios "
                    + "WHERE id_usuario = ? "
                    + "AND rol = 'ADMIN' AND activo = TRUE"
                    + ")";

            try (Connection conn = Conexion.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setBoolean(1, nuevo);
                ps.setInt(2, idExtra);
                ps.setInt(
                    3, VISTA.Login.usuarioLogueado.idUsuario
                );

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                        "No se pudo cambiar la disponibilidad.\n"
                        + "Verifica que seas administrador "
                        + "y que el extra siga activo."
                    );
                }
            }

            disponibilidadGuardada.put(idExtra, nuevo);

        } catch (SQLException ex) {
            restaurandoDisponibilidad = true;

            try {
                modelo.setValueAt(anterior, fila, 4);
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
private void configurarFiltroExtras() {
    DefaultTableModel modelo =
            (DefaultTableModel) tablaExtras.getModel();

    filtroExtras =
            new javax.swing.table.TableRowSorter<>(modelo);

    // Conservar el orden actual de los registros.
    for (int i = 0; i < modelo.getColumnCount(); i++) {
        filtroExtras.setSortable(i, false);
    }

    tablaExtras.setRowSorter(filtroExtras);

    cmbFiltroExtras.setEditable(false);

    cmbFiltroExtras.addActionListener(e -> {
        if (cargandoFiltroExtras) {
            return;
        }

        limpiarFormulario();
        aplicarFiltroExtras();
    });
}

private void actualizarOpcionesFiltroExtras() {
    Object seleccionAnterior =
            cmbFiltroExtras.getSelectedItem();

    boolean teniaCategoria =
            cmbFiltroExtras.getSelectedIndex() > 0;

    DefaultTableModel modelo =
            (DefaultTableModel) tablaExtras.getModel();

    java.util.Set<String> categorias = new java.util.TreeSet<>();

    // Leer todas las filas del modelo, incluyendo las ocultas.
    for (int fila = 0; fila < modelo.getRowCount(); fila++) {
        Object categoria = modelo.getValueAt(fila, 1);

        if (categoria != null) {
            categorias.add(categoria.toString());
        }
    }

    cargandoFiltroExtras = true;

    try {
        cmbFiltroExtras.removeAllItems();
        cmbFiltroExtras.addItem("TODAS LAS CATEGORIAS");

        for (String categoria : categorias) {
            cmbFiltroExtras.addItem(categoria);
        }

        cmbFiltroExtras.setSelectedIndex(0);

        // Recuperar la categoria elegida si sigue disponible.
        if (teniaCategoria && seleccionAnterior != null) {
            for (int i = 1;
                    i < cmbFiltroExtras.getItemCount();
                    i++) {

                if (seleccionAnterior.equals(
                        cmbFiltroExtras.getItemAt(i))) {

                    cmbFiltroExtras.setSelectedIndex(i);
                    break;
                }
            }
        }
    } finally {
        cargandoFiltroExtras = false;
    }

    aplicarFiltroExtras();
}

private void aplicarFiltroExtras() {
    if (filtroExtras == null) {
        return;
    }

    if (cmbFiltroExtras.getSelectedIndex() <= 0) {
        filtroExtras.setRowFilter(null);
        return;
    }

    final String categoria =
            cmbFiltroExtras.getSelectedItem().toString();

    filtroExtras.setRowFilter(
        new javax.swing.RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(
                    javax.swing.RowFilter.Entry<
                            ? extends DefaultTableModel,
                            ? extends Integer> fila) {

                return categoria.equals(fila.getStringValue(1));
            }
        }
    );
}
private void cargarDatosExtrasDesdeBD() {
    limpiarFormulario();

    idsCategorias.clear();
    cmbCategoriaExtra.removeAllItems();

    DefaultTableModel modelo =
        (DefaultTableModel) tablaExtras.getModel();
    disponibilidadGuardada.clear();
    modelo.setRowCount(0);

    try (Connection conn = Conexion.getConexion()) {

        // Incluye todas las categorias activas, tambien Crepas.
        String sqlCategorias =
            "SELECT id_categoria, nombre FROM categorias "
            + "WHERE activo = TRUE ORDER BY nombre";

        try (PreparedStatement ps = conn.prepareStatement(sqlCategorias);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                idsCategorias.add(rs.getInt("id_categoria"));
                cmbCategoriaExtra.addItem(rs.getString("nombre"));
            }
        }

        // Permite ver extras activos aunque su categoria se desactive.
        String sqlExtras =
            "SELECT e.id_extra, c.nombre AS categoria, "
            + "e.nombre, e.precio, e.disponible "
            + "FROM extras e "
            + "INNER JOIN categorias c "
            + "ON c.id_categoria = e.id_categoria "
            + "WHERE e.activo = TRUE "
            + "ORDER BY c.nombre, e.nombre";

        try (PreparedStatement ps = conn.prepareStatement(sqlExtras);
             ResultSet rs = ps.executeQuery()) {

         while (rs.next()) {
    int idExtra = rs.getInt("id_extra");
    boolean disponible = rs.getBoolean("disponible");

    disponibilidadGuardada.put(idExtra, disponible);

    modelo.addRow(new Object[]{
        idExtra,
        rs.getString("categoria"),
        rs.getString("nombre"),
        rs.getBigDecimal("precio"),
        disponible
    });
}  
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al cargar extras:\n" + e.getMessage()
        );
    }

   if (cmbCategoriaExtra.getItemCount() > 0) {
    cmbCategoriaExtra.setSelectedIndex(0);
}
      actualizarOpcionesFiltroExtras();
}

private void limpiarFormulario() {
    idExtraSeleccionado = null;

    tablaExtras.clearSelection();


    txtNombreExtra.setText("");
    txtPrecioExtra.setText("");

    btnAgregarExtra.setEnabled(true);
    btnModificarExtra.setEnabled(false);
    btnDesactivarExtra.setEnabled(false);
}

private void cargarExtraSeleccionado() {
    if (recargandoTabla) {
    return;
}
    int fila = tablaExtras.getSelectedRow();

    if (fila == -1) {
        limpiarFormulario();
        return;
    }

    int id = ((Number) tablaExtras.getValueAt(fila, 0)).intValue();

    idExtraSeleccionado = null;
    btnAgregarExtra.setEnabled(false);
    btnModificarExtra.setEnabled(false);
    btnDesactivarExtra.setEnabled(false);

    txtNombreExtra.setText("");
    txtPrecioExtra.setText("");
    cmbCategoriaExtra.setSelectedIndex(-1);

    String sql =
        "SELECT id_categoria, nombre, precio FROM extras "
        + "WHERE id_extra = ? AND activo = TRUE";

    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                JOptionPane.showMessageDialog(
                    this,
                    "El extra ya no esta activo."
                );
                actualizarDatos();
                return;
            }

            idExtraSeleccionado = id;

            txtNombreExtra.setText(rs.getString("nombre"));
            txtPrecioExtra.setText(
                rs.getBigDecimal("precio").toPlainString()
            );

            int indice = idsCategorias.indexOf(
                rs.getInt("id_categoria")
            );

            cmbCategoriaExtra.setSelectedIndex(indice);

            btnModificarExtra.setEnabled(true);
            btnDesactivarExtra.setEnabled(true);
        }

    } catch (SQLException e) {
        limpiarFormulario();

        JOptionPane.showMessageDialog(
            this,
            "Error al consultar el extra:\n" + e.getMessage()
        );
    }
}

private void guardarExtra(boolean modificar) {
    if (modificar && idExtraSeleccionado == null) {
        JOptionPane.showMessageDialog(
            this,
            "Selecciona un extra para modificar."
        );
        return;
    }

    int indice = cmbCategoriaExtra.getSelectedIndex();

    if (indice < 0 || indice >= idsCategorias.size()) {
        JOptionPane.showMessageDialog(
            this,
            "Selecciona una categoria activa."
        );
        return;
    }

    String nombre = txtNombreExtra.getText().trim();

    if (nombre.isEmpty() || nombre.length() > 100) {
        JOptionPane.showMessageDialog(
            this,
            "Ingresa un nombre de entre 1 y 100 caracteres."
        );
        txtNombreExtra.requestFocusInWindow();
        return;
    }

    BigDecimal precio;

    try {
        precio = new BigDecimal(
            txtPrecioExtra.getText().trim().replace(",", ".")
        ).setScale(2, RoundingMode.UNNECESSARY);

    } catch (NumberFormatException | ArithmeticException e) {
        JOptionPane.showMessageDialog(
            this,
            "Ingresa un precio valido con hasta dos decimales.\n"
            + "Ejemplo: 10.00"
        );
        return;
    }

    if (precio.signum() < 0
            || precio.compareTo(new BigDecimal("99999999.99")) > 0) {
        JOptionPane.showMessageDialog(
            this,
            "El precio debe estar entre 0.00 y 99999999.99."
        );
        return;
    }

    int idCategoria = idsCategorias.get(indice);

    if (modificar) {
        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Guardar los cambios del extra '" + nombre + "'?",
            "Modificar extra",
            JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
    }

    String sql;

    if (modificar) {
        sql =
            "UPDATE extras SET nombre = ?, precio = ?, "
            + "id_categoria = ? "
            + "WHERE id_extra = ? AND activo = TRUE "
            + "AND EXISTS (SELECT 1 FROM categorias "
            + "WHERE id_categoria = ? AND activo = TRUE)";
    } else {
        sql =
            "INSERT INTO extras "
            + "(nombre, precio, id_categoria, activo) "
            + "SELECT ?, ?, id_categoria, TRUE FROM categorias "
            + "WHERE id_categoria = ? AND activo = TRUE";
    }

    int filas;

    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, nombre);
        ps.setBigDecimal(2, precio);
        ps.setInt(3, idCategoria);

        if (modificar) {
            ps.setInt(4, idExtraSeleccionado);
            ps.setInt(5, idCategoria);
        }

        filas = ps.executeUpdate();

    } catch (SQLException e) {
        if (e.getErrorCode() == 1062) {
            JOptionPane.showMessageDialog(
                this,
                "Ya existe un extra con ese nombre en esa categoria.\n"
                + "Puede estar activo o desactivado."
            );
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Error al guardar el extra:\n" + e.getMessage()
            );
        }
        return;
    }
actualizarDatos();

// Recuperar la categoria utilizada si sigue activa.
int indiceCategoria = idsCategorias.indexOf(idCategoria);
cmbCategoriaExtra.setSelectedIndex(indiceCategoria);

txtNombreExtra.requestFocusInWindow();

if (filas == 0) {
        JOptionPane.showMessageDialog(
            this,
            "No hubo cambios. Los datos pueden ser iguales "
            + "o el extra o la categoria ya no estan disponibles."
        );
        return;
    }

    JOptionPane.showMessageDialog(
        this,
        modificar
            ? "Extra modificado correctamente."
            : "Extra agregado correctamente."
    );
}

private void desactivarExtra() {
    if (idExtraSeleccionado == null) {
        JOptionPane.showMessageDialog(
            this,
            "Selecciona un extra para desactivar."
        );
        return;
    }

    int fila = tablaExtras.getSelectedRow();

    if (fila == -1) {
        limpiarFormulario();
        return;
    }

    int id = idExtraSeleccionado;
    String nombre = tablaExtras.getValueAt(fila, 2).toString();

    int respuesta = JOptionPane.showConfirmDialog(
        this,
        "¿Desactivar el extra '" + nombre + "'?",
        "Desactivar extra",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
    );

    if (respuesta != JOptionPane.YES_OPTION) {
        return;
    }

    String sql =
        "UPDATE extras SET activo = FALSE "
        + "WHERE id_extra = ? AND activo = TRUE";

    int filas;

    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);
        filas = ps.executeUpdate();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al desactivar:\n" + e.getMessage()
        );
        return;
    }

    actualizarDatos();

    JOptionPane.showMessageDialog(
        this,
        filas > 0
            ? "Extra desactivado correctamente."
            : "El extra ya no estaba activo."
    );
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
        cmbCategoriaExtra = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtNombreExtra = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtPrecioExtra = new javax.swing.JTextField();
        btnAgregarExtra = new javax.swing.JButton();
        btnModificarExtra = new javax.swing.JButton();
        btnDesactivarExtra = new javax.swing.JButton();
        btnLimpiarExtra = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaExtras = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        cmbFiltroExtras = new javax.swing.JComboBox<>();

        setBackground(new java.awt.Color(255, 204, 255));
        setMaximumSize(new java.awt.Dimension(980, 620));
        setMinimumSize(new java.awt.Dimension(980, 620));
        setPreferredSize(new java.awt.Dimension(980, 620));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel1.setText("CATEGORIA:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, -1, -1));

        cmbCategoriaExtra.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbCategoriaExtra, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 10, -1, -1));

        jLabel2.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel2.setText("Nombre de extra:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, -1, -1));

        txtNombreExtra.setBorder(null);
        jPanel1.add(txtNombreExtra, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 60, 140, -1));

        jLabel3.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel3.setText("Precio adicional:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, -1));

        txtPrecioExtra.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPrecioExtra.setBorder(null);
        txtPrecioExtra.addActionListener(this::txtPrecioExtraActionPerformed);
        jPanel1.add(txtPrecioExtra, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 120, 150, -1));

        btnAgregarExtra.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnAgregarExtra.setText("AGREGAR EXTRA");
        jPanel1.add(btnAgregarExtra, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, -1, -1));

        btnModificarExtra.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnModificarExtra.setText("MODIFICAR");
        jPanel1.add(btnModificarExtra, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 500, -1, -1));

        btnDesactivarExtra.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnDesactivarExtra.setText("DESACTIVAR");
        jPanel1.add(btnDesactivarExtra, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 240, -1, -1));

        btnLimpiarExtra.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnLimpiarExtra.setText("LIMPIAR");
        jPanel1.add(btnLimpiarExtra, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 520, -1, -1));

        tablaExtras.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Categoria", "EXTRA", "Precio", "Disponible"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaExtras);
        if (tablaExtras.getColumnModel().getColumnCount() > 0) {
            tablaExtras.getColumnModel().getColumn(0).setMinWidth(100);
            tablaExtras.getColumnModel().getColumn(0).setPreferredWidth(100);
            tablaExtras.getColumnModel().getColumn(0).setMaxWidth(100);
            tablaExtras.getColumnModel().getColumn(3).setMinWidth(100);
            tablaExtras.getColumnModel().getColumn(3).setPreferredWidth(100);
            tablaExtras.getColumnModel().getColumn(3).setMaxWidth(100);
            tablaExtras.getColumnModel().getColumn(4).setMinWidth(70);
            tablaExtras.getColumnModel().getColumn(4).setPreferredWidth(70);
            tablaExtras.getColumnModel().getColumn(4).setMaxWidth(70);
        }

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 20, 660, 500));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 80, 150, -1));
        jPanel1.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 140, 150, -1));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 960, 550));

        jLabel4.setFont(new java.awt.Font("Roboto Black", 1, 48)); // NOI18N
        jLabel4.setText("EXTRAS");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, -1, -1));

        cmbFiltroExtras.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cmbFiltroExtras, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 30, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void txtPrecioExtraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrecioExtraActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrecioExtraActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarExtra;
    private javax.swing.JButton btnDesactivarExtra;
    private javax.swing.JButton btnLimpiarExtra;
    private javax.swing.JButton btnModificarExtra;
    private javax.swing.JComboBox<String> cmbCategoriaExtra;
    private javax.swing.JComboBox<String> cmbFiltroExtras;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable tablaExtras;
    private javax.swing.JTextField txtNombreExtra;
    private javax.swing.JTextField txtPrecioExtra;
    // End of variables declaration//GEN-END:variables
}
