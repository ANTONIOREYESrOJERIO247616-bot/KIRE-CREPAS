    
package VISTA.AGREGARPRODUCTOS;
import Conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PanelBasesFrutas extends javax.swing.JPanel {
    private Integer idSeleccionado = null;
    private boolean seleccionEsBase = true;
    private boolean cambiandoSeleccion = false;
    private boolean actualizandoDisponibilidad = false;

private final java.util.Map<Integer, Boolean> disponibilidadBases =
        new java.util.HashMap<>();

private final java.util.Map<Integer, Boolean> disponibilidadFrutas =
        new java.util.HashMap<>();
   
   public PanelBasesFrutas() {
    initComponents();

    javax.swing.ButtonGroup grupo = new javax.swing.ButtonGroup();
    grupo.add(rbGestionBase);
    grupo.add(rbGestionFruta);

    rbGestionBase.setSelected(true);

    tablaBases.setSelectionMode(
        javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    tablaFrutas.setSelectionMode(
        javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    jScrollPane3.getViewport().setBackground(java.awt.Color.WHITE);
    jScrollPane1.getViewport().setBackground(java.awt.Color.WHITE);
    configurarSeleccionGestion();
configurarDisponibilidad(true);
configurarDisponibilidad(false);
actualizarDatos();
}
   private void configurarDisponibilidad(boolean esBase) {
    javax.swing.JTable tabla =
            esBase ? tablaBases : tablaFrutas;

    DefaultTableModel modelo =
            (DefaultTableModel) tabla.getModel();

    java.util.Map<Integer, Boolean> valoresGuardados =
            esBase ? disponibilidadBases : disponibilidadFrutas;

    // Nombres internos fijos, no ingresados por el usuario.
    String nombreTabla = esBase ? "bases" : "frutas";
    String columnaId = esBase ? "id_base" : "id_fruta";

    tabla.getTableHeader().setReorderingAllowed(false);

    modelo.addTableModelListener(e -> {
        if (actualizandoDisponibilidad) {
            return;
        }

        // Disponible es la columna 2 en ambas tablas.
        if (e.getType() != javax.swing.event.TableModelEvent.UPDATE
                || e.getColumn() != 2) {
            return;
        }

        int fila = e.getFirstRow();

        if (fila < 0 || fila >= modelo.getRowCount()) {
            return;
        }

        int id = ((Number) modelo.getValueAt(fila, 0)).intValue();

        Boolean anterior = valoresGuardados.get(id);

        if (anterior == null) {
            return;
        }

        boolean nuevo =
                Boolean.TRUE.equals(modelo.getValueAt(fila, 2));

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
                    "UPDATE " + nombreTabla
                    + " SET disponible = ? "
                    + "WHERE " + columnaId + " = ? "
                    + "AND activo = TRUE "
                    + "AND EXISTS ("
                    + "SELECT 1 FROM usuarios "
                    + "WHERE id_usuario = ? "
                    + "AND rol = 'ADMIN' AND activo = TRUE"
                    + ")";

            try (Connection conn = Conexion.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setBoolean(1, nuevo);
                ps.setInt(2, id);
                ps.setInt(
                    3, VISTA.Login.usuarioLogueado.idUsuario
                );

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                        "No se pudo cambiar la disponibilidad.\n"
                        + "Verifica que seas administrador "
                        + "y que el registro siga activo."
                    );
                }
            }

            valoresGuardados.put(id, nuevo);

        } catch (SQLException ex) {
            actualizandoDisponibilidad = true;

            try {
                modelo.setValueAt(anterior, fila, 2);
            } finally {
                actualizandoDisponibilidad = false;
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
   private void configurarSeleccionGestion() {
    tablaBases.setSelectionMode(
        javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    tablaFrutas.setSelectionMode(
        javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    tablaBases.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting() && !cambiandoSeleccion) {
            seleccionarBaseFruta(true);
        }
    });

    tablaFrutas.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting() && !cambiandoSeleccion) {
            seleccionarBaseFruta(false);
        }
    });

    rbGestionBase.addActionListener(e -> limpiarFormularioGestion());
    rbGestionFruta.addActionListener(e -> limpiarFormularioGestion());

    limpiarFormularioGestion();
}

private void limpiarFormularioGestion() {
    cambiandoSeleccion = true;

    try {
        idSeleccionado = null;

        tablaBases.clearSelection();
        tablaFrutas.clearSelection();

        txtNombreGestion.setText("");

        btnModificarGestion.setEnabled(false);
        btnEliminarGestion.setEnabled(false);
    } finally {
        cambiandoSeleccion = false;
    }
}

private void seleccionarBaseFruta(boolean esBase) {
    javax.swing.JTable tabla = esBase ? tablaBases : tablaFrutas;

    int filaVista = tabla.getSelectedRow();

    if (filaVista == -1) {
        if (idSeleccionado != null && seleccionEsBase == esBase) {
            limpiarFormularioGestion();
        }
        return;
    }

    int filaModelo = tabla.convertRowIndexToModel(filaVista);

    cambiandoSeleccion = true;

    try {
        if (esBase) {
            tablaFrutas.clearSelection();
        } else {
            tablaBases.clearSelection();
        }

        idSeleccionado = ((Number) tabla.getModel()
            .getValueAt(filaModelo, 0)).intValue();

        seleccionEsBase = esBase;

        rbGestionBase.setSelected(esBase);
        rbGestionFruta.setSelected(!esBase);

        txtNombreGestion.setText(
            tabla.getModel().getValueAt(filaModelo, 1).toString()
        );

        btnModificarGestion.setEnabled(true);
        btnEliminarGestion.setEnabled(true);

    } finally {
        cambiandoSeleccion = false;
    }
}
private void modificarBaseFruta() {
    if (idSeleccionado == null) {
        JOptionPane.showMessageDialog(
            this,
            "Selecciona una base o fruta en la tabla."
        );
        return;
    }

    String nombre = txtNombreGestion.getText().trim();

    if (nombre.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ingresa el nombre.");
        txtNombreGestion.requestFocusInWindow();
        return;
    }

    if (nombre.length() > 50) {
        JOptionPane.showMessageDialog(
            this,
            "El nombre no debe superar los 50 caracteres."
        );
        return;
    }

    int id = idSeleccionado;
    String tabla = seleccionEsBase ? "bases" : "frutas";
    String columnaId = seleccionEsBase ? "id_base" : "id_fruta";

    int respuesta = JOptionPane.showConfirmDialog(
        this,
        "¿Guardar el nombre: " + nombre + "?",
        "Modificar",
        JOptionPane.YES_NO_OPTION
    );

    if (respuesta != JOptionPane.YES_OPTION) {
        return;
    }

    try (Connection conn = Conexion.getConexion()) {

        String sqlBuscar =
            "SELECT 1 FROM " + tabla
            + " WHERE nombre = ? AND activo = TRUE"
            + " AND " + columnaId + " <> ?";

        try (PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombre);
            ps.setInt(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Ya existe otro registro activo con ese nombre."
                    );
                    return;
                }
            }
        }

        String sqlActualizar =
            "UPDATE " + tabla
            + " SET nombre = ?"
            + " WHERE " + columnaId + " = ? AND activo = TRUE";

        try (PreparedStatement ps =
                conn.prepareStatement(sqlActualizar)) {

            ps.setString(1, nombre);
            ps.setInt(2, id);

            int filas = ps.executeUpdate();

            if (filas == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "No hubo cambios. El nombre puede ser el mismo "
                    + "o el registro ya no esta activo."
                );
                actualizarDatos();
                return;
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al modificar:\n" + e.getMessage()
        );
        return;
    }

    actualizarDatos();

    JOptionPane.showMessageDialog(
        this,
        "Nombre modificado correctamente."
    );
}
private void eliminarBaseFruta() {
    if (idSeleccionado == null) {
        JOptionPane.showMessageDialog(
            this,
            "Selecciona una base o fruta en la tabla."
        );
        return;
    }

    int id = idSeleccionado;
    boolean esBase = seleccionEsBase;

    String tabla = esBase ? "bases" : "frutas";
    String columnaId = esBase ? "id_base" : "id_fruta";
    String tipo = esBase ? "base" : "fruta";

    javax.swing.JTable tablaSeleccionada =
        esBase ? tablaBases : tablaFrutas;

    int fila = tablaSeleccionada.getSelectedRow();

    if (fila == -1) {
        limpiarFormularioGestion();
        return;
    }

    // Tomar el nombre de la tabla, aunque se haya editado el campo.
    String nombre = tablaSeleccionada.getValueAt(fila, 1).toString();

    int respuesta = JOptionPane.showConfirmDialog(
        this,
        "¿Eliminar la " + tipo + " '" + nombre + "'?\n"
        + "Dejara de estar disponible para nuevas ventas.",
        "Eliminar",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
    );

    if (respuesta != JOptionPane.YES_OPTION) {
        return;
    }

    int filas;

    try (Connection conn = Conexion.getConexion()) {

        String sql =
            "UPDATE " + tabla
            + " SET activo = FALSE"
            + " WHERE " + columnaId + " = ? AND activo = TRUE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            filas = ps.executeUpdate();
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al eliminar:\n" + e.getMessage()
        );
        return;
    }

    actualizarDatos();

    JOptionPane.showMessageDialog(
        this,
        filas > 0
            ? "Registro eliminado correctamente."
            : "El registro ya no estaba activo."
    );
}
   private void agregarBaseFruta() {
    String nombre = txtNombreGestion.getText().trim();

    if (nombre.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ingresa el nombre.");
        txtNombreGestion.requestFocusInWindow();
        return;
    }

    if (nombre.length() > 50) {
        JOptionPane.showMessageDialog(
            this,
            "El nombre no debe superar los 50 caracteres."
        );
        return;
    }

    if (!rbGestionBase.isSelected()
            && !rbGestionFruta.isSelected()) {
        JOptionPane.showMessageDialog(
            this,
            "Selecciona Base o Fruta."
        );
        return;
    }

    boolean esBase = rbGestionBase.isSelected();

    // Estos nombres dependen solamente de los botones de opcion.
    String tabla = esBase ? "bases" : "frutas";
    String tipo = esBase ? "Base" : "Fruta";

    try (Connection conn = Conexion.getConexion()) {

        String sqlBuscar =
            "SELECT 1 FROM " + tabla
            + " WHERE nombre = ? AND activo = TRUE";

        try (PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Ya existe una " + tipo.toLowerCase()
                        + " activa con ese nombre."
                    );
                    return;
                }
            }
        }

        String sqlInsertar =
            "INSERT INTO " + tabla
            + " (nombre, activo) VALUES (?, TRUE)";

        try (PreparedStatement ps =
                conn.prepareStatement(sqlInsertar)) {

            ps.setString(1, nombre);
            ps.executeUpdate();
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al agregar:\n" + e.getMessage()
        );
        return;
    }

    actualizarDatos();

    JOptionPane.showMessageDialog(
        this,
        tipo + " agregada correctamente."
    );

    txtNombreGestion.requestFocusInWindow();
}
  public void actualizarDatos() {
    limpiarFormularioGestion();

    DefaultTableModel modeloBases =
            (DefaultTableModel) tablaBases.getModel();

    DefaultTableModel modeloFrutas =
            (DefaultTableModel) tablaFrutas.getModel();

    actualizandoDisponibilidad = true;

    try {
        modeloBases.setRowCount(0);
        modeloFrutas.setRowCount(0);

        disponibilidadBases.clear();
        disponibilidadFrutas.clear();

        try (Connection conn = Conexion.getConexion()) {
            String sqlBases =
                    "SELECT id_base, nombre, disponible FROM bases "
                    + "WHERE activo = TRUE ORDER BY nombre";

            try (PreparedStatement ps = conn.prepareStatement(sqlBases);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("id_base");
                    boolean disponible = rs.getBoolean("disponible");

                    disponibilidadBases.put(id, disponible);

                    modeloBases.addRow(new Object[]{
                        id,
                        rs.getString("nombre"),
                        disponible
                    });
                }
            }

            String sqlFrutas =
                    "SELECT id_fruta, nombre, disponible FROM frutas "
                    + "WHERE activo = TRUE ORDER BY nombre";

            try (PreparedStatement ps = conn.prepareStatement(sqlFrutas);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("id_fruta");
                    boolean disponible = rs.getBoolean("disponible");

                    disponibilidadFrutas.put(id, disponible);

                    modeloFrutas.addRow(new Object[]{
                        id,
                        rs.getString("nombre"),
                        disponible
                    });
                }
            }
        }

    } catch (SQLException e) {
        modeloBases.setRowCount(0);
        modeloFrutas.setRowCount(0);
        disponibilidadBases.clear();
        disponibilidadFrutas.clear();

        JOptionPane.showMessageDialog(
            this,
            "Error al cargar bases y frutas:\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );

    } finally {
        actualizandoDisponibilidad = false;
    }
}
  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        txtNombreGestion = new javax.swing.JTextField();
        btnAgregarGestion = new javax.swing.JButton();
        btnModificarGestion = new javax.swing.JButton();
        btnEliminarGestion = new javax.swing.JButton();
        rbGestionBase = new javax.swing.JRadioButton();
        rbGestionFruta = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tablaBases = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaFrutas = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 204, 255));
        setMaximumSize(new java.awt.Dimension(980, 620));
        setMinimumSize(new java.awt.Dimension(980, 620));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(980, 620));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel12.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel12.setText("Nombre: ");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, -1, -1));

        txtNombreGestion.setBorder(null);
        jPanel4.add(txtNombreGestion, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 50, 451, -1));

        btnAgregarGestion.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnAgregarGestion.setText("AGREGAR");
        btnAgregarGestion.addActionListener(this::btnAgregarGestionActionPerformed);
        jPanel4.add(btnAgregarGestion, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, -1));

        btnModificarGestion.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnModificarGestion.setText("MODIFICAR");
        btnModificarGestion.addActionListener(this::btnModificarGestionActionPerformed);
        jPanel4.add(btnModificarGestion, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 100, -1, -1));

        btnEliminarGestion.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnEliminarGestion.setText("ELIMINAR");
        btnEliminarGestion.addActionListener(this::btnEliminarGestionActionPerformed);
        jPanel4.add(btnEliminarGestion, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 100, -1, -1));

        rbGestionBase.setText("BASE");
        rbGestionBase.addActionListener(this::rbGestionBaseActionPerformed);
        jPanel4.add(rbGestionBase, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 10, -1, -1));

        rbGestionFruta.setText("FRUTA");
        jPanel4.add(rbGestionFruta, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 10, -1, -1));

        jLabel6.setFont(new java.awt.Font("Roboto Condensed ExtraBold", 1, 14)); // NOI18N
        jLabel6.setText("Frutas");
        jLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 150, -1, -1));

        tablaBases.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Disponible"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaBases.setGridColor(new java.awt.Color(0, 0, 0));
        tablaBases.setRowHeight(25);
        tablaBases.setSelectionBackground(new java.awt.Color(255, 204, 255));
        jScrollPane3.setViewportView(tablaBases);
        if (tablaBases.getColumnModel().getColumnCount() > 0) {
            tablaBases.getColumnModel().getColumn(0).setMinWidth(100);
            tablaBases.getColumnModel().getColumn(0).setPreferredWidth(100);
            tablaBases.getColumnModel().getColumn(0).setMaxWidth(100);
            tablaBases.getColumnModel().getColumn(2).setMinWidth(70);
            tablaBases.getColumnModel().getColumn(2).setPreferredWidth(70);
            tablaBases.getColumnModel().getColumn(2).setMaxWidth(70);
        }

        jPanel4.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 480, 350));

        tablaFrutas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Disponible"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaFrutas.setGridColor(new java.awt.Color(0, 0, 0));
        tablaFrutas.setRowHeight(25);
        tablaFrutas.setSelectionBackground(new java.awt.Color(255, 204, 255));
        jScrollPane1.setViewportView(tablaFrutas);
        if (tablaFrutas.getColumnModel().getColumnCount() > 0) {
            tablaFrutas.getColumnModel().getColumn(0).setMinWidth(100);
            tablaFrutas.getColumnModel().getColumn(0).setPreferredWidth(100);
            tablaFrutas.getColumnModel().getColumn(0).setMaxWidth(100);
            tablaFrutas.getColumnModel().getColumn(2).setMinWidth(70);
            tablaFrutas.getColumnModel().getColumn(2).setPreferredWidth(70);
            tablaFrutas.getColumnModel().getColumn(2).setMaxWidth(70);
        }

        jPanel4.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 180, 460, 350));

        jLabel7.setFont(new java.awt.Font("Roboto ExtraBold", 1, 14)); // NOI18N
        jLabel7.setText("Bases");
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 150, -1, -1));
        jPanel4.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 70, 460, -1));

        jLabel11.setFont(new java.awt.Font("Roboto Black", 1, 48)); // NOI18N
        jLabel11.setText("BASES Y FRUTAS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel11)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 968, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 545, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbGestionBaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbGestionBaseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbGestionBaseActionPerformed

    private void btnAgregarGestionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarGestionActionPerformed
     agregarBaseFruta();
    }//GEN-LAST:event_btnAgregarGestionActionPerformed

    private void btnModificarGestionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarGestionActionPerformed
        modificarBaseFruta();
    }//GEN-LAST:event_btnModificarGestionActionPerformed

    private void btnEliminarGestionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarGestionActionPerformed
        eliminarBaseFruta();
    }//GEN-LAST:event_btnEliminarGestionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarGestion;
    private javax.swing.JButton btnEliminarGestion;
    private javax.swing.JButton btnModificarGestion;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JRadioButton rbGestionBase;
    private javax.swing.JRadioButton rbGestionFruta;
    private javax.swing.JTable tablaBases;
    private javax.swing.JTable tablaFrutas;
    private javax.swing.JTextField txtNombreGestion;
    // End of variables declaration//GEN-END:variables
}
