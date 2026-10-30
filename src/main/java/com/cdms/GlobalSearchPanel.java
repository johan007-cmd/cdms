package com.cdms;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

final class GlobalSearchPanel extends JPanel implements PdfExportable {
    private final JTextField query = new JTextField(30);
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Record type", "ID", "Matching record details"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    GlobalSearchPanel() {
        super(new BorderLayout(0, 16));
        setBackground(UiStyle.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        JLabel title = new JLabel("Global Search");
        title.setFont(UiStyle.HEADING);
        title.setForeground(UiStyle.NAVY);
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        UiStyle.textField(query);
        query.setToolTipText("Enter a name, case number, officer, crime category, or other record detail");
        var search = UiStyle.button("Search all records", true);
        search.addActionListener(this::search);
        query.addActionListener(this::search);
        JLabel searchLabel = new JLabel("Search term");
        searchLabel.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.BOLD, 13));
        searchLabel.setForeground(UiStyle.INK);
        searchBar.add(searchLabel);
        searchBar.add(query);
        searchBar.add(search);
        header.add(searchBar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        UiStyle.table(table);
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiStyle.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.add(new JScrollPane(table));
        add(card, BorderLayout.CENTER);
    }

    private void search(ActionEvent event) {
        String text = query.getText().trim();
        model.setRowCount(0);
        if (text.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter a search term.", "Search", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            RecordDAO dao = new RecordDAO();
            for (EntityType type : EntityType.values()) {
                for (Object[] row : dao.list(type, text)) {
                    StringBuilder details = new StringBuilder();
                    for (int i = 1; i < row.length; i++) {
                        if (row[i] != null && !row[i].toString().isBlank()) {
                            if (!details.isEmpty()) details.append(" | ");
                            details.append(type.columns().get(i - 1).label()).append(": ").append(row[i]);
                        }
                    }
                    model.addRow(new Object[]{type.label(), row[0], details.toString()});
                }
            }
            if (model.getRowCount() == 0) JOptionPane.showMessageDialog(this, "No matching records found.",
                    "Search complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Search could not be completed: " + exception.getMessage(),
                    "Search failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override public void exportPdf(Component parent) {
        List<String> lines = new ArrayList<>();
        lines.add("CDMS Global Search Report");
        lines.add("Search term: " + query.getText().trim());
        lines.add(" ");
        for (int i = 0; i < model.getRowCount(); i++)
            lines.add(model.getValueAt(i, 0) + " | ID " + model.getValueAt(i, 1) + " | " + model.getValueAt(i, 2));
        ReportExporter.export(parent, "global-search-report", lines);
    }
}
