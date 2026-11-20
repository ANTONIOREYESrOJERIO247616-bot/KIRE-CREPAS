package DETALLES.tablaVcat;

import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JTable;

public class AltoFilaUtil {

    public static int calcularAlto(JTable table, int fila, int columna, int altoMinimo) {
        Object valor = table.getValueAt(fila, columna);
        if (valor == null) return altoMinimo;

        String texto = valor.toString();
        if (texto.isEmpty()) return altoMinimo;

        // Fuente y métricas
        Font fuente = table.getFont();
        FontMetrics fm = table.getFontMetrics(fuente);

        // Ancho disponible
        int anchoReal = table.getColumnModel().getColumn(columna).getWidth();
        if (anchoReal <= 0) {
            anchoReal = table.getColumnModel().getColumn(columna).getPreferredWidth();
        }
        int anchoColumna = anchoReal - 12;
        if (anchoColumna < 20) anchoColumna = 20;

        // Contar líneas respetando <br>
        int lineas = 1;
        if (texto.contains("<br>")) {
            String[] partes = texto.split("<br>");
            lineas = partes.length;
        } else {
            int anchoTexto = fm.stringWidth(texto);
            lineas = (int) Math.ceil((double) anchoTexto / anchoColumna);
        }
        if (lineas < 1) lineas = 1;

        // Alto necesario
        int altoLinea = fm.getHeight();
        int padding = 4;
        int alto = (lineas * altoLinea) + padding;

        return Math.max(alto, altoMinimo);
    }
}