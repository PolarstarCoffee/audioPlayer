package src;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class musicPlaylistPrompt extends JDialog {
    private musicPlayerGUI musicPlayerGUI;
    // store all of the paths to be written to a text file (when we load a playlist,
    // we read from this text file)
    private ArrayList<String> songPaths;

    public musicPlaylistPrompt(musicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
        songPaths = new ArrayList<String>();
        setTitle("Create New Playlist");
        setResizable(false);
        setSize(400, 400);
        getContentPane().setBackground(musicPlayerGUI.FRAME_COLOR);
        setLayout(null);
        // make the dialog modal (block input to other windows until closed)
        setModal(true);

        setLocationRelativeTo(musicPlayerGUI);

        addDialogComponents();

    }

    private void addDialogComponents() {
        // TODO Auto-generated method stub
        JPanel songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds((int) (getWidth() * 0.025), 10, (int) (getWidth() * .9), (int) (getHeight() * 0.75));
        add(songContainer);

        // add song button
        JButton addSongButton = new JButton("Add Track");
        addSongButton.setBounds(60, (int) (getWidth() * 0.80), 100, 25);
        addSongButton.setFont(new Font("Dialog", Font.BOLD, 10));
        addSongButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // open file explorer to add song(s)
                JFileChooser fileChooser = new JFileChooser();
                // allow multiple selection
                fileChooser.setMultiSelectionEnabled(true);
                // set file filter to only show mp3 files
                fileChooser.setFileFilter(new FileNameExtensionFilter("MP3 Files", "mp3", "flac"));
                // set initial directory
                fileChooser.setCurrentDirectory(new File("src/assets"));
                // store the result of the file chooser
                int result = fileChooser.showOpenDialog(musicPlaylistPrompt.this);

                File selectedFile = fileChooser.getSelectedFile();

                if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    //
                    JLabel filePathLabel = new JLabel(selectedFile.getPath());
                    filePathLabel.setFont(new Font("Dialog", Font.BOLD, 11));
                    filePathLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    // add to the list
                    songPaths.add(filePathLabel.getText());
                    songContainer.add(filePathLabel);
                    // refreshes the panel to show the new label
                    songContainer.revalidate();

                }
            }

        });
        add(addSongButton);

        JButton saveplaylistButton = new JButton("Save");
        saveplaylistButton.setBounds(215, (int) (getWidth() * 0.80), 100, 25);
        saveplaylistButton.setFont(new Font("Dialog", Font.BOLD, 10));
        saveplaylistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser jfileChooser = new JFileChooser();
                    jfileChooser.setCurrentDirectory(new File("src/assets"));
                    int userSelection = jfileChooser.showSaveDialog(musicPlaylistPrompt.this);

                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        // use getSelectedFile to get a reference to the file that we are about to save
                        File selectedFile = jfileChooser.getSelectedFile();
                        // convert to .txt file if not already
                        if (!selectedFile.getName().substring(selectedFile.getName().length() - 4)
                                .equalsIgnoreCase(".txt")) {
                            selectedFile = new File(selectedFile.getAbsoluteFile() + ".txt");
                        }

                        // create the new fle at the designated dir
                        selectedFile.createNewFile();

                        // write all the song paths to the text file
                        FileWriter fileWriter = new FileWriter(selectedFile);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                        // iterate through our song paths list and write each string into the file
                        for (String songPath : songPaths) {
                            bufferedWriter.write(songPath + "\n");

                        }
                        bufferedWriter.close();
                        // display success dialog
                        JOptionPane.showMessageDialog(musicPlaylistPrompt.this, "Playlist saved successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        // close the dialog
                        musicPlaylistPrompt.this.dispose();

                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        add(saveplaylistButton);
    }
}
