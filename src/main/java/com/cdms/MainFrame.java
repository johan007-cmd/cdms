package com.cdms;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

final class MainFrame extends JFrame {
    private final UserSession session;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel sidebar = new JPanel();
    private final List<JButton> navigationButtons = new ArrayList<>();
    private String currentKey = "dashboard";
    private PdfExportable currentReport;
    private DashboardPanel dashboard;

    MainFrame(UserSession session) {
        this.session = session;
        setTitle("CDMS | Criminal Database Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(UiStyle.CANVAS);
        content.setBackground(UiStyle.CANVAS);
        add(buildTopBar(), BorderLayout.NORTH);
        sidebar.setLayout(new javax.swing.BoxLayout(sidebar, javax.swing.BoxLayout.Y_AXIS));
        sidebar.setBackground(UiStyle.NAVY);
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));
        sidebar.setPreferredSize(new Dimension(260, 0));
        JLabel brand = new JLabel("  CDMS");
        brand.setForeground(java.awt.Color.WHITE);
        brand.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 23));
        brand.setBorder(BorderFactory.createEmptyBorder(8, 2, 20, 2));
        sidebar.add(brand);
        addSection("HOME");
        addNav("Overview", "dashboard");
        addSection("SEARCH");
        addNav("Global Search", "global");
        addSection("RECORDS");
        addNav("Criminal Records", EntityType.CRIMINALS.name());
        addNav("Crime Cases", EntityType.CASES.name());
        addNav("Suspects", EntityType.SUSPECTS.name());
        addNav("Victims", EntityType.VICTIMS.name());
        addSection("OPERATIONS");
        addNav("Investigation Management", EntityType.INVESTIGATIONS.name());
        addNav("Evidence Management", EntityType.EVIDENCE.name());
        addNav("Reports", "reports");
        addSection("STAFF");
        addNav("Police Officers", EntityType.OFFICERS.name());
        if (session.isAdmin()) {
            addSection("ADMINISTRATION");
            addNav("User Management", "users");
        }
        JScrollPane navigation = new JScrollPane(sidebar,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navigation.setBorder(null);
        navigation.setPreferredSize(new Dimension(260, 0));
        navigation.getViewport().setBackground(UiStyle.NAVY);
        add(navigation, BorderLayout.WEST);
        dashboard = new DashboardPanel();
        content.add(dashboard, "dashboard");
        content.add(new GlobalSearchPanel(), "global");
        content.add(new ReportsPanel(), "reports");
        for (EntityType type : EntityType.values()) {
            content.add(new DataTablePanel(type, session, this::refreshDashboard), type.name());
        }
        if (session.isAdmin()) content.add(new UsersPanel(session), "users");
        add(content, BorderLayout.CENTER);
        navigate("dashboard");
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new GridLayout(2, 1, 0, 8));
        top.setBackground(java.awt.Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                new java.awt.Color(226, 232, 240)), BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        JPanel identity = new JPanel(new BorderLayout());
        identity.setOpaque(false);
        JLabel heading = new JLabel("CDMS  /  Police Department Records");
        heading.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.BOLD, 15));
        heading.setForeground(UiStyle.NAVY);
        identity.add(heading, BorderLayout.WEST);
        JLabel user = new JLabel(session.username() + "  |  " + (session.isAdmin() ? "Administrator" : "Police Officer"));
        user.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        user.setForeground(UiStyle.MUTED);
        identity.add(user, BorderLayout.EAST);
        top.add(identity);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton export = UiStyle.button("Export PDF", false);
        export.addActionListener(e -> {
            if (currentReport == null) {
                JOptionPane.showMessageDialog(this, "Select a record list or search results to export.",
                        "PDF report", JOptionPane.INFORMATION_MESSAGE);
            } else currentReport.exportPdf(this);
        });
        actions.add(export);
        JButton password = UiStyle.button("Change password", false);
        password.addActionListener(e -> changePassword());
        actions.add(password);
        JButton logout = UiStyle.button("Sign out", false);
        logout.addActionListener(e -> logout());
        actions.add(logout);
        top.add(actions);
        return top;
    }

    private void addSection(String title) {
        JLabel section = new JLabel(title);
        section.setForeground(new java.awt.Color(213, 226, 241));
        section.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.BOLD, 11));
        section.setBorder(BorderFactory.createEmptyBorder(15, 11, 5, 6));
        section.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(section);
    }

    private void addNav(String label, String key) {
        JButton button = new JButton(label);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.BOLD, 14));
        button.setForeground(UiStyle.NAVY);
        button.setBackground(UiStyle.NAV_BUTTON);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiStyle.NAV_BUTTON),
                BorderFactory.createEmptyBorder(11, 12, 11, 8)));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setName(key);
        button.setToolTipText(label);
        button.setRolloverEnabled(true);
        button.getModel().addChangeListener(event -> {
            boolean active = key.equals(currentKey);
            boolean pressed = button.isEnabled() && button.getModel().isPressed() && button.getModel().isArmed();
            boolean hovered = button.isEnabled() && button.getModel().isRollover();
            styleNavigationButton(button, active, hovered, pressed);
        });
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) {
                styleNavigationButton(button, key.equals(currentKey), true, false);
            }

            @Override public void mouseExited(MouseEvent event) {
                styleNavigationButton(button, key.equals(currentKey), false, false);
            }
        });
        button.addActionListener(e -> navigate(key));
        navigationButtons.add(button);
        sidebar.add(button);
    }

    private void navigate(String key) {
        currentKey = key;
        cards.show(content, key);
        currentReport = null;
        for (java.awt.Component component : content.getComponents()) {
            if (key.equals("global") && component instanceof GlobalSearchPanel panel) currentReport = panel;
            if (key.equals("reports") && component instanceof ReportsPanel panel) currentReport = panel;
            if (key.equals("dashboard") && component instanceof DashboardPanel panel) {
                dashboard = panel;
                dashboard.refresh();
            }
            if (key.equals("users") && component instanceof UsersPanel) currentReport = null;
            if (component instanceof DataTablePanel panel && component.getName() != null
                    && component.getName().equals(key)) currentReport = panel;
        }
        for (JButton button : navigationButtons) {
            boolean active = button.getName().equals(key);
            styleNavigationButton(button, active, button.getModel().isRollover(),
                    button.getModel().isPressed() && button.getModel().isArmed());
        }
    }

    private void styleNavigationButton(JButton button, boolean active, boolean hovered, boolean pressed) {
        Color background = active ? UiStyle.NAV_SELECTED
                : pressed ? new Color(218, 230, 243)
                : hovered ? UiStyle.NAV_HOVER : UiStyle.NAV_BUTTON;
        button.setBackground(background);
        button.setForeground(UiStyle.NAVY);
        button.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.BOLD, 14));
        if (active) {
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, UiStyle.BLUE),
                    BorderFactory.createEmptyBorder(11, 8, 11, 8)));
        } else {
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(hovered ? new java.awt.Color(154, 189, 224) : UiStyle.NAV_BUTTON),
                    BorderFactory.createEmptyBorder(11, 12, 11, 8)));
        }
    }

    private void refreshDashboard() {
        if (dashboard != null) dashboard.refresh();
    }

    private void changePassword() {
        JPasswordField oldPassword = new JPasswordField(24);
        JPasswordField newPassword = new JPasswordField(24);
        JPasswordField confirmPassword = new JPasswordField(24);
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Current password"));
        form.add(oldPassword);
        form.add(new JLabel("New password"));
        form.add(newPassword);
        form.add(new JLabel("Confirm new password"));
        form.add(confirmPassword);
        UiStyle.styleTree(form);
        if (JOptionPane.showConfirmDialog(this, form, "Change password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        char[] oldChars = oldPassword.getPassword();
        char[] newChars = newPassword.getPassword();
        char[] confirmChars = confirmPassword.getPassword();
        try {
            String next = new String(newChars);
            UserDAO.validatePassword(next);
            if (!java.security.MessageDigest.isEqual(newCharsToBytes(newChars), newCharsToBytes(confirmChars)))
                throw new IllegalArgumentException("The new password and confirmation do not match.");
            new UserDAO().changePassword(session, new String(oldChars), next);
            JOptionPane.showMessageDialog(this, "Password changed successfully.",
                    "Password updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Password not changed", JOptionPane.ERROR_MESSAGE);
        } finally {
            java.util.Arrays.fill(oldChars, '\0');
            java.util.Arrays.fill(newChars, '\0');
            java.util.Arrays.fill(confirmChars, '\0');
        }
    }

    private byte[] newCharsToBytes(char[] chars) {
        return new String(chars).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "Sign out of CDMS?", "Confirm sign out",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
