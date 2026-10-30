package com.cdms;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

final class DashboardPanel extends JPanel {
    private final DashboardDAO dashboard = new DashboardDAO();

    DashboardPanel() {
        super(new BorderLayout(0, 18));
        setBackground(UiStyle.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        refresh();
    }

    void refresh() {
        removeAll();
        JLabel heading = new JLabel("Department Overview");
        heading.setFont(UiStyle.HEADING);
        heading.setForeground(UiStyle.NAVY);
        add(heading, BorderLayout.NORTH);
        try {
            long[] stats = dashboard.statistics();
            String[] labels = {"Criminal records", "Registered cases", "Active investigations",
                    "Closed cases", "Wanted criminals", "Police officers"};
            JPanel cards = new JPanel(new GridLayout(2, 3, 14, 14));
            cards.setOpaque(false);
            for (int i = 0; i < stats.length; i++) cards.add(card(labels[i], stats[i], i));
            JPanel main = new JPanel(new BorderLayout(0, 18));
            main.setOpaque(false);
            main.add(cards, BorderLayout.NORTH);
            JPanel lower = new JPanel(new GridLayout(1, 2, 14, 0));
            lower.setOpaque(false);
            lower.add(tableCard("Recent case updates", new String[]{"Case number", "Case title", "Status", "Created"},
                    dashboard.recentCases()));
            lower.add(tableCard("Recent investigation activity", new String[]{"Case", "Officer", "Status", "Updated"},
                    dashboard.recentInvestigations()));
            main.add(lower, BorderLayout.CENTER);
            add(main, BorderLayout.CENTER);
        } catch (Exception exception) {
            exception.printStackTrace();
            JLabel error = new JLabel("Dashboard data could not be loaded: " + exception.getMessage());
            error.setForeground(new Color(170, 45, 45));
            add(error, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    private JPanel card(String label, long value, int index) {
        Color[] accents = {new Color(37, 99, 166), new Color(31, 132, 125), new Color(125, 92, 178),
                new Color(82, 132, 96), new Color(196, 107, 57), new Color(81, 112, 145)};
        JPanel card = new JPanel(new BorderLayout(0, 9));
        card.setBackground(UiStyle.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, accents[index]),
                BorderFactory.createEmptyBorder(17, 18, 16, 16)));
        JLabel title = new JLabel(label);
        title.setForeground(UiStyle.MUTED);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        JLabel number = new JLabel(Long.toString(value));
        number.setForeground(UiStyle.NAVY);
        number.setFont(new Font("Segoe UI Semibold", Font.BOLD, 29));
        card.add(title, BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);
        return card;
    }

    private JPanel tableCard(String title, String[] headers, List<Object[]> rows) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UiStyle.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        label.setForeground(UiStyle.NAVY);
        card.add(label, BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        rows.forEach(model::addRow);
        JTable table = new JTable(model);
        UiStyle.table(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }
}
