/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package pdfforge;

import javax.swing.SwingUtilities;
import pdfforge.ui.PdfForgeLookAndFeel;

/**
 *
 * @author johnl
 */
public class PdfForge {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PdfForgeLookAndFeel.install();
            new StartPage();
            new EditPage();
        });
    }
}
