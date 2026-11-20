
package VISTA;


import VISTA.CORTEDECAJA.panelCajaActual;
import VISTA.CORTEDECAJA.panelHistorialCortes;
import javax.swing.table.DefaultTableModel;

public class CorteCaja extends javax.swing.JPanel {
 private panelCajaActual cajaActual;
private panelHistorialCortes historialCortes;

private final java.awt.CardLayout tarjetasCaja =
    new java.awt.CardLayout();

public CorteCaja() {
    initComponents();
    configurarMenuCaja();
}

private void configurarMenuCaja() {

    DefaultTableModel modeloMenu = new DefaultTableModel(
        new Object[][]{
            {"CAJA ACTUAL"},
            {"HISTORIAL DE CORTES"}
        },
        new String[]{"MENU"}
    ) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    tablaMenuCaja.setModel(modeloMenu);
    tablaMenuCaja.setRowHeight(100);

    tablaMenuCaja.setSelectionMode(
        javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    tablaMenuCaja.getTableHeader().setReorderingAllowed(false);

    // Centrar los nombres de las secciones.
    javax.swing.table.DefaultTableCellRenderer centrado =
        new javax.swing.table.DefaultTableCellRenderer() {

            @Override
            protected void setValue(Object valor) {
                super.setValue(valor);

                if ("HISTORIAL DE CORTES".equals(valor)) {
                    setText(
                        "<html><center>"
                        + "HISTORIAL DE<br>CORTES"
                        + "</center></html>"
                    );
                }
            }
        };

    centrado.setHorizontalAlignment(
        javax.swing.SwingConstants.CENTER
    );

    centrado.setVerticalAlignment(
        javax.swing.SwingConstants.CENTER
    );

    tablaMenuCaja.getColumnModel()
        .getColumn(0)
        .setCellRenderer(centrado);

    jScrollPane3.getViewport().setBackground(
        java.awt.Color.WHITE
    );

    // Preparar el contenedor de los dos paneles.
    panelContenidoCaja.setLayout(tarjetasCaja);

    tablaMenuCaja.getSelectionModel()
        .addListSelectionListener(e -> {

            if (e.getValueIsAdjusting()) {
                return;
            }

            mostrarSeccionSeleccionada();
        });

    // Entrar directamente a caja actual.
    tablaMenuCaja.setRowSelectionInterval(0, 0);
}

private void mostrarSeccionSeleccionada() {

    int filaVista = tablaMenuCaja.getSelectedRow();

    if (filaVista == -1) {
        return;
    }

    int filaModelo =
        tablaMenuCaja.convertRowIndexToModel(filaVista);

    if (filaModelo == 0) {

        if (cajaActual == null) {
            cajaActual = new panelCajaActual();

            panelContenidoCaja.add(
                cajaActual,
                "CAJA_ACTUAL"
            );
        } else {
            cajaActual.actualizarDatos();
        }

        tarjetasCaja.show(
            panelContenidoCaja,
            "CAJA_ACTUAL"
        );

    } else if (filaModelo == 1) {

        if (historialCortes == null) {
            historialCortes = new panelHistorialCortes();

            panelContenidoCaja.add(
                historialCortes,
                "HISTORIAL"
            );
        } else {
            historialCortes.actualizarDatos();
        }

        tarjetasCaja.show(
            panelContenidoCaja,
            "HISTORIAL"
        );
    }

    panelContenidoCaja.revalidate();
    panelContenidoCaja.repaint();
}

// Conserva la llamada que hace tu VentanaPrincipal.
public void abrirCorteManual() {

    if (tablaMenuCaja.getSelectedRow() == 0) {
        mostrarSeccionSeleccionada();
    } else {
        tablaMenuCaja.setRowSelectionInterval(0, 0);
    }

    cajaActual.abrirCorteManual();
}

public void actualizarDatos() {
    mostrarSeccionSeleccionada();
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        tablaMenuCaja = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        panelContenidoCaja = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 204, 255));
        setMaximumSize(new java.awt.Dimension(1280, 800));
        setMinimumSize(new java.awt.Dimension(1280, 800));
        setPreferredSize(new java.awt.Dimension(1280, 800));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tablaMenuCaja.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        tablaMenuCaja.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "MENU"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaMenuCaja.setSelectionBackground(new java.awt.Color(102, 153, 255));
        jScrollPane3.setViewportView(tablaMenuCaja);

        add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 180, 720));

        jLabel1.setFont(new java.awt.Font("Roboto Black", 1, 48)); // NOI18N
        jLabel1.setText("CORTE DE CAJA");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 700, -1));

        panelContenidoCaja.setMaximumSize(new java.awt.Dimension(1100, 720));
        panelContenidoCaja.setMinimumSize(new java.awt.Dimension(1100, 720));
        panelContenidoCaja.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        add(panelContenidoCaja, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 80, 1100, 720));

        getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel panelContenidoCaja;
    private javax.swing.JTable tablaMenuCaja;
    // End of variables declaration//GEN-END:variables
}
