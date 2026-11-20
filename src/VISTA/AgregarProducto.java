
package VISTA;


import java.awt.Color;
import javax.swing.table.DefaultTableModel;

public class AgregarProducto extends javax.swing.JPanel {
public AgregarProducto() {
    initComponents();
    jScrollPane5.getViewport().setBackground(new Color(255, 255, 255));
     tablaMenuCatalogo.setSelectionBackground(new Color(102, 153, 255));
    configurarMenuCatalogo();
}
 private void configurarMenuCatalogo() {

    // Crear las cuatro opciones del menu
    DefaultTableModel modeloMenu = new DefaultTableModel(
        new Object[][]{
            {"CATEGORIAS"},
            {"PRODUCTOS"},
            {"BASES Y FRUTAS"},
            {"PRECIOS DE CREPAS"},
            {"EXTRAS"}
        },
        new String[]{"SECCIONES"}
    ) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    tablaMenuCatalogo.setModel(modeloMenu);

    // Tipo, estilo y tamano de letra.
tablaMenuCatalogo.setFont(
    new java.awt.Font("Roboto Light", java.awt.Font.BOLD, 16)
);

// Centrar el texto horizontal y verticalmente.
javax.swing.table.DefaultTableCellRenderer centrado =
    new javax.swing.table.DefaultTableCellRenderer();

centrado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
centrado.setVerticalAlignment(javax.swing.SwingConstants.CENTER);

tablaMenuCatalogo.getColumnModel()
    .getColumn(0).setCellRenderer(centrado);
    tablaMenuCatalogo.setSelectionMode(
        javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    tablaMenuCatalogo.getTableHeader().setReorderingAllowed(false);


    // Preparar las secciones
    java.awt.CardLayout tarjetas = new java.awt.CardLayout();

    panelContenidoCatalogo.removeAll();
    panelContenidoCatalogo.setLayout(tarjetas);

  // Seccion Categorias
panelContenidoCatalogo.add(
    new Categorias(),
    "CATEGORIAS"
);

// Seccion Productos
VISTA.AGREGARPRODUCTOS.PanelProductos panelProductos =
        new VISTA.AGREGARPRODUCTOS.PanelProductos();

panelContenidoCatalogo.add(
    panelProductos,
    "PRODUCTOS"
);
VISTA.AGREGARPRODUCTOS.PanelBasesFrutas panelBasesFrutas =
        new VISTA.AGREGARPRODUCTOS.PanelBasesFrutas();

panelContenidoCatalogo.add(
    panelBasesFrutas,
    "BASES_FRUTAS"
);
VISTA.AGREGARPRODUCTOS.PanelPreciosCrepas panelPrecios =
    new VISTA.AGREGARPRODUCTOS.PanelPreciosCrepas();

panelContenidoCatalogo.add(
    panelPrecios,
    "PRECIOS"
);
VISTA.AGREGARPRODUCTOS.PanelExtras panelExtras =
    new VISTA.AGREGARPRODUCTOS.PanelExtras();

panelContenidoCatalogo.add(
    panelExtras,
    "EXTRAS"
);


    String[] secciones = {
    "CATEGORIAS",
    "PRODUCTOS",
    "BASES_FRUTAS",
    "PRECIOS",
    "EXTRAS"
};

    // Cambiar de seccion al seleccionar una fila
    tablaMenuCatalogo.getSelectionModel()
        .addListSelectionListener(e -> {

            if (e.getValueIsAdjusting()) {
                return;
            }

            int filaVista = tablaMenuCatalogo.getSelectedRow();

            if (filaVista == -1) {
                return;
            }

            int filaModelo =
                    tablaMenuCatalogo.convertRowIndexToModel(filaVista);
if (filaModelo == 1) {
    panelProductos.actualizarDatos();
} else if (filaModelo == 2) {
    panelBasesFrutas.actualizarDatos();
} else if (filaModelo == 3) {
    panelPrecios.actualizarDatos();
} else if (filaModelo == 4) {
    panelExtras.actualizarDatos();
}
            tarjetas.show(
                panelContenidoCatalogo,
                secciones[filaModelo]
            );
        });

    // Mostrar Categorias al entrar
    tablaMenuCatalogo.setRowSelectionInterval(0, 0);

    jLabel1.setText("ADMINISTRAR CATALOGO");

    jPanel5.revalidate();
    jPanel5.repaint();
}


    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tablaMenuCatalogo = new javax.swing.JTable();
        panelContenidoCatalogo = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 214, 234));
        setMaximumSize(new java.awt.Dimension(1280, 800));
        setMinimumSize(new java.awt.Dimension(1280, 800));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(new java.awt.Dimension(1260, 140));
        jPanel1.setMinimumSize(new java.awt.Dimension(1260, 140));
        jPanel1.setPreferredSize(new java.awt.Dimension(1260, 140));

        jLabel1.setFont(new java.awt.Font("Roboto Black", 1, 48)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/VISTA/IMAGENES/DISAP2.jpg"))); // NOI18N
        jLabel1.setText("ADMINISTRADOR DE PRODUCTOS");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(350, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 17, Short.MAX_VALUE))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1280, -1));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tablaMenuCatalogo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "SECCIONES"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaMenuCatalogo.setRowHeight(120);
        tablaMenuCatalogo.setSelectionBackground(new java.awt.Color(102, 153, 255));
        jScrollPane5.setViewportView(tablaMenuCatalogo);

        jPanel5.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 250, 640));

        panelContenidoCatalogo.setMaximumSize(new java.awt.Dimension(980, 620));
        panelContenidoCatalogo.setMinimumSize(new java.awt.Dimension(980, 620));
        panelContenidoCatalogo.setPreferredSize(new java.awt.Dimension(980, 620));
        panelContenidoCatalogo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel5.add(panelContenidoCatalogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 10, 980, 620));

        add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, 1250, 640));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPanel panelContenidoCatalogo;
    private javax.swing.JTable tablaMenuCatalogo;
    // End of variables declaration//GEN-END:variables
}
