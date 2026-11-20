package VISTA.DETALLESDEVENTAS;

import Conexion.Conexion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public final class SelectorExtras {

    private SelectorExtras() {
    }

    // Guarda el nombre y precio que tenia el extra al seleccionarlo.
    public static final class Extra {

        public final int id;
        public final String nombre;
        public final BigDecimal precio;

        public Extra(int id, String nombre, BigDecimal precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }
    }

    public static final class Seleccion {

        private final List<Extra> extras;

        public Seleccion(List<Extra> extras) {
            this.extras = Collections.unmodifiableList(
                new ArrayList<>(extras)
            );
        }

        public List<Extra> getExtras() {
            return extras;
        }

        public BigDecimal getTotalPorUnidad() {
            BigDecimal total = BigDecimal.ZERO;

            for (Extra extra : extras) {
                total = total.add(extra.precio);
            }

            return total;
        }

        @Override
        public String toString() {
            if (extras.isEmpty()) {
                return "Sin extras";
            }

            StringBuilder texto = new StringBuilder();

            for (Extra extra : extras) {
                if (texto.length() > 0) {
                    texto.append("<br>");
                }

                // Compatible con el renderer HTML del carrito.
                texto.append(escaparHtml(extra.nombre));
                texto.append(" ($");
                texto.append(extra.precio.toPlainString());
                texto.append(")");
            }

            return texto.toString();
        }
    }

    private static String escaparHtml(String texto) {
        return texto.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
    }

    /*
     * Devuelve:
     * - Una seleccion con los extras elegidos.
     * - Una seleccion vacia si se continua sin extras.
     * - null si se cancela o falla la consulta.
     */
    public static Seleccion mostrar(
            Component padre,
            int idCategoria,
            String producto) {

        List<Extra> disponibles = new ArrayList<>();

        String sql =
    "SELECT e.id_extra, e.nombre, e.precio "
    + "FROM extras e "
    + "INNER JOIN categorias c "
    + "ON c.id_categoria = e.id_categoria "
    + "WHERE e.id_categoria = ? "
    + "AND e.activo = TRUE "
    + "AND e.disponible = TRUE "
    + "AND c.activo = TRUE "
    + "AND c.disponible = TRUE "
    + "ORDER BY e.nombre";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idCategoria);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    disponibles.add(new Extra(
                        rs.getInt("id_extra"),
                        rs.getString("nombre"),
                        rs.getBigDecimal("precio")
                    ));
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                padre,
                "Error al consultar extras:\n" + e.getMessage(),
                "Extras",
                JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        // Si no existen extras, no hace falta abrir la ventana.
        if (disponibles.isEmpty()) {
            return new Seleccion(Collections.emptyList());
        }

        DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Agregar", "Extra", "Precio"},
            0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return columna == 0;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                return columna == 0 ? Boolean.class : String.class;
            }
        };

        for (Extra extra : disponibles) {
            modelo.addRow(new Object[]{
                false,
                extra.nombre,
                "$" + extra.precio.toPlainString()
            });
        }

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(32);
        tabla.setFont(new Font("Roboto Light", Font.PLAIN, 14));
        tabla.setSelectionBackground(new Color(255, 214, 234));
        tabla.setSelectionForeground(Color.BLACK);
        tabla.setGridColor(new Color(230, 230, 230));
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);
        tabla.getTableHeader().setReorderingAllowed(false);

        // Encabezado estilizado
        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Roboto", Font.BOLD, 13));
        header.setBackground(new Color(255, 204, 255));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(
            header.getPreferredSize().width, 30
        ));

        // ============================================================
        // Renderer de la columna 0: mantiene el JCheckBox y lo centra
        // ============================================================
        tabla.getColumnModel().getColumn(0).setCellRenderer(
            new TableCellRenderer() {
                private final JCheckBox check = new JCheckBox();

                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {

                    check.setHorizontalAlignment(SwingConstants.CENTER);
                    check.setSelected(Boolean.TRUE.equals(value));
                    check.setBackground(isSelected
                        ? table.getSelectionBackground()
                        : table.getBackground());
                    check.setOpaque(true);

                    return check;
                }
            }
        );

        // ============================================================
        // Renderer de la columna 2: centra el precio
        // ============================================================
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getColumnModel().getColumn(2).setCellRenderer(centrado);

        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(90);

        JLabel lblTotal = new JLabel("Extras por unidad: $0.00");
        lblTotal.setFont(new Font("Roboto", Font.BOLD, 14));
        lblTotal.setForeground(new Color(120, 0, 90));

        modelo.addTableModelListener(e -> {
            BigDecimal total = BigDecimal.ZERO;

            for (int i = 0; i < modelo.getRowCount(); i++) {
                if (Boolean.TRUE.equals(modelo.getValueAt(i, 0))) {
                    total = total.add(disponibles.get(i).precio);
                }
            }

            lblTotal.setText(
                "Extras por unidad: $" + total.toPlainString()
            );
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(460, 260));
        scroll.setBorder(BorderFactory.createLineBorder(
            new Color(220, 220, 220)
        ));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblInfo = new JLabel(
            "<html>Producto: <b>" + escaparHtml(producto)
            + "</b><br>Selecciona los extras que deseas agregar.</html>"
        );
        lblInfo.setFont(new Font("Roboto Light", Font.PLAIN, 14));
        lblInfo.setForeground(new Color(60, 60, 60));

        panel.add(lblInfo, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lblTotal, BorderLayout.SOUTH);

        Object[] opciones = {"Agregar al carrito", "Cancelar"};

      JOptionPane optionPane = new JOptionPane(
    panel,
    JOptionPane.PLAIN_MESSAGE,
    JOptionPane.DEFAULT_OPTION,
    null,
    opciones,
    opciones[0]
);

// ✅ Forzar fondo blanco en el contenedor interno de JOptionPane
optionPane.setBackground(Color.WHITE);

// (opcional) recorrer los hijos por si el L&F pinta otro subpanel
for (Component c : optionPane.getComponents()) {
    c.setBackground(Color.WHITE);
}

javax.swing.JDialog dialog = optionPane.createDialog(padre, "Seleccionar extras");

// ✅ También el contentPane del JDialog
dialog.getContentPane().setBackground(Color.WHITE);
dialog.setVisible(true);

Object seleccionado = optionPane.getValue();

// Cerrar bien el diálogo
dialog.dispose();

if (seleccionado == null
        || !seleccionado.equals(opciones[0])) {
    return null;
}

        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }

        List<Extra> elegidos = new ArrayList<>();

        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modelo.getValueAt(i, 0))) {
                elegidos.add(disponibles.get(i));
            }
        }

        return new Seleccion(elegidos);
    }
}