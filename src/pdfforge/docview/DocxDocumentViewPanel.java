package pdfforge.docview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 * Dark, centered reading view for DOCX body text (paragraphs and simple headings).
 */
public final class DocxDocumentViewPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(DocxDocumentViewPanel.class.getName());

    private static final Color CANVAS = new Color(28, 28, 30);
    private static final Color PAGE = new Color(250, 250, 250);
    private static final Color PAGE_BORDER = new Color(200, 200, 200);
    private static final Color TEXT = new Color(32, 32, 32);

    private final JScrollPane scroll = new JScrollPane();
    private final JTextPane textPane = new JTextPane();
    private SwingWorker<DocxPages, Void> activeDocx;

    public DocxDocumentViewPanel() {
        super(new BorderLayout());
        setBackground(CANVAS);
        textPane.setEditable(false);
        textPane.setOpaque(true);
        textPane.setBackground(PAGE);
        textPane.setForeground(TEXT);
        textPane.setCaretColor(TEXT);
        textPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        textPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        HTMLEditorKit kit = new HTMLEditorKit();
        textPane.setEditorKit(kit);
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        textPane.setDocument(doc);
        applyDarkHtmlStyles(doc.getStyleSheet());
        scroll.setViewportView(textPane);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CANVAS);
        add(scroll, BorderLayout.CENTER);
        clear();
    }

    private static void applyDarkHtmlStyles(StyleSheet css) {
        css.addRule("body { margin: 0; padding: 0; font-family: Segoe UI, sans-serif; color: #202020; background: #ffffff; }");
        css.addRule("h1, h2, h3 { color: #111111; margin: 24px 0 12px 0; }");
        css.addRule("h1 { font-size: 24px; font-weight: bold; }");
        css.addRule("h2 { font-size: 20px; font-weight: bold; }");
        css.addRule("h3 { font-size: 18px; font-weight: bold; }");
        css.addRule("p { font-size: 14px; line-height: 1.7; margin: 10px 0; }");
        css.addRule("strong, b { font-weight: bold; }");
    }

    public void clear() {
        cancelPendingLoad();
        textPane.setText("");
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(CANVAS);
        JLabel hint = new JLabel("Open a PDF or Word document", SwingConstants.CENTER);
        hint.setForeground(new Color(160, 160, 168));
        wrap.add(hint, BorderLayout.CENTER);
        scroll.setViewportView(wrap);
        revalidate();
        repaint();
    }

    public void cancelPendingLoad() {
        if (activeDocx != null) {
            activeDocx.cancel(true);
            activeDocx = null;
        }
    }

    public void loadDocxAsync(File file, Consumer<Throwable> onFailure) {
        if (activeDocx != null) {
            activeDocx.cancel(true);
        }
        SwingWorker<DocxPages, Void> worker = new SwingWorker<>() {
            @Override
            protected DocxPages doInBackground() throws Exception {
                return buildPages(file);
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    DocxPages pages = get();
                    try {
                        JPanel host = new JPanel();
                        host.setLayout(new javax.swing.BoxLayout(host, javax.swing.BoxLayout.Y_AXIS));
                        host.setBackground(CANVAS);

                        for (int i = 0; i < pages.pages.size(); i++) {
                            String htmlPage = pages.pages.get(i);
                            JTextPane pagePane = createPageTextPane(htmlPage, pages.pageWidth);
                            pagePane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

                            JPanel pagePanel = new JPanel(new BorderLayout());
                            pagePanel.setOpaque(true);
                            pagePanel.setBackground(PAGE);
                            pagePanel.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(PAGE_BORDER, 1),
                                    BorderFactory.createEmptyBorder(24, 24, 24, 24)));
                            pagePanel.add(pagePane, BorderLayout.CENTER);
                            pagePanel.setMaximumSize(new Dimension(pages.pageWidth, Integer.MAX_VALUE));
                            pagePanel.setAlignmentX(CENTER_ALIGNMENT);

                            host.add(pagePanel);
                            if (i < pages.pages.size() - 1) {
                                host.add(javax.swing.Box.createRigidArea(new Dimension(0, 16)));
                            }
                        }

                        JPanel wrapper = new JPanel(new GridBagLayout());
                        wrapper.setOpaque(false);
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.gridx = 0;
                        gbc.gridy = 0;
                        gbc.anchor = GridBagConstraints.NORTH;
                        gbc.fill = GridBagConstraints.NONE;
                        wrapper.add(host, gbc);
                        scroll.setViewportView(wrapper);
                        scroll.getVerticalScrollBar().setValue(0);
                        revalidate();
                        repaint();
                    } catch (RuntimeException ex) {
                        LOG.log(Level.WARNING, "DOCX HTML view failed", ex);
                        fail(onFailure, ex);
                    }
                } catch (java.util.concurrent.CancellationException e) {
                    // superseded
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail(onFailure, e);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    LOG.log(Level.WARNING, "DOCX load failed", cause);
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
        activeDocx = worker;
        worker.execute();
    }

    private static DocxPages buildPages(File file) throws IOException {
        try (InputStream in = FileAccessUtils.openForRead(file);
             XWPFDocument document = new XWPFDocument(in)) {
            int pageWidth = getPageWidthPx(document);
            int pageHeight = getPageHeightPx(document);
            int charsPerPage = Math.max(3600, (int) ((pageWidth / 7.0) * (pageHeight / 20.0)));

            List<String> pages = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            int currentCount = 0;
            for (IBodyElement el : document.getBodyElements()) {
                if (el instanceof XWPFParagraph) {
                    String paragraphHtml = buildParagraphHtml((XWPFParagraph) el);
                    if (currentCount > 0 && currentCount + paragraphHtml.length() > charsPerPage) {
                        pages.add(wrapHtml(current.toString()));
                        current.setLength(0);
                        currentCount = 0;
                    }
                    current.append(paragraphHtml);
                    currentCount += paragraphHtml.length();
                }
            }
            if (currentCount > 0 || pages.isEmpty()) {
                pages.add(wrapHtml(current.toString()));
            }
            return new DocxPages(pageWidth, pageHeight, pages);
        }
    }

    private static int getPageWidthPx(XWPFDocument document) {
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr sectPr = document.getDocument().getBody().getSectPr();
        if (sectPr != null && sectPr.isSetPgSz()) {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz pgSz = sectPr.getPgSz();
            if (pgSz.isSetW()) {
                long widthTwips = parseTwipsValue(pgSz.getW());
                if (widthTwips > 0) {
                    return Math.max(640, (int) (widthTwips * 96.0 / 1440.0));
                }
            }
        }
        return 850;
    }

    private static int getPageHeightPx(XWPFDocument document) {
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr sectPr = document.getDocument().getBody().getSectPr();
        if (sectPr != null && sectPr.isSetPgSz()) {
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz pgSz = sectPr.getPgSz();
            if (pgSz.isSetH()) {
                long heightTwips = parseTwipsValue(pgSz.getH());
                if (heightTwips > 0) {
                    return Math.max(900, (int) (heightTwips * 96.0 / 1440.0));
                }
            }
        }
        return 1100;
    }

    private static long parseTwipsValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof java.math.BigInteger) {
            return ((java.math.BigInteger) value).longValue();
        }
        if (value == null) {
            return 0L;
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            try {
                return new java.math.BigDecimal(text).longValue();
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
    }

    private static JTextPane createPageTextPane(String html, int pageWidth) {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setOpaque(true);
        pane.setBackground(PAGE);
        pane.setForeground(TEXT);
        pane.setCaretColor(TEXT);
        pane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        HTMLEditorKit kit = new HTMLEditorKit();
        pane.setEditorKit(kit);
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        applyDarkHtmlStyles(doc.getStyleSheet());
        pane.setDocument(doc);
        pane.setText(html);
        pane.setSize(pageWidth - 64, Integer.MAX_VALUE);
        Dimension d = pane.getPreferredSize();
        pane.setPreferredSize(new Dimension(pageWidth - 64, d.height));
        return pane;
    }

    private static String wrapHtml(String body) {
        return "<html><body>" + body + "</body></html>";
    }

    private static String buildParagraphHtml(XWPFParagraph p) {
        String raw = p.getText();
        if (raw == null) {
            return "";
        }
        String text = raw.trim();
        if (text.isEmpty()) {
            return "<p>&nbsp;</p>";
        }
        String esc = escapeHtml(text);
        String styleId = p.getStyle();
        String tag = "p";
        if (styleId != null) {
            String s = styleId.toLowerCase(Locale.ROOT);
            if (s.contains("heading1") || s.contains("title")) {
                tag = "h1";
            } else if (s.contains("heading2")) {
                tag = "h2";
            } else if (s.contains("heading3")) {
                tag = "h3";
            }
        }
        java.util.List<org.apache.poi.xwpf.usermodel.XWPFRun> runs = p.getRuns();
        if (runs != null && runs.stream().anyMatch(r -> r != null && r.isBold()) && "p".equals(tag)) {
            return "<p><b>" + esc + "</b></p>";
        }
        return "<" + tag + ">" + esc + "</" + tag + ">";
    }

    private static final class DocxPages {
        final int pageWidth;
        final int pageHeight;
        final java.util.List<String> pages;

        DocxPages(int pageWidth, int pageHeight, java.util.List<String> pages) {
            this.pageWidth = pageWidth;
            this.pageHeight = pageHeight;
            this.pages = pages;
        }
    }

    private static String escapeHtml(String s) {
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
