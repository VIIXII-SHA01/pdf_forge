package pdfforge.docview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private static final Color PAGE = new Color(58, 58, 62);
    private static final Color TEXT = new Color(236, 236, 240);

    private final JScrollPane scroll = new JScrollPane();
    private final JTextPane textPane = new JTextPane();
    private SwingWorker<String, Void> activeDocx;

    public DocxDocumentViewPanel() {
        super(new BorderLayout());
        setBackground(CANVAS);
        textPane.setEditable(false);
        textPane.setOpaque(true);
        textPane.setBackground(PAGE);
        textPane.setForeground(TEXT);
        textPane.setCaretColor(TEXT);
        textPane.setBorder(BorderFactory.createEmptyBorder(28, 36, 40, 36));
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
        css.addRule("body { margin: 0; font-family: Segoe UI, sans-serif; color: #ececec; background: #3a3a3e; }");
        css.addRule("h1, h2, h3 { color: #ffffff; margin: 18px 0 8px 0; }");
        css.addRule("h1 { font-size: 22px; font-weight: bold; }");
        css.addRule("h2 { font-size: 18px; font-weight: bold; }");
        css.addRule("h3 { font-size: 16px; font-weight: bold; }");
        css.addRule("p { font-size: 14px; line-height: 1.5; margin: 6px 0; }");
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
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return buildHtml(file);
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    String html = get();
                    try {
                        textPane.setText(html);
                        textPane.setCaretPosition(0);
                        textPane.setSize(new Dimension(680, 50_000));
                        int bodyHeight = textPane.getPreferredSize().height;

                        JPanel page = new JPanel(new BorderLayout());
                        page.setOpaque(true);
                        page.setBackground(PAGE);
                        page.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(52, 52, 58), 1),
                                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
                        page.add(textPane, BorderLayout.CENTER);
                        page.setPreferredSize(new Dimension(720, bodyHeight + 64));

                        JPanel host = new JPanel(new GridBagLayout());
                        host.setBackground(CANVAS);
                        GridBagConstraints c = new GridBagConstraints();
                        c.gridx = 0;
                        c.gridy = 0;
                        c.weightx = 1;
                        c.weighty = 1;
                        c.anchor = GridBagConstraints.NORTH;
                        c.insets = new Insets(28, 40, 40, 40);
                        host.add(page, c);

                        scroll.setViewportView(host);
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

    private static String buildHtml(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(in)) {
            StringBuilder sb = new StringBuilder(4096);
            sb.append("<html><body>");
            for (IBodyElement el : document.getBodyElements()) {
                if (el instanceof XWPFParagraph) {
                    appendParagraphHtml(sb, (XWPFParagraph) el);
                }
            }
            sb.append("</body></html>");
            return sb.toString();
        }
    }

    private static void appendParagraphHtml(StringBuilder sb, XWPFParagraph p) {
        String raw = p.getText();
        if (raw == null) {
            return;
        }
        String text = raw.trim();
        if (text.isEmpty()) {
            sb.append("<p>&nbsp;</p>");
            return;
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
        var runs = p.getRuns();
        if (runs != null && runs.stream().anyMatch(r -> r != null && r.isBold()) && "p".equals(tag)) {
            sb.append("<p><b>").append(esc).append("</b></p>");
        } else {
            sb.append("<").append(tag).append(">").append(esc).append("</").append(tag).append(">");
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
