
package VISTA.AGREGARPRODUCTOS;
import Conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
public class PanelProductos extends javax.swing.JPanel {
private Integer idProductoSeleccionado = null;
private String nombreImagenSeleccionada = "sin_imagen.png";
private javax.swing.table.TableRowSorter<DefaultTableModel>
        filtroProductos;

private boolean cargandoFiltroProductos = false;
private boolean actualizandoDisponibilidad = false;
private boolean recargandoTabla = false;

private final java.util.Map<Integer, Boolean> disponibilidadGuardada =
        new java.util.HashMap<>();

// Usar la misma carpeta que actualmente utiliza la pantalla de ventas
private final java.nio.file.Path carpetaImagenes =
        java.nio.file.Paths.get("src", "VISTA", "IMAGENES", "productos");
   public PanelProductos() {
    initComponents();
    cmbCategoria.setRenderer(
    new javax.swing.DefaultListCellRenderer() {
        @Override
        public java.awt.Component getListCellRendererComponent(
                javax.swing.JList<?> lista,
                Object valor,
                int indice,
                boolean seleccionado,
                boolean tieneFoco) {

            super.getListCellRendererComponent(
                lista, valor, indice, seleccionado, tieneFoco
            );

            if (valor == null) {
                setText(
                    cmbCategoria.getItemCount() == 0
                        ? "No hay categorias disponibles"
                        : "Selecciona una categoria"
                );
            }

            return this;
        }
    }
);
    FiltroPrecio.aplicar(txtPrecio);

    jScrollPane2.getViewport().setBackground(java.awt.Color.WHITE);

    configurarEventosProductos();
   configurarFiltroProductos();
   configurarDisponibilidad();
   actualizarDatos();;
}

public void actualizarDatos() {
    limpiarFormulario();
    cargarCategorias();
    cargarProductos();
}
private void cargarProductos() {
    if (recargandoTabla) {
        return;
    }

    // Terminar cualquier edicion antes de reemplazar las filas.
    if (tablaProductos.isEditing()
            && !tablaProductos.getCellEditor().stopCellEditing()) {
        return;
    }

    recargandoTabla = true;

    try {
        limpiarFormulario();

        // Mientras se cargan las filas, la tabla no usa el filtro.
        tablaProductos.setRowSorter(null);

        cargarProductosDesdeBD();

    } finally {
        try {
            if (filtroProductos != null) {
                // Reconstruir las posiciones con los datos actuales.
                filtroProductos.allRowsChanged();
                tablaProductos.setRowSorter(filtroProductos);
            }
        } finally {
            recargandoTabla = false;
        }
    }
}
private void configurarDisponibilidad() {
    DefaultTableModel modelo =
            (DefaultTableModel) tablaProductos.getModel();

    modelo.addTableModelListener(e -> {
        // Ignorar la carga de datos y la restauracion del checkbox.
        if (actualizandoDisponibilidad) {
            return;
        }

        // Solo atender cambios en Disponible: columna 4.
        if (e.getType() != javax.swing.event.TableModelEvent.UPDATE
                || e.getColumn() != 4) {
            return;
        }

        int fila = e.getFirstRow();

        if (fila < 0 || fila >= modelo.getRowCount()) {
            return;
        }

        // El evento ya proporciona la fila del modelo.
        int idProducto =
                ((Number) modelo.getValueAt(fila, 0)).intValue();

        Boolean anterior = disponibilidadGuardada.get(idProducto);

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

            // Comprobar el permiso en la base de datos al guardar.
            String sql =
                    "UPDATE productos "
                    + "SET disponible = ? "
                    + "WHERE id_producto = ? AND activo = TRUE "
                    + "AND EXISTS ("
                    + "SELECT 1 FROM usuarios "
                    + "WHERE id_usuario = ? "
                    + "AND rol = 'ADMIN' AND activo = TRUE"
                    + ")";

            try (Connection conn = Conexion.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setBoolean(1, nuevo);
                ps.setInt(2, idProducto);
                ps.setInt(
                    3, VISTA.Login.usuarioLogueado.idUsuario
                );

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                        "No se pudo cambiar la disponibilidad.\n"
                        + "Verifica que seas administrador y que "
                        + "el producto siga activo."
                    );
                }
            }

            disponibilidadGuardada.put(idProducto, nuevo);

        } catch (SQLException eGuardar) {
            // Restaurar el valor anterior si no pudo guardarse.
            actualizandoDisponibilidad = true;

            try {
                modelo.setValueAt(anterior, fila, 4);
            } finally {
                actualizandoDisponibilidad = false;
            }

            JOptionPane.showMessageDialog(
                this,
                "Error al guardar disponibilidad:\n"
                + eGuardar.getMessage(),
                "Disponibilidad",
                JOptionPane.ERROR_MESSAGE
            );
        }
    });
}
private void configurarFiltroProductos() {
    DefaultTableModel modelo =
            (DefaultTableModel) tablaProductos.getModel();

    filtroProductos =
            new javax.swing.table.TableRowSorter<>(modelo);

    // Usar el sorter para filtrar, conservando el orden actual.
    for (int i = 0; i < modelo.getColumnCount(); i++) {
        filtroProductos.setSortable(i, false);
    }

    tablaProductos.setRowSorter(filtroProductos);

    cmbFiltroProductos.setEditable(false);

    cmbFiltroProductos.addActionListener(e -> {
        if (cargandoFiltroProductos) {
            return;
        }

        // Evitar editar un producto que quede oculto por el filtro.
        limpiarFormulario();
        aplicarFiltroProductos();
    });
}

