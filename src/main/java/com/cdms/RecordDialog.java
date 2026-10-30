package com.cdms;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

final class RecordDialog extends JDialog {
    private final EntityType type;
    private final Integer id;
    private final List<JComponent> inputs = new ArrayList<>();
    private boolean saved;

    RecordDialog(Window owner, EntityType type, Integer id, List<String> values, boolean readOnly) {
        super(owner, (id == null ? "Add " : readOnly ? "View " : "Edit ") + type.label(), ModalityType.APPLICATION_MODAL);
        this.type = type;
        this.id = id;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UiStyle.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        for (int i = 0; i < type.columns().size(); i++) {
            EntityType.Column column = type.columns().get(i);
            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.gridy = i;
            labelConstraints.anchor = GridBagConstraints.NORTHWEST;
            labelConstraints.insets = new Insets(7, 4, 7, 14);
            JLabel label = new JLabel(column.label() + (column.required() ? " *" : ""));
            label.setFont(UiStyle.BODY);
            label.setForeground(UiStyle.INK);
            form.add(label, labelConstraints);

            JComponent input;
            if (column.kind() == EntityType.Kind.LONG_TEXT) {
                JTextArea area = new JTextArea(3, 32);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setFont(UiStyle.BODY);
                area.setForeground(UiStyle.INK);
                area.setBackground(UiStyle.WHITE);
                area.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
                input = new JScrollPane(area);
                ((JScrollPane) input).getViewport().setBackground(UiStyle.WHITE);
                ((JScrollPane) input).setBorder(BorderFactory.createLineBorder(new java.awt.Color(190, 202, 216)));
                input.setPreferredSize(new Dimension(340, 75));
                if (values != null) area.setText(values.get(i));
                area.setEditable(!readOnly);
            } else if (isChoice(column)) {
                JComboBox<String> combo = new JComboBox<>(choices(column));
                UiStyle.comboBox(combo);
                if (values != null && values.get(i) != null && !values.get(i).isBlank()) combo.setSelectedItem(values.get(i));
                combo.setEnabled(!readOnly);
                input = combo;
            } else {
                JTextField field = new JTextField(32);
                UiStyle.textField(field);
                if (values != null && values.get(i) != null) field.setText(values.get(i));
                field.setEditable(!readOnly);
                input = field;
            }
            inputs.add(input);
            GridBagConstraints inputConstraints = new GridBagConstraints();
            inputConstraints.gridx = 1;
            inputConstraints.gridy = i;
            inputConstraints.weightx = 1;
            inputConstraints.fill = GridBagConstraints.HORIZONTAL;
            inputConstraints.anchor = GridBagConstraints.NORTH;
            inputConstraints.insets = new Insets(5, 0, 5, 4);
            form.add(input, inputConstraints);
        }
        JScrollPane formScroll = new JScrollPane(form);
        formScroll.getViewport().setBackground(UiStyle.WHITE);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        add(formScroll, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 10));
        buttons.setBackground(UiStyle.WHITE);
        if (!readOnly) {
            JButton save = UiStyle.button(id == null ? "Add record" : "Save changes", true);
            save.addActionListener(e -> submit());
            buttons.add(save);
            getRootPane().setDefaultButton(save);
        }
        JButton cancel = UiStyle.button(readOnly ? "Close" : "Cancel", false);
        cancel.addActionListener(e -> dispose());
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);
        setSize(650, Math.min(780, Math.max(420, 190 + type.columns().size() * 62)));
        setLocationRelativeTo(owner);
    }

    private boolean isChoice(EntityType.Column column) {
        return column.name().equals("status") && (type == EntityType.CRIMINALS || type == EntityType.CASES);
    }

    private String[] choices(EntityType.Column column) {
        if (type == EntityType.CRIMINALS) return new String[]{"WANTED", "ARRESTED", "RELEASED", "UNDER_SURVEILLANCE"};
        return new String[]{"OPEN", "UNDER_INVESTIGATION", "SOLVED", "CLOSED"};
    }

    private void submit() {
        List<String> result = values();
        try {
            for (int i = 0; i < result.size(); i++) {
                String value = result.get(i).trim();
                EntityType.Column column = type.columns().get(i);
                if (column.required() && value.isBlank()) throw new IllegalArgumentException(column.label() + " is required.");
                if (value.isBlank()) continue;
                if (column.name().equals("email") && !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                    throw new IllegalArgumentException("Enter a valid email address.");
                switch (column.kind()) {
                    case INTEGER -> Integer.parseInt(value);
                    case DATE -> LocalDate.parse(value);
                    case DATETIME -> LocalDateTime.parse(value.replace(' ', 'T'));
                    default -> { }
                }
            }
        } catch (RuntimeException exception) {
            String message = exception instanceof IllegalArgumentException && exception.getMessage() != null
                    && !exception.getMessage().contains("could not be parsed")
                    ? exception.getMessage() : "Check the entered number, date, and date-time formats.";
            JOptionPane.showMessageDialog(this, message, "Check the form", JOptionPane.WARNING_MESSAGE);
            return;
        }
        saved = true;
        dispose();
    }

    List<String> values() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            JComponent input = inputs.get(i);
            if (input instanceof JScrollPane scroll && scroll.getViewport().getView() instanceof JTextArea area) {
                result.add(area.getText());
            } else if (input instanceof JComboBox<?> combo) {
                result.add(String.valueOf(combo.getSelectedItem()));
            } else if (input instanceof JTextField field) {
                result.add(field.getText());
            }
        }
        return result;
    }

    boolean isSaved() { return saved; }
}
