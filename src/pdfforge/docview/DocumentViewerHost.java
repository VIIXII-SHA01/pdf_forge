package pdfforge.docview;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.io.File;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Switches between PDF raster view and DOCX HTML view.
 */
public final class DocumentViewerHost extends JPanel {

    public static final String CARD_EMPTY = "empty";
    public static final String CARD_PDF = "pdf";
    public static final String CARD_DOCX = "docx";

    private final CardLayout cards = new CardLayout();
    private final JPanel deck;
    private final PdfDocumentViewPanel pdfPanel = new PdfDocumentViewPanel();
    private final DocxDocumentViewPanel docxPanel = new DocxDocumentViewPanel();

    public DocumentViewerHost() {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(28, 28, 30));
        deck = new JPanel(cards);
        deck.setOpaque(false);

        JPanel emptyCard = new JPanel(new BorderLayout());
        emptyCard.setOpaque(false);
        JLabel hint = new JLabel("Open a PDF or Word document", SwingConstants.CENTER);
        hint.setForeground(new Color(160, 160, 168));
        emptyCard.add(hint, BorderLayout.CENTER);
        deck.add(emptyCard, CARD_EMPTY);
        deck.add(pdfPanel, CARD_PDF);
        deck.add(docxPanel, CARD_DOCX);
        add(deck, BorderLayout.CENTER);
        cards.show(deck, CARD_EMPTY);
    }

    public void showEmpty() {
        pdfPanel.cancelPendingLoad();
        docxPanel.cancelPendingLoad();
        pdfPanel.clear();
        docxPanel.clear();
        cards.show(deck, CARD_EMPTY);
    }

    public void showPdf(File file, Consumer<Throwable> onFailure) {
        cards.show(deck, CARD_PDF);
        pdfPanel.loadPdfAsync(file, onFailure);
    }

    public void showDocx(File file, Consumer<Throwable> onFailure) {
        cards.show(deck, CARD_DOCX);
        docxPanel.loadDocxAsync(file, onFailure);
    }

    public void displayFile(File file, Consumer<Throwable> onFailure) {
        pdfPanel.cancelPendingLoad();
        docxPanel.cancelPendingLoad();
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".pdf")) {
            showPdf(file, onFailure);
        } else if (name.endsWith(".docx")) {
            showDocx(file, onFailure);
        } else {
            showEmpty();
            if (onFailure != null) {
                onFailure.accept(new IllegalArgumentException("Only .pdf and .docx are supported."));
            }
        }
    }
}