private void actualizarOpcionesFiltroProductos() {
    Object seleccionAnterior =
            cmbFiltroProductos.getSelectedItem();

    boolean teniaCategoria =
            cmbFiltroProductos.getSelectedIndex() > 0;

    DefaultTableModel modelo =
            (DefaultTableModel) tablaProductos.getModel();

    java.util.Set<String> categorias = new java.util.TreeSet<>();

    // Obtener las categorias de todos los productos cargados.
    for (int fila = 0; fila < modelo.getRowCount(); fila++) {
        Object categoria = modelo.getValueAt(fila, 3);

        if (categoria != null) {
            categorias.add(categoria.toString());
        }
    }

    cargandoFiltroProductos = true;

    try {
        cmbFiltroProductos.removeAllItems();
        cmbFiltroProductos.addItem("TODAS LAS CATEGORIAS");

        for (String categoria : categorias) {
            cmbFiltroProductos.addItem(categoria);
        }

        cmbFiltroProductos.setSelectedIndex(0);

        // Conservar el filtro si esa categoria sigue disponible.
        if (teniaCategoria && seleccionAnterior != null) {
            for (int i = 1;
                    i < cmbFiltroProductos.getItemCount();
                    i++) {

                if (seleccionAnterior.equals(
                        cmbFiltroProductos.getItemAt(i))) {

                    cmbFiltroProductos.setSelectedIndex(i);
                    break;
                }
            }
        }
    } finally {
        cargandoFiltroProductos = false;
    }

    aplicarFiltroProductos();
}

private void aplicarFiltroProductos() {
    if (filtroProductos == null) {
        return;
    }

    if (cmbFiltroProductos.getSelectedIndex() <= 0) {
        filtroProductos.setRowFilter(null);
        return;
    }

    final String categoria =
            cmbFiltroProductos.getSelectedItem().toString();

    filtroProductos.setRowFilter(
        new javax.swing.RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(
                    javax.swing.RowFilter.Entry<
                            ? extends DefaultTableModel,
                            ? extends Integer> fila) {

                // Categoria es la columna 3 del modelo.
                return categoria.equals(fila.getStringValue(3));
            }
        }
    );
}
private void configurarEventosProductos() {

    tablaProductos.setSelectionMode(
        javax.swing.ListSelectionModel.SINGLE_SELECTION
    );
tablaProductos.getSelectionModel().addListSelectionListener(e -> {
    if (e.getValueIsAdjusting()) {
        return;
    }

    javax.swing.SwingUtilities.invokeLater(() -> {
        if (tablaProductos.getSelectionModel()
                .getValueIsAdjusting()) {
            return;
        }

        cargarProductoSeleccionado();
    });
});

    // Agregar y Modificar ya tienen eventos en el disenador.
    // Aqui conectamos solamente estos dos botones.
    btnEliminar.addActionListener(e -> eliminarProducto());
    btnSeleccionarImagen.addActionListener(e -> seleccionarImagen());
}

