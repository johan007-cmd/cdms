package com.cdms;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

final class UsersPanel extends JPanel {
    private final UserSession session;
    private final UserDAO users = new UserDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Username", "Role", "Officer ID", "Officer name"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    UsersPanel(UserSession session) {
        super(new BorderLayout(0, 16));
        this.session = session;
        setBackground(UiStyle.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        JLabel title = new JLabel("User Management");
        title.setFont(UiStyle.HEADING);
        title.setForeground(UiStyle.NAVY);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        var add = UiStyle.button("Create user", true);
        add.addActionListener(e -> create());
        var remove = UiStyle.button("Delete user", false);
        remove.addActionListener(e -> delete());
        actions.add(add);
        actions.add(remove);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        table.setRowHeight(31);
        UiStyle.table(table);
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiStyle.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.add(new JScrollPane(table));
        add(card, BorderLayout.CENTER);
        reload();
    }

    private void reload() {
        try {
            model.setRowCount(0);
            for (Object[] row : users.listUsers()) model.addRow(row);
        } catch (Exception exception) {
            showError("User list could not be loaded.", exception);
        }
    }

    private void create() {
        var username = new javax.swing.JTextField(22);
        var password = new javax.swing.JPasswordField(22);
        var officer = new javax.swing.JTextField(22);
        var role = new JComboBox<>(new String[]{"OFFICER", "ADMIN"});
        UiStyle.comboBox(role);
        JPanel form = new JPanel(new java.awt.GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Username"));
        form.add(username);
        form.add(new JLabel("Temporary password"));
        form.add(password);
        form.add(new JLabel("Role"));
        form.add(role);
        form.add(new JLabel("Officer ID (optional)"));
        form.add(officer);
        UiStyle.styleTree(form);
        if (JOptionPane.showConfirmDialog(this, form, "Create user", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            Integer officerId = officer.getText().isBlank() ? null : Integer.valueOf(officer.getText().trim());
            char[] chars = password.getPassword();
            try {
                users.createUser(username.getText().trim(), new String(chars), (String) role.getSelectedItem(), officerId);
            } finally {
                java.util.Arrays.fill(chars, '\0');
            }
            reload();
            JOptionPane.showMessageDialog(this, "User created.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            showError("User could not be created.", exception);
        }
    }

    private void delete() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int userId = ((Number) model.getValueAt(table.convertRowIndexToModel(selected), 0)).intValue();
        if (JOptionPane.showConfirmDialog(this, "Delete the selected user account?", "Confirm deletion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            users.deleteUser(userId, session.userId());
            reload();
            JOptionPane.showMessageDialog(this, "User deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            showError("User could not be deleted.", exception);
        }
    }

    private void showError(String message, Exception exception) {
        exception.printStackTrace();
        JOptionPane.showMessageDialog(this, message + "\n" + exception.getMessage(),
                "Operation failed", JOptionPane.ERROR_MESSAGE);
    }
}
