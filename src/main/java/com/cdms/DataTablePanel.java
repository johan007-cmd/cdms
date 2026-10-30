package com.cdms;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

final class DataTablePanel extends JPanel implements PdfExportable {
    private final EntityType type;
    private final UserSession session;
    private final Runnable changed;
    private final RecordDAO records = new RecordDAO();
    private final JTextField search = new JTextField(22);
    private final JTextField categoryFilter = new JTextField(16);
    private final JTextField fromDate = new JTextField(11);
    private final JTextField toDate = new JTextField(11);
    private final JComboBox<String> statusFilter = new JComboBox<>(
            new String[]{"All statuses", "OPEN", "UNDER_INVESTIGATION", "SOLVED", "CLOSED"});
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel count = new JLabel(" ");

    DataTablePanel(EntityType type, UserSession session, Runnable changed) {
        super(new BorderLayout(0, 16));
        this.type = type;
        this.session = session;
        this.changed = changed;
        setName(type.name());
        setBackground(UiStyle.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        UiStyle.comboBox(statusFilter);
        Object[] headers = new Object[type.columns().size() + 1];
        headers[0] = "ID";
        for (int i = 0; i < type.columns().size(); i++) headers[i + 1] = type.columns().get(i).label();
        model = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(33);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new java.awt.Color(232, 237, 243));
        table.setSelectionBackground(new java.awt.Color(219, 234, 250));
        table.setSelectionForeground(UiStyle.NAVY);
        table.getTableHeader().setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        UiStyle.table(table);
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel title = new JLabel(type.label());
        title.setFont(UiStyle.HEADING);
        title.setForeground(UiStyle.NAVY);
        JPanel toolbar = new JPanel(new BorderLayout(0, 10));
        toolbar.setOpaque(false);
        titleBar.add(title, BorderLayout.WEST);
        UiStyle.textField(search);
        search.setToolTipText("Type to search every field in this list");
        search.getDocument().addDocumentListener((SimpleListener) () -> {
            String text = search.getText().trim();
            sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
            updateCount();
        });
        JButton refresh = UiStyle.button("Refresh", false);
        refresh.addActionListener(e -> reload());
        JButton view = UiStyle.button("View", false);
        view.addActionListener(e -> showSelected(false));
        JButton edit = UiStyle.button("Edit", false);
        edit.addActionListener(e -> showSelected(true));
        JButton add = UiStyle.button("Add new record", true);
        add.addActionListener(e -> editRecord(null, null));
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        JLabel searchLabel = new JLabel("Search records");
        searchLabel.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.BOLD, 13));
        searchLabel.setForeground(UiStyle.INK);
        searchRow.add(searchLabel);
        searchRow.add(search);
        searchRow.add(refresh);
        JPanel recordActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        recordActions.setOpaque(false);
        JButton delete = null;
        if (session.isAdmin()) {
            delete = UiStyle.button("Delete", false);
            delete.addActionListener(e -> deleteSelected());
        }
        JPanel actionRow = new JPanel(new GridLayout(3, 1, 0, 6));
        actionRow.setOpaque(false);
        JLabel actionHint = new JLabel("Select a row to view or edit a record");
        actionHint.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        actionHint.setForeground(UiStyle.MUTED);
        recordActions.add(view);
        recordActions.add(edit);
        recordActions.add(add);
        if (delete != null) recordActions.add(delete);
        JPanel actionButtonsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionButtonsRow.setOpaque(false);
        actionButtonsRow.add(actionHint);
        actionRow.add(searchRow);
        actionRow.add(actionButtonsRow);
        actionRow.add(recordActions);
        toolbar.add(titleBar, BorderLayout.NORTH);
        toolbar.add(actionRow, BorderLayout.SOUTH);
        if (type == EntityType.CASES) {
            JPanel north = new JPanel(new BorderLayout(0, 12));
            north.setOpaque(false);
            north.add(toolbar, BorderLayout.NORTH);
            JPanel filters = new JPanel(new GridLayout(2, 3, 10, 6));
            filters.setOpaque(false);
            UiStyle.textField(categoryFilter);
            UiStyle.textField(fromDate);
            UiStyle.textField(toDate);
            categoryFilter.setToolTipText("Filter by exact crime category");
            fromDate.setToolTipText("Start incident date, YYYY-MM-DD");
            toDate.setToolTipText("End incident date, YYYY-MM-DD");
            filters.add(filterControl("Category", categoryFilter));
            filters.add(filterControl("Status", statusFilter));
            filters.add(filterControl("Incident date from", fromDate));
            filters.add(filterControl("Incident date to", toDate));
            JButton applyFilters = UiStyle.button("Apply filters", false);
            applyFilters.addActionListener(e -> reload());
            JButton clearFilters = UiStyle.button("Clear", false);
            clearFilters.addActionListener(e -> {
                categoryFilter.setText("");
                statusFilter.setSelectedIndex(0);
                fromDate.setText("");
                toDate.setText("");
                reload();
            });
            JPanel filterActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            filterActions.setOpaque(false);
            filterActions.add(applyFilters);
            filterActions.add(clearFilters);
            filters.add(filterActions);
            JLabel dateHint = new JLabel("Dates use YYYY-MM-DD");
            dateHint.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            dateHint.setForeground(UiStyle.MUTED);
            filters.add(dateHint);
            filters.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)));
            north.add(filters, BorderLayout.SOUTH);
            add(north, BorderLayout.NORTH);
        } else {
            add(toolbar, BorderLayout.NORTH);
        }
        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(UiStyle.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 12, 8, 12)));
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        count.setForeground(UiStyle.MUTED);
        count.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        tableCard.add(count, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);
        reload();
    }

    private JPanel filterControl(String label, JComponent input) {
        JPanel control = new JPanel(new BorderLayout(0, 3));
        control.setOpaque(false);
        JLabel caption = new JLabel(label);
        caption.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.BOLD, 12));
        caption.setForeground(UiStyle.INK);
        control.add(caption, BorderLayout.NORTH);
        control.add(input, BorderLayout.CENTER);
        return control;
    }

    private void reload() {
        try {
            if (type == EntityType.CASES) {
                java.time.LocalDate start = fromDate.getText().isBlank() ? null
                        : java.time.LocalDate.parse(fromDate.getText().trim());
                java.time.LocalDate end = toDate.getText().isBlank() ? null
                        : java.time.LocalDate.parse(toDate.getText().trim());
                if (start != null && end != null && start.isAfter(end))
                    throw new IllegalArgumentException("The start date must be on or before the end date.");
            }
            model.setRowCount(0);
            String category = type == EntityType.CASES ? categoryFilter.getText().trim() : "";
            String status = type == EntityType.CASES && statusFilter.getSelectedIndex() > 0
                    ? (String) statusFilter.getSelectedItem() : "";
            String start = type == EntityType.CASES ? fromDate.getText().trim() : "";
            String end = type == EntityType.CASES ? toDate.getText().trim() : "";
            for (Object[] row : records.list(type, "", category, status, start, end)) model.addRow(row);
            updateCount();
        } catch (Exception exception) {
            if (exception instanceof IllegalArgumentException || exception instanceof java.time.DateTimeException) {
                JOptionPane.showMessageDialog(this, "Use valid dates in YYYY-MM-DD format and ensure the date range is in order.\n"
                        + exception.getMessage(), "Invalid filters", JOptionPane.WARNING_MESSAGE);
            } else showError("Records could not be loaded.", exception);
        }
    }

    private void updateCount() {
        count.setText(table.getRowCount() + " record(s)");
    }

    private void showSelected(boolean edit) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a record first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = ((Number) model.getValueAt(modelRow, 0)).intValue();
        List<String> values = new java.util.ArrayList<>();
        for (int i = 0; i < type.columns().size(); i++) {
            Object value = model.getValueAt(modelRow, i + 1);
            values.add(value == null ? "" : value.toString());
        }
        editRecord(id, values, !edit);
    }

    private void editRecord(Integer id, List<String> values) {
        editRecord(id, values, false);
    }

    private void editRecord(Integer id, List<String> values, boolean readOnly) {
        RecordDialog dialog = new RecordDialog(javax.swing.SwingUtilities.getWindowAncestor(this), type, id, values, readOnly);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                records.save(type, id, dialog.values());
                reload();
                changed.run();
                JOptionPane.showMessageDialog(this, id == null ? "Record added." : "Record updated.",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception exception) {
                showError("Record could not be saved.", exception);
            }
        }
    }

    private void deleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a record first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = ((Number) model.getValueAt(modelRow, 0)).intValue();
        int confirm = JOptionPane.showConfirmDialog(this, "Delete record #" + id + "? This cannot be undone.",
                "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            records.delete(type, id);
            reload();
            changed.run();
            JOptionPane.showMessageDialog(this, "Record deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            showError("Record could not be deleted. Check whether another record depends on it.", exception);
        }
    }

    private void showError(String message, Exception exception) {
        exception.printStackTrace();
        JOptionPane.showMessageDialog(this, message + "\n" + exception.getMessage(), "Operation failed", JOptionPane.ERROR_MESSAGE);
    }

    @Override public void exportPdf(Component parent) {
        List<String> lines = new ArrayList<>();
        lines.add("CDMS " + type.label() + " Report");
        lines.add("Records in this report: " + table.getRowCount());
        lines.add(" ");
        for (int row = 0; row < table.getRowCount(); row++) {
            StringBuilder line = new StringBuilder("ID ").append(table.getValueAt(row, 0));
            for (int column = 1; column < table.getColumnCount(); column++)
                line.append(" | ").append(table.getColumnName(column)).append(": ").append(table.getValueAt(row, column));
            lines.add(line.toString());
        }
        ReportExporter.export(parent, type.name().toLowerCase() + "-report", lines);
    }

    @FunctionalInterface
    private interface SimpleListener extends javax.swing.event.DocumentListener {
        void changed();
        @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
        @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
        @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
    }
}