private void limpiarFormulario() {
    idProductoSeleccionado = null;
    nombreImagenSeleccionada = "sin_imagen.png";

    tablaProductos.clearSelection();
    txtNombre.setText("");
    txtPrecio.setText("");
    btnEditar.setEnabled(false);
    btnEliminar.setEnabled(false);

    mostrarImagen(nombreImagenSeleccionada);
    // Conservar la categoria elegida.
// Seleccionar la primera solo si no habia ninguna seleccionada.
if (cmbCategoria.getSelectedIndex() == -1
        && cmbCategoria.getItemCount() > 0) {

    cmbCategoria.setSelectedIndex(0);
}

cmbCategoria.setToolTipText(null);
}

private void cargarProductoSeleccionado() {
if (recargandoTabla) {
        return;
    }
    int filaVista = tablaProductos.getSelectedRow();

    if (filaVista == -1) {
        idProductoSeleccionado = null;
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
        return;
    }

    int filaModelo = tablaProductos.convertRowIndexToModel(filaVista);

    int id = ((Number) tablaProductos.getModel()
            .getValueAt(filaModelo, 0)).intValue();

    idProductoSeleccionado = null;
    btnEditar.setEnabled(false);
    btnEliminar.setEnabled(false);

    String sql =
            "SELECT nombre, precio, imagen, id_categoria "
            + "FROM productos "
            + "WHERE id_producto = ? AND activo = TRUE";

    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);

        try (ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                limpiarFormulario();
                JOptionPane.showMessageDialog(
                    this, "El producto ya no esta disponible."
                );
                return;
            }

            txtNombre.setText(rs.getString("nombre"));
            txtPrecio.setText(
                rs.getBigDecimal("precio").toPlainString()
            );

            // Seleccionar por ID, no por nombre
            int idCategoria = rs.getInt("id_categoria");
int indiceCategoria = -1;

for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
    String item = cmbCategoria.getItemAt(i);
    String idTexto = item.split(" - ", 2)[0].trim();

    if (idTexto.equals(String.valueOf(idCategoria))) {
        indiceCategoria = i;
        break;
    }
}

cmbCategoria.setSelectedIndex(indiceCategoria);

if (indiceCategoria == -1) {
    cmbCategoria.setToolTipText(
        "La categoria de este producto no esta en la lista. "
        + "Selecciona una categoria valida antes de guardar."
    );
} else {
    cmbCategoria.setToolTipText(null);
}

            nombreImagenSeleccionada = rs.getString("imagen");

            if (nombreImagenSeleccionada == null
                    || nombreImagenSeleccionada.trim().isEmpty()) {
                nombreImagenSeleccionada = "sin_imagen.png";
            }

            mostrarImagen(nombreImagenSeleccionada);

            idProductoSeleccionado = id;
            btnEditar.setEnabled(true);
            btnEliminar.setEnabled(true);
        }

    } catch (SQLException e) {
        limpiarFormulario();

        JOptionPane.showMessageDialog(
            this,
            "Error al cargar el producto:\n" + e.getMessage()
        );
        e.printStackTrace();
    }
}

private java.math.BigDecimal leerPrecio() {

    try {
        java.math.BigDecimal precio = new java.math.BigDecimal(
            txtPrecio.getText().trim().replace(",", ".")
        ).setScale(2, java.math.RoundingMode.UNNECESSARY);

        if (precio.signum() <= 0
                || precio.compareTo(
                    new java.math.BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException();
        }

        return precio;

    } catch (IllegalArgumentException | ArithmeticException e) {
        JOptionPane.showMessageDialog(
            this,
            "Ingresa un precio mayor que cero,\n"
            + "con un maximo de dos decimales."
        );
        txtPrecio.requestFocusInWindow();
        return null;
    }
}

private Integer obtenerIdCategoria() {

    Object seleccion = cmbCategoria.getSelectedItem();

    if (seleccion == null) {
        JOptionPane.showMessageDialog(
            this, "Selecciona una categoria activa."
        );
        return null;
    }

    try {
        return Integer.valueOf(
            seleccion.toString().split(" - ", 2)[0].trim()
        );

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
            this, "La categoria seleccionada no es valida."
        );
        return null;
    }
}

