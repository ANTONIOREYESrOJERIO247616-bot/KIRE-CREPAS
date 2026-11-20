package DETALLES.tablaVcat;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

public class WrapRenderer extends JTextArea
        implements TableCellRenderer {

    public WrapRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setEditable(false);
        setOpaque(true);

        setBorder(
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        );
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

        // Usar exactamente la fuente de la tabla.
        setFont(table.getFont());
        setComponentOrientation(table.getComponentOrientation());

        String texto = value == null ? "" : value.toString();

        // Convertir los saltos HTML del producto y los extras.
        texto = texto.replaceAll("(?i)<br\\s*/?>", "\n");
        texto = texto.replaceAll("(?i)</?html\\s*>", "");

        // Recuperar caracteres escapados por SelectorExtras.
        texto = texto.replace("&nbsp;", " ")
                     .replace("&lt;", "<")
                     .replace("&gt;", ">")
                     .replace("&quot;", "\"")
                     .replace("&#39;", "'")
                     .replace("&amp;", "&");

        setText(texto);

        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        int anchoCelda = table.getCellRect(
            row, column, false
        ).width;

        // JTextArea descuenta sus margenes internos al envolver.
        setSize(Math.max(1, anchoCelda), Short.MAX_VALUE);

        return this;
    }
}