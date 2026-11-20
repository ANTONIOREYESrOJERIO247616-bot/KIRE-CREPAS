
package VISTA.AGREGARPRODUCTOS;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class FiltroPrecio extends DocumentFilter {

    public static void aplicar(JTextField campo) {
        AbstractDocument documento =
                (AbstractDocument) campo.getDocument();

        documento.setDocumentFilter(new FiltroPrecio());
    }

    @Override
    public void insertString(
            FilterBypass fb,
            int offset,
            String texto,
            AttributeSet atributos) throws BadLocationException {

        replace(fb, offset, 0, texto, atributos);
    }

    @Override
    public void replace(
            FilterBypass fb,
            int offset,
            int longitud,
            String texto,
            AttributeSet atributos) throws BadLocationException {

        String actual = fb.getDocument().getText(
            0, fb.getDocument().getLength()
        );

        String nuevo = new StringBuilder(actual)
                .replace(
                    offset,
                    offset + longitud,
                    texto == null ? "" : texto
                )
                .toString();

        // Permitir vacio para borrar y hasta dos decimales.
        if (nuevo.matches("[0-9]*(\\.[0-9]{0,2})?")) {
            fb.replace(offset, longitud, texto, atributos);
        }
    }

    @Override
    public void remove(
            FilterBypass fb,
            int offset,
            int longitud) throws BadLocationException {

        replace(fb, offset, longitud, "", null);
    }
}