private boolean nombreValido(String nombre) {

    if (nombre.isEmpty() || nombre.length() > 100) {
        JOptionPane.showMessageDialog(
            this,
            "El nombre debe tener entre 1 y 100 caracteres."
        );
        txtNombre.requestFocusInWindow();
        return false;
    }

    return true;
}

private boolean existeProducto(
        Connection conn,
        String nombre,
        int idCategoria,
        int idExcluir) throws SQLException {

    String sql =
            "SELECT id_producto FROM productos "
            + "WHERE nombre = ? AND id_categoria = ? "
            + "AND activo = TRUE AND id_producto <> ? LIMIT 1";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, nombre);
        ps.setInt(2, idCategoria);
        ps.setInt(3, idExcluir);

        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}

private void editarProducto() {

    if (idProductoSeleccionado == null) {
        JOptionPane.showMessageDialog(
            this, "Selecciona un producto de la tabla."
        );
        return;
    }

    int id = idProductoSeleccionado;
    String nombre = txtNombre.getText().trim();

    if (!nombreValido(nombre)) {
        return;
    }

    java.math.BigDecimal precio = leerPrecio();
    if (precio == null) {
        return;
    }

    Integer idCategoria = obtenerIdCategoria();
    if (idCategoria == null) {
        return;
    }

    int opcion = JOptionPane.showConfirmDialog(
        this,
        "Guardar los cambios del producto #" + id + "?",
        "Modificar producto",
        JOptionPane.YES_NO_OPTION
    );

    if (opcion != JOptionPane.YES_OPTION) {
        return;
    }

    try (Connection conn = Conexion.getConexion()) {

        if (existeProducto(conn, nombre, idCategoria, id)) {
            JOptionPane.showMessageDialog(
                this,
                "Ya existe otro producto activo con ese nombre\n"
                + "en la categoria seleccionada."
            );
            return;
        }

        String sql =
                "UPDATE productos p "
                + "INNER JOIN categorias c ON c.id_categoria = ? "
                + "SET p.nombre = ?, p.precio = ?, p.imagen = ?, "
                + "p.id_categoria = c.id_categoria "
                + "WHERE p.id_producto = ? AND p.activo = TRUE "
                + "AND c.activo = TRUE AND c.es_especial = FALSE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCategoria);
            ps.setString(2, nombre);
            ps.setBigDecimal(3, precio);
            ps.setString(4, nombreImagenSeleccionada);
            ps.setInt(5, id);

            int actualizados = ps.executeUpdate();

            if (actualizados == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "No hubo cambios aplicados.\n"
                    + "Los datos pueden ser iguales o el producto\n"
                    + "o la categoria ya no estan disponibles."
                );
                return;
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al modificar el producto:\n" + e.getMessage()
        );
        e.printStackTrace();
        return;
    }

    cargarProductos();
    limpiarFormulario();

    JOptionPane.showMessageDialog(
        this, "Producto modificado correctamente."
    );
}

private void eliminarProducto() {

    if (idProductoSeleccionado == null) {
        JOptionPane.showMessageDialog(
            this, "Selecciona un producto de la tabla."
        );
        return;
    }

    int id = idProductoSeleccionado;

    int opcion = JOptionPane.showConfirmDialog(
        this,
        "Desactivar el producto #" + id + "?\n"
        + "Dejara de estar disponible para nuevas ventas.",
        "Eliminar producto",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
    );

    if (opcion != JOptionPane.YES_OPTION) {
        return;
    }

    String sql =
            "UPDATE productos SET activo = FALSE "
            + "WHERE id_producto = ? AND activo = TRUE";

    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);

        if (ps.executeUpdate() != 1) {
            JOptionPane.showMessageDialog(
                this, "El producto ya fue desactivado o no existe."
            );
        } else {
            JOptionPane.showMessageDialog(
                this, "Producto desactivado correctamente."
            );
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al desactivar el producto:\n" + e.getMessage()
        );
        e.printStackTrace();
        return;
    }

    cargarProductos();
    limpiarFormulario();
}

