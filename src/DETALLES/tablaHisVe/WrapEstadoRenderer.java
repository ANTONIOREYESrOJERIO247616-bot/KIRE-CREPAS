package DETALLES.tablaHisVe;

import DETALLES.tablaVcat.WrapRenderer;
import java.awt.Color;
import java.awt.Component;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableModel;

public class WrapEstadoRenderer extends WrapRenderer {

    private final int columnaEstadoModelo;

    public WrapEstadoRenderer() {
        this(9);
    }

    public WrapEstadoRenderer(int columnaEstadoModelo) {
        this.columnaEstadoModelo = columnaEstadoModelo;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

        // Reutilizar el ajuste de texto del carrito.
        super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column
        );

        TableModel modelo = table.getModel();
        int filaModelo = table.convertRowIndexToModel(row);

        Object idPedido = modelo.getValueAt(filaModelo, 0);

        String estado = String.valueOf(
            modelo.getValueAt(filaModelo, columnaEstadoModelo)
        ).trim();

        boolean primeraFila = true;
        boolean ultimaFila = true;

        if (row > 0) {
            int anterior = table.convertRowIndexToModel(row - 1);

            primeraFila = !Objects.equals(
                idPedido,
                modelo.getValueAt(anterior, 0)
            );
        }

        if (row < table.getRowCount() - 1) {
            int siguiente = table.convertRowIndexToModel(row + 1);

            ultimaFila = !Objects.equals(
                idPedido,
                modelo.getValueAt(siguiente, 0)
            );
        }

        // Solo las verticales cambian de color.
        final Color colorVertical = Color.BLACK;

        int arriba;
        int abajo;
        int izquierda = 0;
        int derecha = 0;
        Color colorBorde;

        if ("CANCELADO".equalsIgnoreCase(estado)) {
            if (!isSelected) {
                setBackground(new Color(255, 230, 230));
                setForeground(Color.RED);
            }

            // Contorno del pedido cancelado completo (rojo).
            arriba = primeraFila ? 2 : 0;
            abajo = ultimaFila ? 2 : 0;
            izquierda = column == 0 ? 2 : 0;
            derecha = column == table.getColumnCount() - 1 ? 2 : 0;
            colorBorde = Color.RED;

        } else {
            // Horizontales: linea azul arriba, blanca el resto.
            arriba = primeraFila ? 2 : 0;
            abajo = primeraFila ? 0 : 1;

            colorBorde = primeraFila
                ? new Color(33, 150, 243)
                : Color.WHITE;   // <-- antes era gris claro
        }

        // Bordes horizontales con su color original.
        Border bordeHorizontal = BorderFactory.createMatteBorder(
            arriba, 0, abajo, 0, colorBorde
        );

        // Verticales en negro (solo si hay ancho que pintar).
        Border bordeIzquierda = izquierda > 0
            ? BorderFactory.createMatteBorder(
                0, izquierda, 0, 0, colorVertical
            )
            : BorderFactory.createMatteBorder(
                0, 1, 0, 0, colorVertical
            );

        Border bordeDerecha = derecha > 0
            ? BorderFactory.createMatteBorder(
                0, 0, 0, derecha, colorVertical
            )
            : BorderFactory.createEmptyBorder();

        // Margenes internos que compensan los bordes dibujados.
        Border margenInterno = BorderFactory.createEmptyBorder(
            Math.max(0, 4 - arriba),
            Math.max(0, 6 - izquierda),
            Math.max(0, 4 - abajo),
            Math.max(0, 6 - derecha)
        );

        setBorder(
            BorderFactory.createCompoundBorder(
                bordeHorizontal,
                BorderFactory.createCompoundBorder(
                    bordeIzquierda,
                    BorderFactory.createCompoundBorder(
                        bordeDerecha,
                        margenInterno
                    )
                )
            )
        );

        int anchoCelda = table.getCellRect(
            row, column, false
        ).width;

        setSize(Math.max(1, anchoCelda), Short.MAX_VALUE);

        return this;
    }
}