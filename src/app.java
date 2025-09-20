package src;

import javax.swing.SwingUtilities;

public class app {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new musicPlayerGUI().setVisible(true);
            }
        });
    }

}