private void seleccionarImagen() {

    javax.swing.JFileChooser selector =
            new javax.swing.JFileChooser();

    selector.setDialogTitle("Seleccionar imagen del producto");
    selector.setAcceptAllFileFilterUsed(false);

    selector.setFileFilter(
        new javax.swing.filechooser.FileNameExtensionFilter(
            "Imagenes PNG, JPG y JPEG",
            "png", "jpg", "jpeg"
        )
    );

    if (selector.showOpenDialog(this)
            != javax.swing.JFileChooser.APPROVE_OPTION) {
        return;
    }

    try {
        java.awt.image.BufferedImage imagen =
                javax.imageio.ImageIO.read(selector.getSelectedFile());

        if (imagen == null) {
            throw new java.io.IOException(
                "El archivo no contiene una imagen compatible."
            );
        }

        java.nio.file.Files.createDirectories(carpetaImagenes);

        // Nombre nuevo para no sobrescribir imagenes existentes
        String nombreArchivo =
                java.util.UUID.randomUUID().toString() + ".png";

        java.nio.file.Path destino =
                carpetaImagenes.resolve(nombreArchivo);

        if (!javax.imageio.ImageIO.write(
                imagen, "png", destino.toFile())) {
            throw new java.io.IOException(
                "No se pudo guardar la imagen."
            );
        }

        nombreImagenSeleccionada = nombreArchivo;
        mostrarImagen(nombreImagenSeleccionada);

    } catch (java.io.IOException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al seleccionar la imagen:\n" + e.getMessage()
        );
        e.printStackTrace();
    }
}

private void mostrarImagen(String nombreArchivo) {

    lblVistaPreviaImagen.setIcon(null);
    lblVistaPreviaImagen.setText("");

    try {
        java.nio.file.Path ruta =
                carpetaImagenes.resolve(nombreArchivo);

        if (!java.nio.file.Files.isRegularFile(ruta)) {
            lblVistaPreviaImagen.setText("Sin imagen");
            return;
        }

        java.awt.image.BufferedImage imagen =
                javax.imageio.ImageIO.read(ruta.toFile());

        if (imagen == null) {
            lblVistaPreviaImagen.setText("Sin imagen");
            return;
        }

        // Ajustar sin deformar la imagen
        double escala = Math.min(
            100.0 / imagen.getWidth(),
            100.0 / imagen.getHeight()
        );

        int ancho = Math.max(
            1, (int) Math.round(imagen.getWidth() * escala)
        );

        int alto = Math.max(
            1, (int) Math.round(imagen.getHeight() * escala)
        );

        lblVistaPreviaImagen.setIcon(
            new javax.swing.ImageIcon(
                imagen.getScaledInstance(
                    ancho,
                    alto,
                    java.awt.Image.SCALE_SMOOTH
                )
            )
        );

    } catch (java.io.IOException e) {
        lblVistaPreviaImagen.setText("Sin imagen");
        e.printStackTrace();
    }
}
private void cargarCategorias() {

    cmbCategoria.removeAllItems();

    String sql =
            "SELECT id_categoria, nombre FROM categorias "
            + "WHERE activo = TRUE AND es_especial = FALSE "
            + "ORDER BY nombre";

    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            cmbCategoria.addItem(
                rs.getInt("id_categoria")
                + " - "
                + rs.getString("nombre")
            );
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al cargar categorias:\n" + e.getMessage()
        );
        e.printStackTrace();
    }
}

