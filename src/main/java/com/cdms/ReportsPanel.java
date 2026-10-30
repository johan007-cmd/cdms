package com.cdms;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

final class ReportsPanel extends JPanel implements PdfExportable {
    private final JComboBox<EntityType> recordType = new JComboBox<>(EntityType.values());
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel summary = new JLabel("Choose a record type and select Preview report.");
    private EntityType previewedType;
    private List<Object[]> previewedRows = List.of();

    ReportsPanel() {
        super(new BorderLayout(0, 16));
        setBackground(UiStyle.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JLabel title = new JLabel("Reports");
        title.setFont(UiStyle.HEADING);
        title.setForeground(UiStyle.NAVY);
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);
        UiStyle.comboBox(recordType);
        recordType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                                                          int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value instanceof EntityType type) setText(type.label());
                setFont(UiStyle.BODY);
                setForeground(selected ? UiStyle.WHITE : UiStyle.INK);
                setBackground(selected ? UiStyle.NAVY : UiStyle.WHITE);
                setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                return this;
            }
        });
        var preview = UiStyle.button("Preview report", true);
        preview.addActionListener(event -> loadPreview());
        controls.add(new JLabel("Report data"));
        controls.add(recordType);
        controls.add(preview);
        header.add(controls, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(UiStyle.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 14, 12, 14)));
        summary.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.BOLD, 14));
        summary.setForeground(UiStyle.INK);
        card.add(summary, BorderLayout.NORTH);
        UiStyle.table(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
        loadPreview();
    }

    private void loadPreview() {
        EntityType type = (EntityType) recordType.getSelectedItem();
        if (type == null) return;
        try {
            List<Object[]> rows = new RecordDAO().list(type, "");
            previewedType = type;
            previewedRows = rows;
            model.setColumnIdentifiers(headers(type));
            model.setRowCount(0);
            for (Object[] row : rows) model.addRow(row);
            UiStyle.table(table);
            summary.setText(type.label() + "  |  " + rows.size() + " record(s) included in this report");
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Report preview could not be loaded: " + exception.getMessage(),
                    "Report unavailable", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object[] headers(EntityType type) {
        Object[] headers = new Object[type.columns().size() + 1];
        headers[0] = "ID";
        for (int i = 0; i < type.columns().size(); i++) headers[i + 1] = type.columns().get(i).label();
        return headers;
    }

    @Override
    public void exportPdf(Component parent) {
        if (previewedType == null) {
            JOptionPane.showMessageDialog(parent, "Preview a report before exporting it.",
                    "Report not ready", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<String> lines = new ArrayList<>();
        lines.add("CDMS " + previewedType.label() + " Report");
        lines.add("Records in report: " + previewedRows.size());
        lines.add(" ");
        for (Object[] row : previewedRows) {
            StringBuilder line = new StringBuilder("ID ").append(row[0]);
            for (int i = 1; i < row.length; i++) {
                if (row[i] != null) line.append(" | ")
                        .append(previewedType.columns().get(i - 1).label()).append(": ").append(row[i]);
            }
            lines.add(line.toString());
        }
        ReportExporter.export(parent, previewedType.name().toLowerCase() + "-report", lines);
    }
}
