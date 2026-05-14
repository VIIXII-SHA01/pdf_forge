/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pdfforge;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import pdfforge.docview.DocumentViewerHost;
import pdfforge.ui.PdfForgeFileChoosers;
import pdfforge.ui.PdfForgeLookAndFeel;
/**
 *
 * @author johnl
 */
public class EditPage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EditPage.class.getName());

    private static final int EXPLORER_MIN_WIDTH = 160;
    private static final int EXPLORER_MAX_WIDTH = 900;
    private static final int EXPLORER_GRIP_PX = 5;
    private static final double EXPLORER_DEFAULT_WIDTH_FRACTION = 0.20;

    private static int defaultExplorerWidthPx() {
        int screenW = Toolkit.getDefaultToolkit().getScreenSize().width;
        int target = (int) Math.round(screenW * EXPLORER_DEFAULT_WIDTH_FRACTION);
        return Math.min(EXPLORER_MAX_WIDTH, Math.max(EXPLORER_MIN_WIDTH, target));
    }

    /** Last width before hide; used when toggling visible again (VS Code style). */
    private int explorerWidthWhenVisible = -1;

    private File currentOpenFile;
    private File currentWorkspaceFolder;

    private final DocumentViewerHost documentViewerHost = new DocumentViewerHost();

    /**
     * Creates new form EditPage
     */
    public EditPage() {
        initComponents();
        installMainWorkspaceLayout();
        configureFileMenu();
        configureEditMenu();
        configureViewMenu();
        configureSettingsMenu();
        installExplorerResizeGrip();
        installExplorerToggleShortcut();
        this.setTitle("PDF Forge");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(56, 56, 56));
        this.setVisible(true);
    }

    private void installExplorerToggleShortcut() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "toggleExplorerPane");
        getRootPane().getActionMap().put("toggleExplorerPane", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleExplorerPane();
            }
        });
    }

    private void toggleExplorerPane() {
        if (explorerPane.isVisible()) {
            int w = explorerPane.getWidth();
            if (w < EXPLORER_MIN_WIDTH) {
                w = explorerPane.getPreferredSize().width;
            }
            explorerWidthWhenVisible = Math.min(EXPLORER_MAX_WIDTH, Math.max(EXPLORER_MIN_WIDTH, w));
            explorerPane.setVisible(false);
        } else {
            int w = explorerWidthWhenVisible > 0 ? explorerWidthWhenVisible : defaultExplorerWidthPx();
            int h = explorerPane.getPreferredSize().height;
            if (h < 120) {
                h = 400;
            }
            explorerPane.setPreferredSize(new Dimension(w, h));
            explorerPane.setMinimumSize(new Dimension(EXPLORER_MIN_WIDTH, 120));
            explorerPane.setMaximumSize(new Dimension(EXPLORER_MAX_WIDTH, Integer.MAX_VALUE));
            explorerPane.setVisible(true);
        }
        explorerPane.revalidate();
        getContentPane().repaint();
    }

    private void installMainWorkspaceLayout() {
        java.awt.Container root = getContentPane();
        root.removeAll();
        root.setLayout(new BorderLayout(0, 0));
        root.setBackground(new Color(56, 56, 56));
        root.add(jPanel1, BorderLayout.NORTH);
        JPanel mainRow = new JPanel(new BorderLayout());
        mainRow.setOpaque(false);
        mainRow.setBackground(new Color(56, 56, 56));
        explorerPane.setPreferredSize(new Dimension(defaultExplorerWidthPx(), 400));
        mainRow.add(explorerPane, BorderLayout.WEST);
        mainRow.add(documentViewerHost, BorderLayout.CENTER);
        root.add(mainRow, BorderLayout.CENTER);
        root.revalidate();
    }

    private void configureFileMenu() {
        for (ActionListener al : file.getActionListeners()) {
            file.removeActionListener(al);
        }
        file.removeAll();

        JMenuItem openFile = new JMenuItem("Open File...");
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openFile.addActionListener(e -> onFileMenuOpenFile());

        JMenuItem openFolder = new JMenuItem("Open Folder...");
        openFolder.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        openFolder.addActionListener(e -> onFileMenuOpenFolder());

        JMenuItem save = new JMenuItem("Save");
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        save.addActionListener(e -> onFileMenuSave());

        JMenuItem saveAs = new JMenuItem("Save As...");
        saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        saveAs.addActionListener(e -> onFileMenuSaveAs());

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> onFileMenuExit());

        file.add(openFile);
        file.add(openFolder);
        file.addSeparator();
        file.add(save);
        file.add(saveAs);
        file.addSeparator();
        file.add(exit);

        file.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent e) {
                styleDropdownMenu(file);
            }

            @Override
            public void menuDeselected(javax.swing.event.MenuEvent e) {
            }

            @Override
            public void menuCanceled(javax.swing.event.MenuEvent e) {
            }
        });
    }

    private void configureEditMenu() {
        for (ActionListener al : edit.getActionListeners()) {
            edit.removeActionListener(al);
        }
        edit.removeAll();

        JMenuItem undo = new JMenuItem("Undo");
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undo.addActionListener(e -> onEditMenuUndo());

        JMenuItem redo = new JMenuItem("Redo");
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redo.addActionListener(e -> onEditMenuRedo());

        JMenuItem cut = new JMenuItem("Cut");
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        cut.addActionListener(e -> onEditMenuCut());

        JMenuItem copy = new JMenuItem("Copy");
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copy.addActionListener(e -> onEditMenuCopy());

        JMenuItem paste = new JMenuItem("Paste");
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        paste.addActionListener(e -> onEditMenuPaste());

        JMenuItem find = new JMenuItem("Find");
        find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        find.addActionListener(e -> onEditMenuFind());

        edit.add(undo);
        edit.add(redo);
        edit.addSeparator();
        edit.add(cut);
        edit.add(copy);
        edit.add(paste);
        edit.addSeparator();
        edit.add(find);

        edit.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent e) {
                styleDropdownMenu(edit);
            }

            @Override
            public void menuDeselected(javax.swing.event.MenuEvent e) {
            }

            @Override
            public void menuCanceled(javax.swing.event.MenuEvent e) {
            }
        });
    }

    private void configureViewMenu() {
        for (ActionListener al : view.getActionListeners()) {
            view.removeActionListener(al);
        }
        view.removeAll();

        JMenuItem zoomIn = new JMenuItem("Zoom in");
        zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
        zoomIn.addActionListener(e -> onViewMenuZoomIn());

        JMenuItem zoomOut = new JMenuItem("Zoom out");
        zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
        zoomOut.addActionListener(e -> onViewMenuZoomOut());

        JMenuItem themes = new JMenuItem("Themes");
        themes.addActionListener(e -> onViewMenuThemes());

        JRadioButtonMenuItem documentView = new JRadioButtonMenuItem("Document view", true);
        documentView.addActionListener(e -> onViewMenuDocumentView());
        JRadioButtonMenuItem pagesView = new JRadioButtonMenuItem("Pages view");
        pagesView.addActionListener(e -> onViewMenuPagesView());
        ButtonGroup viewModeGroup = new ButtonGroup();
        viewModeGroup.add(documentView);
        viewModeGroup.add(pagesView);

        view.add(zoomIn);
        view.add(zoomOut);
        view.addSeparator();
        view.add(themes);
        view.addSeparator();
        view.add(documentView);
        view.add(pagesView);

        view.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent e) {
                styleDropdownMenu(view);
            }

            @Override
            public void menuDeselected(javax.swing.event.MenuEvent e) {
            }

            @Override
            public void menuCanceled(javax.swing.event.MenuEvent e) {
            }
        });
    }

    private void configureSettingsMenu() {
        for (ActionListener al : settings.getActionListeners()) {
            settings.removeActionListener(al);
        }
        settings.removeAll();

        JMenuItem exportDocx = new JMenuItem("Export as DOCX");
        exportDocx.addActionListener(e -> onSettingsMenuExportDocx());

        JMenuItem exportPdf = new JMenuItem("Export as PDF");
        exportPdf.addActionListener(e -> onSettingsMenuExportPdf());

        JMenuItem help = new JMenuItem("Help");
        help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        help.addActionListener(e -> onSettingsMenuHelp());

        settings.add(exportDocx);
        settings.add(exportPdf);
        settings.addSeparator();
        settings.add(help);

        settings.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent e) {
                styleDropdownMenu(settings);
            }

            @Override
            public void menuDeselected(javax.swing.event.MenuEvent e) {
            }

            @Override
            public void menuCanceled(javax.swing.event.MenuEvent e) {
            }
        });
    }

    private void onViewMenuZoomIn() {
        logger.fine("View > Zoom in");
    }

    private void onViewMenuZoomOut() {
        logger.fine("View > Zoom out");
    }

    private void onViewMenuThemes() {
        logger.fine("View > Themes");
    }

    private void onViewMenuDocumentView() {
        logger.fine("View > Document view");
    }

    private void onViewMenuPagesView() {
        logger.fine("View > Pages view");
    }

    private void onSettingsMenuExportDocx() {
        logger.fine("Settings > Export as DOCX");
    }

    private void onSettingsMenuExportPdf() {
        logger.fine("Settings > Export as PDF");
    }

    private void onSettingsMenuHelp() {
        logger.fine("Settings > Help");
    }

    private void onEditMenuUndo() {
        logger.fine("Edit > Undo");
    }

    private void onEditMenuRedo() {
        logger.fine("Edit > Redo");
    }

    private void onEditMenuCut() {
        logger.fine("Edit > Cut");
    }

    private void onEditMenuCopy() {
        logger.fine("Edit > Copy");
    }

    private void onEditMenuPaste() {
        logger.fine("Edit > Paste");
    }

    private void onEditMenuFind() {
        logger.fine("Edit > Find");
    }

    private static final Color DROPDOWN_MENU_BG = new Color(43, 43, 46);
    private static final Color DROPDOWN_MENU_FG = new Color(245, 245, 245);
    private static final Color DROPDOWN_MENU_BORDER = new Color(58, 58, 62);
    private static final Color DROPDOWN_MENU_ROW_HOVER = new Color(55, 62, 72);
    private static final Color DROPDOWN_MENU_SEP_LINE = new Color(72, 72, 78);
    private static final int DROPDOWN_MENU_MIN_WIDTH = 300;
    private static final int DROPDOWN_MENU_ROW_HEIGHT = 36;
    private static final Font DROPDOWN_MENU_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    /** Styled dark dropdown for top-level menus (File, Edit, …); does not change global LaF. */
    private void styleDropdownMenu(javax.swing.JMenu menu) {
        javax.swing.JPopupMenu popup = menu.getPopupMenu();
        popup.setBackground(DROPDOWN_MENU_BG);
        popup.setOpaque(true);
        popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DROPDOWN_MENU_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        Dimension rowSize = new Dimension(DROPDOWN_MENU_MIN_WIDTH, DROPDOWN_MENU_ROW_HEIGHT);

        for (Component c : popup.getComponents()) {
            if (c instanceof JMenuItem) {
                JMenuItem mi = (JMenuItem) c;
                mi.setFont(DROPDOWN_MENU_FONT);
                mi.setOpaque(true);
                mi.setBackground(DROPDOWN_MENU_BG);
                mi.setForeground(DROPDOWN_MENU_FG);
                mi.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 16));
                mi.setPreferredSize(rowSize);
                mi.setMinimumSize(rowSize);
                mi.setMaximumSize(new Dimension(Integer.MAX_VALUE, DROPDOWN_MENU_ROW_HEIGHT));

                if (!Boolean.TRUE.equals(mi.getClientProperty("pdfForge.menuRowHover"))) {
                    mi.putClientProperty("pdfForge.menuRowHover", Boolean.TRUE);
                    mi.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                            mi.setBackground(DROPDOWN_MENU_ROW_HOVER);
                        }

                        @Override
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            mi.setBackground(DROPDOWN_MENU_BG);
                        }
                    });
                }
            } else if (c instanceof javax.swing.JSeparator) {
                javax.swing.JSeparator s = (javax.swing.JSeparator) c;
                s.setOpaque(false);
                s.setBackground(DROPDOWN_MENU_BG);
                s.setForeground(DROPDOWN_MENU_SEP_LINE);
                s.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(6, 8, 6, 8),
                        BorderFactory.createMatteBorder(1, 0, 0, 0, DROPDOWN_MENU_SEP_LINE)));
                s.setPreferredSize(new Dimension(DROPDOWN_MENU_MIN_WIDTH, 14));
                s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
            }
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            Dimension ps = popup.getPreferredSize();
            int w = Math.max(DROPDOWN_MENU_MIN_WIDTH, ps.width);
            popup.setPreferredSize(new Dimension(w, ps.height));
            popup.revalidate();
        });
    }

    private void onFileMenuOpenFile() {
        JFileChooser chooser = PdfForgeFileChoosers.create();
        chooser.setDialogTitle("Open File");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter pdfOrDocx = new FileNameExtensionFilter(
                "PDF or Word (*.pdf, *.docx)", "pdf", "docx");
        chooser.setFileFilter(pdfOrDocx);
        if (currentWorkspaceFolder != null) {
            chooser.setCurrentDirectory(currentWorkspaceFolder);
        } else if (currentOpenFile != null && currentOpenFile.getParentFile() != null) {
            chooser.setCurrentDirectory(currentOpenFile.getParentFile());
        }
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File chosen = chooser.getSelectedFile();
            if (chosen != null && isPdfOrDocxFile(chosen)) {
                currentOpenFile = chosen;
                setTitle("PDF Forge - " + chosen.getName());
                logger.info("Open file: " + currentOpenFile.getAbsolutePath());
                documentViewerHost.displayFile(chosen, err -> {
                    setTitle("PDF Forge");
                    currentOpenFile = null;
                    logger.log(java.util.logging.Level.WARNING, "Document view failed", err);
                    String detail = err.getMessage();
                    if (detail == null || detail.isBlank()) {
                        detail = err.getClass().getSimpleName();
                    }
                    JOptionPane.showMessageDialog(this,
                            "Could not load this document.\n\n" + detail,
                            "Open File",
                            JOptionPane.ERROR_MESSAGE);
                });
            } else {
                JOptionPane.showMessageDialog(this,
                        "Only PDF (.pdf) and Word (.docx) files can be opened.",
                        "Open File",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static boolean isPdfOrDocxFile(File f) {
        String name = f.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".pdf") || name.endsWith(".docx");
    }

    private void onFileMenuOpenFolder() {
        JFileChooser chooser = PdfForgeFileChoosers.create();
        chooser.setDialogTitle("Open Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (currentWorkspaceFolder != null) {
            chooser.setCurrentDirectory(currentWorkspaceFolder);
        } else if (currentOpenFile != null && currentOpenFile.getParentFile() != null) {
            chooser.setCurrentDirectory(currentOpenFile.getParentFile());
        }
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentWorkspaceFolder = chooser.getSelectedFile();
            logger.info("Open folder: " + currentWorkspaceFolder.getAbsolutePath());
        }
    }

    private void onFileMenuSave() {
        if (currentOpenFile == null) {
            JOptionPane.showMessageDialog(this,
                    "No file is open. Use Save As… to choose a location.",
                    "Save",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Save is not wired to a document yet.\nActive path:\n" + currentOpenFile.getAbsolutePath(),
                "Save",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onFileMenuSaveAs() {
        JFileChooser chooser = PdfForgeFileChoosers.create();
        chooser.setDialogTitle("Save As");
        chooser.setSelectedFile(currentOpenFile != null ? currentOpenFile : new File("document.pdf"));
        FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter("PDF documents (*.pdf)", "pdf");
        chooser.addChoosableFileFilter(pdfFilter);
        chooser.setFileFilter(pdfFilter);
        if (currentWorkspaceFolder != null) {
            chooser.setCurrentDirectory(currentWorkspaceFolder);
        } else if (currentOpenFile != null && currentOpenFile.getParentFile() != null) {
            chooser.setCurrentDirectory(currentOpenFile.getParentFile());
        }
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentOpenFile = chooser.getSelectedFile();
            logger.info("Save as: " + currentOpenFile.getAbsolutePath());
        }
    }

    private void onFileMenuExit() {
        dispose();
    }

    private void installExplorerResizeGrip() {
        explorerPane.removeAll();
        explorerPane.setLayout(new BorderLayout(0, 0));
        explorerPane.setCursor(Cursor.getDefaultCursor());

        javax.swing.JPanel explorerBody = new javax.swing.JPanel(new BorderLayout());
        explorerBody.setOpaque(false);
        explorerBody.add(jLabel8, BorderLayout.NORTH);

        javax.swing.JPanel explorerResizeGrip = new javax.swing.JPanel();
        explorerResizeGrip.setOpaque(true);
        explorerResizeGrip.setBackground(new Color(64, 64, 64));
        explorerResizeGrip.setPreferredSize(new Dimension(EXPLORER_GRIP_PX, 0));
        explorerResizeGrip.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));

        MouseAdapter resize = new MouseAdapter() {
            private int dragStartScreenX;
            private int widthAtDragStart;

            @Override
            public void mousePressed(MouseEvent e) {
                dragStartScreenX = e.getXOnScreen();
                widthAtDragStart = explorerPane.getWidth();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int delta = e.getXOnScreen() - dragStartScreenX;
                int next = Math.min(EXPLORER_MAX_WIDTH, Math.max(EXPLORER_MIN_WIDTH, widthAtDragStart + delta));
                int h = explorerPane.getHeight() > 0 ? explorerPane.getHeight() : explorerPane.getPreferredSize().height;
                explorerPane.setPreferredSize(new Dimension(next, h));
                explorerPane.revalidate();
            }
        };
        explorerResizeGrip.addMouseListener(resize);
        explorerResizeGrip.addMouseMotionListener(resize);

        explorerPane.add(explorerBody, BorderLayout.CENTER);
        explorerPane.add(explorerResizeGrip, BorderLayout.EAST);

        Dimension pref = explorerPane.getPreferredSize();
        int w = defaultExplorerWidthPx();
        int h = pref.height > 0 ? pref.height : 400;
        explorerPane.setPreferredSize(new Dimension(w, h));
        explorerPane.setMinimumSize(new Dimension(EXPLORER_MIN_WIDTH, 120));
        explorerPane.setMaximumSize(new Dimension(EXPLORER_MAX_WIDTH, Integer.MAX_VALUE));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        explorerPane = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        file = new javax.swing.JMenu();
        edit = new javax.swing.JMenu();
        view = new javax.swing.JMenu();
        settings = new javax.swing.JMenu();

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jMenuItem1.setText("jMenuItem1");

        jMenuItem2.setText("jMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(100, 100, 100));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pdfforge/arrows-split-up-and-left-white.png"))); // NOI18N
        jButton1.setBorder(null);
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pdfforge/refresh-white.png"))); // NOI18N
        jButton3.setBorder(null);
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setFocusPainted(false);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pdfforge/file-zipper-white.png"))); // NOI18N
        jButton4.setBorder(null);
        jButton4.setBorderPainted(false);
        jButton4.setContentAreaFilled(false);
        jButton4.setFocusPainted(false);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pdfforge/water-well-white.png"))); // NOI18N
        jButton5.setBorder(null);
        jButton5.setBorderPainted(false);
        jButton5.setContentAreaFilled(false);
        jButton5.setFocusPainted(false);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pdfforge/merge-white.png"))); // NOI18N
        jButton2.setBorder(null);
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setFocusPainted(false);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pdfforge/swap-white.png"))); // NOI18N
        jButton7.setBorder(null);
        jButton7.setBorderPainted(false);
        jButton7.setContentAreaFilled(false);
        jButton7.setFocusPainted(false);

        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pdfforge/compress-white.png"))); // NOI18N
        jButton9.setBorder(null);
        jButton9.setBorderPainted(false);
        jButton9.setContentAreaFilled(false);
        jButton9.setFocusPainted(false);

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Merge");

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Split");

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Extract Text");

        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Rotate");

        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Extract Range");

        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Compress");

        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Reorder");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2)
                    .addComponent(jLabel1))
                .addGap(39, 39, 39)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(jLabel4))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(jButton3)))
                .addGap(54, 54, 54)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(53, 53, 53)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(42, 42, 42)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(220, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGap(0, 0, 0))
        );

        explorerPane.setBackground(new java.awt.Color(80, 80, 80));
        explorerPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(80, 80, 80)));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("EXPLORER");

        javax.swing.GroupLayout explorerPaneLayout = new javax.swing.GroupLayout(explorerPane);
        explorerPane.setLayout(explorerPaneLayout);
        explorerPaneLayout.setHorizontalGroup(
            explorerPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(explorerPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addContainerGap(25, Short.MAX_VALUE))
        );
        explorerPaneLayout.setVerticalGroup(
            explorerPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(explorerPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addContainerGap(375, Short.MAX_VALUE))
        );

        jMenuBar1.setBackground(new java.awt.Color(100, 100, 100));
        jMenuBar1.setBorder(null);

        file.setForeground(new java.awt.Color(255, 255, 255));
        file.setText("File");
        file.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jMenuBar1.add(file);

        edit.setForeground(new java.awt.Color(255, 255, 255));
        edit.setText("Edit");
        edit.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jMenuBar1.add(edit);

        view.setForeground(new java.awt.Color(255, 255, 255));
        view.setText("View");
        view.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jMenuBar1.add(view);

        settings.setForeground(new java.awt.Color(255, 255, 255));
        settings.setText("Settings");
        settings.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jMenuBar1.add(settings);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(explorerPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(explorerPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            PdfForgeLookAndFeel.install();
            new EditPage().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu edit;
    private javax.swing.JPanel explorerPane;
    private javax.swing.JMenu file;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JMenu settings;
    private javax.swing.JMenu view;
    // End of variables declaration//GEN-END:variables
}
