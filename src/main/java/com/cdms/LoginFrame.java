package com.cdms;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

final class LoginFrame extends JFrame {
    private final PlaceholderTextField username = new PlaceholderTextField("Enter your username");
    private final PlaceholderPasswordField password = new PlaceholderPasswordField("Enter your password");
    private final JButton login = new JButton("LOGIN");
    private final UserDAO users = new UserDAO();

    LoginFrame() {
        setTitle("CDMS | Secure Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(430, 560));
        setSize(520, 690);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(239, 244, 249));

        JPanel centered = new JPanel(new GridBagLayout());
        centered.setOpaque(false);
        centered.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel card = createCard();
        GridBagConstraints cardConstraints = new GridBagConstraints();
        cardConstraints.gridx = 0;
        cardConstraints.gridy = 0;
        cardConstraints.weightx = 1;
        cardConstraints.weighty = 1;
        cardConstraints.anchor = GridBagConstraints.CENTER;
        cardConstraints.fill = GridBagConstraints.HORIZONTAL;
        centered.add(card, cardConstraints);
        add(centered, BorderLayout.CENTER);

        username.addActionListener(this::signIn);
        password.addActionListener(this::signIn);
        getRootPane().setDefaultButton(login);
    }

    private JPanel createCard() {
        JPanel card = new LoginCard();
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(30, 38, 30, 38));
        card.setPreferredSize(new Dimension(460, 520));
        card.setMinimumSize(new Dimension(300, 470));
        card.setMaximumSize(new Dimension(500, 530));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        JLabel emblem = new JLabel("CDMS");
        emblem.setForeground(UiStyle.NAVY);
        emblem.setFont(new Font("Segoe UI", Font.BOLD, 34));
        emblem.setAlignmentX(LEFT_ALIGNMENT);
        header.add(emblem);

        JLabel title = new JLabel("Criminal Database Management System");
        title.setForeground(UiStyle.NAVY);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 17));
        title.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);

        JLabel subtitle = new JLabel("AUTHORIZED PERSONNEL ONLY");
        subtitle.setForeground(UiStyle.MUTED);
        subtitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(13, 0, 0, 0));
        header.add(subtitle);
        card.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        addField(form, 0, "Username", username);
        addField(form, 2, "Password", password);

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPassword.setForeground(UiStyle.INK);
        showPassword.setOpaque(false);
        showPassword.setFocusPainted(false);
        showPassword.addActionListener(event -> password.setPasswordVisible(showPassword.isSelected()));
        GridBagConstraints showConstraints = new GridBagConstraints();
        showConstraints.gridx = 0;
        showConstraints.gridy = 4;
        showConstraints.gridwidth = 2;
        showConstraints.anchor = GridBagConstraints.LINE_START;
        showConstraints.insets = new Insets(3, 0, 0, 0);
        form.add(showPassword, showConstraints);

        styleLoginButton();
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 5;
        buttonConstraints.gridwidth = 2;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.insets = new Insets(25, 0, 0, 0);
        form.add(login, buttonConstraints);
        login.addActionListener(this::signIn);
        card.add(form, BorderLayout.CENTER);

        JLabel footer = new JLabel("Secure access for authorized department staff");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.setForeground(UiStyle.MUTED);
        footer.setHorizontalAlignment(JLabel.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void addField(JPanel form, int row, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        label.setForeground(UiStyle.INK);
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.gridwidth = 2;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.anchor = GridBagConstraints.LINE_START;
        labelConstraints.insets = new Insets(row == 0 ? 21 : 18, 0, 7, 0);
        form.add(label, labelConstraints);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(UiStyle.INK);
        field.setBackground(Color.WHITE);
        field.setCaretColor(UiStyle.NAVY);
        field.setPreferredSize(new Dimension(350, 48));
        field.setBorder(fieldBorder(false));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent event) {
                field.setBorder(fieldBorder(true));
            }

            @Override public void focusLost(FocusEvent event) {
                field.setBorder(fieldBorder(false));
            }
        });
        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 0;
        fieldConstraints.gridy = row + 1;
        fieldConstraints.gridwidth = 2;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1;
        fieldConstraints.insets = new Insets(0, 0, 0, 0);
        form.add(field, fieldConstraints);
    }

    private javax.swing.border.Border fieldBorder(boolean focused) {
        Color outline = focused ? UiStyle.BLUE : new Color(190, 202, 216);
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(outline, focused ? 2 : 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12));
    }

    private void styleLoginButton() {
        login.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        login.setForeground(Color.WHITE);
        login.setBackground(UiStyle.NAVY);
        login.setOpaque(true);
        login.setContentAreaFilled(true);
        login.setBorderPainted(false);
        login.setFocusPainted(false);
        login.setPreferredSize(new Dimension(350, 50));
        login.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        ChangeListener buttonState = event -> {
            if (login.getModel().isPressed()) {
                login.setBackground(new Color(11, 30, 54));
            } else if (login.getModel().isRollover()) {
                login.setBackground(UiStyle.BLUE);
            } else {
                login.setBackground(UiStyle.NAVY);
            }
            login.setForeground(Color.WHITE);
        };
        login.getModel().addChangeListener(buttonState);
    }

    private void signIn(ActionEvent event) {
        char[] chars = password.getPassword();
        try {
            UserSession session = users.authenticate(username.getText(), new String(chars));
            if (session == null) {
                JOptionPane.showMessageDialog(this, "Username or password is incorrect.",
                        "Sign in failed", JOptionPane.ERROR_MESSAGE);
                password.setText("");
                password.requestFocusInWindow();
                return;
            }
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame(session).setVisible(true));
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Sign in could not be completed: " + exception.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        } finally {
            java.util.Arrays.fill(chars, '\0');
        }
    }

    private static class LoginCard extends JPanel {
        LoginCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            for (int i = 5; i >= 1; i--) {
                g.setColor(new Color(18, 39, 65, 8 + i * 3));
                g.fill(new RoundRectangle2D.Float(i, i + 2, width - i * 2f, height - i * 2f, 20, 20));
            }
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(5, 5, width - 11, height - 11, 20, 20));
            g.setColor(new Color(222, 230, 239));
            g.draw(new RoundRectangle2D.Float(5, 5, width - 11, height - 11, 20, 20));
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class PlaceholderTextField extends JTextField {
        private final String placeholder;

        PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty() && !hasFocus()) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setFont(getFont());
                g.setColor(UiStyle.MUTED);
                Insets insets = getInsets();
                g.drawString(placeholder, insets.left, getHeight() / 2 + g.getFontMetrics().getAscent() / 2 - 2);
                g.dispose();
            }
        }
    }

    private static class PlaceholderPasswordField extends JPasswordField {
        private final String placeholder;
        private final char mask;

        PlaceholderPasswordField(String placeholder) {
            this.placeholder = placeholder;
            mask = getEchoChar();
        }

        void setPasswordVisible(boolean visible) {
            setEchoChar(visible ? (char) 0 : mask);
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            if (getDocument().getLength() == 0 && !hasFocus()) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setFont(getFont());
                g.setColor(UiStyle.MUTED);
                Insets insets = getInsets();
                g.drawString(placeholder, insets.left, getHeight() / 2 + g.getFontMetrics().getAscent() / 2 - 2);
                g.dispose();
            }
        }
    }
}
