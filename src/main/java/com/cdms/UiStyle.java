package com.cdms;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

final class UiStyle {
    static final Color NAVY = new Color(20, 43, 74);
    static final Color BLUE = new Color(37, 99, 166);
    static final Color BLUE_HOVER = new Color(29, 82, 143);
    static final Color NAV_BUTTON = new Color(238, 244, 250);
    static final Color NAV_HOVER = Color.WHITE;
    static final Color NAV_SELECTED = new Color(207, 229, 250);
    static final Color CANVAS = new Color(244, 247, 251);
    static final Color INK = new Color(34, 48, 65);
    static final Color MUTED = new Color(83, 98, 117);
    static final Color WHITE = Color.WHITE;
    static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font HEADING = new Font("Segoe UI Semibold", Font.BOLD, 24);

    private UiStyle() {}

    static void installDefaults() {
        Font body = BODY;
        UIManager.put("Label.foreground", INK);
        UIManager.put("Label.font", body);
        UIManager.put("Button.font", new Font("Segoe UI Semibold", Font.BOLD, 14));
        UIManager.put("Button.foreground", NAVY);
        UIManager.put("Button.background", WHITE);
        UIManager.put("Button.disabledText", MUTED);
        UIManager.put("Button.select", new Color(225, 233, 242));
        UIManager.put("CheckBox.foreground", INK);
        UIManager.put("CheckBox.font", body);
        UIManager.put("TextField.font", body);
        UIManager.put("TextField.foreground", INK);
        UIManager.put("TextField.background", WHITE);
        UIManager.put("TextField.inactiveForeground", INK);
        UIManager.put("PasswordField.font", body);
        UIManager.put("PasswordField.foreground", INK);
        UIManager.put("PasswordField.background", WHITE);
        UIManager.put("TextArea.font", body);
        UIManager.put("TextArea.foreground", INK);
        UIManager.put("TextArea.background", WHITE);
        UIManager.put("ComboBox.font", body);
        UIManager.put("ComboBox.foreground", INK);
        UIManager.put("ComboBox.background", WHITE);
        UIManager.put("ComboBox.selectionForeground", WHITE);
        UIManager.put("ComboBox.selectionBackground", NAVY);
        UIManager.put("ComboBox.disabledForeground", MUTED);
        UIManager.put("List.foreground", INK);
        UIManager.put("List.background", WHITE);
        UIManager.put("List.selectionForeground", WHITE);
        UIManager.put("List.selectionBackground", NAVY);
        UIManager.put("OptionPane.messageForeground", INK);
        UIManager.put("OptionPane.messageFont", body);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI Semibold", Font.BOLD, 13));
        UIManager.put("Table.font", body);
        UIManager.put("Table.foreground", INK);
        UIManager.put("Table.background", WHITE);
        UIManager.put("TableHeader.font", new Font("Segoe UI Semibold", Font.BOLD, 13));
        UIManager.put("TableHeader.foreground", NAVY);
        UIManager.put("TableHeader.background", new Color(232, 239, 247));
    }

    static void style(JComponent component) {
        component.setFont(BODY);
        component.setForeground(INK);
    }

    static JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        button.setForeground(primary ? WHITE : NAVY);
        button.setBackground(primary ? BLUE : WHITE);
        button.setFocusPainted(false);
        button.setBorder(buttonBorder(primary, false, false));
        button.setOpaque(true);
        button.setRolloverEnabled(true);
        button.getModel().addChangeListener(event -> {
            boolean enabled = button.isEnabled();
            boolean pressed = enabled && button.getModel().isPressed() && button.getModel().isArmed();
            boolean hovered = enabled && button.getModel().isRollover();
            Color background = !enabled ? new Color(231, 235, 240)
                    : pressed ? (primary ? new Color(18, 54, 91) : new Color(225, 233, 242))
                    : hovered ? (primary ? BLUE_HOVER : new Color(239, 245, 251))
                    : primary ? BLUE : WHITE;
            button.setForeground(!enabled ? MUTED : primary ? WHITE : NAVY);
            button.setBackground(background);
            button.setBorder(buttonBorder(primary, enabled && (hovered || pressed), !enabled));
        });
        return button;
    }

    private static javax.swing.border.Border buttonBorder(boolean primary, boolean highlighted, boolean disabled) {
        Color outline = disabled ? new Color(205, 213, 222)
                : highlighted ? (primary ? BLUE_HOVER : new Color(160, 185, 211))
                : primary ? BLUE : new Color(190, 202, 216);
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(outline),
                BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }

    static void textField(JTextField field) {
        field.setFont(BODY);
        field.setForeground(INK);
        field.setBackground(WHITE);
        field.setCaretColor(NAVY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 202, 216)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }

    static <T> JComboBox<T> comboBox(JComboBox<T> combo) {
        combo.setFont(BODY);
        combo.setForeground(INK);
        combo.setBackground(WHITE);
        combo.setBorder(BorderFactory.createLineBorder(new Color(190, 202, 216)));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                          boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                setForeground(selected ? WHITE : INK);
                setBackground(selected ? NAVY : WHITE);
                setBorder(new EmptyBorder(5, 8, 5, 8));
                return this;
            }
        });
        return combo;
    }

    static void styleTree(Component component) {
        if (component instanceof JLabel label) {
            label.setForeground(INK);
            label.setFont(BODY);
        } else if (component instanceof JTextField field) {
            textField(field);
        } else if (component instanceof JTextArea area) {
            area.setFont(BODY);
            area.setForeground(INK);
            area.setBackground(WHITE);
            area.setCaretColor(NAVY);
        } else if (component instanceof JComboBox<?> combo) {
            comboBox(combo);
        } else if (component instanceof JPanel panel) {
            panel.setBackground(WHITE);
            for (Component child : panel.getComponents()) styleTree(child);
        } else if (component instanceof JComponent swing) {
            swing.setFont(BODY);
            swing.setForeground(INK);
        }
    }

    static void table(JTable table) {
        table.setFont(BODY);
        table.setForeground(INK);
        table.setBackground(WHITE);
        table.setRowHeight(34);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(229, 235, 242));
        table.setSelectionBackground(new Color(219, 234, 250));
        table.setSelectionForeground(NAVY);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        header.setForeground(NAVY);
        header.setBackground(new Color(232, 239, 247));
        header.setOpaque(true);
        header.setPreferredSize(new java.awt.Dimension(0, 38));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                super.getTableCellRendererComponent(source, value, selected, focused, row, column);
                setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
                setForeground(NAVY);
                setBackground(new Color(232, 239, 247));
                setHorizontalAlignment(LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(207, 218, 231)),
                        new EmptyBorder(0, 9, 0, 9)));
                return this;
            }
        });
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                Component cell = super.getTableCellRendererComponent(source, value, selected, focused, row, column);
                cell.setForeground(selected ? NAVY : INK);
                cell.setBackground(selected ? new Color(219, 234, 250)
                        : row % 2 == 0 ? WHITE : new Color(246, 249, 252));
                setBorder(new EmptyBorder(0, 9, 0, 9));
                if (cell instanceof javax.swing.JLabel label && value != null)
                    label.setToolTipText(value.toString().length() > 45 ? value.toString() : null);
                return cell;
            }
        };
        for (int column = 0; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setCellRenderer(renderer);
            table.getColumnModel().getColumn(column).setPreferredWidth(column == 0 ? 75 : 170);
        }
    }
}
