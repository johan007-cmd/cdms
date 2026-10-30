package com.cdms;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class App {
    private App() {}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UiStyle.installDefaults();
            Database.initialize();
        } catch (Exception exception) {
            exception.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Could not connect to MySQL or initialize the CDMS database.\n\n"
                            + exception.getMessage() + "\n\nCheck the database settings in README.md.",
                    "CDMS startup failed", JOptionPane.ERROR_MESSAGE));
            return;
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
