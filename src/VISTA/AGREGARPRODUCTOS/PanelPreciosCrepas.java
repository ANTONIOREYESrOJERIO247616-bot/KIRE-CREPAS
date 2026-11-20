/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package VISTA.AGREGARPRODUCTOS;

import Conexion.Conexion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
public class PanelPreciosCrepas extends javax.swing.JPanel {

  public PanelPreciosCrepas() {
    initComponents();
    FiltroPrecio.aplicar(txtPrecio2);

    txtTipoPrecio.setEditable(false);
    btnModificarPrecio.setEnabled(false);

    jScrollPane4.getViewport().setBackground(java.awt.Color.WHITE);

    tablaPreciosCrepas.setSelectionMode(
        javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    tablaPreciosCrepas.getTableHeader().setReorderingAllowed(false);

    tablaPreciosCrepas.getSelectionModel()
        .addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarPrecioSeleccionado();
            }
        });

    actualizarDatos();
}
  public void actualizarDatos() {
    tablaPreciosCrepas.clearSelection();
    limpiarFormularioPrecio();

    DefaultTableModel modelo =
        (DefaultTableModel) tablaPreciosCrepas.getModel();

    modelo.setRowCount(0);

    String sql =
        "SELECT id_precio, tipo, precio "
        + "FROM precios_crepas ORDER BY id_precio";

    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            modelo.addRow(new Object[]{
                rs.getInt("id_precio"),
                rs.getString("tipo"),
                rs.getBigDecimal("precio")
            });
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al cargar precios:\n" + e.getMessage()
        );
    }
}

private void limpiarFormularioPrecio() {
    txtTipoPrecio.setText("");
    txtPrecio2.setText("");
    btnModificarPrecio.setEnabled(false);
}

private void cargarPrecioSeleccionado() {
    int filaVista = tablaPreciosCrepas.getSelectedRow();

    if (filaVista == -1) {
        limpiarFormularioPrecio();
        return;
    }

    int filaModelo =
        tablaPreciosCrepas.convertRowIndexToModel(filaVista);

    DefaultTableModel modelo =
        (DefaultTableModel) tablaPreciosCrepas.getModel();

    txtTipoPrecio.setText(
        modelo.getValueAt(filaModelo, 1).toString()
    );

    BigDecimal precio =
        (BigDecimal) modelo.getValueAt(filaModelo, 2);

    txtPrecio2.setText(precio.toPlainString());
    btnModificarPrecio.setEnabled(true);
}

