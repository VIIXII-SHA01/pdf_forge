package pdfforge.ui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Single factory for all file dialogs so sizing and behavior stay consistent app-wide.
 */
public final class PdfForgeFileChoosers {

    private static final Dimension PREFERRED = new Dimension(980, 660);
    private static final Dimension MINIMUM = new Dimension(720, 480);

    private static final Map<Path, Boolean> LOCAL_FILE_CACHE = new ConcurrentHashMap<>();

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

    public static FileFilter createLocalPdfDocxFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file == null) {
                    return false;
                }
                if (file.isDirectory()) {
                    return true;
                }
                String name = file.getName().toLowerCase(Locale.ROOT);
                return (name.endsWith(".pdf") || name.endsWith(".docx")) && isLocalFile(file);
            }

            @Override
            public String getDescription() {
                return "Local PDF and Word documents (*.pdf, *.docx)";
            }
        };
    }

    public static boolean isLocalFile(File file) {
        if (file == null || !file.isFile() || !file.canRead()) {
            return false;
        }
        Path path = file.toPath().toAbsolutePath();
        return LOCAL_FILE_CACHE.computeIfAbsent(path, PdfForgeFileChoosers::checkLocalFile);
    }

    private static boolean checkLocalFile(Path path) {
        try {
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                return false;
            }
            if (!Files.isReadable(path)) {
                return false;
            }
            try (var in = Files.newInputStream(path)) {
                int first = in.read();
                return first != -1;
            }
        } catch (UnsupportedOperationException | SecurityException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }
}
