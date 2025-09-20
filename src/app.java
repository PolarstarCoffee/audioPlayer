package src;

import javax.swing.SwingUtilities;

public class app {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new musicPlayerGUI().setVisible(true);

                // Song song = new Song(
                // "src/assets/Otorii Station - Chill 🌴 Marvel vs. Capcom 2- New Age of Heroes
                // 🥋 - 04 Airship Stage.mp3");
                // System.out.println("Title: " + song.getSongTitle());
                // System.out.println("Artist: " + song.getSongArtist());
            }
        });
    }

}
