package budgetsystem;

import budgetsystem.gui.WelcomeFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WelcomeFrame wf = new WelcomeFrame();
            wf.setVisible(true);
        });
    }
}
