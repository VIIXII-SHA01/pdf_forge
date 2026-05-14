package pdfforge.docview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Scrollable, centered column of rendered PDF pages (dark sheet on darker canvas).
 */
public final class PdfDocumentViewPanel extends JPanel {

    private static final Color CANVAS = new Color(28, 28, 30);
    private static final Color PAGE_BORDER = new Color(56, 56, 60);
    private static final Color PAGE_BACK = new Color(52, 52, 56);
    private static final int PAGE_GAP = 20;
    private static final float RENDER_DPI = 108f;

    private final JScrollPane scroll = new JScrollPane();
    private SwingWorker<List<BufferedImage>, Void> activeRender;

    public PdfDocumentViewPanel() {
        super(new BorderLayout());
        setBackground(CANVAS);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CANVAS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll, BorderLayout.CENTER);
        clear();
    }

    public void clear() {
        cancelPendingLoad();
        JPanel empty = new JPanel(new BorderLayout());
        empty.setBackground(CANVAS);
        JLabel hint = new JLabel("Open a PDF or Word document", SwingConstants.CENTER);
        hint.setForeground(new Color(160, 160, 168));
        empty.add(hint, BorderLayout.CENTER);
        scroll.setViewportView(empty);
        revalidate();
        repaint();
    }

    public void cancelPendingLoad() {
        if (activeRender != null) {
            activeRender.cancel(true);
            activeRender = null;
        }
    }

    /**
     * Renders the PDF off the EDT, then shows all pages centered in a scroll area.
     */
    public void loadPdfAsync(File file, Consumer<Throwable> onFailure) {
        if (activeRender != null) {
            activeRender.cancel(true);
        }
        SwingWorker<List<BufferedImage>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BufferedImage> doInBackground() throws Exception {
                try (PDDocument doc = PDDocument.load(file)) {
                    PDFRenderer renderer = new PDFRenderer(doc);
                    int n = doc.getNumberOfPages();
                    List<BufferedImage> images = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) {
                        if (isCancelled()) {
                            return List.of();
                        }
                        images.add(renderer.renderImageWithDPI(i, RENDER_DPI, ImageType.RGB));
                    }
                    return images;
                }
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    showPageImages(get());
                } catch (CancellationException e) {
                    // superseded by a newer open action
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail(onFailure, e);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    fail(onFailure, cause);
                }
            }

            private void fail(Consumer<Throwable> callback, Throwable t) {
                clear();
                if (callback != null && t != null) {
                    callback.accept(t);
                }
            }
        };
        activeRender = worker;
        worker.execute();
    }

    private void showPageImages(List<BufferedImage> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        for (int i = 0; i < images.size(); i++) {
            JLabel pageLabel = new JLabel(new ImageIcon(images.get(i)));
            pageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            pageLabel.setOpaque(true);
            pageLabel.setBackground(PAGE_BACK);
            pageLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PAGE_BORDER, 1),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)));
            column.add(pageLabel);
            if (i < images.size() - 1) {
                column.add(Box.createRigidArea(new Dimension(0, PAGE_GAP)));
            }
        }
        JPanel host = new JPanel(new GridBagLayout());
        host.setBackground(CANVAS);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(28, 40, 40, 40);
        host.add(column, c);
        scroll.setViewportView(host);
        scroll.getVerticalScrollBar().setValue(0);
        revalidate();
        repaint();
    }
}