private void modificarPrecio() {
    int filaVista = tablaPreciosCrepas.getSelectedRow();

    if (filaVista == -1) {
        JOptionPane.showMessageDialog(
            this,
            "Selecciona un tipo de crepa en la tabla."
        );
        return;
    }

    String textoPrecio = txtPrecio2.getText().trim();

    if (textoPrecio.isEmpty()) {
        JOptionPane.showMessageDialog(
            this,
            "Ingresa el nuevo precio."
        );
        txtPrecio2.requestFocusInWindow();
        return;
    }

    BigDecimal nuevoPrecio;

    try {
        nuevoPrecio = new BigDecimal(
            textoPrecio.replace(",", ".")
        ).setScale(2, RoundingMode.UNNECESSARY);

    } catch (NumberFormatException | ArithmeticException e) {
        JOptionPane.showMessageDialog(
            this,
            "Ingresa un precio valido con hasta dos decimales.\n"
            + "Ejemplo: 55.00"
        );
        txtPrecio2.requestFocusInWindow();
        return;
    }

    if (nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0
            || nuevoPrecio.compareTo(
                new BigDecimal("99999999.99")
            ) > 0) {

        JOptionPane.showMessageDialog(
            this,
            "El precio debe ser mayor que cero "
            + "y no superar 99999999.99."
        );
        return;
    }

    int filaModelo =
        tablaPreciosCrepas.convertRowIndexToModel(filaVista);

    DefaultTableModel modelo =
        (DefaultTableModel) tablaPreciosCrepas.getModel();

    int idPrecio =
        ((Number) modelo.getValueAt(filaModelo, 0)).intValue();

    String tipo = modelo.getValueAt(filaModelo, 1).toString();

    BigDecimal precioAnterior =
        (BigDecimal) modelo.getValueAt(filaModelo, 2);

    if (nuevoPrecio.compareTo(precioAnterior) == 0) {
        JOptionPane.showMessageDialog(
            this,
            "El precio ingresado es igual al actual."
        );
        return;
    }

    int respuesta = JOptionPane.showConfirmDialog(
        this,
        "Tipo: " + tipo + "\n"
        + "Precio anterior: $" + precioAnterior.toPlainString() + "\n"
        + "Nuevo precio: $" + nuevoPrecio.toPlainString() + "\n\n"
        + "¿Guardar el cambio?",
        "Modificar precio",
        JOptionPane.YES_NO_OPTION
    );

    if (respuesta != JOptionPane.YES_OPTION) {
        return;
    }

    // Verificar tambien el precio anterior evita sobrescribir
    // un cambio realizado por otro usuario mientras tanto.
    String sql =
        "UPDATE precios_crepas SET precio = ? "
        + "WHERE id_precio = ? AND precio = ?";

    int filas;

    try (Connection conn = Conexion.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setBigDecimal(1, nuevoPrecio);
        ps.setInt(2, idPrecio);
        ps.setBigDecimal(3, precioAnterior);

        filas = ps.executeUpdate();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Error al modificar el precio:\n" + e.getMessage()
        );
        return;
    }

    actualizarDatos();

    if (filas == 0) {
        JOptionPane.showMessageDialog(
            this,
            "El registro cambio o ya no existe.\n"
            + "Revisa la tabla actualizada e intenta nuevamente."
        );
        return;
    }

    JOptionPane.showMessageDialog(
        this,
        "Precio modificado correctamente."
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

        jPanel2 = new javax.swing.JPanel();
        txtTipoPrecio = new javax.swing.JTextField();
        txtPrecio2 = new javax.swing.JTextField();
        btnModificarPrecio = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tablaPreciosCrepas = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 204, 255));
        setMaximumSize(new java.awt.Dimension(980, 620));
        setMinimumSize(new java.awt.Dimension(980, 620));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(980, 620));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtTipoPrecio.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtTipoPrecio.setBorder(null);
        txtTipoPrecio.addActionListener(this::txtTipoPrecioActionPerformed);
        jPanel2.add(txtTipoPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 50, 252, -1));

        txtPrecio2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPrecio2.setBorder(null);
        jPanel2.add(txtPrecio2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 100, 252, -1));

        btnModificarPrecio.setText("Modificar Precio");
        btnModificarPrecio.addActionListener(this::btnModificarPrecioActionPerformed);
        jPanel2.add(btnModificarPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 80, 219, -1));

        jLabel9.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel9.setText("Tipo:");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 53, -1, -1));

        jLabel10.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        jLabel10.setText("Precio:");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, -1, -1));

        tablaPreciosCrepas.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        tablaPreciosCrepas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Tipo", "Precio"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaPreciosCrepas.setMinimumSize(new java.awt.Dimension(44, 80));
        tablaPreciosCrepas.setRowHeight(25);
        jScrollPane4.setViewportView(tablaPreciosCrepas);
        if (tablaPreciosCrepas.getColumnModel().getColumnCount() > 0) {
            tablaPreciosCrepas.getColumnModel().getColumn(0).setMinWidth(100);
            tablaPreciosCrepas.getColumnModel().getColumn(0).setPreferredWidth(100);
            tablaPreciosCrepas.getColumnModel().getColumn(0).setMaxWidth(100);
            tablaPreciosCrepas.getColumnModel().getColumn(2).setMinWidth(100);
            tablaPreciosCrepas.getColumnModel().getColumn(2).setPreferredWidth(100);
            tablaPreciosCrepas.getColumnModel().getColumn(2).setMaxWidth(100);
        }

        jPanel2.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 940, 370));
        jPanel2.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 70, 250, -1));
        jPanel2.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 120, 250, -1));

        jLabel8.setFont(new java.awt.Font("Roboto Black", 1, 48)); // NOI18N
        jLabel8.setText("PRECIOS CREPAS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 968, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtTipoPrecioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTipoPrecioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTipoPrecioActionPerformed

    private void btnModificarPrecioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarPrecioActionPerformed
      modificarPrecio();
    }//GEN-LAST:event_btnModificarPrecioActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnModificarPrecio;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable tablaPreciosCrepas;
    private javax.swing.JTextField txtPrecio2;
    private javax.swing.JTextField txtTipoPrecio;
    // End of variables declaration//GEN-END:variables
}
