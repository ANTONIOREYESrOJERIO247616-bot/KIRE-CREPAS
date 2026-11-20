
package VISTA;

import Conexion.Conexion;
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class AdminUsuario extends javax.swing.JPanel {


    public AdminUsuario() {
        initComponents();
        jScrollPane1.getViewport().setBackground(new Color(255, 255, 255));
        cargarUsuarios();
    configurarEventos();
    }
        // ============================================================
    // ACTIVAR / DESACTIVAR USUARIO
    // ============================================================
    private void activarDesactivarUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario para activar o desactivar");
            return;
        }

        int id = (int) tablaUsuarios.getValueAt(fila, 0);
        String usuario = (String) tablaUsuarios.getValueAt(fila, 1);
        String estadoActual = (String) tablaUsuarios.getValueAt(fila, 4);
        
        // No permitir desactivar al admin
        if (usuario.equals("admin")) {
            JOptionPane.showMessageDialog(this, "❌ No se puede desactivar al usuario administrador");
            return;
        }

        boolean estaActivo = estadoActual.contains("Activo");
        String nuevoEstado = estaActivo ? "desactivar" : "activar";
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de " + nuevoEstado + " al usuario '" + usuario + "'?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Conexion.getConexion();
            
            // Cambiar el estado (activo = 1 → 0  o  0 → 1)
            String sql = "UPDATE usuarios SET activo = NOT activo WHERE id_usuario = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Usuario " + nuevoEstado + "do exitosamente");
            cargarUsuarios();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cambiar estado: " + e.getMessage());
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
    // CONFIGURAR EVENTOS DE LOS BOTONES
    // ============================================================
    private void configurarEventos() {
    btnAgregar.addActionListener(e -> agregarUsuario());
    btnEditar.addActionListener(e -> editarUsuario());
    cmbFiltroEstado.addActionListener(e -> filtrarPorEstado());
    btnActivarDesactivar.addActionListener(e -> activarDesactivarUsuario()); // ← NUEVO
    btnCambiarPass.addActionListener(e -> cambiarContraseña());
    btnBuscar.addActionListener(e -> buscarUsuarios());
    btnActualizar.addActionListener(e -> cargarUsuarios());
    cmbFiltroRol.addActionListener(e -> filtrarPorRol());
    txtBuscar.addActionListener(e -> buscarUsuarios());
}
        // ============================================================
    // FILTRAR POR ESTADO (Activos / Inactivos / Todos)
    // ============================================================
    private void filtrarPorEstado() {
        String estadoSeleccionado = (String) cmbFiltroEstado.getSelectedItem();

        if (estadoSeleccionado.equals("Todos")) {
            cargarUsuarios();
            return;
        }

        // Determinar el valor booleano según el filtro
        boolean activo = estadoSeleccionado.equals("Activos");

        DefaultTableModel modelo = (DefaultTableModel) tablaUsuarios.getModel();
        modelo.setRowCount(0);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Conexion.getConexion();
            String sql = "SELECT id_usuario, usuario, nombre_completo, rol, activo " +
                        "FROM usuarios WHERE activo = ? ORDER BY id_usuario";

            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, activo);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_usuario");
                String usuario = rs.getString("usuario");
                String nombre = rs.getString("nombre_completo");
                String rol = rs.getString("rol");
                boolean esActivo = rs.getBoolean("activo");
                String estado = esActivo ? "🟢 Activo" : "🔴 Inactivo";

                modelo.addRow(new Object[]{id, usuario, nombre, rol, estado});
            }

            actualizarTotal();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al filtrar por estado: " + e.getMessage());
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
    // CARGAR USUARIOS EN LA TABLA
    // ============================================================
    private void cargarUsuarios() {
    // Resetear filtros a "Todos"
    cmbFiltroRol.setSelectedIndex(0);
    cmbFiltroEstado.setSelectedIndex(0);
    
    DefaultTableModel modelo = (DefaultTableModel) tablaUsuarios.getModel();
    modelo.setRowCount(0);

    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        conn = Conexion.getConexion();
        String sql = "SELECT id_usuario, usuario, nombre_completo, rol, activo FROM usuarios ORDER BY id_usuario";
        ps = conn.prepareStatement(sql);
        rs = ps.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("id_usuario");
            String usuario = rs.getString("usuario");
            String nombre = rs.getString("nombre_completo");
            String rol = rs.getString("rol");
            boolean activo = rs.getBoolean("activo");
            String estado = activo ? "🟢 Activo" : "🔴 Inactivo";

            modelo.addRow(new Object[]{id, usuario, nombre, rol, estado});
        }

        actualizarTotal();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + e.getMessage());
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
    // ACTUALIZAR TOTAL DE USUARIOS
    // ============================================================
    private void actualizarTotal() {
        int total = tablaUsuarios.getRowCount();
        lblTotal.setText("Total: " + total + " usuarios");
    }

    // ============================================================
    // BUSCAR USUARIOS POR TEXTO
    // ============================================================
    private void buscarUsuarios() {
        String busqueda = txtBuscar.getText().trim().toLowerCase();

        if (busqueda.isEmpty()) {
            cargarUsuarios();
            return;
        }

        DefaultTableModel modelo = (DefaultTableModel) tablaUsuarios.getModel();
        modelo.setRowCount(0);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Conexion.getConexion();
            String sql = "SELECT id_usuario, usuario, nombre_completo, rol, activo " +
                        "FROM usuarios " +
                        "WHERE usuario LIKE ? OR nombre_completo LIKE ? " +
                        "ORDER BY id_usuario";

            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + busqueda + "%");
            ps.setString(2, "%" + busqueda + "%");
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_usuario");
                String usuario = rs.getString("usuario");
                String nombre = rs.getString("nombre_completo");
                String rol = rs.getString("rol");
                boolean activo = rs.getBoolean("activo");
                String estado = activo ? "🟢 Activo" : "🔴 Inactivo";

                modelo.addRow(new Object[]{id, usuario, nombre, rol, estado});
            }

            actualizarTotal();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar: " + e.getMessage());
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
    // FILTRAR POR ROL
    // ============================================================
    private void filtrarPorRol() {
        String rolSeleccionado = (String) cmbFiltroRol.getSelectedItem();

        if (rolSeleccionado.equals("Todos")) {
            cargarUsuarios();
            return;
        }

        DefaultTableModel modelo = (DefaultTableModel) tablaUsuarios.getModel();
        modelo.setRowCount(0);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Conexion.getConexion();
            String sql = "SELECT id_usuario, usuario, nombre_completo, rol, activo " +
                        "FROM usuarios WHERE rol = ? ORDER BY id_usuario";

            ps = conn.prepareStatement(sql);
            ps.setString(1, rolSeleccionado);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_usuario");
                String usuario = rs.getString("usuario");
                String nombre = rs.getString("nombre_completo");
                String rol = rs.getString("rol");
                boolean activo = rs.getBoolean("activo");
                String estado = activo ? "🟢 Activo" : "🔴 Inactivo";

                modelo.addRow(new Object[]{id, usuario, nombre, rol, estado});
            }

            actualizarTotal();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al filtrar: " + e.getMessage());
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
    // AGREGAR USUARIO
    // ============================================================
   private void agregarUsuario() {

    // Crear campos del formulario
    javax.swing.JTextField txtUsuario = new javax.swing.JTextField(18);
    javax.swing.JTextField txtNombre = new javax.swing.JTextField(18);
    javax.swing.JPasswordField txtContrasena = new javax.swing.JPasswordField(18);

    javax.swing.JComboBox<String> cmbRol = new javax.swing.JComboBox<>(
        new String[]{"CAJERO", "ADMIN"}
    );

    // Crear un solo panel para todos los datos
    javax.swing.JPanel formulario = new javax.swing.JPanel(
        new java.awt.GridLayout(0, 2, 10, 12)
    );

    formulario.add(new javax.swing.JLabel("Nombre de usuario:"));
    formulario.add(txtUsuario);

    formulario.add(new javax.swing.JLabel("Nombre completo:"));
    formulario.add(txtNombre);

    formulario.add(new javax.swing.JLabel("Contraseña:"));
    formulario.add(txtContrasena);

    formulario.add(new javax.swing.JLabel("Rol:"));
    formulario.add(cmbRol);

    // Mostrar una sola ventana
    int opcion = JOptionPane.showConfirmDialog(
        this,
        formulario,
        "AGREGAR NUEVO USUARIO",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );

    if (opcion != JOptionPane.OK_OPTION) {
        return;
    }

    // Obtener los datos
    String usuario = txtUsuario.getText().trim();
    String nombre = txtNombre.getText().trim();
    String contrasena = new String(txtContrasena.getPassword()).trim();
    String rol = (String) cmbRol.getSelectedItem();

    // Validar campos obligatorios
    if (usuario.isEmpty() || nombre.isEmpty() || contrasena.isEmpty()) {
        JOptionPane.showMessageDialog(
            this,
            "Todos los campos son obligatorios",
            "Campos vacios",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    Connection conn = null;
    PreparedStatement ps = null;

    try {
        conn = Conexion.getConexion();

        String sql = "INSERT INTO usuarios "
                   + "(usuario, nombre_completo, contrasena, rol) "
                   + "VALUES (?, ?, ?, ?)";

        ps = conn.prepareStatement(sql);

        ps.setString(1, usuario);
        ps.setString(2, nombre);
        ps.setString(3, contrasena);
        ps.setString(4, rol);

        ps.executeUpdate();

        JOptionPane.showMessageDialog(
            this,
            "Usuario agregado exitosamente",
            "Registro correcto",
            JOptionPane.INFORMATION_MESSAGE
        );

        cargarUsuarios();

    } catch (SQLException e) {

        if (e.getMessage().contains("Duplicate entry")) {
            JOptionPane.showMessageDialog(
                this,
                "El usuario '" + usuario + "' ya existe",
                "Usuario duplicado",
                JOptionPane.WARNING_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Error al agregar usuario: " + e.getMessage()
            );
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
    // EDITAR USUARIO (AHORA TAMBIÉN EDITA EL NOMBRE DE USUARIO)
    // ============================================================
   private void editarUsuario() {
    int fila = tablaUsuarios.getSelectedRow();
    if (fila == -1) {
        JOptionPane.showMessageDialog(this, "Selecciona un usuario para editar");
        return;
    }

    int id = (int) tablaUsuarios.getValueAt(fila, 0);
    String usuarioActual = (String) tablaUsuarios.getValueAt(fila, 1);
    String nombreActual = (String) tablaUsuarios.getValueAt(fila, 2);
    String rolActual = (String) tablaUsuarios.getValueAt(fila, 3);

    // ============================================================
    // 1. VERIFICAR SI ES EL ADMIN
    // ============================================================
    boolean esAdmin = usuarioActual.equals("admin");

    // ============================================================
    // 2. SI ES ADMIN → SOLO NOMBRE COMPLETO
    // ============================================================
    if (esAdmin) {
        String nuevoNombre = JOptionPane.showInputDialog(
            this, 
            "🔒 ADMINISTRADOR - Solo puedes editar el nombre completo\n" +
            "Nombre completo (actual: " + nombreActual + "):",
            nombreActual
        );
        if (nuevoNombre == null) return;
        if (nuevoNombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "❌ El nombre completo no puede estar vacío");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Conexion.getConexion();
            String sql = "UPDATE usuarios SET nombre_completo = ? WHERE id_usuario = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, nuevoNombre.trim());
            ps.setInt(2, id);

            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "✅ Nombre del administrador actualizado exitosamente");
                cargarUsuarios();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al editar: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return; // ← IMPORTANTE: Termina aquí para el admin
    }

    // ============================================================
    // 3. SI NO ES ADMIN → EDICIÓN COMPLETA
    // ============================================================
    // Editar nombre de usuario
    String nuevoUsuario = JOptionPane.showInputDialog(
        this, 
        "Nuevo nombre de usuario (actual: " + usuarioActual + "):",
        usuarioActual
    );
    if (nuevoUsuario == null) return;
    if (nuevoUsuario.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "❌ El nombre de usuario no puede estar vacío");
        return;
    }

    // Editar nombre completo
    String nuevoNombre = JOptionPane.showInputDialog(
        this, 
        "Nuevo nombre completo (actual: " + nombreActual + "):",
        nombreActual
    );
    if (nuevoNombre == null) return;
    if (nuevoNombre.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "❌ El nombre completo no puede estar vacío");
        return;
    }

    // Editar rol
    String[] roles = {"CAJERO", "ADMIN"};
    String nuevoRol = (String) JOptionPane.showInputDialog(
        this,
        "Selecciona el nuevo rol (actual: " + rolActual + "):",
        "Rol del usuario",
        JOptionPane.QUESTION_MESSAGE,
        null,
        roles,
        rolActual
    );
    if (nuevoRol == null) return;

    // Guardar cambios en la base de datos
    Connection conn = null;
    PreparedStatement ps = null;

    try {
        conn = Conexion.getConexion();
        String sql = "UPDATE usuarios SET usuario = ?, nombre_completo = ?, rol = ? WHERE id_usuario = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, nuevoUsuario.trim());
        ps.setString(2, nuevoNombre.trim());
        ps.setString(3, nuevoRol);
        ps.setInt(4, id);

        int filasAfectadas = ps.executeUpdate();
        
        if (filasAfectadas > 0) {
            JOptionPane.showMessageDialog(this, "✅ Usuario editado exitosamente");
            cargarUsuarios();
        }

    } catch (SQLException e) {
        if (e.getMessage().contains("Duplicate entry")) {
            JOptionPane.showMessageDialog(this, "❌ El usuario '" + nuevoUsuario + "' ya existe");
        } else {
            JOptionPane.showMessageDialog(this, "Error al editar: " + e.getMessage());
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
    // CAMBIAR CONTRASEÑA
    // ============================================================
    private void cambiarContraseña() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario para cambiar su contraseña");
            return;
        }

        int id = (int) tablaUsuarios.getValueAt(fila, 0);
        String usuario = (String) tablaUsuarios.getValueAt(fila, 1);

        String nuevaPass = JOptionPane.showInputDialog(this, "Nueva contraseña para '" + usuario + "':");
        if (nuevaPass == null || nuevaPass.trim().isEmpty()) return;

        String confirmar = JOptionPane.showInputDialog(this, "Confirma la nueva contraseña:");
        if (confirmar == null || confirmar.trim().isEmpty()) return;

        if (!nuevaPass.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "❌ Las contraseñas no coinciden");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Conexion.getConexion();
            String sql = "UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, nuevaPass.trim());
            ps.setInt(2, id);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Contraseña cambiada exitosamente");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cambiar contraseña: " + e.getMessage());
        } finally {
            try {
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
        jLabel1 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnBuscar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaUsuarios = new javax.swing.JTable();
        btnAgregar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnCambiarPass = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();
        cmbFiltroRol = new javax.swing.JComboBox<>();
        btnActivarDesactivar = new javax.swing.JButton();
        cmbFiltroEstado = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(1260, 800));
        setMinimumSize(new java.awt.Dimension(1260, 800));

        jPanel1.setBackground(new java.awt.Color(255, 204, 255));
        jPanel1.setMaximumSize(new java.awt.Dimension(1260, 32767));
        jPanel1.setMinimumSize(new java.awt.Dimension(1260, 100));
        jPanel1.setPreferredSize(new java.awt.Dimension(1260, 100));

        jLabel1.setFont(new java.awt.Font("Roboto Black", 1, 48)); // NOI18N
        jLabel1.setText("ADMINISTRACION DE USUARIOS ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 828, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(361, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel2.setText("Buscar:");

        btnBuscar.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnBuscar.setText("Buscar");

        btnActualizar.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnActualizar.setText("Actualizar");

        tablaUsuarios.setFont(new java.awt.Font("Roboto Light", 1, 12)); // NOI18N
        tablaUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Usuario", "Nombre Completo", "Rol", "Estado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaUsuarios);

        btnAgregar.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnAgregar.setText("Agregar");

        btnEditar.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnEditar.setText("Editar");

        btnCambiarPass.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnCambiarPass.setText("Cambiar Contraseña");

        lblTotal.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        lblTotal.setText("Total:");

        cmbFiltroRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "ADMIN", "CAJERO", " " }));

        btnActivarDesactivar.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnActivarDesactivar.setText("Activar/Desactivar");

        cmbFiltroEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Activos", "Inactivos", " " }));

        jLabel3.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel3.setText("Estado:");

        jLabel4.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel4.setText("Puesto:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addGroup(layout.createSequentialGroup()
                    .addGap(67, 67, 67)
                    .addComponent(btnAgregar)
                    .addGap(92, 92, 92)
                    .addComponent(btnEditar)
                    .addGap(262, 262, 262)
                    .addComponent(btnCambiarPass)
                    .addGap(84, 84, 84)
                    .addComponent(btnActivarDesactivar)
                    .addContainerGap(347, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                    .addGap(93, 93, 93)
                    .addComponent(jLabel2)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(btnBuscar)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(btnActualizar)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(cmbFiltroEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbFiltroRol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(lblTotal))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(53, 53, 53)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1152, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBuscar)
                            .addComponent(btnActualizar)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbFiltroRol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbFiltroEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotal)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 464, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCambiarPass, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnActivarDesactivar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 82, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActivarDesactivar;
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCambiarPass;
    private javax.swing.JButton btnEditar;
    private javax.swing.JComboBox<String> cmbFiltroEstado;
    private javax.swing.JComboBox<String> cmbFiltroRol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable tablaUsuarios;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables
}
