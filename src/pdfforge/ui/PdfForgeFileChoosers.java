package pdfforge.ui;

import java.awt.Dimension;
import javax.swing.JFileChooser;

/**
 * Single factory for all file dialogs so sizing and behavior stay consistent app-wide.
 */
public final class PdfForgeFileChoosers {

    private static final Dimension PREFERRED = new Dimension(980, 660);
    private static final Dimension MINIMUM = new Dimension(720, 480);

    private PdfForgeFileChoosers() {
    }

    public static JFileChooser create() {
        JFileChooser fc = new JFileChooser();
        applyStandardLayout(fc);
        return fc;
    }

    /**
     * Applies the same layout hints used by {@link #create()} to an existing chooser.
     */
    public static void applyStandardLayout(JFileChooser fc) {
        if (fc == null) {
            return;
        }
        fc.setPreferredSize(PREFERRED);
        fc.setMinimumSize(MINIMUM);
        fc.setFileHidingEnabled(false);
    }
}