private void cargarProductosDesdeBD() {
    DefaultTableModel modelo =
            (DefaultTableModel) tablaProductos.getModel();

    actualizandoDisponibilidad = true;

    try {
        modelo.setRowCount(0);
        disponibilidadGuardada.clear();

        String sql =
                "SELECT p.id_producto, p.nombre, p.precio, "
                + "p.disponible, c.nombre AS categoria "
                + "FROM productos p "
                + "INNER JOIN categorias c "
                + "ON p.id_categoria = c.id_categoria "
                + "WHERE p.activo = TRUE "
                + "ORDER BY c.nombre, p.nombre";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_producto");
                boolean disponible = rs.getBoolean("disponible");

                disponibilidadGuardada.put(id, disponible);

                modelo.addRow(new Object[]{
                    id,
                    rs.getString("nombre"),
                    "$" + rs.getBigDecimal("precio").toPlainString(),
                    rs.getString("categoria"),
                    disponible
                });
            }
        }

    } catch (SQLException e) {
        modelo.setRowCount(0);
        disponibilidadGuardada.clear();

        JOptionPane.showMessageDialog(
            this,
            "Error al cargar productos:\n" + e.getMessage()
        );

    } finally {
        actualizandoDisponibilidad = false;
    }

    actualizarOpcionesFiltroProductos();
}
private void agregarProducto() {

    String nombre = txtNombre.getText().trim();
    String textoPrecio = txtPrecio.getText().trim();

    if (nombre.isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "Escribe el nombre del producto."
        );
        txtNombre.requestFocusInWindow();
        return;
    }

    if (nombre.length() > 100) {
        JOptionPane.showMessageDialog(
            this, "El nombre no debe superar 100 caracteres."
        );
        return;
    }

    java.math.BigDecimal precio;

    try {
        precio = new java.math.BigDecimal(
            textoPrecio.replace(",", ".")
        ).setScale(2, java.math.RoundingMode.UNNECESSARY);

        if (precio.signum() <= 0
                || precio.compareTo(
                    new java.math.BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException();
        }

    } catch (IllegalArgumentException | ArithmeticException e) {
        JOptionPane.showMessageDialog(
            this,
            "Ingresa un precio mayor que cero,\n"
            + "con un maximo de dos decimales."
        );
        txtPrecio.requestFocusInWindow();
        return;
    }

    Object seleccion = cmbCategoria.getSelectedItem();

    if (seleccion == null) {
        JOptionPane.showMessageDialog(
            this, "Selecciona una categoria."
        );
        return;
    }

    int idCategoria;

    try {
        idCategoria = Integer.parseInt(
            seleccion.toString().split(" - ", 2)[0].trim()
        );
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
            this, "La categoria seleccionada no es valida."
        );
        return;
    }

    try (Connection conn = Conexion.getConexion()) {

        // Comprobar si ya existe un producto activo con ese nombre
        String sqlBuscar =
                "SELECT id_producto FROM productos "
                + "WHERE nombre = ? AND id_categoria = ? "
                + "AND activo = TRUE LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombre);
            ps.setInt(2, idCategoria);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Ya existe un producto activo con ese nombre\n"
                        + "en la categoria seleccionada."
                    );
                    return;
                }
            }
        }

        // Insertar solamente si la categoria sigue activa
        String sql =
                "INSERT INTO productos "
                + "(nombre, precio, imagen, id_categoria, activo) "
                + "SELECT ?, ?, ?, id_categoria, TRUE "
                + "FROM categorias "
                + "WHERE id_categoria = ? "
                + "AND activo = TRUE AND es_especial = FALSE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setBigDecimal(2, precio);
            ps.setString(3, nombreImagenSeleccionada);
            ps.setInt(4, idCategoria);

            if (ps.executeUpdate() != 1) {
                JOptionPane.showMessageDialog(
                    this,
                    "La categoria ya no esta disponible.\n"
                    + "Vuelve a entrar en Productos para actualizarla."
                );
                return;
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al agregar el producto:\n" + e.getMessage()
        );
        e.printStackTrace();
        return;
    }

    // Actualizar la tabla y limpiar los campos
   cargarProductos();
   limpiarFormulario();
   txtNombre.requestFocusInWindow();

    JOptionPane.showMessageDialog(
        this, "Producto agregado correctamente."
    );
}

  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtPrecio = new javax.swing.JTextField();
        btnEditar = new javax.swing.JButton();
        cmbCategoria = new javax.swing.JComboBox<>();
        btnSeleccionarImagen = new javax.swing.JButton();
        lblVistaPreviaImagen = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaProductos = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        cmbFiltroProductos = new javax.swing.JComboBox<>();

        setBackground(new java.awt.Color(255, 204, 255));
        setMaximumSize(new java.awt.Dimension(980, 620));
        setMinimumSize(new java.awt.Dimension(980, 620));
        setPreferredSize(new java.awt.Dimension(950, 620));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel4.setText("Nombre:");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 63, -1, -1));

        txtNombre.setBorder(null);
        jPanel3.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 60, 174, 20));

        btnAgregar.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnAgregar.setText("Agregar ");
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);
        jPanel3.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 356, -1, -1));

        btnEliminar.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnEliminar.setText("Eliminar ");
        jPanel3.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(109, 356, -1, -1));

        jLabel3.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel3.setText("Precio:");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));

        txtPrecio.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPrecio.setBorder(null);
        jPanel3.add(txtPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 100, 178, -1));

        btnEditar.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnEditar.setText("Modificar");
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        jPanel3.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(214, 356, -1, -1));

        cmbCategoria.setFont(new java.awt.Font("Roboto Light", 1, 12)); // NOI18N
        cmbCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel3.add(cmbCategoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 10, -1, -1));

        btnSeleccionarImagen.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        btnSeleccionarImagen.setText("Seleccionar imagen");
        jPanel3.add(btnSeleccionarImagen, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 155, -1, -1));

        lblVistaPreviaImagen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblVistaPreviaImagen.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblVistaPreviaImagen.setMaximumSize(new java.awt.Dimension(100, 100));
        lblVistaPreviaImagen.setMinimumSize(new java.awt.Dimension(100, 100));
        lblVistaPreviaImagen.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel3.add(lblVistaPreviaImagen, new org.netbeans.lib.awtextra.AbsoluteConstraints(86, 196, -1, -1));

        jLabel5.setFont(new java.awt.Font("Roboto ExtraBold", 1, 24)); // NOI18N
        jLabel5.setText("Agregar");
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(91, 7, -1, 25));

        tablaProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Precio", "Categoria", "Disponible"
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
        tablaProductos.setGridColor(new java.awt.Color(0, 0, 0));
        tablaProductos.setRowHeight(25);
        tablaProductos.setSelectionBackground(new java.awt.Color(255, 204, 255));
        jScrollPane2.setViewportView(tablaProductos);
        if (tablaProductos.getColumnModel().getColumnCount() > 0) {
            tablaProductos.getColumnModel().getColumn(0).setMinWidth(100);
            tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(100);
            tablaProductos.getColumnModel().getColumn(0).setMaxWidth(100);
            tablaProductos.getColumnModel().getColumn(2).setMinWidth(100);
            tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(100);
            tablaProductos.getColumnModel().getColumn(2).setMaxWidth(100);
            tablaProductos.getColumnModel().getColumn(3).setMinWidth(150);
            tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(150);
            tablaProductos.getColumnModel().getColumn(3).setMaxWidth(150);
            tablaProductos.getColumnModel().getColumn(4).setMinWidth(70);
            tablaProductos.getColumnModel().getColumn(4).setPreferredWidth(70);
            tablaProductos.getColumnModel().getColumn(4).setMaxWidth(70);
        }

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(341, 7, 620, 530));
        jPanel3.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 80, 180, -1));
        jPanel3.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 120, 180, -1));

        jLabel6.setFont(new java.awt.Font("Roboto ExtraBold", 1, 48)); // NOI18N
        jLabel6.setText("PRODUCTOS");

        cmbFiltroProductos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 968, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cmbFiltroProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cmbFiltroProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 543, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
       agregarProducto();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
     editarProducto();
    }//GEN-LAST:event_btnEditarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnSeleccionarImagen;
    private javax.swing.JComboBox<String> cmbCategoria;
    private javax.swing.JComboBox<String> cmbFiltroProductos;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblVistaPreviaImagen;
    private javax.swing.JTable tablaProductos;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecio;
    // End of variables declaration//GEN-END:variables
}
