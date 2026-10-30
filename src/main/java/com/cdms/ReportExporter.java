package com.cdms;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class ReportExporter {
    private ReportExporter() {}

    static void export(Component parent, String title, List<String> lines) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export " + title + " as PDF");
        chooser.setSelectedFile(new File(title.toLowerCase().replaceAll("[^a-z0-9]+", "-") + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF documents (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) target = new File(target.getParentFile(), target.getName() + ".pdf");
        if (target.exists() && JOptionPane.showConfirmDialog(parent, "Replace the existing file?",
                "Confirm overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try (PDDocument document = new PDDocument()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            List<String> wrapped = new ArrayList<>();
            for (String line : lines) {
                String safeLine = line.replaceAll("[^\\x20-\\x7E]", "?");
                while (safeLine.length() > 105) {
                    wrapped.add(safeLine.substring(0, 105));
                    safeLine = "  " + safeLine.substring(105);
                }
                wrapped.add(safeLine);
            }
            int index = 0;
            while (index < wrapped.size() || document.getNumberOfPages() == 0) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(font, 9);
                    content.newLineAtOffset(42, 748);
                    int count = 0;
                    while (index < wrapped.size() && count < 48) {
                        content.showText(wrapped.get(index++));
                        content.newLineAtOffset(0, -14);
                        count++;
                    }
                    content.endText();
                }
            }
            document.save(target);
            JOptionPane.showMessageDialog(parent, "PDF report saved to:\n" + target.getAbsolutePath(),
                    "Export complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(parent, "PDF report could not be saved: " + exception.getMessage(),
                    "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
