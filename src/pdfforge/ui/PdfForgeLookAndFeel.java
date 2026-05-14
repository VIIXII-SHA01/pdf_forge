package pdfforge.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 * Application-wide Swing look-and-feel. Flat dark theme gives {@link javax.swing.JFileChooser}
 * a modern layout (sidebar, toolbar, icon grid) similar to Windows 11 Explorer dark mode.
 */
public final class PdfForgeLookAndFeel {

    private static final Logger LOG = Logger.getLogger(PdfForgeLookAndFeel.class.getName());

    private PdfForgeLookAndFeel() {
    }

    /**
     * Installs FlatLaf dark theme on the current thread (must be the EDT). Invoke before
     * constructing any {@code JFrame}, {@code JDialog}, or {@code JFileChooser}.
     */
    public static void install() {
        try {
            // Avoid FlatLaf native window decorations (System.load) on JDK 24+ restricted native access.
            System.setProperty("flatlaf.useWindowDecorations", "false");
            FlatDarkLaf.setup();
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Failed to install FlatLaf (is lib/flatlaf-3.5.4.jar on the classpath?)", ex);
        }
    }
}
